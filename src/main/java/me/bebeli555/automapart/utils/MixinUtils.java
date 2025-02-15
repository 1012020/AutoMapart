package me.bebeli555.automapart.utils;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.events.Cancellable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class MixinUtils {
    /**
     * Posts the given event to the BUS and also cancels it if the event is cancelled
     */
    public static void basicEvent(Object event, CallbackInfo info) {
        Mod.EVENT_BUS.post(event);

        if (event instanceof Cancellable cancellable) {
            if (cancellable.isCancelled()) {
                info.cancel();
            }
        }
    }
}
