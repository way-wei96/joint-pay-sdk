package com.jointpay.joinpay.openapi;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.common.channel.ChannelApiClient;
import com.jointpay.common.crypto.Rsa2SignUtil;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

/**
 * 汇聚 OpenAPI 客户端（RSA2 + bizContent JSON 字符串）。
 */
public final class JoinPayOpenApiClient {

    public static final String SIGN_TYPE = "RSA2";
    public static final String EXTRA_OPEN_API_GATEWAY = "openApiGateway";

    private final ChannelConfig config;
    private final ChannelApiClient http;

    public JoinPayOpenApiClient(ChannelConfig config) {
        this.config = resolveOpenApiConfig(config);
        this.http = new ChannelApiClient(this.config);
    }

    public Map<String, Object> post(String path, Map<String, Object> bizContent) {
        Map<String, String> envelope = buildSignedEnvelope(bizContent);
        HttpResponse response = http.postJson(path, envelope);
        Map<String, Object> body = Jsons.parseMap(response.getBody());
        Object code = body.get("code");
        if (code instanceof Number num && num.intValue() != 200) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇聚 OpenAPI 请求失败",
                    String.valueOf(code),
                    Jsons.text(body, "msg"),
                    null);
        }
        return body;
    }

    private Map<String, String> buildSignedEnvelope(Map<String, Object> bizContent) {
        String appId = requireAppId();
        String privateKey = requirePrivateKey();
        String bizJson = Jsons.toJson(new TreeMap<>(bizContent));

        Map<String, String> signParams = new TreeMap<>();
        signParams.put("appId", appId);
        signParams.put("bizContent", bizJson);
        signParams.put("requestId", UUID.randomUUID().toString().replace("-", ""));
        signParams.put("signType", SIGN_TYPE);
        signParams.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

        String sign = Rsa2SignUtil.sign(signParams, privateKey);

        Map<String, String> envelope = new LinkedHashMap<>(signParams);
        envelope.put("sign", sign);
        return envelope;
    }

    private String requireAppId() {
        if (config.getAppId() == null || config.getAppId().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚 OpenAPI 需配置 appId");
        }
        return config.getAppId();
    }

    private String requirePrivateKey() {
        if (config.getPrivateKey() == null || config.getPrivateKey().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚 OpenAPI RSA2 需配置 privateKey");
        }
        return config.getPrivateKey();
    }

    private static ChannelConfig resolveOpenApiConfig(ChannelConfig config) {
        String gateway = config.getExtras().get(EXTRA_OPEN_API_GATEWAY);
        if (gateway == null || gateway.isBlank()) {
            gateway = JoinPayOpenApiConstants.DEFAULT_GATEWAY;
        }
        if (gateway.equals(config.getGatewayUrl())) {
            return config;
        }
        return ChannelConfig.builder(config).gatewayUrl(gateway).build();
    }
}
