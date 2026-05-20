package com.jointpay.api.exception;

/**
 * 统一错误码（支付基础能力阶段；分账相关码后续按需扩展）。
 */
public enum ErrorCode {

    INVALID_ARGUMENT("JP400", "参数不合法"),
    CHANNEL_UNSUPPORTED("JP501", "渠道不支持该操作"),
    CHANNEL_ERROR("JP502", "渠道返回错误"),
    SIGN_VERIFY_FAILED("JP503", "签名校验失败"),
    NETWORK_ERROR("JP504", "网络请求失败"),
    INTERNAL_ERROR("JP500", "内部错误");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
