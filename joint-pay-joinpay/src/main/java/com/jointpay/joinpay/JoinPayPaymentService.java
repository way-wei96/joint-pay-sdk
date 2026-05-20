package com.jointpay.joinpay;

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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 汇聚支付聚合下单（uniPayApi），对应官方文档「聚合支付」。
 */
public final class JoinPayPaymentService extends AbstractChannelPaymentService {

    private final ChannelApiClient apiClient;
    private final ChannelConfig config;

    public JoinPayPaymentService(ChannelConfig config) {
        super("汇聚支付");
        this.config = withDefaultGateway(config);
        this.apiClient = new ChannelApiClient(this.config);
    }

    @Override
    protected PrepayResult doPrepay(PrepayRequest request) {
        Map<String, String> params = buildUniPayParams(request);
        String secret = requireApiSecret();
        params.put("hmac", JoinPaySignUtil.sign(params, secret));

        HttpResponse response = apiClient.postForm(JoinPayConstants.UNI_PAY_PATH, params);
        return toPrepayResult(response.getBody());
    }

    @Override
    protected PayOrderResult doCreateOrder(PayOrderRequest request) {
        PrepayRequest prepayRequest = PrepayRequest.builder()
                .outTradeNo(request.getOutTradeNo())
                .amountCent(request.getAmountCent())
                .subject(request.getSubject())
                .notifyUrl(request.getNotifyUrl())
                .extras(request.getExtras())
                .build();
        PrepayResult prepay = doPrepay(prepayRequest);
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
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚支付查单需提供 outTradeNo");
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("p1_MerchantNo", config.getMerchantId());
        params.put("p2_OrderNo", outTradeNo);
        String secret = requireApiSecret();
        params.put("hmac", JoinPaySignUtil.sign(params, secret));

        HttpResponse response = apiClient.postForm(JoinPayConstants.QUERY_ORDER_PATH, params);
        return toQueryResult(outTradeNo, response.getBody());
    }

    private OrderQueryResult toQueryResult(String outTradeNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String code = Jsons.text(map, "rb_Code");
        if (!"100".equals(code)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇聚支付查单失败",
                    code,
                    Jsons.text(map, "rb_CodeMsg"),
                    null);
        }
        PayStatus status = mapJoinPayStatus(Jsons.text(map, "ra_Status"));
        long amountCent = yuanToCent(Jsons.text(map, "r3_Amount"));
        String channelTradeNo = firstNonBlank(Jsons.text(map, "r7_TrxNo"), outTradeNo);
        return new OrderQueryResult(outTradeNo, channelTradeNo, status, amountCent);
    }

    private static PayStatus mapJoinPayStatus(String raStatus) {
        if (raStatus == null) {
            return PayStatus.UNKNOWN;
        }
        return switch (raStatus) {
            case "100" -> PayStatus.SUCCESS;
            case "101" -> PayStatus.FAILED;
            case "102" -> PayStatus.WAIT_PAY;
            case "103" -> PayStatus.CLOSED;
            default -> PayStatus.UNKNOWN;
        };
    }

    private static long yuanToCent(String yuan) {
        if (yuan == null || yuan.isBlank()) {
            return 0L;
        }
        return new BigDecimal(yuan).movePointRight(2).longValue();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private Map<String, String> buildUniPayParams(PrepayRequest request) {
        String frpCode = requireExtra(request, JoinPayConstants.EXTRA_FRP_CODE);
        String tradeMerchantNo = request.getExtras().get(JoinPayConstants.EXTRA_TRADE_MERCHANT_NO);
        if (tradeMerchantNo == null || tradeMerchantNo.isBlank()) {
            tradeMerchantNo = config.getAppId();
        }
        if (tradeMerchantNo == null || tradeMerchantNo.isBlank()) {
            throw new JointPayException(
                    ErrorCode.INVALID_ARGUMENT,
                    "汇聚支付需配置 tradeMerchantNo（extras）或 appId（报备商户号）");
        }

        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("p0_Version", JoinPayConstants.API_VERSION);
        params.put("p1_MerchantNo", config.getMerchantId());
        params.put("p2_OrderNo", request.getOutTradeNo());
        params.put("p3_Amount", toAmountYuan(request.getAmountCent()));
        params.put("p4_Cur", JoinPayConstants.CURRENCY_CNY);
        params.put("p5_ProductName", nullToEmpty(request.getSubject()));
        if (request.getNotifyUrl() != null && !request.getNotifyUrl().isBlank()) {
            params.put("p9_NotifyUrl", request.getNotifyUrl());
        }
        params.put("q1_FrpCode", frpCode);
        params.put("qa_TradeMerchantNo", tradeMerchantNo);
        request.getExtras().forEach((k, v) -> {
            if (!JoinPayConstants.EXTRA_FRP_CODE.equals(k)
                    && !JoinPayConstants.EXTRA_TRADE_MERCHANT_NO.equals(k)
                    && v != null && !v.isBlank()) {
                params.putIfAbsent(k, v);
            }
        });
        return params;
    }

    private PrepayResult toPrepayResult(String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String code = Jsons.text(map, "ra_Code");
        if (!"100".equals(code)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇聚支付下单失败",
                    code,
                    Jsons.text(map, "rb_CodeMsg"),
                    null);
        }
        String trxNo = Jsons.text(map, "r7_TrxNo");
        String payInfo = Jsons.text(map, "rc_Result");
        return new PrepayResult(
                trxNo,
                trxNo,
                payInfo == null ? Map.of() : Map.of("payInfo", payInfo));
    }

    private String requireApiSecret() {
        String secret = config.getApiSecret();
        if (secret == null || secret.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚支付 MD5 签名需配置 apiSecret");
        }
        return secret;
    }

    private static ChannelConfig withDefaultGateway(ChannelConfig config) {
        if (config.getGatewayUrl() != null && !config.getGatewayUrl().isBlank()) {
            return config;
        }
        return ChannelConfig.builder(config.getChannel())
                .environment(config.getEnvironment())
                .merchantId(config.getMerchantId())
                .appId(config.getAppId())
                .apiKey(config.getApiKey())
                .apiSecret(config.getApiSecret())
                .privateKey(config.getPrivateKey())
                .publicKey(config.getPublicKey())
                .gatewayUrl(JoinPayConstants.DEFAULT_GATEWAY)
                .extras(config.getExtras())
                .build();
    }

    private static String toAmountYuan(long amountCent) {
        return BigDecimal.valueOf(amountCent, 2).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
