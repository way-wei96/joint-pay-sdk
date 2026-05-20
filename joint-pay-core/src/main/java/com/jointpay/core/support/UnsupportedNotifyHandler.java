package com.jointpay.core.support;

import com.jointpay.api.exception.ErrorCode;
import com.jointpay.api.exception.JointPayException;
import com.jointpay.api.notify.NotifyHandler;
import com.jointpay.api.notify.NotifyParseResult;
import com.jointpay.api.notify.NotifyRawRequest;

public final class UnsupportedNotifyHandler implements NotifyHandler {

    private final String channelName;

    public UnsupportedNotifyHandler(String channelName) {
        this.channelName = channelName;
    }

    @Override
    public NotifyParseResult parse(NotifyRawRequest request) {
        throw new JointPayException(
                ErrorCode.CHANNEL_UNSUPPORTED,
                channelName + " 异步回调解析尚未实现");
    }
}
