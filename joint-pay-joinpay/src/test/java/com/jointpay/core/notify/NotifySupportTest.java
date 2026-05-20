package com.jointpay.core.notify;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.core.PayClientFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotifySupportTest {

    @Test
    void parseAndAck() {
        var client = PayClientFactory.create(ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("M1")
                .build());

        NotifyParseResult result = NotifySupport.parse(client, NotifyRawRequest.builder()
                .params(Map.of(
                        "mchAcctOrderNo", "S1",
                        "mchOrderNo", "O1",
                        "fundStatus", "4"))
                .build());

        assertEquals(NotifyType.PROFIT_SHARING, result.getType());
        assertEquals("success", NotifySupport.ackBody(result));
        assertEquals("O1", NotifySupport.requireProfitSharing(result).getOutTradeNo());
    }
}
