package com.jointpay.allinpay;

import com.jointpay.api.PayChannel;
import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.payment.PayStatus;
import com.jointpay.api.profitsharing.ProfitSharingStatus;
import com.jointpay.api.refund.RefundStatus;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllinpayNotifyHandlerTest {

    private final AllinpayNotifyHandler handler = new AllinpayNotifyHandler(
            ChannelConfig.builder(PayChannel.ALLINPAY).merchantId("M1").build());

    @Test
    void parsesPayNotify() {
        NotifyParseResult result = handler.parse(NotifyRawRequest.builder()
                .params(Map.of(
                        "reqsn", "ORDER001",
                        "trxid", "TL001",
                        "trxstatus", "0000",
                        "trxamt", "100"))
                .build());

        assertEquals(NotifyType.PAY, result.getType());
        assertEquals("ORDER001", result.getPay().getOutTradeNo());
        assertEquals(PayStatus.SUCCESS, result.getPay().getStatus());
        assertEquals(100L, result.getPay().getAmountCent());
    }

    @Test
    void parsesRefundBeforeProfitSharingHeuristic() {
        NotifyParseResult result = handler.parse(NotifyRawRequest.builder()
                .params(Map.of(
                        "trxtype", "REFUND",
                        "reqsn", "REFUND001",
                        "oldreqsn", "ORDER001",
                        "trxid", "TL002",
                        "trxstatus", "0000",
                        "trxamt", "100"))
                .build());

        assertEquals(NotifyType.REFUND, result.getType());
        assertEquals("REFUND001", result.getRefund().getOutRefundNo());
        assertEquals(RefundStatus.SUCCESS, result.getRefund().getStatus());
    }

    @Test
    void parsesProfitSharingNotify() {
        NotifyParseResult result = handler.parse(NotifyRawRequest.builder()
                .params(Map.of(
                        "shareno", "SHARE001",
                        "oldreqsn", "ORDER001",
                        "shareid", "TL003",
                        "trxstatus", "0000"))
                .build());

        assertEquals(NotifyType.PROFIT_SHARING, result.getType());
        assertEquals("SHARE001", result.getProfitSharing().getOutSharingNo());
        assertEquals(ProfitSharingStatus.SUCCESS, result.getProfitSharing().getStatus());
    }
}
