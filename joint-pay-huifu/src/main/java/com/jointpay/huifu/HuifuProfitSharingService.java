package com.jointpay.huifu;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.config.ChannelExtras;
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
import com.jointpay.common.util.ChannelRequestExtras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇付斗拱分账。
 *
 * @see <a href="https://paas.huifu.com/open/doc/api/">斗拱 API 文档</a>
 */
public final class HuifuProfitSharingService extends AbstractChannelProfitSharingService {

    static final String DEFAULT_SUBMIT_PATH = "/v2/trade/acctpayment/pay";
    static final String DEFAULT_QUERY_PATH = "/v2/trade/acctpayment/query";
    static final String DEFAULT_CANCEL_PATH = "/v2/trade/acctpayment/refund";
    static final String DEFAULT_ROLLBACK_PATH = "/v2/trade/acctpayment/rollback";

    private final HuifuDougonClient client;
    private final ChannelConfig config;

    public HuifuProfitSharingService(ChannelConfig config) {
        super("汇付天下");
        this.config = HuifuDougonClient.withDefaultGateway(config);
        this.client = new HuifuDougonClient(this.config);
    }

    @Override
    protected ProfitSharingResult doSubmit(ProfitSharingRequest request) {
        String path = request.getExtras().getOrDefault(ChannelExtras.Huifu.SUBMIT_PATH, DEFAULT_SUBMIT_PATH);
        Map<String, Object> data = new HashMap<>();
        data.put("huifu_id", config.getMerchantId());
        data.put("req_seq_id", request.getOutSharingNo());
        if (request.getOutTradeNo() != null) {
            data.put("org_req_seq_id", request.getOutTradeNo());
        }
        data.put("acct_split_bunch", toSplitBunch(request.getScheme().getParticipants()));
        ChannelRequestExtras.mergeInto(data, request.getExtras());

        Map<String, Object> map = client.post(path, data);
        return new ProfitSharingResult(
                request.getOutSharingNo(),
                firstNonBlank(Jsons.text(map, "hf_seq_id"), request.getOutSharingNo()),
                ProfitSharingStatus.PROCESSING);
    }

    @Override
    protected ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("huifu_id", config.getMerchantId());
        if (request.getOutSharingNo() != null) {
            data.put("req_seq_id", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null) {
            data.put("hf_seq_id", request.getChannelSharingNo());
        }

        Map<String, Object> map = client.post(DEFAULT_QUERY_PATH, data);
        return new ProfitSharingQueryResult(
                Jsons.text(map, "org_req_seq_id"),
                Jsons.text(map, "req_seq_id"),
                Jsons.text(map, "hf_seq_id"),
                ProfitSharingStatus.UNKNOWN,
                List.of());
    }

    @Override
    protected ProfitSharingResult doCancel(ProfitSharingCancelRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("huifu_id", config.getMerchantId());
        if (request.getOutSharingNo() != null) {
            data.put("req_seq_id", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null) {
            data.put("hf_seq_id", request.getChannelSharingNo());
        }
        Map<String, Object> map = client.post(DEFAULT_CANCEL_PATH, data);
        return toOperateResult(request.getOutSharingNo(), map);
    }

    @Override
    protected ProfitSharingResult doRollback(ProfitSharingRollbackRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("huifu_id", config.getMerchantId());
        data.put("req_seq_id", request.getOutRollbackNo());
        data.put("org_req_seq_id", request.getOutSharingNo());
        data.put("ord_amt", String.format("%.2f", request.getRollbackAmountCent() / 100.0));
        Map<String, Object> map = client.post(DEFAULT_ROLLBACK_PATH, data);
        return toOperateResult(request.getOutRollbackNo(), map);
    }

    private ProfitSharingResult toOperateResult(String outNo, Map<String, Object> map) {
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

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return null;
    }
}
