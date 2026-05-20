package com.jointpay.common.http;

/**
 * HTTP 传输抽象，各渠道实现可注入自定义实例（超时、代理等）。
 */
public interface HttpTransport {

    HttpResponse execute(HttpRequest request);
}
