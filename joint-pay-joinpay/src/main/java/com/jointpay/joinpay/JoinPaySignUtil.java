package com.jointpay.joinpay;

import com.jointpay.common.crypto.Md5SignUtil;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

final class JoinPaySignUtil {

    private JoinPaySignUtil() {
    }

    static String sign(Map<String, String> orderedParams, String secret) {
        return Md5SignUtil.signOrderedValues(orderedParams, secret);
    }

    static boolean verifyNotify(Map<String, String> params, String secret) {
        String remoteSign = params.get("hmac");
        if (remoteSign == null || remoteSign.isBlank()) {
            return false;
        }
        Map<String, String> sorted = new TreeMap<>(params);
        sorted.remove("hmac");
        StringBuilder str = new StringBuilder();
        for (Map.Entry<String, String> entry : sorted.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue() == null ? "" : entry.getValue();
            if ("ra_PayTime".equals(key) || "rb_DealTime".equals(key) || "r5_Mp".equals(key)) {
                str.append(urlDecode(value));
            } else {
                str.append(value);
            }
        }
        str.append(secret);
        return Md5SignUtil.md5Hex(str.toString()).equalsIgnoreCase(remoteSign);
    }

    private static String urlDecode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
