package com.jointpay.common.crypto;

import java.util.Map;

/**
 * 渠道报文签名与验签 SPI。
 */
public interface ChannelSigner {

    String sign(Map<String, String> params);

    boolean verify(Map<String, String> params, String signature);
}
