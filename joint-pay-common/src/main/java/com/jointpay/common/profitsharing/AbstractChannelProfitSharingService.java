package com.jointpay.common.profitsharing;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.profitsharing.ProfitSharingBindRequest;
import com.jointpay.api.profitsharing.ProfitSharingCancelRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryResult;
import com.jointpay.api.profitsharing.ProfitSharingRequest;
import com.jointpay.api.profitsharing.ProfitSharingResult;
import com.jointpay.api.profitsharing.ProfitSharingRollbackRequest;
import com.jointpay.api.profitsharing.ProfitSharingScheme;
import com.jointpay.api.profitsharing.ProfitSharingService;

/**
 * 渠道分账服务模板：统一校验分账方案与单号。
 */
public abstract class AbstractChannelProfitSharingService implements ProfitSharingService {

    private final String channelName;

    protected AbstractChannelProfitSharingService(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public final void bindOnOrder(ProfitSharingBindRequest request) {
        validateBind(request);
        doBindOnOrder(request);
    }

    @Override
    public final ProfitSharingResult submit(ProfitSharingRequest request) {
        validateSubmit(request);
        return doSubmit(request);
    }

    @Override
    public final ProfitSharingQueryResult query(ProfitSharingQueryRequest request) {
        if ((request.getOutSharingNo() == null || request.getOutSharingNo().isBlank())
                && (request.getChannelSharingNo() == null || request.getChannelSharingNo().isBlank())) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outSharingNo 与 channelSharingNo 不能同时为空");
        }
        return doQuery(request);
    }

    @Override
    public final ProfitSharingResult cancel(ProfitSharingCancelRequest request) {
        if ((request.getOutSharingNo() == null || request.getOutSharingNo().isBlank())
                && (request.getChannelSharingNo() == null || request.getChannelSharingNo().isBlank())) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outSharingNo 与 channelSharingNo 不能同时为空");
        }
        return doCancel(request);
    }

    @Override
    public final ProfitSharingResult rollback(ProfitSharingRollbackRequest request) {
        validateRollback(request);
        return doRollback(request);
    }

    protected void doBindOnOrder(ProfitSharingBindRequest request) {
        ProfitSharingBindStores.put(request.getOutTradeNo(), request.getScheme());
    }

    protected abstract ProfitSharingResult doSubmit(ProfitSharingRequest request);

    protected abstract ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request);

    protected abstract ProfitSharingResult doCancel(ProfitSharingCancelRequest request);

    protected abstract ProfitSharingResult doRollback(ProfitSharingRollbackRequest request);

    protected void validateScheme(ProfitSharingScheme scheme) {
        if (scheme == null || scheme.getParticipants().isEmpty()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, channelName + " 分账方案不能为空");
        }
    }

    protected void validateBind(ProfitSharingBindRequest request) {
        if (request.getOutTradeNo() == null || request.getOutTradeNo().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outTradeNo 不能为空");
        }
        validateScheme(request.getScheme());
    }

    protected void validateSubmit(ProfitSharingRequest request) {
        if (request.getOutSharingNo() == null || request.getOutSharingNo().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outSharingNo 不能为空");
        }
        validateScheme(request.getScheme());
    }

    protected void validateRollback(ProfitSharingRollbackRequest request) {
        if (request.getOutSharingNo() == null || request.getOutSharingNo().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outSharingNo 不能为空");
        }
        if (request.getOutRollbackNo() == null || request.getOutRollbackNo().isBlank()) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "outRollbackNo 不能为空");
        }
        if (request.getRollbackAmountCent() <= 0) {
            throw new JointPayException(ErrorCode.INVALID_ARGUMENT, "rollbackAmountCent 必须大于 0");
        }
    }
}
