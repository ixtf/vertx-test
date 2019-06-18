package org.jzb.test.resteasy;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import io.vertx.reactivex.core.eventbus.Message;
import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotBlank;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.security.Principal;

/**
 * @author jzb 2019-06-04
 */
@Slf4j
@Path("/")
public class HelloWorldResource {
    @GET
    @Path("/test/{name:.*}")
    public Response doGet(@PathParam("name") String name) {
        if (name == null || name.isEmpty()) {
            name = "World";
        }
        log.info("doGet: " + Thread.currentThread());
        return Response.status(200).entity("Hello " + name).build();
    }

//    @GET
//    @Path("/auth")
//    public Response sync(@Context SecurityContext sc, @Context io.vertx.core.Vertx vertx, @PathParam("name") String name) {
//        final Principal userPrincipal = sc.getUserPrincipal();
//        if (name == null || name.isEmpty()) {
//            name = "World";
//        }
//        return Response.status(200).entity("auth" + name).build();
//    }

    @GET
    @Path("/sync/{name:.*}")
    public void sync(@Suspended final AsyncResponse asyncResponse,
                     @Context io.vertx.core.Context context,
                     @Context io.vertx.core.Vertx vertx,
                     @Context io.vertx.core.http.HttpServerRequest req,
                     @NotBlank @QueryParam("test") String test,
                     @PathParam("name") String name) {
        EventBus.newInstance(vertx.eventBus()).<String>rxSend("backend-sync", name)
                .map(Message::body)
                .doOnError(asyncResponse::resume)
                .subscribe(asyncResponse::resume);
    }

    @GET
    @Path("/async/{name:.*}")
    public void async(@Suspended final AsyncResponse asyncResponse,
                      @Context io.vertx.core.Context context,
                      @Context io.vertx.core.Vertx vertx,
                      @Context io.vertx.core.http.HttpServerRequest req,
                      @NotBlank @QueryParam("test") String test,
                      @PathParam("name") String name) {
        Vertx.newInstance(vertx).eventBus().<Principal>rxSend("backend-async", name)
                .map(Message::body)
                .map(Principal::getName)
                .doOnError(asyncResponse::resume)
                .subscribe(asyncResponse::resume);
    }
}
