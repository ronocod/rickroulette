package com.ronocod.rickroulette.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class RickSyncService extends Service {
    private static RickSyncAdapter SYNC_ADAPTER = null;

    @Override
    public void onCreate() {
        synchronized (RickSyncService.class) {
            if (SYNC_ADAPTER == null) {
                SYNC_ADAPTER = new RickSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return SYNC_ADAPTER.getSyncAdapterBinder();
    }
}