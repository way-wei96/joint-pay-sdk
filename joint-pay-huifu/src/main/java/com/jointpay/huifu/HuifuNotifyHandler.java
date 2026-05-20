package com.jointpay.huifu;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.notify.PayNotifyPayload;
import com.jointpay.api.payment.PayStatus;
import com.jointpay.common.json.Jsons;

import java.util.Map;

/**
 * 汇付天下回调解析（JSON 为主，签名字段以商户文档为准，可在 extras 扩展验签逻辑）。
 */
public final class HuifuNotifyHandler implements NotifyHandler {

    @Override
    public NotifyParseResult parse(NotifyRawRequest request) {
        Map<String, Object> map = parsePayload(request);
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
