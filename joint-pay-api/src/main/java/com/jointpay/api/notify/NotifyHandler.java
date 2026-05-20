package com.jointpay.api.notify;

/**
 * 异步回调 SPI：验签并解析为统一载荷。
 */
public interface NotifyHandler {

    NotifyParseResult parse(NotifyRawRequest request);
}
