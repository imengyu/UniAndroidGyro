package uni.imengyu.gyro;

import android.content.Context;
import android.hardware.SensorManager;

import androidx.annotation.Keep;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.utils.UniLogUtils;
import uni.imengyu.gyro.sensor.ImprovedOrientationSensor1Provider;

@Keep
public class GyroModule extends WXModule {
    String TAG = "GyroModule";

    private boolean lastPauseState = false;
    private ImprovedOrientationSensor1Provider sensor1Provider;

    @Override
    public void onActivityPause() {
        super.onActivityPause();
        if(sensor1Provider != null) {
            sensor1Provider.stop();
            lastPauseState = true;
        }
    }
    @Override
    public void onActivityResume() {
        super.onActivityResume();
        if(sensor1Provider != null && lastPauseState) {
            sensor1Provider.start();
            lastPauseState = false;
        }
    }

    private void checkAndInit() {
        if(sensor1Provider == null) {
            SensorManager sensorManager = (SensorManager) mWXSDKInstance.getContext().getSystemService(Context.SENSOR_SERVICE);
            sensor1Provider = new ImprovedOrientationSensor1Provider(sensorManager);
        }
    }

    /**
     * 返回当前设备是否支持陀螺仪
     * @return 是否支持
     */
    @UniJSMethod
    @Keep
    public boolean isGyroAvailable() {
        checkAndInit();
        if(sensor1Provider != null)
            return sensor1Provider.isDeviceSupport();
        return false;
    }

    /**
     * 获取当前是否开启了监听。
     * @return 是否开启
     */
    @UniJSMethod
    @Keep
    public boolean isGyroStarted() {
        checkAndInit();
        if(sensor1Provider != null)
            return sensor1Provider.isRunning();
        return false;
    }

    /**
     * 启动非回调式陀螺仪，需要手动调用获取当前数据。
     * @param options
     * {
     *     interval: 'fastest'|'game'|'ui'|'normal', 监听速度，可选：normal正常（5次秒），ui较缓慢(约16次秒)，game游戏(50次秒)，fastest最快。此数据对应于安卓的SensorManager.SENSOR_DELAY_*
     * }
     * @param callback 此回调仅回传表示调用是否成功
     */
    @UniJSMethod
    @Keep
    public void startGyro(JSONObject options, JSCallback callback) {
        checkAndInit();
        if(sensor1Provider.isRunning()) {
            if(callback != null) {
                JSONObject data = new JSONObject();
                data.put("success", false);
                data.put("errMsg", "The listener is running. Please call stopGyro stop it first!");
                callback.invoke(data);
            }
            return;
        }

        String intervalStr = "";
        if(options.containsKey("interval")) {
            try {
                intervalStr = options.getString("interval");
            } catch (Exception e) {
                UniLogUtils.i("startGyro!");
            }
        }
        switch (intervalStr) {
            default:
            case "normal":
                sensor1Provider.setSensorDelay(SensorManager.SENSOR_DELAY_NORMAL);
                break;
            case "ui":
                sensor1Provider.setSensorDelay(SensorManager.SENSOR_DELAY_UI);
                break;
            case "game":
                sensor1Provider.setSensorDelay(SensorManager.SENSOR_DELAY_GAME);
                break;
            case "fastest":
                sensor1Provider.setSensorDelay(SensorManager.SENSOR_DELAY_FASTEST);
                break;
        }

        sensor1Provider.setOnSensorChangedListener(null);
        sensor1Provider.start();

        if(callback != null) {
            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("errMsg", "ok");
            callback.invoke(data);
        }
    }

    /**
     * 自动回调模式获取陀螺仪参数
     * @param options
     * {
     *     interval: 'fastest'|'game'|'ui'|'normal', 监听速度，可选：normal正常（5次秒），ui较缓慢(约16次秒)，game游戏(50次秒)，fastest最快。此数据对应于安卓的SensorManager.SENSOR_DELAY_*
     * }
     * @param callback
     * 回调参数：
     * {
     *     x: number,
     *     y: number,
     *     z: number,
     * }
     */
    @UniJSMethod
    @Keep
    public void startGyroWithCallback(JSONObject options, JSCallback callback) {
        checkAndInit();
        if(sensor1Provider.isRunning()) {
            JSONObject data = new JSONObject();
            data.put("success", false);
            data.put("errMsg", "The listener is running. Please call stopGyro stop it first!");
            callback.invoke(data);
            return;
        }

        sensor1Provider.setOnSensorChangedListener(() -> {
            JSONObject data = new JSONObject();
            float[] xyz = new float[3];
            sensor1Provider.getEulerAngles(xyz);
            data.put("x", Math.toDegrees(xyz[0]));
            data.put("y", Math.toDegrees(xyz[1]));
            data.put("z", Math.toDegrees(xyz[2]));
            data.put("success", false);
            data.put("errMsg", "ok");
            callback.invokeAndKeepAlive(data);
        });
        startGyro(options, null);
    }

    /**
     * 停止陀螺仪参数获取
     */
    @UniJSMethod
    @Keep
    public void stopGyro(JSCallback callback) {
        checkAndInit();
        if(!sensor1Provider.isRunning()) {
            if(callback != null) {
                JSONObject data = new JSONObject();
                data.put("success", false);
                data.put("errMsg", "The listener is not running.");
                callback.invoke(data);
            }
            return;
        }
        sensor1Provider.stop();

        if(callback != null) {
            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("errMsg", "ok");
            callback.invoke(data);
        }
    }

    /**
     * 异步获取当前陀螺仪数据
     * @param callback 回调参数为当前陀螺仪数据
     * 回调参数：
     * {
     *     x: number,
     *     y: number,
     *     z: number,
     * }
     */
    @UniJSMethod
    @Keep
    public void getGyroValue(JSCallback callback) {
        JSONObject data = new JSONObject();
        if(sensor1Provider != null) {
            float[] xyz = new float[3];
            sensor1Provider.getEulerAngles(xyz);
            data.put("x", Math.toDegrees(xyz[0]));
            data.put("y", Math.toDegrees(xyz[1]));
            data.put("z", Math.toDegrees(xyz[2]));
            data.put("success", false);
            data.put("errMsg", "ok");
        } else {
            data.put("success", false);
            data.put("errMsg", "Not init!");
        }
        callback.invoke(data);
    }

    /**
     * 同步获取当前陀螺仪数据
     * @return 当前陀螺仪数据
     * {
     *     x: number,
     *     y: number,
     *     z: number,
     * }
     */
    @UniJSMethod
    @Keep
    public JSONObject getGyroValueSync() {
        JSONObject data = new JSONObject();
        if(sensor1Provider != null) {
            float[] xyz = new float[3];
            sensor1Provider.getEulerAngles(xyz);
            data.put("x", Math.toDegrees(xyz[0]));
            data.put("y", Math.toDegrees(xyz[1]));
            data.put("z", Math.toDegrees(xyz[2]));
            data.put("success", false);
            data.put("errMsg", "ok");
        } else {
            data.put("success", false);
            data.put("errMsg", "Not init!");
        }
        return data;
    }
}
