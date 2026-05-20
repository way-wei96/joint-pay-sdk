package com.jointpay.huifu;

import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;
import com.jointpay.api.notify.NotifyType;
import com.jointpay.api.payment.PayStatus;
import com.jointpay.api.profitsharing.ProfitSharingStatus;
import com.jointpay.api.refund.RefundStatus;
import com.jointpay.common.json.Jsons;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HuifuNotifyHandlerTest {

    private final HuifuNotifyHandler handler = new HuifuNotifyHandler();

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
}
