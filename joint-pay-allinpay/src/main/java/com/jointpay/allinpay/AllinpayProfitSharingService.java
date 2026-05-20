package com.jointpay.allinpay;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.profitsharing.ProfitSharingCancelRequest;
import com.jointpay.api.profitsharing.ProfitSharingParticipant;
import com.jointpay.api.profitsharing.ProfitSharingQueryRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryResult;
import com.jointpay.api.profitsharing.ProfitSharingRequest;
import com.jointpay.api.profitsharing.ProfitSharingResult;
import com.jointpay.api.profitsharing.ProfitSharingRollbackRequest;
import com.jointpay.api.profitsharing.ProfitSharingStatus;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.profitsharing.AbstractChannelProfitSharingService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class AllinpayProfitSharingService extends AbstractChannelProfitSharingService {

    private static final String SUBMIT_PATH = "/api/share";
    private static final String QUERY_PATH = "/api/sharequery";
    private static final String CANCEL_PATH = "/api/sharecancel";
    private static final String ROLLBACK_PATH = "/api/sharerollback";

    private final AllinpaySignedApi api;

    public AllinpayProfitSharingService(ChannelConfig config) {
        super("通联支付");
        this.api = new AllinpaySignedApi(config);
    }

    @Override
    protected ProfitSharingResult doSubmit(ProfitSharingRequest request) {
        Map<String, String> params = api.baseParams();
        params.put("reqsn", request.getOutSharingNo());
        if (request.getOutTradeNo() != null) {
            params.put("oldreqsn", request.getOutTradeNo());
        }
        params.put("sharelist", Jsons.toJson(toShareList(request.getScheme().getParticipants())));

        Map<String, Object> resp = api.post(SUBMIT_PATH, params);
        api.assertSuccess(resp, "分账提交");
        return toResult(request.getOutSharingNo(), resp);
    }

    @Override
    protected ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request) {
        Map<String, String> params = api.baseParams();
        if (request.getOutSharingNo() != null) {
            params.put("reqsn", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null) {
            params.put("trxid", request.getChannelSharingNo());
        }
        Map<String, Object> resp = api.post(QUERY_PATH, params);
        api.assertSuccess(resp, "分账查询");
        @SuppressWarnings("unchecked")
        Map<String, Object> msg = resp.get("msg") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        return new ProfitSharingQueryResult(
                Jsons.text(msg, "oldreqsn"),
                Jsons.text(msg, "reqsn"),
                Jsons.text(msg, "trxid"),
                mapStatus(Jsons.text(msg, "trxstatus")),
                List.of());
    }

    @Override
    protected ProfitSharingResult doCancel(ProfitSharingCancelRequest request) {
        Map<String, String> params = api.baseParams();
        if (request.getOutSharingNo() != null) {
            params.put("reqsn", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null) {
            params.put("trxid", request.getChannelSharingNo());
        }
        Map<String, Object> resp = api.post(CANCEL_PATH, params);
        api.assertSuccess(resp, "分账撤销");
        return toResult(request.getOutSharingNo(), resp);
    }

    @Override
    protected ProfitSharingResult doRollback(ProfitSharingRollbackRequest request) {
        Map<String, String> params = api.baseParams();
        params.put("reqsn", request.getOutRollbackNo());
        params.put("oldreqsn", request.getOutSharingNo());
        params.put("trxamt", String.valueOf(request.getRollbackAmountCent()));
        String accountNo = request.getExtras().getOrDefault("accountNo", request.getParticipantId());
        if (accountNo != null) {
            params.put("account", accountNo);
        }
        Map<String, Object> resp = api.post(ROLLBACK_PATH, params);
        api.assertSuccess(resp, "分账回退");
        return toResult(request.getOutRollbackNo(), resp);
    }

    private List<Map<String, String>> toShareList(List<ProfitSharingParticipant> participants) {
        List<Map<String, String>> list = new ArrayList<>();
        for (ProfitSharingParticipant p : participants) {
            Map<String, String> item = new HashMap<>();
            item.put("cusid", firstNonBlank(p.getMerchantId(), p.getAccountNo(), p.getParticipantId()));
            item.put("trxamt", String.valueOf(p.getAmountCent()));
            list.add(item);
        }
        return list;
    }

    private ProfitSharingResult toResult(String outNo, Map<String, Object> resp) {
        @SuppressWarnings("unchecked")
        Map<String, Object> msg = resp.get("msg") instanceof Map<?, ?> m ? (Map<String, Object>) m : Map.of();
        return new ProfitSharingResult(
                outNo,
                firstNonBlank(Jsons.text(msg, "trxid"), outNo),
                mapStatus(Jsons.text(msg, "trxstatus")));
    }

    private static ProfitSharingStatus mapStatus(String status) {
        if (status == null) {
            return ProfitSharingStatus.UNKNOWN;
        }
        return switch (status) {
            case "0000", "200" -> ProfitSharingStatus.SUCCESS;
            case "2000" -> ProfitSharingStatus.PROCESSING;
            case "3000" -> ProfitSharingStatus.FAILED;
            default -> ProfitSharingStatus.UNKNOWN;
        };
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
