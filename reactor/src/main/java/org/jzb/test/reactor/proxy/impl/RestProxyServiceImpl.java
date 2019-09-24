package org.jzb.test.reactor.proxy.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jzb.test.reactor.proxy.RestProxyService;

/**
 * @author jzb 2019-09-02
 */
public class RestProxyServiceImpl implements RestProxyService {
    @Override
    public void save(String path, JsonObject params, Handler<AsyncResult<String>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(RestProxyServiceImpl.class.getName()));
    }
}
