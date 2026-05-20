package com.jointpay.joinpay;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.profitsharing.ProfitSharingCancelRequest;
import com.jointpay.api.profitsharing.ProfitSharingMode;
import com.jointpay.api.profitsharing.ProfitSharingParticipant;
import com.jointpay.api.profitsharing.ProfitSharingQueryRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryResult;
import com.jointpay.api.profitsharing.ProfitSharingRequest;
import com.jointpay.api.profitsharing.ProfitSharingResult;
import com.jointpay.api.profitsharing.ProfitSharingRollbackRequest;
import com.jointpay.api.profitsharing.ProfitSharingStatus;
import com.jointpay.common.json.Jsons;
import com.jointpay.common.profitsharing.AbstractChannelProfitSharingService;
import com.jointpay.joinpay.openapi.JoinPayOpenApiClient;
import com.jointpay.joinpay.openapi.JoinPayOpenApiConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 汇聚支付分账 OpenAPI（RSA2）。
 */
public final class JoinPayProfitSharingService extends AbstractChannelProfitSharingService {

    private final JoinPayOpenApiClient openApi;

    public JoinPayProfitSharingService(ChannelConfig config) {
        super("汇聚支付");
        this.openApi = new JoinPayOpenApiClient(config);
    }

    @Override
    protected ProfitSharingResult doSubmit(ProfitSharingRequest request) {
        Map<String, Object> biz = buildSubmitBizContent(request);
        Map<String, Object> resp = openApi.post(JoinPayOpenApiConstants.ACCT_ORDER_SUBMIT, biz);
        return toSubmitResult(request.getOutSharingNo(), resp);
    }

    @Override
    protected ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request) {
        Map<String, Object> biz = new HashMap<>();
        if (request.getOutSharingNo() != null && !request.getOutSharingNo().isBlank()) {
            biz.put("mchAcctOrderNo", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null && !request.getChannelSharingNo().isBlank()) {
            biz.put("batchNo", request.getChannelSharingNo());
        }
        Map<String, Object> resp = openApi.post(JoinPayOpenApiConstants.ACCT_ORDER_QUERY, biz);
        return toQueryResult(resp);
    }

    @Override
    protected ProfitSharingResult doCancel(ProfitSharingCancelRequest request) {
        Map<String, Object> biz = new HashMap<>();
        if (request.getOutSharingNo() != null && !request.getOutSharingNo().isBlank()) {
            biz.put("mchAcctOrderNo", request.getOutSharingNo());
        }
        if (request.getChannelSharingNo() != null && !request.getChannelSharingNo().isBlank()) {
            biz.put("batchNo", request.getChannelSharingNo());
        }
        Map<String, Object> resp = openApi.post(JoinPayOpenApiConstants.ACCT_ORDER_CANCEL, biz);
        return toOperateResult(request.getOutSharingNo(), resp);
    }

    @Override
    protected ProfitSharingResult doRollback(ProfitSharingRollbackRequest request) {
        String accountNo = firstNonBlank(
                request.getExtras().get("accountNo"),
                request.getParticipantId());
        if (accountNo == null) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "汇聚分账回退需提供 participantId 或 extras.accountNo");
        }
        Map<String, String> item = new HashMap<>();
        item.put("accountNo", accountNo);
        item.put("amount", toAmountYuan(request.getRollbackAmountCent()));
        item.put("description", request.getReason() == null ? "分账回退" : request.getReason());

        Map<String, Object> biz = new HashMap<>();
        biz.put("mchAcctOrderNo", request.getOutSharingNo());
        biz.put("mchAcctRefundOrderNo", request.getOutRollbackNo());
        String batchNo = request.getExtras().get("batchNo");
        if (batchNo != null && !batchNo.isBlank()) {
            biz.put("batchNo", batchNo);
        }
        String notifyUrl = request.getExtras().get("notifyUrl");
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            biz.put("notifyUrl", notifyUrl);
        }
        biz.put("acctInfos", List.of(item));

        Map<String, Object> resp = openApi.post(JoinPayOpenApiConstants.ACCT_REFUND_SUBMIT, biz);
        return toOperateResult(request.getOutRollbackNo(), resp);
    }

    private ProfitSharingResult toOperateResult(String outNo, Map<String, Object> resp) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = resp.get("data") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        return new ProfitSharingResult(
                firstNonBlank(Jsons.text(data, "mchAcctOrderNo"), Jsons.text(data, "mchAcctRefundOrderNo"), outNo),
                firstNonBlank(Jsons.text(data, "batchNo"), outNo),
                ProfitSharingStatus.PROCESSING);
    }

    private Map<String, Object> buildSubmitBizContent(ProfitSharingRequest request) {
        Map<String, Object> biz = new HashMap<>();
        biz.put("mchAcctOrderNo", request.getOutSharingNo());
        if (request.getOutTradeNo() != null && !request.getOutTradeNo().isBlank()) {
            biz.put("mchOrderNo", request.getOutTradeNo());
        }
        if (request.getChannelTradeNo() != null && !request.getChannelTradeNo().isBlank()) {
            biz.put("payOrderNo", request.getChannelTradeNo());
        }
        String notifyUrl = request.getExtras().get("notifyUrl");
        if (notifyUrl != null && !notifyUrl.isBlank()) {
            biz.put("notifyUrl", notifyUrl);
        }
        biz.put("acctInfos", toAcctInfos(request.getScheme().getParticipants()));
        return biz;
    }

    private List<Map<String, String>> toAcctInfos(List<ProfitSharingParticipant> participants) {
        List<Map<String, String>> list = new ArrayList<>();
        for (ProfitSharingParticipant p : participants) {
            String accountNo = firstNonBlank(p.getAccountNo(), p.getMerchantId(), p.getParticipantId());
            if (accountNo == null) {
                throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "分账参与方缺少 accountNo/merchantId");
            }
            String amount = p.getMode() == ProfitSharingMode.RATIO
                    ? null
                    : toAmountYuan(p.getAmountCent());
            if (amount == null && p.getRatioBps() <= 0) {
                throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "分账参与方需指定 amountCent 或 ratioBps");
            }
            Map<String, String> item = new HashMap<>();
            item.put("accountNo", accountNo);
            if (amount != null) {
                item.put("amount", amount);
            }
            String desc = p.getRole() == null ? "分账" : p.getRole();
            item.put("description", desc);
            list.add(item);
        }
        return list;
    }

    private ProfitSharingResult toSubmitResult(String outSharingNo, Map<String, Object> resp) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = resp.get("data") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        String batchNo = Jsons.text(data, "batchNo");
        ProfitSharingStatus status = mapFundStatusFromList(data.get("acctInfos"));
        return new ProfitSharingResult(
                firstNonBlank(Jsons.text(data, "mchAcctOrderNo"), outSharingNo),
                firstNonBlank(batchNo, outSharingNo),
                status);
    }

    private ProfitSharingQueryResult toQueryResult(Map<String, Object> resp) {
        @SuppressWarnings("unchecked")
        Map<String, Object> data = resp.get("data") instanceof Map<?, ?> m
                ? (Map<String, Object>) m
                : Map.of();
        ProfitSharingStatus status = mapFundStatusFromList(data.get("acctInfos"));
        return new ProfitSharingQueryResult(
                Jsons.text(data, "mchOrderNo"),
                Jsons.text(data, "mchAcctOrderNo"),
                Jsons.text(data, "batchNo"),
                status,
                List.of());
    }

    private static ProfitSharingStatus mapFundStatusFromList(Object acctInfos) {
        if (!(acctInfos instanceof List<?> list) || list.isEmpty()) {
            return ProfitSharingStatus.PROCESSING;
        }
        Object first = list.getFirst();
        if (first instanceof Map<?, ?> item) {
            Object fundStatus = item.get("fundStatus");
            if (fundStatus instanceof Number num) {
                return mapFundStatus(num.intValue());
            }
        }
        return ProfitSharingStatus.UNKNOWN;
    }

    private static ProfitSharingStatus mapFundStatus(int fundStatus) {
        return switch (fundStatus) {
            case 4 -> ProfitSharingStatus.SUCCESS;
            case 2, 3 -> ProfitSharingStatus.PROCESSING;
            case 5 -> ProfitSharingStatus.FAILED;
            case 6 -> ProfitSharingStatus.CANCELLED;
            default -> ProfitSharingStatus.UNKNOWN;
        };
    }

    private static String toAmountYuan(long amountCent) {
        return BigDecimal.valueOf(amountCent, 2).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
