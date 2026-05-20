package com.jointpay.allinpay;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.notify.PayNotifyPayload;
import com.jointpay.api.payment.PayStatus;
import com.jointpay.common.crypto.Md5SignUtil;
import com.jointpay.common.json.Jsons;

import java.util.Map;
import java.util.TreeMap;

public final class AllinpayNotifyHandler implements NotifyHandler {

    private final ChannelConfig config;

    public AllinpayNotifyHandler(ChannelConfig config) {
        this.config = config;
    }

    @Override
    public NotifyParseResult parse(NotifyRawRequest request) {
        Map<String, String> params = new TreeMap<>(request.getParams());
        if (params.isEmpty() && request.getBody() != null) {
            Map<String, Object> body = Jsons.parseMap(request.getBody());
            body.forEach((k, v) -> params.put(k, v == null ? "" : String.valueOf(v)));
        }
        if (params.isEmpty()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付回调参数为空");
        }
        verifySignIfPresent(params);

        String outTradeNo = firstNonBlank(params.get("reqsn"), params.get("cusorderid"));
        PayStatus status = mapStatus(firstNonBlank(params.get("trxstatus"), params.get("status")));
        long amountCent = parseAmount(params.get("trxamt"));
        String channelTradeNo = firstNonBlank(params.get("trxid"), outTradeNo);

        return NotifyParseResult.builder(NotifyType.PAY)
                .pay(new PayNotifyPayload(outTradeNo, channelTradeNo, status, amountCent))
                .successResponseBody(AllinpayConstants.NOTIFY_SUCCESS_RESPONSE)
                .build();
    }

    private void verifySignIfPresent(Map<String, String> params) {
        if (!params.containsKey("sign")) {
            return;
        }
        String secret = config.getApiSecret();
        if (secret == null || secret.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付回调验签需配置 apiSecret");
        }
        String remote = params.get("sign");
        Map<String, String> copy = new TreeMap<>(params);
        if (!Md5SignUtil.signSortedKeyValues(copy, secret, true).equalsIgnoreCase(remote)) {
            throw new JointPayException(ErrorCode.SIGN_VERIFY_FAILED, "通联支付回调验签失败");
        }
    }

    private static PayStatus mapStatus(String status) {
        if (status == null) {
            return PayStatus.UNKNOWN;
        }
        return switch (status) {
            case "0000", "200" -> PayStatus.SUCCESS;
            case "2000" -> PayStatus.PAYING;
            case "3000" -> PayStatus.FAILED;
            default -> PayStatus.UNKNOWN;
        };
    }

    private static long parseAmount(String amt) {
        if (amt == null || amt.isBlank()) {
            return 0L;
        }
        return Long.parseLong(amt);
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
