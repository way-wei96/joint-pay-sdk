package com.jointpay.api.config;

import com.jointpay.api.PayChannel;

import java.util.Map;

/**
 * 渠道商户配置（各渠道专有字段通过 {@link #extras} 扩展）。
 */
public final class ChannelConfig {

    private final PayChannel channel;
    private final ChannelEnvironment environment;
    private final String merchantId;
    private final String appId;
    private final String apiKey;
    private final String apiSecret;
    private final String privateKey;
    private final String publicKey;
    private final String gatewayUrl;
    private final Map<String, String> extras;

    private ChannelConfig(Builder builder) {
        this.channel = builder.channel;
        this.environment = builder.environment == null ? ChannelEnvironment.PRODUCTION : builder.environment;
        this.merchantId = builder.merchantId;
        this.appId = builder.appId;
        this.apiKey = builder.apiKey;
        this.apiSecret = builder.apiSecret;
        this.privateKey = builder.privateKey;
        this.publicKey = builder.publicKey;
        this.gatewayUrl = builder.gatewayUrl;
        this.extras = builder.extras == null ? Map.of() : Map.copyOf(builder.extras);
    }

    public PayChannel getChannel() {
        return channel;
    }

    public ChannelEnvironment getEnvironment() {
        return environment;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getAppId() {
        return appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getApiSecret() {
        return apiSecret;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getGatewayUrl() {
        return gatewayUrl;
    }

    public Map<String, String> getExtras() {
        return extras;
    }

    public static Builder builder(PayChannel channel) {
        return new Builder(channel);
    }

    /** 基于已有配置复制，便于仅覆盖网关等少量字段。 */
    public static Builder builder(ChannelConfig source) {
        return new Builder(source.channel)
                .environment(source.environment)
                .merchantId(source.merchantId)
                .appId(source.appId)
                .apiKey(source.apiKey)
                .apiSecret(source.apiSecret)
                .privateKey(source.privateKey)
                .publicKey(source.publicKey)
                .gatewayUrl(source.gatewayUrl)
                .extras(source.extras);
    }

    public static final class Builder {
        private final PayChannel channel;
        private ChannelEnvironment environment;
        private String merchantId;
        private String appId;
        private String apiKey;
        private String apiSecret;
        private String privateKey;
        private String publicKey;
        private String gatewayUrl;
        private Map<String, String> extras;

        private Builder(PayChannel channel) {
            this.channel = channel;
        }

        public Builder environment(ChannelEnvironment environment) {
            this.environment = environment;
            return this;
        }

        public Builder merchantId(String merchantId) {
            this.merchantId = merchantId;
            return this;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder apiSecret(String apiSecret) {
            this.apiSecret = apiSecret;
            return this;
        }

        public Builder privateKey(String privateKey) {
            this.privateKey = privateKey;
            return this;
        }

        public Builder publicKey(String publicKey) {
            this.publicKey = publicKey;
            return this;
        }

        public Builder gatewayUrl(String gatewayUrl) {
            this.gatewayUrl = gatewayUrl;
            return this;
        }

        public Builder extras(Map<String, String> extras) {
            this.extras = extras;
            return this;
        }

        public ChannelConfig build() {
            if (channel == null) {
                throw new IllegalStateException("channel is required");
            }
            if (merchantId == null || merchantId.isBlank()) {
                throw new IllegalStateException("merchantId is required");
            }
            return new ChannelConfig(this);
        }
    }
}
