package org.jzb.test.reactor;

import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.FlowableHelper;
import io.vertx.serviceproxy.ServiceBinder;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jzb.test.reactor.proxy.RestProxyService;
import org.jzb.test.reactor.proxy.impl.RestProxyServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-06-08
 */
public class BackendVerticle extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        final ServiceBinder serviceBinder = new ServiceBinder(vertx);
        final MessageConsumer<JsonObject> consumer = serviceBinder.setAddress(RestProxyService.ADDRESS)
                .register(RestProxyService.class, new RestProxyServiceImpl());


        vertx.eventBus().<JsonArray>consumer("mes-auto:report:statisticReport:combines", reply -> {
            final byte[] bytes = test(reply.body());
            final DeliveryOptions deliveryOptions = new DeliveryOptions()
                    .addHeader("access-control-expose-headers", "content-disposition")
                    .addHeader("content-disposition", "attachment;filename=combines.xlsx");
            reply.reply(bytes, deliveryOptions);
        });

        final MessageConsumer<String> dyeing = vertx.eventBus().consumer("dyeing");
        FlowableHelper.toFlowable(dyeing).concatMapSingle(reply -> {
            final String body = reply.body();
            final int delay = new Random().nextInt(5);
            return Single.just(body)
                    .delay(delay, TimeUnit.SECONDS)
                    .doOnSuccess(it -> System.out.println(it + " handled"));
        }).subscribe();

//        vertx.eventBus().<String>consumer("dyeing", reply -> {
//            final String body = reply.body();
//            final int delay = new Random().nextInt(5);
//
////            vertx.runOnContext(ev -> {
////                try {
////                    TimeUnit.SECONDS.sleep(delay);
////                    System.out.println(body + " handled");
////                    reply.reply("");
////                } catch (Exception e) {
////                    reply.fail(400, e.getLocalizedMessage());
////                }
////            });
//
//            vertx.runOnContext(event -> Single.just(body)
//                    .delay(delay, TimeUnit.SECONDS)
//                    .doOnSuccess(it -> System.out.println(it + " handled"))
//                    .subscribe());
//
////            Single.just(body)
////                    .delay(delay, TimeUnit.SECONDS)
////                    .doOnSuccess(it -> System.out.println(it + " handled"))
////                    .subscribe(it -> reply.reply(null), err -> reply.fail(400, err.getLocalizedMessage()));
//        });
    }

    @SneakyThrows
    private byte[] test(JsonArray jsonArray) {
        for (Object o : jsonArray) {
            final byte[] buf;
            if (o instanceof byte[]) {
                buf = (byte[]) o;
            } else {
                final String s = (String) o;
                buf = Base64.getDecoder().decode(s);
            }
            @Cleanup final ByteArrayInputStream bais = new ByteArrayInputStream(buf);
            @Cleanup final Workbook wb = WorkbookFactory.create(bais);
            final Sheet sheet = wb.getSheetAt(0);
            System.out.println(sheet);
        }
        @Cleanup final Workbook wb = WorkbookFactory.create(new File("/home/jzb/test.xlsx"));
        @Cleanup final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        return baos.toByteArray();
    }

}
