package me.bebeli555.automapart.events;

/**
 * Cancellable class for event bus
 */
public class Cancellable {
    private boolean cancelled;

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }
}
