package com.jointpay.joinpay;

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
 * 汇聚支付分账（账户分账 OpenAPI，路径以官方文档为准，可通过 extras 覆盖）。
 */
public final class JoinPayProfitSharingService extends AbstractChannelProfitSharingService {

    /** 分账提交（示例路径，以商户文档为准）。 */
    static final String DEFAULT_SUBMIT_PATH = "/openapi/acct/order/share/submit";
    static final String DEFAULT_QUERY_PATH = "/openapi/acct/order/share/query";
    static final String DEFAULT_CANCEL_PATH = "/openapi/acct/order/share/cancel";
    static final String DEFAULT_ROLLBACK_PATH = "/openapi/acct/order/share/rollback";

    public JoinPayProfitSharingService(ChannelConfig config) {
        super("汇聚支付");
        JoinPayPaymentService.withDefaultGateway(config);
    }

    @Override
    protected void doBindOnOrder(ProfitSharingBindRequest request) {
        throw new JointPayException(
                ErrorCode.CHANNEL_UNSUPPORTED,
                "汇聚支付下单绑分账请通过预下单 extras 传入，或支付成功后调用 submit");
    }

    @Override
    protected ProfitSharingResult doSubmit(ProfitSharingRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇聚支付分账提交待按 OpenAPI 文档接入");
    }

    @Override
    protected ProfitSharingQueryResult doQuery(ProfitSharingQueryRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇聚支付分账查询待接入");
    }

    @Override
    protected ProfitSharingResult doCancel(ProfitSharingCancelRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇聚支付分账撤销待接入");
    }

    @Override
    protected ProfitSharingResult doRollback(ProfitSharingRollbackRequest request) {
        throw new JointPayException(ErrorCode.CHANNEL_UNSUPPORTED, "汇聚支付分账回退待接入");
    }
}
