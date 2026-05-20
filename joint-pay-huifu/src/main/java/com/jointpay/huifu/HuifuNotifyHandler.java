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

/**
 * 汇付斗拱异步通知。支付类多为 {@code resp_data} + {@code sign}（对 resp_data 原文验签）。
 *
 * @see <a href="https://spin.cloudpnr.com/topds/paramStandards.html">参数规定</a>
 */
public final class HuifuNotifyHandler implements NotifyHandler {

    private final ChannelConfig config;

    public HuifuNotifyHandler(ChannelConfig config) {
        this.config = config;
    }

    @Override
    public NotifyParseResult parse(NotifyRawRequest request) {
        Map<String, Object> envelope = parseEnvelope(request);
        Map<String, Object> map = unwrapBusinessPayload(envelope);
        verifyNotifySignIfPresent(envelope, map);

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

    private void verifyNotifySignIfPresent(Map<String, Object> envelope, Map<String, Object> business) {
        String sign = firstNonBlank(Jsons.text(envelope, "sign"), Jsons.text(business, "sign"));
        if (sign == null || sign.isBlank()) {
            return;
        }
        String publicKey = config.getPublicKey();
        if (publicKey == null || publicKey.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下回调验签需配置 publicKey");
        }
        String signedContent = resolveSignedContent(envelope);
        if (signedContent == null) {
            return;
        }
        if (!Rsa2SignUtil.verifyContent(signedContent, sign, publicKey)) {
            throw new JointPayException(ErrorCode.SIGN_VERIFY_FAILED, "汇付天下回调验签失败");
        }
    }

    private static String resolveSignedContent(Map<String, Object> envelope) {
        if (envelope.containsKey("resp_data")) {
            Object respData = envelope.get("resp_data");
            return respData == null ? null : String.valueOf(respData);
        }
        if (envelope.get("data") instanceof String dataStr) {
            return dataStr;
        }
        return null;
    }

    private static Map<String, Object> unwrapBusinessPayload(Map<String, Object> envelope) {
        if (envelope.containsKey("resp_data")) {
            Object respData = envelope.get("resp_data");
            if (respData instanceof Map<?, ?> map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> cast = (Map<String, Object>) map;
                return cast;
            }
            return Jsons.parseMap(String.valueOf(respData));
        }
        return HuifuDougonClient.extractDataMap(envelope);
    }

    private static Map<String, Object> parseEnvelope(NotifyRawRequest request) {
        if (request.getBody() != null && !request.getBody().isBlank()) {
            String body = request.getBody().trim();
            if (body.startsWith("{")) {
                return Jsons.parseMap(body);
            }
            return parseFormLike(body);
        }
        if (!request.getParams().isEmpty()) {
            Map<String, Object> map = new java.util.HashMap<>();
            request.getParams().forEach(map::put);
            return map;
        }
        throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付天下回调内容为空");
    }

    private static Map<String, Object> parseFormLike(String body) {
        Map<String, Object> map = new java.util.HashMap<>();
        for (String pair : body.split("&")) {
            int idx = pair.indexOf('=');
            if (idx > 0) {
                map.put(pair.substring(0, idx), pair.substring(idx + 1));
            }
        }
        return map;
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
