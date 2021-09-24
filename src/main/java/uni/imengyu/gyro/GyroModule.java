package uni.imengyu.gyro;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.utils.UniLogUtils;
import uni.imengyu.gyro.sensor.ImprovedOrientationSensor1Provider;

public class GyroModule extends WXModule {
    String TAG = "GyroModule";

    private SensorManager sensorManager;
    private ImprovedOrientationSensor1Provider sensor1Provider;

    @UniJSMethod(uiThread = true)
    public void startGyro(JSONObject options) {
        UniLogUtils.i("startGyro!");

        if(sensorManager == null) {
            sensorManager = (SensorManager) mWXSDKInstance.getContext().getSystemService(Context.SENSOR_SERVICE);
            sensor1Provider = new ImprovedOrientationSensor1Provider(sensorManager);
            sensor1Provider.start();
        }

    }
    @UniJSMethod(uiThread = true)
    public void stopGyro() {
        UniLogUtils.i("stopGyro!");

        if(sensor1Provider != null) {
            sensor1Provider.stop();
            sensor1Provider = null;
        }
    }
    @UniJSMethod(uiThread = true)
    public void getGyroValue(JSCallback callback) {
        JSONObject data = new JSONObject();
        if(sensor1Provider != null) {
            float[] xyz = new float[3];
            sensor1Provider.getEulerAngles(xyz);
            data.put("x", Math.toDegrees(xyz[0]));
            data.put("y", Math.toDegrees(xyz[1]));
            data.put("z", Math.toDegrees(xyz[2]));
        }
        callback.invoke(data);
    }
}
