package com.jointpay.allinpay;

import com.jointpay.api.config.ChannelConfig;
import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.profitsharing.ProfitSharingBindRequest;
import com.jointpay.api.profitsharing.ProfitSharingCancelRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryRequest;
import com.jointpay.api.profitsharing.ProfitSharingQueryResult;
import com.jointpay.api.profitsharing.ProfitSharingRequest;
import com.jointpay.api.profitsharing.ProfitSharingResult;
import com.jointpay.api.profitsharing.ProfitSharingRollbackRequest;
import com.jointpay.common.profitsharing.AbstractChannelProfitSharingService;

/**
 * 通联支付分账（路径与字段以商户文档为准）。
 */
public final class AllinpayProfitSharingService extends AbstractChannelProfitSharingService {

    static final String DEFAULT_SUBMIT_PATH = "/api/share";
    static final String DEFAULT_QUERY_PATH = "/api/sharequery";
    static final String DEFAULT_CANCEL_PATH = "/api/sharecancel";
    static final String DEFAULT_ROLLBACK_PATH = "/api/sharerollback";

    public AllinpayProfitSharingService(ChannelConfig config) {
        super("通联支付");
    }

    @Override
    protected void doBindOnOrder(ProfitSharingBindRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "通联支付下单绑分账待接入");
    }

    @Override
    protected ProfitSharingResult doSubmit(ProfitSharingRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "通联支付分账提交待接入");
    }

    @Override
    protected ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "通联支付分账查询待接入");
    }

    @Override
    protected ProfitSharingResult doCancel(ProfitSharingCancelRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "通联支付分账撤销待接入");
    }

    @Override
    protected ProfitSharingResult doRollback(ProfitSharingRollbackRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "通联支付分账回退待接入");
    }
}
