package com.jointpay.joinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.profitsharing.ProfitSharingStatus;
import com.jointpay.common.crypto.Rsa2SignUtil;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JoinPayNotifyHandlerTest {

    @Test
    void parsesProfitSharingWithoutSign() {
        ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("M1")
                .build();
        JoinPayNotifyHandler handler = new JoinPayNotifyHandler(config);

        NotifyParseResult result = handler.parse(NotifyRawRequest.builder()
                .params(Map.of(
                        "mchAcctOrderNo", "SHARE001",
                        "mchOrderNo", "ORDER001",
                        "fundStatus", "4"))
                .build());

        assertEquals(NotifyType.PROFIT_SHARING, result.getType());
        assertEquals("ORDER001", result.getProfitSharing().getOutTradeNo());
        assertEquals("SHARE001", result.getProfitSharing().getOutSharingNo());
        assertEquals(ProfitSharingStatus.SUCCESS, result.getProfitSharing().getStatus());
        assertEquals(JoinPayConstants.NOTIFY_SUCCESS_RESPONSE, result.getSuccessResponseBody());
    }

    @Test
    void verifiesOpenApiSignWhenPresent() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        Map<String, String> params = new TreeMap<>();
        params.put("mchAcctOrderNo", "SHARE002");
        params.put("mchOrderNo", "ORDER002");
        params.put("fundStatus", "4");
        params.put("appId", "APP1");
        params.put("sign", Rsa2SignUtil.sign(params, privateKey));

        ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("M1")
                .publicKey(publicKey)
                .build();
        JoinPayNotifyHandler handler = new JoinPayNotifyHandler(config);

        NotifyParseResult result = handler.parse(NotifyRawRequest.builder().params(params).build());
        assertEquals(NotifyType.PROFIT_SHARING, result.getType());
        assertTrue(Rsa2SignUtil.verify(params, publicKey));
    }
}
