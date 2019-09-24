package org.jzb.test.reactor.proxy;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author jzb 2019-09-02
 */
@ProxyGen
@VertxGen
public interface RestProxyService {
    String ADDRESS = RestProxyService.class.getName();

    void save(String path, JsonObject params, Handler<AsyncResult<String>> resultHandler);
}
