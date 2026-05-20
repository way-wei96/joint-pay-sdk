package com.jointpay.huifu;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.payment.OrderQueryRequest;
import com.jointpay.api.payment.OrderQueryResult;
import com.jointpay.api.payment.PayOrderRequest;
import com.jointpay.api.payment.PayOrderResult;
import com.jointpay.api.payment.PayStatus;
import com.jointpay.api.payment.PrepayRequest;
import com.jointpay.api.payment.PrepayResult;
import com.jointpay.common.channel.ChannelApiClient;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.payment.AbstractChannelPaymentService;

import java.util.HashMap;
import java.util.Map;

/**
 * 汇付天下斗拱预下单（JSON POST），字段名按斗拱通用习惯映射，具体以商户签约文档为准。
 */
public final class HuifuPaymentService extends AbstractChannelPaymentService {

    private final ChannelApiClient apiClient;
    private final ChannelConfig config;

    public HuifuPaymentService(ChannelConfig config) {
        super("汇付天下");
        this.config = config;
        this.apiClient = new ChannelApiClient(config);
    }

    @Override
    protected PrepayResult doPrepay(PrepayRequest request) {
        String path = request.getExtras().getOrDefault(
                HuifuConstants.EXTRA_API_PATH, HuifuConstants.DEFAULT_PREPAY_PATH);
        String payType = requireExtra(request, HuifuConstants.EXTRA_PAY_TYPE);

        Map<String, Object> body = new HashMap<>();
        body.put("req_seq_id", request.getOutTradeNo());
        body.put("huifu_id", config.getMerchantId());
        body.put("trans_amt", String.format("%.2f", request.getAmountCent() / 100.0));
        body.put("goods_desc", request.getSubject());
        body.put("notify_url", request.getNotifyUrl());
        body.put("pay_type", payType);
        body.putAll(request.getExtras());

        HttpResponse response = apiClient.postJson(path, body);
        return toPrepayResult(request.getOutTradeNo(), response.getBody());
    }

    @Override
    protected PayOrderResult doCreateOrder(PayOrderRequest request) {
        PrepayResult prepay = doPrepay(PrepayRequest.builder()
                .outTradeNo(request.getOutTradeNo())
                .amountCent(request.getAmountCent())
                .subject(request.getSubject())
                .notifyUrl(request.getNotifyUrl())
                .extras(request.getExtras())
                .build());
        return new PayOrderResult(
                request.getOutTradeNo(),
                prepay.getChannelTradeNo(),
                PayStatus.WAIT_PAY,
                prepay.getPayParams());
    }

    @Override
    protected OrderQueryResult doQueryOrder(OrderQueryRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇付天下订单查询尚未实现");
    }

    private PrepayResult toPrepayResult(String outTradeNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String respCode = firstNonBlank(
                Jsons.text(map, "resp_code"),
                Jsons.text(map, "code"),
                Jsons.text(map, "sub_resp_code"));
        if (respCode != null && !"00000000".equals(respCode) && !"00000100".equals(respCode) && !"SUCCESS".equalsIgnoreCase(respCode)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇付天下预下单失败",
                    respCode,
                    Jsons.text(map, "resp_desc"),
                    null);
        }
        String channelTradeNo = firstNonBlank(
                Jsons.text(map, "hf_seq_id"),
                Jsons.text(map, "party_order_id"),
                outTradeNo);
        String payInfo = firstNonBlank(Jsons.text(map, "pay_info"), Jsons.text(map, "qr_code"));
        return new PrepayResult(
                channelTradeNo,
                channelTradeNo,
                payInfo == null ? Map.of() : Map.of("payInfo", payInfo));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
