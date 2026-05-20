# Changelog

本文件记录本项目的重要变更，格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)。

## [0.1.0] - 2026-05-20

### Added

- 多模块 Maven 工程：`joint-pay-api`、`joint-pay-common`、`joint-pay-core` 及汇聚 / 汇付 / 通联渠道模块、`joint-pay-all` 聚合包
- 统一支付能力：预下单、下单、查单、退款、退款查询
- 统一分账能力：提交、查询、撤销、回退、下单绑分账（`ProfitSharingBindStore` / `ProfitSharingBindStores`）
- 统一回调：`NotifyType`（支付 / 退款 / 分账）、`NotifySupport` 便捷解析
- **汇聚**：交易 MD5（`www.joinpay.com`）、OpenAPI RSA2 分账（`api.huilianlink.com`）
- **汇付斗拱**：标准请求信封（`sys_id` / `data` / RSA2 签名）、网关与业务双层响应、`resp_data` 回调验签
- **通联**：MD5 签名、可配置网关与 API 路径
- `ChannelExtras` 常量、`HttpTransports` 可替换 HTTP 实现
- 示例：`QuickStart`、`HuifuQuickStart`、`NotifyHandlerDemo`、`MockHttpDemo` 等
- GitHub Actions CI（`mvn verify`，JDK 21）

### Notes

- 各渠道字段与路径以官方文档为准，**上线前须在沙箱完成联调**
- 汇付配置：`merchantId`=huifu_id，`apiKey`=sys_id，`appId`=product_id，需 `privateKey` / `publicKey`
- 文档：[汇付斗拱 API](https://paas.huifu.com/open/doc/api/) · [通联收银宝](https://prodoc.allinpay.com/project/12/)

[0.1.0]: https://github.com/way-wei96/joint-pay-sdk/releases/tag/v0.1.0
