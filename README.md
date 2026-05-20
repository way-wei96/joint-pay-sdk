# Joint-Pay-SDK
## 联合企业支付统一 Java 开发 SDK

### 项目介绍
Joint 释义为联合共建，本项目联合整合 **汇聚支付、汇付天下、通联支付** 三大国内主流B端持牌聚合支付渠道。

目前开源圈内绝大多数支付SDK均聚焦微信、支付宝等C端大众支付，
针对企业商用场景常用的汇聚、汇付、通联，缺少完整、规范、可直接投产的成套开发组件，本项目精准填补这一市场空白。

项目采用标准化架构设计，统一封装请求签名、报文组装、订单处理、异步回调等通用底层逻辑，
同时主打**统一多级分账领域模型**这一核心特色能力。
开发者引入依赖即可快速开发，既可单独使用任意一家支付渠道，也可无缝切换、多渠道并存，大幅减少重复开发工作量。

### 适配支付渠道
✅ 汇聚支付
✅ 汇付天下
✅ 通联支付

### 适用场景
1. 企业后端开发，需要对接上述任意一款或多款企业级聚合支付
2. 平台类业务存在商户分润、多级分账、下单实时分账、分账回退等需求
3. 不想重复手写支付接口、签名算法、回调解析等基础通用代码
4. 以此项目代码架构为模板，搭建团队内部统一支付业务脚手架

### 项目能力划分
#### 一、基础通用支付能力
- 渠道商户信息配置、密钥鉴权与签名工具封装
- 统一预下单、创建支付订单
- 订单状态主动查询
- 统一发起退款、查询退款结果
- 支付异步回调统一解析与处理
- 全局统一自定义异常、错误码体系

#### 二、项目核心特色亮点
- 归一化设计分账全套领域实体与顶层接口
- 统一封装分账参与方、分账规则、分账方案
- 支持下单绑定实时分账业务
- 完整实现分账查询、分账撤销、分账回退能力
- 抹平三家渠道接口字段差异，上层业务代码无需改动即可切换渠道

### 工程目录说明

```
joint-pay-sdk/
├── joint-pay-api/       # 对外契约：领域模型、SPI 接口
├── joint-pay-common/    # HTTP、签名等跨渠道基础设施
├── joint-pay-core/      # PayClient 门面、工厂、占位 SPI
├── joint-pay-joinpay/   # 汇聚支付（骨架已接入）
├── joint-pay-huifu/     # 汇付天下（骨架已接入）
├── joint-pay-allinpay/  # 通联支付（骨架已接入）
├── joint-pay-all/       # 聚合依赖，业务方一键引入三家渠道
└── joint-pay-example/ # 使用示例（见 QuickStart）
```

**当前进度**：
- **P1 支付基础**：预下单、下单、查单、退款、回调已三家并行接入（汇聚较完整）
- **P2 分账**：三家均已挂载 submit/query/cancel/rollback（汇聚 RSA2 OpenAPI 较完整；字段以各渠道文档联调为准）

### 快速开始

```xml
<!-- 仅用汇聚时，可只引 joint-pay-core + joint-pay-joinpay -->
<dependency>
    <groupId>com.jointpay</groupId>
    <artifactId>joint-pay-all</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```java
ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
        .merchantId("your-merchant-id")
        .build();
PayClient client = PayClientFactory.create(config);
```

**汇聚预下单示例**（`extras.frpCode` 必填，如 `ALIPAY_H5`）：

```java
ChannelConfig config = ChannelConfig.builder(PayChannel.JOINPAY)
        .merchantId("商户号")
        .apiSecret("MD5密钥")
        .appId("报备商户号")
        .build();
PrepayRequest request = PrepayRequest.builder()
        .outTradeNo("ORDER001")
        .amountCent(100L)
        .subject("测试商品")
        .notifyUrl("https://your.domain/notify")
        .extras(Map.of("frpCode", "ALIPAY_H5"))
        .build();
PrepayResult result = PayClientFactory.create(config).payment().prepay(request);
```

**汇聚退款示例**（`extras.notifyUrl` 必填）：

```java
RefundRequest refund = RefundRequest.builder()
        .outTradeNo("ORDER001")
        .outRefundNo("REFUND001")
        .refundAmountCent(100L)
        .extras(Map.of("notifyUrl", "https://your.domain/refund-notify"))
        .build();
RefundResult refundResult = PayClientFactory.create(config).refund().refund(refund);
```

**分账示例**（契约已就绪，渠道实现接入前调用将返回 `CHANNEL_UNSUPPORTED`）：

```java
ProfitSharingScheme scheme = new ProfitSharingScheme(
        "SCHEME001",
        List.of(ProfitSharingParticipant.builder()
                .participantId("P1")
                .merchantId("子商户号")
                .amountCent(30L)
                .build()),
        Map.of());
ProfitSharingResult ps = client.profitSharing().submit(
        ProfitSharingRequest.builder()
                .outTradeNo("ORDER001")
                .outSharingNo("SHARE001")
                .scheme(scheme)
                .build());
```

**汇聚 OpenAPI 分账**需额外配置：`appId`、`privateKey`（RSA2）、`extras.openApiGateway`（默认 `https://api.huilianlink.com`，与交易网关 `www.joinpay.com` 不同）。

**下单绑分账**：先 `profitSharing().bindOnOrder(...)`，再 `payment().prepay(...)`，方案会在预下单时自动透传（`InMemoryProfitSharingBindStore`，单机有效）。