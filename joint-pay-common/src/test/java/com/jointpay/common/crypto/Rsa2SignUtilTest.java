package com.jointpay.common.crypto;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Rsa2SignUtilTest {

    @Test
    void signAndVerifyRoundTrip() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        Map<String, String> params = new TreeMap<>();
        params.put("appId", "APP001");
        params.put("bizContent", "{\"mchOrderNo\":\"O1\"}");
        params.put("signType", "RSA2");
        params.put("timestamp", "1710000000");

        String sign = Rsa2SignUtil.sign(params, privateKey);
        params.put("sign", sign);

        assertTrue(Rsa2SignUtil.verify(params, publicKey));
    }

    @Test
    void verifyFailsWhenTampered() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();

        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        Map<String, String> params = new TreeMap<>();
        params.put("appId", "APP001");
        params.put("bizContent", "{}");
        params.put("sign", Rsa2SignUtil.sign(params, privateKey));
        params.put("bizContent", "{\"tampered\":true}");

        assertFalse(Rsa2SignUtil.verify(params, publicKey));
    }
}
