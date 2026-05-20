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
import com.jointpay.api.profitsharing.ProfitSharingScheme;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.payment.AbstractChannelPaymentService;
import com.jointpay.common.profitsharing.ProfitSharingBindStores;
import com.jointpay.common.util.ChannelRequestExtras;

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
        applyBoundProfitSharing(request.getOutTradeNo(), body);
        ChannelRequestExtras.mergeInto(body, request.getExtras());

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
        String outTradeNo = request.getOutTradeNo();
        if (outTradeNo == null || outTradeNo.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下查单需提供 outTradeNo（req_seq_id）");
        }
        String path = HuifuConstants.DEFAULT_QUERY_PATH;
        Map<String, Object> body = new HashMap<>();
        body.put("huifu_id", config.getMerchantId());
        body.put("req_seq_id", outTradeNo);
        if (request.getChannelTradeNo() != null && !request.getChannelTradeNo().isBlank()) {
            body.put("hf_seq_id", request.getChannelTradeNo());
        }

        HttpResponse response = apiClient.postJson(path, body);
        return toQueryResult(outTradeNo, response.getBody());
    }

    private OrderQueryResult toQueryResult(String outTradeNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String respCode = firstNonBlank(
                Jsons.text(map, "resp_code"),
                Jsons.text(map, "trans_stat"));
        if (respCode != null && !isHuifuSuccessCode(respCode)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇付天下查单失败",
                    respCode,
                    Jsons.text(map, "resp_desc"),
                    null);
        }
        PayStatus status = mapHuifuStatus(firstNonBlank(
                Jsons.text(map, "trans_stat"),
                Jsons.text(map, "order_status"),
                respCode));
        long amountCent = yuanToCent(firstNonBlank(Jsons.text(map, "trans_amt"), Jsons.text(map, "ord_amt")));
        String channelTradeNo = firstNonBlank(Jsons.text(map, "hf_seq_id"), outTradeNo);
        return new OrderQueryResult(outTradeNo, channelTradeNo, status, amountCent);
    }

    private static boolean isHuifuSuccessCode(String code) {
        return "00000000".equals(code) || "00000100".equals(code) || "S".equalsIgnoreCase(code) || "SUCCESS".equalsIgnoreCase(code);
    }

    private static PayStatus mapHuifuStatus(String stat) {
        if (stat == null) {
            return PayStatus.UNKNOWN;
        }
        return switch (stat.toUpperCase()) {
            case "S", "SUCCESS", "00000000" -> PayStatus.SUCCESS;
            case "P", "PROCESSING" -> PayStatus.PAYING;
            case "F", "FAIL", "FAILED" -> PayStatus.FAILED;
            case "C", "CLOSED", "CANCEL" -> PayStatus.CLOSED;
            default -> PayStatus.UNKNOWN;
        };
    }

    private static long yuanToCent(String yuan) {
        if (yuan == null || yuan.isBlank()) {
            return 0L;
        }
        return Math.round(Double.parseDouble(yuan) * 100);
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

    private void applyBoundProfitSharing(String outTradeNo, Map<String, Object> body) {
        ProfitSharingScheme scheme = ProfitSharingBindStores.take(outTradeNo);
        if (scheme == null) {
            return;
        }
        body.put("acct_split_bunch", Jsons.toJson(buildSplitBunch(scheme)));
    }

    private static java.util.List<Map<String, String>> buildSplitBunch(ProfitSharingScheme scheme) {
        java.util.List<Map<String, String>> bunch = new java.util.ArrayList<>();
        for (var p : scheme.getParticipants()) {
            Map<String, String> item = new HashMap<>();
            item.put("huifu_id", firstNonBlank(p.getMerchantId(), p.getAccountNo()));
            item.put("div_amt", String.format("%.2f", p.getAmountCent() / 100.0));
            bunch.add(item);
        }
        return bunch;
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
