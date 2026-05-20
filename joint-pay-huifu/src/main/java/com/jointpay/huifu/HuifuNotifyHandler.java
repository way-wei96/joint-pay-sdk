package com.jointpay.huifu;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.notify.PayNotifyPayload;
import com.jointpay.api.notify.ProfitSharingNotifyPayload;
import com.jointpay.api.notify.RefundNotifyPayload;
import com.jointpay.api.payment.PayStatus;
import com.jointpay.api.profitsharing.ProfitSharingStatus;
import com.jointpay.api.refund.RefundStatus;
import com.jointpay.common.crypto.Rsa2SignUtil;
import com.jointpay.common.json.Jsons;

import java.util.Map;
import java.util.TreeMap;

/**
 * 汇付天下回调解析（JSON 为主）。若回调含 {@code sign} 且配置了 {@link ChannelConfig#getPublicKey()}，则做 RSA2 验签。
 */
public final class HuifuNotifyHandler implements NotifyHandler {

    private final ChannelConfig config;

    public HuifuNotifyHandler(ChannelConfig config) {
        this.config = config;
    }

    @Override
    public NotifyParseResult parse(NotifyRawRequest request) {
        Map<String, Object> map = parsePayload(request);
        verifySignIfPresent(map);

        if (isRefundNotify(map)) {
            return parseRefundNotify(map);
        }
        if (isProfitSharingNotify(map)) {
            return parseProfitSharingNotify(map);
        }

        String outTradeNo = firstNonBlank(
                Jsons.text(map, "req_seq_id"),
                Jsons.text(map, "out_ord_id"));
        if (outTradeNo == null) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下回调缺少订单号字段");
        }
        PayStatus status = mapStatus(firstNonBlank(
                Jsons.text(map, "trans_stat"),
                Jsons.text(map, "order_status")));
        long amountCent = parseAmount(firstNonBlank(Jsons.text(map, "trans_amt"), Jsons.text(map, "ord_amt")));
        String channelTradeNo = firstNonBlank(Jsons.text(map, "hf_seq_id"), outTradeNo);

        return NotifyParseResult.builder(NotifyType.PAY)
                .pay(new PayNotifyPayload(outTradeNo, channelTradeNo, status, amountCent))
                .successResponseBody(HuifuConstants.NOTIFY_SUCCESS_RESPONSE)
                .build();
    }

    private void verifySignIfPresent(Map<String, Object> map) {
        String remoteSign = Jsons.text(map, "sign");
        if (remoteSign == null || remoteSign.isBlank()) {
            return;
        }
        String publicKey = config.getPublicKey();
        if (publicKey == null || publicKey.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下回调验签需配置 publicKey");
        }
        Map<String, String> params = new TreeMap<>();
        map.forEach((k, v) -> {
            if (v != null && !"sign".equals(k)) {
                params.put(k, String.valueOf(v));
            }
        });
        params.put("sign", remoteSign);
        if (!Rsa2SignUtil.verify(params, publicKey)) {
            throw new JointPayException(ErrorCode.SIGN_VERIFY_FAILED, "汇付天下回调验签失败");
        }
    }

    private static NotifyParseResult parseProfitSharingNotify(Map<String, Object> map) {
        String outSharingNo = firstNonBlank(
                Jsons.text(map, "req_seq_id"),
                Jsons.text(map, "org_req_seq_id"));
        if (outSharingNo == null) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下分账回调缺少分账请求号");
        }
        ProfitSharingNotifyPayload payload = new ProfitSharingNotifyPayload(
                firstNonBlank(Jsons.text(map, "out_ord_id"), Jsons.text(map, "org_req_seq_id")),
                outSharingNo,
                firstNonBlank(Jsons.text(map, "hf_seq_id"), outSharingNo),
                mapProfitSharingStatus(firstNonBlank(
                        Jsons.text(map, "trans_stat"),
                        Jsons.text(map, "order_status"))));
        return NotifyParseResult.builder(NotifyType.PROFIT_SHARING)
                .profitSharing(payload)
                .successResponseBody(HuifuConstants.NOTIFY_SUCCESS_RESPONSE)
                .build();
    }

    private static boolean isProfitSharingNotify(Map<String, Object> map) {
        if (Jsons.text(map, "refund_amt") != null || Jsons.text(map, "refund_seq_id") != null) {
            return false;
        }
        if (Jsons.text(map, "acct_split_bunch") != null || Jsons.text(map, "div_detail") != null) {
            return true;
        }
        String notifyType = firstNonBlank(Jsons.text(map, "notify_type"), Jsons.text(map, "event_type"));
        if (notifyType != null && notifyType.toUpperCase().contains("ACCTPAY")) {
            return true;
        }
        return Jsons.text(map, "org_req_seq_id") != null
                && Jsons.text(map, "req_seq_id") != null
                && Jsons.text(map, "out_ord_id") == null
                && Jsons.text(map, "trans_amt") == null;
    }

    private static ProfitSharingStatus mapProfitSharingStatus(String stat) {
        if (stat == null) {
            return ProfitSharingStatus.UNKNOWN;
        }
        return switch (stat.toUpperCase()) {
            case "S", "SUCCESS" -> ProfitSharingStatus.SUCCESS;
            case "F", "FAIL" -> ProfitSharingStatus.FAILED;
            case "P", "PROCESSING" -> ProfitSharingStatus.PROCESSING;
            default -> ProfitSharingStatus.UNKNOWN;
        };
    }

    private static NotifyParseResult parseRefundNotify(Map<String, Object> map) {
        String outRefundNo = firstNonBlank(
                Jsons.text(map, "req_seq_id"),
                Jsons.text(map, "out_refund_seq_id"));
        if (outRefundNo == null) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下退款回调缺少退款请求号");
        }
        RefundNotifyPayload payload = new RefundNotifyPayload(
                firstNonBlank(Jsons.text(map, "org_req_seq_id"), Jsons.text(map, "out_ord_id")),
                outRefundNo,
                firstNonBlank(Jsons.text(map, "hf_seq_id"), outRefundNo),
                mapRefundStatus(firstNonBlank(
                        Jsons.text(map, "trans_stat"),
                        Jsons.text(map, "order_status"))),
                parseAmount(firstNonBlank(Jsons.text(map, "refund_amt"), Jsons.text(map, "ord_amt"))));
        return NotifyParseResult.builder(NotifyType.REFUND)
                .refund(payload)
                .successResponseBody(HuifuConstants.NOTIFY_SUCCESS_RESPONSE)
                .build();
    }

    private static boolean isRefundNotify(Map<String, Object> map) {
        if (Jsons.text(map, "refund_seq_id") != null || Jsons.text(map, "refund_amt") != null) {
            return true;
        }
        String notifyType = firstNonBlank(Jsons.text(map, "notify_type"), Jsons.text(map, "event_type"));
        return notifyType != null && notifyType.toUpperCase().contains("REFUND");
    }

    private static RefundStatus mapRefundStatus(String stat) {
        if (stat == null) {
            return RefundStatus.UNKNOWN;
        }
        return switch (stat.toUpperCase()) {
            case "S", "SUCCESS" -> RefundStatus.SUCCESS;
            case "F", "FAIL" -> RefundStatus.FAILED;
            case "P", "PROCESSING" -> RefundStatus.PROCESSING;
            default -> RefundStatus.UNKNOWN;
        };
    }

    private static Map<String, Object> parsePayload(NotifyRawRequest request) {
        if (request.getBody() != null && !request.getBody().isBlank()) {
            return Jsons.parseMap(request.getBody());
        }
        if (!request.getParams().isEmpty()) {
            Map<String, Object> map = new java.util.HashMap<>();
            request.getParams().forEach(map::put);
            return map;
        }
        throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下回调内容为空");
    }

    private static PayStatus mapStatus(String stat) {
        if (stat == null) {
            return PayStatus.UNKNOWN;
        }
        return switch (stat.toUpperCase()) {
            case "S", "SUCCESS" -> PayStatus.SUCCESS;
            case "F", "FAIL" -> PayStatus.FAILED;
            default -> PayStatus.UNKNOWN;
        };
    }

    private static long parseAmount(String amt) {
        if (amt == null || amt.isBlank()) {
            return 0L;
        }
        return Math.round(Double.parseDouble(amt) * 100);
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
