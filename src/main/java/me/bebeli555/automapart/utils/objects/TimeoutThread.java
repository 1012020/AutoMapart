package me.bebeli555.automapart.utils.objects;

import me.bebeli555.automapart.Mod;
import me.bebeli555.automapart.utils.Utils;

public class TimeoutThread extends Utils {
    public Thread thread;
    public long lastError;
    public int minTimeout = 25;

    public TimeoutThread(Mod mod) {
        this.thread = new Thread(() -> {
            while(true) {
                if (mod != null) {
                    if (!mod.isOn() || mc.player == null) {
                        Mod.sleep(100);
                        continue;
                    }
                }

                int timeout = 0;
                try {
                    timeout = onRun();
                } catch (Exception | Error e) {
                    if (Math.abs(System.currentTimeMillis() - lastError) > 100) {
                        e.printStackTrace();
                        lastError = System.currentTimeMillis();
                    }
                }

                Mod.sleep(Math.max(minTimeout, timeout));
            }
        });
    }

    public TimeoutThread() {
        this(null);
    }

    public void start(int minTimeout) {
        this.minTimeout = minTimeout;
        this.start();
    }

    public void start() {
        this.thread.start();
    }

    public int onRun() {return 5;}
}
