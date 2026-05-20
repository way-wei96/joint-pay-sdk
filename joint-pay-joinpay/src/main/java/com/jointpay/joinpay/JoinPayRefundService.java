package com.jointpay.joinpay;

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
import com.jointpay.common.refund.AbstractChannelRefundService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JoinPayRefundService extends AbstractChannelRefundService {

    private final ChannelApiClient apiClient;
    private final ChannelConfig config;

    public JoinPayRefundService(ChannelConfig config) {
        super("汇聚支付");
        this.config = JoinPayPaymentService.withDefaultGateway(config);
        this.apiClient = new ChannelApiClient(this.config);
    }

    @Override
    protected RefundResult doRefund(RefundRequest request) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("p1_MerchantNo", config.getMerchantId());
        params.put("p2_OrderNo", requireOutTradeNo(request));
        params.put("p3_RefundOrderNo", request.getOutRefundNo());
        params.put("p4_RefundAmount", toAmountYuan(request.getRefundAmountCent()));
        if (request.getReason() != null && !request.getReason().isBlank()) {
            params.put("p5_RefundReason", request.getReason());
        }
        params.put("p6_NotifyUrl", requireExtra(request, JoinPayConstants.EXTRA_NOTIFY_URL));
        params.put("q1_version", JoinPayConstants.REFUND_API_VERSION);
        params.put("hmac", JoinPaySignUtil.sign(params, requireApiSecret()));

        HttpResponse response = apiClient.postForm(JoinPayConstants.REFUND_PATH, params);
        return toRefundResult(request.getOutRefundNo(), response.getBody());
    }

    @Override
    protected RefundQueryResult doQueryRefund(RefundQueryRequest request) {
        String outRefundNo = request.getOutRefundNo();
        if (outRefundNo == null || outRefundNo.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚支付退款查询需提供 outRefundNo");
        }
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        params.put("p1_MerchantNo", config.getMerchantId());
        params.put("p2_RefundOrderNo", outRefundNo);
        params.put("p3_Version", JoinPayConstants.REFUND_API_VERSION);
        params.put("hmac", JoinPaySignUtil.sign(params, requireApiSecret()));

        HttpResponse response = apiClient.postForm(JoinPayConstants.QUERY_REFUND_PATH, params);
        return toRefundQueryResult(outRefundNo, response.getBody());
    }

    private RefundResult toRefundResult(String outRefundNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String code = firstNonBlank(Jsons.text(map, "ra_Code"), Jsons.text(map, "rb_Code"));
        if (!"100".equals(code)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇聚支付退款失败",
                    code,
                    Jsons.text(map, "rb_CodeMsg"),
                    null);
        }
        String channelRefundNo = firstNonBlank(Jsons.text(map, "r7_TrxNo"), outRefundNo);
        return new RefundResult(outRefundNo, channelRefundNo, mapRefundStatus(Jsons.text(map, "r6_Status")));
    }

    private RefundQueryResult toRefundQueryResult(String outRefundNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        String code = firstNonBlank(Jsons.text(map, "rb_Code"), Jsons.text(map, "ra_Code"));
        if (!"100".equals(code)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇聚支付退款查询失败",
                    code,
                    Jsons.text(map, "rb_CodeMsg"),
                    null);
        }
        RefundStatus status = mapRefundStatus(Jsons.text(map, "ra_Status"));
        long amountCent = yuanToCent(Jsons.text(map, "r3_Amount"));
        String channelRefundNo = firstNonBlank(Jsons.text(map, "r7_TrxNo"), outRefundNo);
        return new RefundQueryResult(
                Jsons.text(map, "r2_OrderNo"),
                outRefundNo,
                channelRefundNo,
                status,
                amountCent);
    }

    private static RefundStatus mapRefundStatus(String status) {
        if (status == null) {
            return RefundStatus.UNKNOWN;
        }
        return switch (status) {
            case "100" -> RefundStatus.SUCCESS;
            case "101" -> RefundStatus.FAILED;
            case "102" -> RefundStatus.PROCESSING;
            default -> RefundStatus.UNKNOWN;
        };
    }

    private String requireOutTradeNo(RefundRequest request) {
        if (request.getOutTradeNo() != null && !request.getOutTradeNo().isBlank()) {
            return request.getOutTradeNo();
        }
        throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚支付退款需提供 outTradeNo（原支付单号）");
    }

    private String requireApiSecret() {
        String secret = config.getApiSecret();
        if (secret == null || secret.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚支付 MD5 签名需配置 apiSecret");
        }
        return secret;
    }

    private static String toAmountYuan(long amountCent) {
        return BigDecimal.valueOf(amountCent, 2).setScale(2, RoundingMode.HALF_UP).toPlainString();
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
}
