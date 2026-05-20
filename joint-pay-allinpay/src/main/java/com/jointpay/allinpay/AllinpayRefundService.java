package com.jointpay.allinpay;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.refund.RefundQueryRequest;
import com.jointpay.api.refund.RefundQueryResult;
import com.jointpay.api.refund.RefundRequest;
import com.jointpay.api.refund.RefundResult;
import com.jointpay.api.refund.RefundStatus;
import com.jointpay.common.channel.ChannelApiClient;
import com.jointpay.common.crypto.Md5SignUtil;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.refund.AbstractChannelRefundService;

import java.util.Map;
import java.util.TreeMap;

public final class AllinpayRefundService extends AbstractChannelRefundService {

    private static final String DEFAULT_REFUND_PATH = "/api/refund";
    private static final String DEFAULT_QUERY_REFUND_PATH = "/api/refundquery";

    private final ChannelApiClient apiClient;
    private final ChannelConfig config;

    public AllinpayRefundService(ChannelConfig config) {
        super("通联支付");
        this.config = config;
        this.apiClient = new ChannelApiClient(config);
    }

    @Override
    protected RefundResult doRefund(RefundRequest request) {
        String path = request.getExtras().getOrDefault("refundPath", DEFAULT_REFUND_PATH);
        Map<String, String> params = baseParams(request.getRefundAmountCent());
        params.put("reqsn", request.getOutRefundNo());
        params.put("oldreqsn", requireOutTradeNo(request));
        if (request.getReason() != null) {
            params.put("remark", request.getReason());
        }
        String notifyUrl = request.getExtras().get("notifyUrl");
        if (notifyUrl != null) {
            params.put("notify_url", notifyUrl);
        }
        params.put("sign", sign(params));

        HttpResponse response = apiClient.postJson(path, params);
        return toRefundResult(request.getOutRefundNo(), response.getBody());
    }

    @Override
    protected RefundQueryResult doQueryRefund(RefundQueryRequest request) {
        String outRefundNo = request.getOutRefundNo();
        if (outRefundNo == null || outRefundNo.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付退款查询需提供 outRefundNo");
        }
        Map<String, String> params = baseQueryParams();
        params.put("reqsn", outRefundNo);
        params.put("sign", sign(params));

        HttpResponse response = apiClient.postJson(DEFAULT_QUERY_REFUND_PATH, params);
        return toRefundQueryResult(outRefundNo, response.getBody());
    }

    private Map<String, String> baseParams(long amountCent) {
        Map<String, String> params = baseQueryParams();
        params.put("trxamt", String.valueOf(amountCent));
        return params;
    }

    private Map<String, String> baseQueryParams() {
        Map<String, String> params = new TreeMap<>();
        params.put("proid", requireAppId());
        params.put("cusid", config.getMerchantId());
        params.put("signtype", "MD5");
        return params;
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
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付需配置 appId（proid）");
        }
        return appId;
    }

    private String requireOutTradeNo(RefundRequest request) {
        if (request.getOutTradeNo() != null && !request.getOutTradeNo().isBlank()) {
            return request.getOutTradeNo();
        }
        throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付退款需提供 outTradeNo（原支付单号）");
    }

    private RefundResult toRefundResult(String outRefundNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        assertSuccess(map);
        @SuppressWarnings("unchecked")
        Map<String, Object> msg = map.get("msg") instanceof Map<?, ?> m ? (Map<String, Object>) m : map;
        return new RefundResult(
                outRefundNo,
                firstNonBlank(Jsons.text(msg, "trxid"), outRefundNo),
                mapStatus(Jsons.text(msg, "trxstatus")));
    }

    private RefundQueryResult toRefundQueryResult(String outRefundNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        assertSuccess(map);
        @SuppressWarnings("unchecked")
        Map<String, Object> msg = map.get("msg") instanceof Map<?, ?> m ? (Map<String, Object>) m : map;
        return new RefundQueryResult(
                Jsons.text(msg, "oldreqsn"),
                outRefundNo,
                firstNonBlank(Jsons.text(msg, "trxid"), outRefundNo),
                mapStatus(Jsons.text(msg, "trxstatus")),
                parseAmount(Jsons.text(msg, "trxamt")));
    }

    private void assertSuccess(Map<String, Object> map) {
        Object code = map.get("code");
        if (code instanceof Number num && num.intValue() != 200) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "通联支付退款请求失败",
                    String.valueOf(code),
                    Jsons.text(map, "message"),
                    null);
        }
    }

    private static RefundStatus mapStatus(String status) {
        if (status == null) {
            return RefundStatus.UNKNOWN;
        }
        return switch (status) {
            case "0000", "200" -> RefundStatus.SUCCESS;
            case "2000" -> RefundStatus.PROCESSING;
            case "3000" -> RefundStatus.FAILED;
            default -> RefundStatus.UNKNOWN;
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
