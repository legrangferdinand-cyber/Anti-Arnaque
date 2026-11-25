package com.antiarnaque.app.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Petits utilitaires pour sortir les accès SQLite/réseau du thread UI. */
public final class AppExecutors {

    private static final ExecutorService DISK_IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    public static void run(Runnable task) {
        DISK_IO.execute(task);
    }

    public static void onMain(Runnable task) {
        MAIN.post(task);
    }
}
