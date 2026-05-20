package com.jointpay.allinpay;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.common.channel.ChannelApiClient;
import com.jointpay.common.crypto.Md5SignUtil;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;

import java.util.Map;
import java.util.TreeMap;

/**
 * 通联 MD5 签名 JSON 请求封装。
 */
final class AllinpaySignedApi {

    private final ChannelConfig config;
    private final ChannelApiClient http;

    AllinpaySignedApi(ChannelConfig config) {
        this.config = config;
        this.http = new ChannelApiClient(config);
    }

    Map<String, Object> post(String path, Map<String, String> params) {
        params.put("sign", sign(params));
        HttpResponse response = http.postJson(path, params);
        return Jsons.parseMap(response.getBody());
    }

    Map<String, String> baseParams() {
        Map<String, String> params = new TreeMap<>();
        params.put("proid", requireAppId());
        params.put("cusid", config.getMerchantId());
        params.put("signtype", "MD5");
        return params;
    }

    String sign(Map<String, String> params) {
        String secret = config.getApiSecret();
        if (secret == null || secret.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付 MD5 签名需配置 apiSecret");
        }
        return Md5SignUtil.signSortedKeyValues(params, secret, true);
    }

    void assertSuccess(Map<String, Object> map, String action) {
        Object code = map.get("code");
        if (code instanceof Number num && num.intValue() != 200) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "通联支付" + action + "失败",
                    String.valueOf(code),
                    Jsons.text(map, "message"),
                    null);
        }
    }

    private String requireAppId() {
        String appId = config.getAppId();
        if (appId == null || appId.isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "通联支付需配置 appId（proid）");
        }
        return appId;
    }
}
