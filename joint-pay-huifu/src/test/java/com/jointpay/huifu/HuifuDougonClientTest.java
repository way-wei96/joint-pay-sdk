package com.jointpay.huifu;

import com.jointpay.api.exception.JointPayException;
import com.jointpay.common.json.Jsons;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HuifuDougonClientTest {

    @Test
    void parsesGatewayEnvelopeAndDataField() {
        String body = Jsons.toJson(Map.of(
                "resp_code", "10000",
                "resp_desc", "成功",
                "data", Map.of(
                        "sub_resp_code", "00000000",
                        "req_seq_id", "ORDER001",
                        "hf_seq_id", "HF001",
                        "trans_stat", "S",
                        "pay_info", "{\"appId\":\"wx\"}")));

        Map<String, Object> business = HuifuDougonClient.parseBusinessBody(body);
        assertEquals("ORDER001", Jsons.text(business, "req_seq_id"));
        assertEquals("HF001", Jsons.text(business, "hf_seq_id"));
    }

    @Test
    void rejectsGatewayFailure() {
        String body = Jsons.toJson(Map.of("resp_code", "20000", "resp_desc", "失败"));
        assertThrows(JointPayException.class, () -> HuifuDougonClient.parseBusinessBody(body));
    }
}
