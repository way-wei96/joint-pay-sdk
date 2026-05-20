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
import com.jointpay.api.profitsharing.ProfitSharingScheme;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.payment.AbstractChannelPaymentService;
import com.jointpay.common.profitsharing.ProfitSharingBindStores;
import com.jointpay.common.util.ChannelRequestExtras;

import java.util.HashMap;
import java.util.Map;

/**
 * 汇付斗拱聚合支付（v3 JS 支付等），出站走 {@link HuifuDougonClient} 标准信封。
 */
public final class HuifuPaymentService extends AbstractChannelPaymentService {

    private final HuifuDougonClient client;
    private final ChannelConfig config;

    public HuifuPaymentService(ChannelConfig config) {
        super("汇付天下");
        this.config = HuifuDougonClient.withDefaultGateway(config);
        this.client = new HuifuDougonClient(this.config);
    }

    @Override
    protected PrepayResult doPrepay(PrepayRequest request) {
        String path = request.getExtras().getOrDefault(
                HuifuConstants.EXTRA_API_PATH, HuifuConstants.DEFAULT_PREPAY_PATH);
        String tradeType = firstNonBlank(
                request.getExtras().get(HuifuConstants.EXTRA_TRADE_TYPE),
                request.getExtras().get(HuifuConstants.EXTRA_PAY_TYPE));
        if (tradeType == null || tradeType.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付预下单需 extras.payType 或 extras.tradeType（如 T_MINIAPP）");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("req_seq_id", request.getOutTradeNo());
        data.put("huifu_id", config.getMerchantId());
        data.put("trans_amt", String.format("%.2f", request.getAmountCent() / 100.0));
        data.put("goods_desc", request.getSubject());
        data.put("notify_url", request.getNotifyUrl());
        data.put("trade_type", tradeType);
        applyBoundProfitSharing(request.getOutTradeNo(), data);
        ChannelRequestExtras.mergeInto(data, request.getExtras());

        Map<String, Object> map = client.post(path, data);
        String channelTradeNo = firstNonBlank(
                Jsons.text(map, "hf_seq_id"),
                Jsons.text(map, "party_order_id"),
                request.getOutTradeNo());
        String payInfo = firstNonBlank(Jsons.text(map, "pay_info"), Jsons.text(map, "qr_code"));
        return new PrepayResult(
                channelTradeNo,
                channelTradeNo,
                payInfo == null ? Map.of() : Map.of("payInfo", payInfo));
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
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付查单需提供 outTradeNo（req_seq_id）");
        }
        String path = request.getExtras().getOrDefault(
                HuifuConstants.EXTRA_QUERY_PATH, HuifuConstants.DEFAULT_QUERY_PATH);
        Map<String, Object> data = new HashMap<>();
        data.put("huifu_id", config.getMerchantId());
        data.put("org_req_seq_id", outTradeNo);
        if (request.getChannelTradeNo() != null && !request.getChannelTradeNo().isBlank()) {
            data.put("org_hf_seq_id", request.getChannelTradeNo());
        }

        Map<String, Object> map = client.post(path, data);
        PayStatus status = mapHuifuStatus(firstNonBlank(
                Jsons.text(map, "trans_stat"),
                Jsons.text(map, "order_status")));
        long amountCent = yuanToCent(firstNonBlank(Jsons.text(map, "trans_amt"), Jsons.text(map, "ord_amt")));
        String channelTradeNo = firstNonBlank(Jsons.text(map, "hf_seq_id"), outTradeNo);
        return new OrderQueryResult(outTradeNo, channelTradeNo, status, amountCent);
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

    private void applyBoundProfitSharing(String outTradeNo, Map<String, Object> data) {
        ProfitSharingScheme scheme = ProfitSharingBindStores.take(outTradeNo);
        if (scheme == null) {
            return;
        }
        data.put("acct_split_bunch", Jsons.toJson(buildSplitBunch(scheme)));
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
