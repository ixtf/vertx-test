package org.jzb.test.reactor.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author jzb 2019-09-02
 */
@ProxyGen // Generate the proxy and handler
@VertxGen // Generate clients in non-java languages
public interface ProcessorService {
    void process(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler);
}
