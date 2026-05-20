package com.jointpay.common.util;

import com.jointpay.api.config.ChannelExtras;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ChannelRequestExtrasTest {

    @Test
    void skipsRoutingKeysAndDoesNotOverrideExistingFields() {
        Map<String, Object> body = new HashMap<>();
        body.put("req_seq_id", "ORDER001");
        body.put("trans_amt", "1.00");

        ChannelRequestExtras.mergeInto(body, Map.of(
                ChannelExtras.Huifu.API_PATH, "/custom",
                ChannelExtras.Huifu.PAY_TYPE, "WX",
                "terminal_device_info", "{\"device_id\":\"D1\"}",
                "req_seq_id", "SHOULD_NOT_OVERRIDE"));

        assertEquals("ORDER001", body.get("req_seq_id"));
        assertEquals("1.00", body.get("trans_amt"));
        assertEquals("{\"device_id\":\"D1\"}", body.get("terminal_device_info"));
        assertFalse(body.containsKey(ChannelExtras.Huifu.API_PATH));
    }

    @Test
    void mergeIntoStringMapSkipsRoutingKeys() {
        Map<String, String> params = new java.util.TreeMap<>();
        params.put("reqsn", "ORDER001");

        ChannelRequestExtras.mergeIntoStringMap(params, Map.of(
                ChannelExtras.Allinpay.PAY_TYPE, "WX",
                "remark", "note"));

        assertEquals("ORDER001", params.get("reqsn"));
        assertEquals("note", params.get("remark"));
        assertFalse(params.containsKey(ChannelExtras.Allinpay.PAY_TYPE));
    }
}
