package org.jzb.test.reactor;

import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

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
        vertx.eventBus().<JsonArray>consumer("mes-auto:report:statisticReport:combines", reply -> {
            final byte[] bytes = test(reply.body());
            final DeliveryOptions deliveryOptions = new DeliveryOptions().addHeader("Content-Disposition", "attachment;filename=combines.xlsx");
            reply.reply(bytes, deliveryOptions);
        });

        vertx.eventBus().<String>consumer("dyeing", reply -> {
            final String body = reply.body();
            final int delay = new Random().nextInt(5);
//            Mono.just(body).delayElement(Duration.ofSeconds(5)).subscribe(it -> System.out.println(body + " handled"));
            Single.just(body)
//                    .subscribeOn(Schedulers.single())
//                    .subscribeOn(Schedulers.single())
//                    .doOnSuccess(it -> System.out.println(it + " success"))
                    .delay(delay, TimeUnit.SECONDS)
                    .doOnSuccess(it -> System.out.println(it + " handled"))
                    .subscribe();
//                    .blockingGet();
        });
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
        @Cleanup final Workbook wb = WorkbookFactory.create(new File("/home/jzb/C车间7月/C.2019-07-31.xlsx"));
        @Cleanup final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        wb.write(baos);
        return baos.toByteArray();
    }

}
