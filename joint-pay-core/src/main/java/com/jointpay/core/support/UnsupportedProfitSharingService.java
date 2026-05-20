package com.jointpay.core.support;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.profitsharing.ProfitSharingBindRequest;
import com.jointpay.api.profitsharing.ProfitSharingCancelRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryResult;
import com.jointpay.api.profitsharing.ProfitSharingRequest;
import com.jointpay.api.profitsharing.ProfitSharingResult;
import com.jointpay.api.profitsharing.ProfitSharingRollbackRequest;
import com.jointpay.api.profitsharing.ProfitSharingService;

public final class UnsupportedProfitSharingService implements ProfitSharingService {

    private final String channelName;

    public UnsupportedProfitSharingService(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public void bindOnOrder(ProfitSharingBindRequest request) {
        throw unsupported("下单绑定分账");
    }

    @Override
    public ProfitSharingResult submit(ProfitSharingRequest request) {
        throw unsupported("发起分账");
    }

    @Override
    public ProfitSharingQueryResult query(ProfitSharingQueryRequest request) {
        throw unsupported("分账查询");
    }

    @Override
    public ProfitSharingResult cancel(ProfitSharingCancelRequest request) {
        throw unsupported("分账撤销");
    }

    @Override
    public ProfitSharingResult rollback(ProfitSharingRollbackRequest request) {
        throw unsupported("分账回退");
    }

    private JointPayException unsupported(String action) {
        return new JointPayException(
                ErrorCode.CHANNEL_UNSUPPORTED,
                channelName + " " + action + " 尚未实现");
    }
}
