package com.jointpay.huifu;

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
 * 汇付天下斗拱分账（路径与字段以商户文档为准）。
 */
public final class HuifuProfitSharingService extends AbstractChannelProfitSharingService {

    static final String DEFAULT_SUBMIT_PATH = "/v2/trade/acctpayment/pay";
    static final String DEFAULT_QUERY_PATH = "/v2/trade/acctpayment/query";
    static final String DEFAULT_CANCEL_PATH = "/v2/trade/acctpayment/refund";
    static final String DEFAULT_ROLLBACK_PATH = "/v2/trade/acctpayment/rollback";

    public HuifuProfitSharingService(ChannelConfig config) {
        super("汇付天下");
    }

    @Override
    protected void doBindOnOrder(ProfitSharingBindRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇付天下下单绑分账待接入");
    }

    @Override
    protected ProfitSharingResult doSubmit(ProfitSharingRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇付天下分账提交待接入");
    }

    @Override
    protected ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇付天下分账查询待接入");
    }

    @Override
    protected ProfitSharingResult doCancel(ProfitSharingCancelRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇付天下分账撤销待接入");
    }

    @Override
    protected ProfitSharingResult doRollback(ProfitSharingRollbackRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇付天下分账回退待接入");
    }
}
