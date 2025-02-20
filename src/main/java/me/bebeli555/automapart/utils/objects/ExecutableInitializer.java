package me.bebeli555.automapart.utils.objects;

public class ExecutableInitializer {
    public ExecutableInitializer(Runnable runnable) {
        runnable.run();
    }
}
