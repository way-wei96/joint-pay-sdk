package com.jointpay.huifu;

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
import com.jointpay.common.channel.ChannelApiClient;
import com.jointpay.common.http.HttpResponse;
import com.jointpay.common.json.Jsons;
import com.jointpay.api.config.ChannelExtras;
import com.jointpay.common.profitsharing.AbstractChannelProfitSharingService;
import com.jointpay.common.util.ChannelRequestExtras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇付天下斗拱分账（路径以商户文档为准，可通过 extras 覆盖）。
 */
public final class HuifuProfitSharingService extends AbstractChannelProfitSharingService {

    static final String DEFAULT_SUBMIT_PATH = "/v2/trade/acctpayment/pay";
    static final String DEFAULT_QUERY_PATH = "/v2/trade/acctpayment/query";
    static final String DEFAULT_CANCEL_PATH = "/v2/trade/acctpayment/refund";
    static final String DEFAULT_ROLLBACK_PATH = "/v2/trade/acctpayment/rollback";

    private final ChannelConfig config;
    private final ChannelApiClient apiClient;

    public HuifuProfitSharingService(ChannelConfig config) {
        super("汇付天下");
        this.config = config;
        this.apiClient = new ChannelApiClient(config);
    }

    @Override
    protected ProfitSharingResult doSubmit(ProfitSharingRequest request) {
        String path = request.getExtras().getOrDefault(ChannelExtras.Huifu.SUBMIT_PATH, DEFAULT_SUBMIT_PATH);
        Map<String, Object> body = new HashMap<>();
        body.put("huifu_id", config.getMerchantId());
        body.put("req_seq_id", request.getOutSharingNo());
        if (request.getOutTradeNo() != null) {
            body.put("org_req_seq_id", request.getOutTradeNo());
        }
        body.put("acct_split_bunch", toSplitBunch(request.getScheme().getParticipants()));
        ChannelRequestExtras.mergeInto(body, request.getExtras());

        HttpResponse response = apiClient.postJson(path, body);
        return toSubmitResult(request.getOutSharingNo(), response.getBody());
    }

    @Override
    protected ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request) {
        String path = DEFAULT_QUERY_PATH;
        Map<String, Object> body = new HashMap<>();
        body.put("huifu_id", config.getMerchantId());
        if (request.getOutSharingNo() != null) {
            body.put("req_seq_id", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null) {
            body.put("hf_seq_id", request.getChannelSharingNo());
        }

        HttpResponse response = apiClient.postJson(path, body);
        return toQueryResult(response.getBody());
    }

    @Override
    protected ProfitSharingResult doCancel(ProfitSharingCancelRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("huifu_id", config.getMerchantId());
        if (request.getOutSharingNo() != null) {
            body.put("req_seq_id", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null) {
            body.put("hf_seq_id", request.getChannelSharingNo());
        }
        HttpResponse response = apiClient.postJson(DEFAULT_CANCEL_PATH, body);
        return toOperateResult(request.getOutSharingNo(), response.getBody());
    }

    @Override
    protected ProfitSharingResult doRollback(ProfitSharingRollbackRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("huifu_id", config.getMerchantId());
        body.put("req_seq_id", request.getOutRollbackNo());
        body.put("org_req_seq_id", request.getOutSharingNo());
        body.put("ord_amt", String.format("%.2f", request.getRollbackAmountCent() / 100.0));
        HttpResponse response = apiClient.postJson(DEFAULT_ROLLBACK_PATH, body);
        return toOperateResult(request.getOutRollbackNo(), response.getBody());
    }

    private ProfitSharingResult toOperateResult(String outNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        assertHuifuOk(map);
        return new ProfitSharingResult(
                outNo,
                firstNonBlank(Jsons.text(map, "hf_seq_id"), outNo),
                ProfitSharingStatus.PROCESSING);
    }

    private List<Map<String, String>> toSplitBunch(List<ProfitSharingParticipant> participants) {
        List<Map<String, String>> bunch = new ArrayList<>();
        for (ProfitSharingParticipant p : participants) {
            Map<String, String> item = new HashMap<>();
            item.put("huifu_id", firstNonBlank(p.getMerchantId(), p.getAccountNo()));
            item.put("div_amt", String.format("%.2f", p.getAmountCent() / 100.0));
            bunch.add(item);
        }
        return bunch;
    }

    private ProfitSharingResult toSubmitResult(String outSharingNo, String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        assertHuifuOk(map);
        return new ProfitSharingResult(
                outSharingNo,
                firstNonBlank(Jsons.text(map, "hf_seq_id"), outSharingNo),
                ProfitSharingStatus.PROCESSING);
    }

    private ProfitSharingQueryResult toQueryResult(String body) {
        Map<String, Object> map = Jsons.parseMap(body);
        assertHuifuOk(map);
        return new ProfitSharingQueryResult(
                Jsons.text(map, "org_req_seq_id"),
                Jsons.text(map, "req_seq_id"),
                Jsons.text(map, "hf_seq_id"),
                ProfitSharingStatus.UNKNOWN,
                List.of());
    }

    private static void assertHuifuOk(Map<String, Object> map) {
        String code = firstNonBlank(Jsons.text(map, "resp_code"), Jsons.text(map, "sub_resp_code"));
        if (code != null && !"00000000".equals(code) && !"00000100".equals(code)) {
            throw new JointPayException(
                    ErrorCode.CHANNEL_ERROR,
                    "汇付天下分账失败",
                    code,
                    Jsons.text(map, "resp_desc"),
                    null);
        }
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
