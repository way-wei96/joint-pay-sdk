package com.jointpay.api.exception;

/**
 * SDK 统一运行时异常。
 */
public class JointPayException extends RuntimeException {

    private final ErrorCode errorCode;
    private final String channelCode;
    private final String channelMessage;

    public JointPayException(ErrorCode errorCode, String detail) {
        this(errorCode, detail, null, null, null);
    }

    public JointPayException(
            ErrorCode errorCode,
            String detail,
            String channelCode,
            String channelMessage,
            Throwable cause) {
        super(buildMessage(errorCode, detail, channelCode, channelMessage), cause);
        this.errorCode = errorCode;
        this.channelCode = channelCode;
        this.channelMessage = channelMessage;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public String getChannelMessage() {
        return channelMessage;
    }

    private static String buildMessage(
            ErrorCode errorCode,
            String detail,
            String channelCode,
            String channelMessage) {
        StringBuilder sb = new StringBuilder('[')
                .append(errorCode.getCode())
                .append("] ")
                .append(errorCode.getMessage());
        if (detail != null && !detail.isBlank()) {
            sb.append(": ").append(detail);
        }
        if (channelCode != null && !channelCode.isBlank()) {
            sb.append(" (channel=").append(channelCode);
            if (channelMessage != null && !channelMessage.isBlank()) {
                sb.append(", msg=").append(channelMessage);
            }
            sb.append(')');
        }
        return sb.toString();
    }
}
