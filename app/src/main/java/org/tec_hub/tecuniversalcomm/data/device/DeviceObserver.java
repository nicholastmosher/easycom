package org.tec_hub.tecuniversalcomm.data.device;

/**
 * Created by Nick Mosher on 9/25/15.
 */
public interface DeviceObserver {

    void onUpdate(Device observable, Device.Cues cue);
}
