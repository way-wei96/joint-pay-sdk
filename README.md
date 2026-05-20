# Joint-Pay-SDK

![CI](https://github.com/way-wei96/joint-pay-sdk/actions/workflows/ci.yml/badge.svg)
[![Release](https://img.shields.io/github/v/release/way-wei96/joint-pay-sdk)](https://github.com/way-wei96/joint-pay-sdk/releases)

## 联合企业支付统一 Java 开发 SDK

### 项目介绍

Joint 释义为联合共建，本项目整合 **汇聚支付、汇付天下、通联支付** 三家国内 B 端聚合支付渠道，提供统一的 Java API 与领域模型。

- 统一接口切换渠道时**仅需修改配置**（`ChannelConfig` + 对应 Maven 模块）
- 各渠道报文、签名、网关以**官方文档为准**，上线前**必须在沙箱完成联调**
- 变更记录见 [CHANGELOG.md](CHANGELOG.md)

### 适配支付渠道

| 渠道 | 文档入口 |
|------|----------|
| 汇聚支付 | [joinpay.com](https://www.joinpay.com) / OpenAPI 分账网关 |
| 汇付天下斗拱 | [paas.huifu.com/open/doc/api/](https://paas.huifu.com/open/doc/api/) |
| 通联收银宝 | [prodoc.allinpay.com/project/12/](https://prodoc.allinpay.com/project/12/) |

### 适用场景

1. 企业后端对接上述一款或多款聚合支付
2. 平台类业务：多级分账、下单绑分账、分账回退
3. 避免重复实现签名、HTTP、回调解析等通用逻辑

### 能力一览

| 能力 | 说明 |
|------|------|
| 支付 | 预下单、创建订单、查单 |
| 退款 | 发起退款、查询退款 |
| 回调 | 支付 / 退款 / 分账异步通知解析与验签（因渠道而异） |
| 分账 | 提交、查询、撤销、回退；下单前 `bindOnOrder` |

**当前未统一封装**：关单、对账下载、商户进件等（可按需在渠道 `extras` 透传或自行扩展）。

### 工程目录

```
joint-pay-sdk/
├── joint-pay-api/        # 契约与领域模型
├── joint-pay-common/     # HTTP、签名、模板实现
├── joint-pay-core/       # PayClientFactory、NotifySupport
├── joint-pay-joinpay/    # 汇聚
├── joint-pay-huifu/      # 汇付斗拱
├── joint-pay-allinpay/   # 通联
├── joint-pay-all/        # 三家聚合依赖（推荐）
└── joint-pay-example/    # 使用示例
```

### 快速开始

**要求**：JDK 21+

```xml
<dependency>
    <groupId>com.jointpay</groupId>
    <artifactId>joint-pay-all</artifactId>
    <version>0.1.0</version>
</dependency>
```

仅接单渠道时引入 `joint-pay-core` + 对应 `joint-pay-joinpay` / `joint-pay-huifu` / `joint-pay-allinpay` 即可。

```java
ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
        .merchantId("your-merchant-id")
        .build();
PayClient client = PayClientFactory.create(config);
```

### 渠道配置速查

| 渠道 | `merchantId` | 常用配置 | `extras` 常用键 |
|------|--------------|----------|-----------------|
| 汇聚 | 商户号 | `apiSecret`（MD5）、`appId`（报备商户号） | `frpCode`（必填）、`openApiGateway` |
| 汇聚分账 OpenAPI | 同上 | `privateKey`、`publicKey` | 分账网关默认 `https://api.huilianlink.com` |
| 汇付 | `huifu_id` | `apiKey`=`sys_id`，`appId`=`product_id`，`privateKey`，`publicKey`；网关默认 `https://api.huifu.com` | `tradeType` / `payType`（如 `T_MINIAPP`）、`apiPath` |
| 通联 | `cusid` | `apiSecret`、`appId`（`proid`）；网关默认 `https://vsp.allinpay.com` | `paytype`（必填）、`apiPath` |

`extras` 键名见 `com.jointpay.api.config.ChannelExtras`。

### 代码示例

**汇聚预下单**（`frpCode` 必填）：

```java
PrepayResult result = PayClientFactory.create(
        ChannelConfig.builder(PayChannel.JOINPAY)
                .merchantId("商户号")
                .apiSecret("MD5密钥")
                .appId("报备商户号")
                .build())
        .payment()
        .prepay(PrepayRequest.builder()
                .outTradeNo("ORDER001")
                .amountCent(100L)
                .subject("测试商品")
                .notifyUrl("https://your.domain/notify")
                .extras(Map.of(ChannelExtras.JoinPay.FRP_CODE, "ALIPAY_H5"))
                .build());
```

**汇付预下单**（斗拱）：

```java
ChannelConfig.builder(PayChannel.HUIFU)
        .merchantId("YOUR_HUIFU_ID")
        .apiKey("YOUR_SYS_ID")
        .appId("YOUR_PRODUCT_ID")
        .privateKey("YOUR_RSA_PRIVATE_KEY")
        .publicKey("PLATFORM_RSA_PUBLIC_KEY")
        .build();
// extras: ChannelExtras.Huifu.TRADE_TYPE -> T_MINIAPP 等
```

**回调**：

```java
NotifyParseResult result = NotifySupport.parse(client, NotifyRawRequest.builder()
        .body(requestBody)
        .params(queryParams)
        .build());
return NotifySupport.ackBody(result);
```

**下单绑分账**：先 `profitSharing().bindOnOrder(...)`，再 `payment().prepay(...)`。集群环境请实现 `ProfitSharingBindStore` 并 `ProfitSharingBindStores.use(...)`。

更多示例见 `joint-pay-example` 模块。

### 开发与发布

```bash
mvn verify                  # 编译 + 测试
mvn -Prelease package       # 附带 sources / javadoc jar
```

`PayClientFactory.supportedChannels()` 可查看当前 classpath 已加载的渠道。

### 许可证

[MIT](LICENSE)
