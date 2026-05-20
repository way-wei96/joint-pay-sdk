package com.jointpay.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;

public final class Md5SignUtil {

    private Md5SignUtil() {
    }

    /** 按 Map 插入顺序拼接值后追加密钥做 MD5（汇聚支付等方式）。 */
    public static String signOrderedValues(Map<String, String> orderedParams, String secret) {
        StringBuilder sb = new StringBuilder();
        orderedParams.values().forEach(sb::append);
        sb.append(secret);
        return md5Hex(sb.toString());
    }

    /** 按 key 字典序拼接 {@code key=value&} 后追加密钥做 MD5（通联等常见方式）。 */
    public static String signSortedKeyValues(Map<String, String> params, String secret, boolean excludeSignKey) {
        String content = params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .filter(e -> !excludeSignKey || !"sign".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return md5Hex(content + "&key=" + secret);
    }

    public static String md5Hex(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 不可用", e);
        }
    }
}
