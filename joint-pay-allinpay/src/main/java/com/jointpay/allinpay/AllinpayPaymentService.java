package com.jointpay.allinpay;

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
import com.jointpay.common.crypto.Md5SignUtil;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.payment.AbstractChannelPaymentService;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 通联支付统一下单（字段参考官方统一下单 API，网关地址由 {@link ChannelConfig#getGatewayUrl()} 配置）。
 */
public final class AllinpayPaymentService extends AbstractChannelPaymentService {

    private final ChannelApiClient apiClient;
    private final ChannelConfig config;

    public AllinpayPaymentService(ChannelConfig config) {
        super("通联支付");
        this.config = config;
        this.apiClient = new ChannelApiClient(config);
    }

    @Override
    protected PrepayResult doPrepay(PrepayRequest request) {
        String path = request.getExtras().getOrDefault(
                AllinpayConstants.EXTRA_API_PATH, AllinpayConstants.DEFAULT_PREPAY_PATH);
        String payType = requireExtra(request, AllinpayConstants.EXTRA_PAY_TYPE);
        String signType = request.getExtras().getOrDefault(AllinpayConstants.EXTRA_SIGN_TYPE, "MD5");

        Map<String, String> params = new TreeMap<>();
        params.put("proid", requireAppId());
        params.put("cusid", config.getMerchantId());
        params.put("trxamt", String.valueOf(request.getAmountCent()));
        params.put("reqsn", request.getOutTradeNo());
        params.put("notify_url", nullToEmpty(request.getNotifyUrl()));
        params.put("body", nullToEmpty(request.getSubject()));
        params.put("paytype", payType);
        params.put("signtype", signType);
        params.put("sign", sign(params));

        HttpResponse response = apiClient.postJson(path, params);
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
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付查单需提供 outTradeNo（reqsn）");
        }
        String path = AllinpayConstants.DEFAULT_QUERY_PATH;
        String signType = "MD5";

        Map<String, String> params = new TreeMap<>();
        params.put("proid", requireAppId());
        params.put("cusid", config.getMerchantId());
        params.put("reqsn", outTradeNo);
        if (request.getChannelTradeNo() != null && !request.getChannelTradeNo().isBlank()) {
            params.put("trxid", request.getChannelTradeNo());
        }
        params.put("signtype", signType);
        params.put("sign", sign(params));

        HttpResponse response = apiClient.postJson(path, params);
        return toQueryResult(outTradeNo, response.getBody());
    }

    private OrderQueryResult toQueryResult(String outTradeNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        Object code = map.get("code");
        if (code instanceof Number num && num.intValue() != 200) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "通联支付查单失败",
                    String.valueOf(code),
                    Jsons.text(map, "message"),
                    null);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> msg = map.get("msg") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : map;
        PayStatus status = mapAllinpayStatus(firstNonBlank(
                Jsons.text(msg, "trxstatus"),
                Jsons.text(msg, "status")));
        long amountCent = parseAmount(firstNonBlank(Jsons.text(msg, "trxamt"), Jsons.text(msg, "amount")));
        String channelTradeNo = firstNonBlank(Jsons.text(msg, "trxid"), Jsons.text(msg, "cusid"), outTradeNo);
        return new OrderQueryResult(outTradeNo, channelTradeNo, status, amountCent);
    }

    private static PayStatus mapAllinpayStatus(String status) {
        if (status == null) {
            return PayStatus.UNKNOWN;
        }
        return switch (status) {
            case "0000", "200", "SUCCESS" -> PayStatus.SUCCESS;
            case "2000", "PROCESSING" -> PayStatus.PAYING;
            case "3000", "FAIL" -> PayStatus.FAILED;
            default -> PayStatus.UNKNOWN;
        };
    }

    private static long parseAmount(String amt) {
        if (amt == null || amt.isBlank()) {
            return 0L;
        }
        return Long.parseLong(amt);
    }

    private String sign(Map<String, String> params) {
        String secret = config.getApiSecret();
        if (secret == null || secret.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付 MD5 签名需配置 apiSecret");
        }
        return Md5SignUtil.signSortedKeyValues(params, secret, true);
    }

    private String requireAppId() {
        String appId = config.getAppId();
        if (appId == null || appId.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付需配置 appId（平台应用 proid）");
        }
        return appId;
    }

    private PrepayResult toPrepayResult(String outTradeNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        Object code = map.get("code");
        if (code instanceof Number num && num.intValue() != 200) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "通联支付下单失败",
                    String.valueOf(code),
                    Jsons.text(map, "message"),
                    null);
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> msg = map.get("msg") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : map;
        String channelTradeNo = firstNonBlank(Jsons.text(msg, "trxid"), Jsons.text(msg, "cusid"), outTradeNo);
        String payInfo = Jsons.text(msg, "payinfo");
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

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
