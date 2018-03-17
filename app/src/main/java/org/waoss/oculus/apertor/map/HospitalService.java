package org.waoss.oculus.apertor.map;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.widget.Toast;

public class HospitalService extends Service {

    private Looper looper;
    private ServiceHandler serviceHandler;


    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();


        looper = thread.getLooper();
        serviceHandler = new ServiceHandler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();


        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);


        return START_STICKY;
    }

    public final class ServiceHandler extends Handler {
        public ServiceHandler(final Looper looper) {
            super(looper);

        }

        @Override
        public void handleMessage(final Message msg) {

        }
    }
}
