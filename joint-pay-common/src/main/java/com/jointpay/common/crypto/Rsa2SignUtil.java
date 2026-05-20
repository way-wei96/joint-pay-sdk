package com.jointpay.common.crypto;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * OpenAPI RSA2 签名（SHA256WithRSA，参数按 key 字典序拼接）。
 */
public final class Rsa2SignUtil {

    private Rsa2SignUtil() {
    }

    public static String sign(Map<String, String> params, String privateKeyPemOrBase64) {
        String content = canonicalContent(params);
        try {
            PrivateKey privateKey = loadPrivateKey(privateKeyPemOrBase64);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(signature.sign());
        } catch (JointPayException e) {
            throw e;
        } catch (Exception e) {
            throw new JointPayException(ErrorCode.INTERNAL_ERROR, "RSA2 签名失败", null, null, e);
        }
    }

    public static boolean verify(Map<String, String> params, String publicKeyPemOrBase64) {
        String remoteSign = params.get("sign");
        if (remoteSign == null || remoteSign.isBlank()) {
            return false;
        }
        String content = canonicalContent(params);
        try {
            PublicKey publicKey = loadPublicKey(publicKeyPemOrBase64);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            return signature.verify(Base64.getDecoder().decode(remoteSign.trim()));
        } catch (JointPayException e) {
            throw e;
        } catch (Exception e) {
            throw new JointPayException(ErrorCode.INTERNAL_ERROR, "RSA2 验签失败", null, null, e);
        }
    }

    private static String canonicalContent(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .filter(e -> !"sign".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }

    private static PublicKey loadPublicKey(String key) throws Exception {
        String normalized = key.trim();
        if (normalized.contains("BEGIN")) {
            normalized = normalized
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
        }
        byte[] encoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encoded));
    }

    private static PrivateKey loadPrivateKey(String key) throws Exception {
        String normalized = key.trim();
        if (normalized.contains("BEGIN")) {
            normalized = normalized
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
        }
        byte[] encoded = Base64.getDecoder().decode(normalized);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(encoded));
    }
}
