package com.jointpay.huifu;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.refund.RefundQueryRequest;
import com.jointpay.api.refund.RefundQueryResult;
import com.jointpay.api.refund.RefundRequest;
import com.jointpay.api.refund.RefundResult;
import com.jointpay.api.refund.RefundStatus;
import com.jointpay.common.channel.ChannelApiClient;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;
import com.jointpay.api.config.ChannelExtras;
import com.jointpay.common.refund.AbstractChannelRefundService;
import com.jointpay.common.util.ChannelRequestExtras;

import java.util.HashMap;
import java.util.Map;

public final class HuifuRefundService extends AbstractChannelRefundService {

    private static final String DEFAULT_REFUND_PATH = "/v3/trade/payment/refund";
    private static final String DEFAULT_QUERY_REFUND_PATH = "/v3/trade/payment/refund/query";

    private final ChannelApiClient apiClient;
    private final ChannelConfig config;

    public HuifuRefundService(ChannelConfig config) {
        super("汇付天下");
        this.config = config;
        this.apiClient = new ChannelApiClient(config);
    }

    @Override
    protected RefundResult doRefund(RefundRequest request) {
        String path = request.getExtras().getOrDefault(ChannelExtras.Huifu.REFUND_PATH, DEFAULT_REFUND_PATH);
        Map<String, Object> body = new HashMap<>();
        body.put("huifu_id", config.getMerchantId());
        body.put("req_seq_id", request.getOutRefundNo());
        body.put("org_req_seq_id", request.getOutTradeNo());
        body.put("ord_amt", String.format("%.2f", request.getRefundAmountCent() / 100.0));
        if (request.getReason() != null) {
            body.put("remark", request.getReason());
        }
        String notifyUrl = request.getExtras().get(ChannelExtras.Huifu.NOTIFY_URL);
        if (notifyUrl != null) {
            body.put("notify_url", notifyUrl);
        }
        ChannelRequestExtras.mergeInto(body, request.getExtras());

        HttpResponse response = apiClient.postJson(path, body);
        return toRefundResult(request.getOutRefundNo(), response.getBody());
    }

    @Override
    protected RefundQueryResult doQueryRefund(RefundQueryRequest request) {
        String outRefundNo = request.getOutRefundNo();
        if (outRefundNo == null || outRefundNo.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下退款查询需提供 outRefundNo");
        }
        Map<String, Object> body = new HashMap<>();
        body.put("huifu_id", config.getMerchantId());
        body.put("req_seq_id", outRefundNo);

        HttpResponse response = apiClient.postJson(DEFAULT_QUERY_REFUND_PATH, body);
        return toRefundQueryResult(outRefundNo, response.getBody());
    }

    private RefundResult toRefundResult(String outRefundNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String code = firstNonBlank(Jsons.text(map, "resp_code"), Jsons.text(map, "trans_stat"));
        if (code != null && !isSuccess(code)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇付天下退款失败",
                    code,
                    Jsons.text(map, "resp_desc"),
                    null);
        }
        return new RefundResult(
                outRefundNo,
                firstNonBlank(Jsons.text(map, "hf_seq_id"), outRefundNo),
                mapStatus(firstNonBlank(Jsons.text(map, "trans_stat"), code)));
    }

    private RefundQueryResult toRefundQueryResult(String outRefundNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String code = firstNonBlank(Jsons.text(map, "resp_code"), Jsons.text(map, "trans_stat"));
        if (code != null && !isSuccess(code)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇付天下退款查询失败",
                    code,
                    Jsons.text(map, "resp_desc"),
                    null);
        }
        return new RefundQueryResult(
                Jsons.text(map, "org_req_seq_id"),
                outRefundNo,
                firstNonBlank(Jsons.text(map, "hf_seq_id"), outRefundNo),
                mapStatus(code),
                yuanToCent(firstNonBlank(Jsons.text(map, "ord_amt"), Jsons.text(map, "refund_amt"))));
    }

    private static boolean isSuccess(String code) {
        return "00000000".equals(code) || "00000100".equals(code) || "S".equalsIgnoreCase(code) || "SUCCESS".equalsIgnoreCase(code);
    }

    private static RefundStatus mapStatus(String stat) {
        if (stat == null) {
            return RefundStatus.UNKNOWN;
        }
        return switch (stat.toUpperCase()) {
            case "S", "SUCCESS", "00000000" -> RefundStatus.SUCCESS;
            case "P", "PROCESSING" -> RefundStatus.PROCESSING;
            case "F", "FAIL" -> RefundStatus.FAILED;
            default -> RefundStatus.UNKNOWN;
        };
    }

    private static long yuanToCent(String yuan) {
        if (yuan == null || yuan.isBlank()) {
            return 0L;
        }
        return Math.round(Double.parseDouble(yuan) * 100);
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
