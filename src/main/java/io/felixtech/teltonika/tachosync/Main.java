package io.felixtech.teltonika.tachosync;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class Main {

    public static void main(String[] args) {
        int frequency = Integer.parseInt(Settings.getInstance().get("DOWNLOAD_FREQUENCY_MINUTES", "15"));
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleWithFixedDelay(new Downloader(), 0, frequency, TimeUnit.MINUTES);
    }
}
