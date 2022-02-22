package uni.imengyu.gyro.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.util.Log;

import uni.imengyu.gyro.representation.Quaternion;

public class CustomSensorProvider extends OrientationProvider {

    private boolean isDeviceSupport = false;
    private final int sensorType;

    public CustomSensorProvider(SensorManager sensorManager, int sensorType) {
        super(sensorManager);
        this.sensorType = sensorType;

        Sensor gyroSensor = sensorManager.getDefaultSensor(sensorType);

        isDeviceSupport = gyroSensor != null;
        sensorList.add(gyroSensor);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == sensorType) {
            if(onSensorChangedListener != null)
                onSensorChangedListener.onSensorChanged(event.values);
        }
    }

    public boolean isDeviceSupport() {
        return isDeviceSupport;
    }
}
