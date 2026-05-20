package com.jointpay.huifu;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.common.crypto.Rsa2SignUtil;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.payment.PayStatus;
import com.jointpay.api.profitsharing.ProfitSharingStatus;
import com.jointpay.api.refund.RefundStatus;
import com.jointpay.common.json.Jsons;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuifuNotifyHandlerTest {

    private final HuifuNotifyHandler handler = new HuifuNotifyHandler(
            ChannelConfig.builder(PayChannel.HUIFU).merchantId("HF001").build());

    @Test
    void parsesPayNotify() {
        String body = Jsons.toJson(java.util.Map.of(
                "req_seq_id", "ORDER001",
                "hf_seq_id", "HF001",
                "trans_stat", "S",
                "trans_amt", "1.00"));

        NotifyParseResult result = handler.parse(NotifyRawRequest.builder().body(body).build());
        assertEquals(NotifyType.PAY, result.getType());
        assertEquals("ORDER001", result.getPay().getOutTradeNo());
        assertEquals(PayStatus.SUCCESS, result.getPay().getStatus());
    }

    @Test
    void parsesRefundNotify() {
        String body = Jsons.toJson(java.util.Map.of(
                "req_seq_id", "REFUND001",
                "org_req_seq_id", "ORDER001",
                "hf_seq_id", "HF002",
                "refund_amt", "1.00",
                "trans_stat", "S"));

        NotifyParseResult result = handler.parse(NotifyRawRequest.builder().body(body).build());
        assertEquals(NotifyType.REFUND, result.getType());
        assertEquals("REFUND001", result.getRefund().getOutRefundNo());
        assertEquals(RefundStatus.SUCCESS, result.getRefund().getStatus());
    }

    @Test
    void parsesProfitSharingNotify() {
        String body = Jsons.toJson(java.util.Map.of(
                "org_req_seq_id", "ORDER001",
                "req_seq_id", "SHARE001",
                "hf_seq_id", "HF003",
                "trans_stat", "S"));

        NotifyParseResult result = handler.parse(NotifyRawRequest.builder().body(body).build());
        assertEquals(NotifyType.PROFIT_SHARING, result.getType());
        assertEquals("SHARE001", result.getProfitSharing().getOutSharingNo());
        assertEquals(ProfitSharingStatus.SUCCESS, result.getProfitSharing().getStatus());
    }

    @Test
    void verifiesRsa2SignWhenPresent() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair pair = generator.generateKeyPair();
        String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        Map<String, String> params = new TreeMap<>();
        params.put("req_seq_id", "ORDER002");
        params.put("hf_seq_id", "HF004");
        params.put("trans_stat", "S");
        params.put("trans_amt", "1.00");
        params.put("sign", Rsa2SignUtil.sign(params, privateKey));

        ChannelConfig config = ChannelConfig.builder(PayChannel.HUIFU)
                .merchantId("HF001")
                .publicKey(publicKey)
                .build();
        HuifuNotifyHandler signedHandler = new HuifuNotifyHandler(config);

        NotifyParseResult result = signedHandler.parse(NotifyRawRequest.builder().params(params).build());
        assertEquals(NotifyType.PAY, result.getType());
        assertEquals("ORDER002", result.getPay().getOutTradeNo());
    }
}
