package ma.portal.controller.service;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

public class HostServiceConnection implements ServiceConnection {
    private HostService service;

    private final int resultCode;
    private final Intent data;
    private final HostService.ImageListener imageListener;

    public HostServiceConnection(int resultCode, Intent data, HostService.ImageListener imageListener) {
        this.resultCode = resultCode;
        this.data = data;
        this.imageListener = imageListener;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder binder) {
        service = ((HostService.HostServiceBinder) binder).getService();
        service.startRecording(resultCode, data, imageListener);
    }

    public void stopRecording() {
        if (service != null) {
            service.stopRecording();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        service = null;
    }

}
