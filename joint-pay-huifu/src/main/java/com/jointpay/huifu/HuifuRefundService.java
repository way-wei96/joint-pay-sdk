package com.jointpay.huifu;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.config.ChannelExtras;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.refund.RefundQueryRequest;
import com.jointpay.api.refund.RefundQueryResult;
import com.jointpay.api.refund.RefundRequest;
import com.jointpay.api.refund.RefundResult;
import com.jointpay.api.refund.RefundStatus;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.refund.AbstractChannelRefundService;
import com.jointpay.common.util.ChannelRequestExtras;

import java.util.HashMap;
import java.util.Map;

public final class HuifuRefundService extends AbstractChannelRefundService {

    private final HuifuDougonClient client;
    private final ChannelConfig config;

    public HuifuRefundService(ChannelConfig config) {
        super("汇付天下");
        this.config = HuifuDougonClient.withDefaultGateway(config);
        this.client = new HuifuDougonClient(this.config);
    }

    @Override
    protected RefundResult doRefund(RefundRequest request) {
        String path = request.getExtras().getOrDefault(
                ChannelExtras.Huifu.REFUND_PATH, HuifuConstants.DEFAULT_REFUND_PATH);
        Map<String, Object> data = new HashMap<>();
        data.put("huifu_id", config.getMerchantId());
        data.put("req_seq_id", request.getOutRefundNo());
        data.put("org_req_seq_id", request.getOutTradeNo());
        data.put("ord_amt", String.format("%.2f", request.getRefundAmountCent() / 100.0));
        if (request.getReason() != null) {
            data.put("remark", request.getReason());
        }
        String notifyUrl = request.getExtras().get(ChannelExtras.Huifu.NOTIFY_URL);
        if (notifyUrl != null) {
            data.put("notify_url", notifyUrl);
        }
        ChannelRequestExtras.mergeInto(data, request.getExtras());

        Map<String, Object> map = client.post(path, data);
        return new RefundResult(
                request.getOutRefundNo(),
                firstNonBlank(Jsons.text(map, "hf_seq_id"), request.getOutRefundNo()),
                mapStatus(firstNonBlank(Jsons.text(map, "trans_stat"), Jsons.text(map, "sub_resp_code"))));
    }

    @Override
    protected RefundQueryResult doQueryRefund(RefundQueryRequest request) {
        String outRefundNo = request.getOutRefundNo();
        if (outRefundNo == null || outRefundNo.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付退款查询需提供 outRefundNo");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("huifu_id", config.getMerchantId());
        data.put("org_req_seq_id", outRefundNo);

        Map<String, Object> map = client.post(HuifuConstants.DEFAULT_QUERY_REFUND_PATH, data);
        return new RefundQueryResult(
                Jsons.text(map, "org_req_seq_id"),
                outRefundNo,
                firstNonBlank(Jsons.text(map, "hf_seq_id"), outRefundNo),
                mapStatus(firstNonBlank(Jsons.text(map, "trans_stat"), Jsons.text(map, "sub_resp_code"))),
                yuanToCent(firstNonBlank(Jsons.text(map, "ord_amt"), Jsons.text(map, "refund_amt"))));
    }

    private static RefundStatus mapStatus(String stat) {
        if (stat == null) {
            return RefundStatus.UNKNOWN;
        }
        return switch (stat.toUpperCase()) {
            case "S", "SUCCESS", "00000000" -> RefundStatus.SUCCESS;
            case "P", "PROCESSING", "00000100" -> RefundStatus.PROCESSING;
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
