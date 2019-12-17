package com.mr47.screenshot_ocr.proxy;

import com.mr47.screenshot_ocr.controller.Context;
import io.vertx.ext.web.client.WebClient;

public interface Service {
    static <R extends Service> R proxy(Context context, Class<R> serviceClass) throws ReflectiveOperationException {
        Object object = Class.forName(serviceClass.getName() + "Impl")
                .getConstructor(WebClient.class)
                .newInstance(context.webClient());
        return serviceClass.cast(object);
    }
}
