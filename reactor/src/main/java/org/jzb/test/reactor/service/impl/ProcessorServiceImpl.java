package org.jzb.test.reactor.service.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceException;
import org.jzb.test.reactor.service.ProcessorService;

/**
 * @author jzb 2019-09-02
 */
public class ProcessorServiceImpl implements ProcessorService {
    @Override
    public void process(JsonObject document, Handler<AsyncResult<JsonObject>> resultHandler) {
        JsonObject result = document.copy();
        if (!document.containsKey("name")) {
            resultHandler.handle(ServiceException.fail(400, "No name in the document"));
        } else if (document.getString("name").isEmpty() || document.getString("name").equalsIgnoreCase("bad")) {
            resultHandler.handle(ServiceException.fail(400, "Bad name in the document: " +
                    document.getString("name"), new JsonObject().put("name", document.getString("name"))));
        } else {
            result.put("approved", true);
            resultHandler.handle(Future.succeededFuture(result));
        }
    }
}
