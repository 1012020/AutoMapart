package me.bebeli555.automapart.utils.objects;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import me.bebeli555.automapart.Mod;

public class CustomEventBus {
    public EventBus bus;

    public CustomEventBus() {
        bus = new EventBus(new CustomEventBusExceptionHandler());
    }

    public void post(Object event) {
        bus.post(event);
    }

    public void register(Object object) {
        bus.register(object);
    }

    public void unregister(Object object) {
        try {
            bus.unregister(object);
        } catch (IllegalArgumentException ignored) {}
    }

    public static class CustomEventBusExceptionHandler implements SubscriberExceptionHandler {
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            System.err.println("Sierra subscriber exception! Subscriber: " + context.getSubscriber() + " Method: " + context.getSubscriberMethod() + " Version: " + Mod.VERSION);
            exception.printStackTrace();
        }
    }
}
