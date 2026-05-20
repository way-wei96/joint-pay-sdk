package com.jointpay.huifu;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.common.channel.ChannelApiClient;
import com.jointpay.common.crypto.Rsa2SignUtil;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * 汇付斗拱标准请求封装：Query 公共头 + Body {@code sys_id/sign/data}，对 {@code data} JSON 做 RSA2 签名。
 *
 * @see <a href="https://spin.cloudpnr.com/topds/paramStandards.html">参数规定</a>
 * @see <a href="https://paas.huifu.com/open/doc/api/">API 文档</a>
 */
final class HuifuDougonClient {

    static final String SIGN_TYPE = "RSA2";
    static final String FORMAT = "JSON";
    static final String CHARSET = "UTF-8";
    static final String VERSION = "1.0.0";

    private final ChannelConfig config;
    private final ChannelApiClient http;

    HuifuDougonClient(ChannelConfig config) {
        this.config = withDefaultGateway(config);
        this.http = new ChannelApiClient(this.config);
    }

    Map<String, Object> post(String path, Map<String, Object> data) {
        Map<String, Object> payload = new TreeMap<>(data);
        payload.putIfAbsent("req_date", LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));

        String dataJson = Jsons.toJson(payload);
        String sign = Rsa2SignUtil.signContent(dataJson, requirePrivateKey());

        Map<String, Object> envelope = new LinkedHashMap<>();
        envelope.put("sys_id", requireSysId());
        envelope.put("sign_type", SIGN_TYPE);
        envelope.put("sign", sign);
        envelope.put("data", dataJson);

        HttpResponse response = http.postJson(path, envelope, queryParams());
        return parseBusinessBody(response.getBody());
    }

    static Map<String, Object> parseBusinessBody(String body) {
        Map<String, Object> envelope = Jsons.parseMap(body);
        String gatewayCode = Jsons.text(envelope, "resp_code");
        if (gatewayCode != null && !"10000".equals(gatewayCode)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇付斗拱网关失败",
                    gatewayCode,
                    Jsons.text(envelope, "resp_desc"),
                    null);
        }
        Map<String, Object> business = extractDataMap(envelope);
        String subCode = firstNonBlank(
                Jsons.text(business, "sub_resp_code"),
                Jsons.text(business, "resp_code"));
        if (subCode != null
                && !"00000000".equals(subCode)
                && !"00000100".equals(subCode)
                && !"S".equalsIgnoreCase(subCode)
                && !"SUCCESS".equalsIgnoreCase(subCode)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇付斗拱业务失败",
                    subCode,
                    firstNonBlank(Jsons.text(business, "sub_resp_desc"), Jsons.text(business, "resp_desc")),
                    null);
        }
        return business;
    }

    static Map<String, Object> extractDataMap(Map<String, Object> envelope) {
        Object data = envelope.get("data");
        if (data == null) {
            return envelope;
        }
        if (data instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cast = (Map<String, Object>) map;
            return cast;
        }
        return Jsons.parseMap(String.valueOf(data));
    }

    private Map<String, String> queryParams() {
        return Map.of(
                "product_id", requireProductId(),
                "format", FORMAT,
                "charset", CHARSET,
                "version", VERSION);
    }

    private String requireSysId() {
        String sysId = firstNonBlank(config.getApiKey(), config.getExtras().get("sysId"));
        if (sysId == null || sysId.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付斗拱需配置 apiKey（sys_id）或 extras.sysId");
        }
        return sysId;
    }

    private String requireProductId() {
        String productId = firstNonBlank(config.getAppId(), config.getExtras().get("productId"));
        if (productId == null || productId.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付斗拱需配置 appId（product_id）或 extras.productId");
        }
        return productId;
    }

    private String requirePrivateKey() {
        if (config.getPrivateKey() == null || config.getPrivateKey().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇付斗拱出站请求需配置 privateKey");
        }
        return config.getPrivateKey();
    }

    static ChannelConfig withDefaultGateway(ChannelConfig config) {
        if (config.getGatewayUrl() != null && !config.getGatewayUrl().isBlank()) {
            return config;
        }
        return ChannelConfig.builder(config).gatewayUrl(HuifuConstants.DEFAULT_GATEWAY).build();
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
