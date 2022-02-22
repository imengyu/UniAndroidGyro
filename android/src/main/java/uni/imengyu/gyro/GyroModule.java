package uni.imengyu.gyro;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;

import androidx.annotation.Keep;

import com.alibaba.fastjson.JSONObject;
import com.taobao.weex.bridge.JSCallback;
import com.taobao.weex.common.WXModule;

import java.util.HashMap;
import java.util.Map;

import io.dcloud.feature.uniapp.annotation.UniJSMethod;
import io.dcloud.feature.uniapp.utils.UniLogUtils;
import uni.imengyu.gyro.sensor.CustomSensorProvider;
import uni.imengyu.gyro.sensor.ImprovedOrientationSensor1Provider;
import uni.imengyu.gyro.sensor.OrientationProvider;

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
     * 返回当前设备是否支持陀螺仪。(弃用)
     * @return 是否支持
     */
    @UniJSMethod
    @Keep
    public String isGyroAvailable() {
        checkAndInit();
        if(sensor1Provider != null)
            return sensor1Provider.isDeviceSupport() ? "true" : "";
        return "";
    }

    /**
     * 获取当前是否开启了监听。(弃用)
     * @return 是否开启
     */
    @UniJSMethod
    @Keep
    public String isGyroStarted() {
        checkAndInit();
        if(sensor1Provider != null)
            return sensor1Provider.isRunning() ? "true" : "";
        return "";
    }

    /**
     * 获取当前是否开启了监听
     * @param callback
     * {
     *     started: boolean
     * }
     */
    @UniJSMethod
    @Keep
    public void getGyroStarted(JSCallback callback) {
        JSONObject data = new JSONObject();
        if(sensor1Provider != null)
            data.put("started", sensor1Provider.isRunning());
        else
            data.put("started", false);
        callback.invoke(data);
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

        if(!sensor1Provider.isDeviceSupport()) {
            JSONObject data = new JSONObject();
            data.put("success", false);
            data.put("notSupport", true);
            data.put("errMsg", "This device does not support gyroscopes");
            callback.invoke(data);
        }
        if(sensor1Provider.isRunning()) {
            if(callback != null) {
                JSONObject data = new JSONObject();
                data.put("success", false);
                data.put("errMsg", "The listener is running. Please call stopGyro stop it first!");
                callback.invoke(data);
            }
            return;
        }

        setSensorDelay(options, sensor1Provider);

        sensor1Provider.setOnSensorChangedListener(null);
        sensor1Provider.start();

        if(callback != null) {
            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("errMsg", "ok");
            callback.invoke(data);
        }
    }

    //获取陀螺仪JSON数据
    private JSONObject getSensor1ProviderJsonValue() {
        JSONObject data = new JSONObject();
        float[] xyz = new float[3];
        sensor1Provider.getEulerAngles(xyz);
        data.put("x", Math.toDegrees(xyz[0]));
        data.put("y", Math.toDegrees(xyz[1]));
        data.put("z", Math.toDegrees(xyz[2]));

        float[] xyzw =  sensor1Provider.getCurrentRotationVector();
        JSONObject rotationVector = new JSONObject();
        rotationVector.put("x", xyzw[0]);
        rotationVector.put("y", xyzw[1]);
        rotationVector.put("z", xyzw[2]);
        rotationVector.put("w", xyzw[3]);
        data.put("rotationVector", rotationVector);

        xyz = sensor1Provider.getCurrentGyroscopeValue();
        JSONObject rawGyroscopeValue = new JSONObject();
        rawGyroscopeValue.put("x", xyz[0]);
        rawGyroscopeValue.put("y", xyz[1]);
        rawGyroscopeValue.put("z", xyz[2]);
        data.put("rawGyroscopeValue", rawGyroscopeValue);

        data.put("success", true);
        data.put("errMsg", "ok");
        return data;
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
     *
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

        sensor1Provider.setOnSensorChangedListener((values) -> {
            callback.invokeAndKeepAlive(getSensor1ProviderJsonValue());
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
        if(sensor1Provider != null) {
            callback.invoke(getSensor1ProviderJsonValue());
        } else {
            JSONObject data = new JSONObject();
            data.put("success", false);
            data.put("errMsg", "Not init!");
            callback.invoke(data);
        }
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
        if(sensor1Provider != null) {
            return (getSensor1ProviderJsonValue());
        } else {
            JSONObject data = new JSONObject();
            data.put("success", false);
            data.put("errMsg", "Not init!");
            return data;
        }
    }

    private static int initCustomSensorProviderId = 0;
    private static final Map<Integer, CustomSensorProvider> allCustomSensorProvider = new HashMap<>();

    //设置 interval
    private void setSensorDelay(JSONObject options, OrientationProvider provider) {
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
                provider.setSensorDelay(SensorManager.SENSOR_DELAY_NORMAL);
                break;
            case "ui":
                provider.setSensorDelay(SensorManager.SENSOR_DELAY_UI);
                break;
            case "game":
                provider.setSensorDelay(SensorManager.SENSOR_DELAY_GAME);
                break;
            case "fastest":
                provider.setSensorDelay(SensorManager.SENSOR_DELAY_FASTEST);
                break;
        }
    }
    //开始自定义 Sensor
    private void startCustomSensorProvider(JSONObject options, final JSCallback callback, int type) {
        SensorManager sensorManager = (SensorManager) mWXSDKInstance.getContext().getSystemService(Context.SENSOR_SERVICE);
        int id = ++initCustomSensorProviderId;
        CustomSensorProvider provider = new CustomSensorProvider(sensorManager, type);
        provider.setOnSensorChangedListener((values) -> {
            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("errMsg", "ok");
            data.put("values", values);
            data.put("customSensorId", id);
            callback.invoke(data);
        });
        setSensorDelay(options, provider);
        allCustomSensorProvider.put(id, provider);

        JSONObject data = new JSONObject();
        data.put("success", true);
        data.put("errMsg", "ok");
        data.put("customSensorId", id);
        callback.invoke(data);
    }

    /**
     * 开始自定义传感器监测 （Android）
     * @param options
     * {
     *      type: 'TYPE_ACCELEROMETER'|'TYPE_ACCELEROMETER_UNCALIBRATED'|'TYPE_GRAVITY'|'TYPE_GYROSCOPE'|'TYPE_GYROSCOPE_UNCALIBRATED'|'TYPE_LINEAR_ACCELERATION'|'TYPE_ROTATION_VECTOR'|'TYPE_STEP_COUNTER',
     *      interval: 'fastest'|'game'|'ui'|'normal'
     * }
     * @param callback
     * {
     *      success: boolean,
     *      errMsg: string,
     *      values: number[],
     *      customSensorId: number,
     * }
     */
    @UniJSMethod
    @Keep
    public void startCustomSensor(JSONObject options, final JSCallback callback) {
        String type = options.getString("type");
        switch (type) {
            case "TYPE_ACCELEROMETER": {
                startCustomSensorProvider(options, callback, Sensor.TYPE_ACCELEROMETER);
                break;
            }
            case "TYPE_ACCELEROMETER_UNCALIBRATED": {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startCustomSensorProvider(options, callback, Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
                } else {
                    JSONObject data = new JSONObject();
                    data.put("success", false);
                    data.put("errMsg", "TYPE_ACCELEROMETER_UNCALIBRATED require API Level 26");
                    callback.invoke(data);
                }
                break;
            }
            case "TYPE_GRAVITY": {
                startCustomSensorProvider(options, callback, Sensor.TYPE_GRAVITY);
                break;
            }
            case "TYPE_GYROSCOPE": {
                startCustomSensorProvider(options, callback, Sensor.TYPE_GYROSCOPE);
                break;
            }
            case "TYPE_GYROSCOPE_UNCALIBRATED": {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startCustomSensorProvider(options, callback, Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
                } else {
                    JSONObject data = new JSONObject();
                    data.put("success", false);
                    data.put("errMsg", "TYPE_GYROSCOPE_UNCALIBRATED require API Level 26");
                    callback.invoke(data);
                }
                break;
            }
            case "TYPE_LINEAR_ACCELERATION": {
                startCustomSensorProvider(options, callback, Sensor.TYPE_LINEAR_ACCELERATION);
                break;
            }
            case "TYPE_ROTATION_VECTOR": {
                startCustomSensorProvider(options, callback, Sensor.TYPE_ROTATION_VECTOR);
                break;
            }
            case "TYPE_STEP_COUNTER": {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    startCustomSensorProvider(options, callback, Sensor.TYPE_STEP_COUNTER);
                } else {
                    JSONObject data = new JSONObject();
                    data.put("success", false);
                    data.put("errMsg", "TYPE_ACCELEROMETER_UNCALIBRATED require API Level 19");
                    callback.invoke(data);
                }
                break;
            }
            default: {
                //错误
                JSONObject data = new JSONObject();
                data.put("success", false);
                data.put("errMsg", "Unknow type " + type);
                callback.invoke(data);
                break;
            }
        }
    }

    /**
     * 停止自定义传感器监测（Android）
     * @param options
     * {
     *     id: number, //startCustomSensor返回的customSensorId
     * }
     * @param callback
     * {
     *      success: boolean,
     *      errMsg: string,
     * }
     */
    @UniJSMethod
    @Keep
    public void stopCustomSensor(JSONObject options, final JSCallback callback) {
        if(!options.containsKey("id")) {
            JSONObject data = new JSONObject();
            data.put("success", false);
            data.put("errMsg", "id is required");
            callback.invoke(data);
        }

        Integer id = options.getInteger("id");
        if(allCustomSensorProvider.containsKey(id)) {
            CustomSensorProvider provider = allCustomSensorProvider.get(id);
            if(provider != null)
                provider.stop();
            allCustomSensorProvider.remove(id);

            JSONObject data = new JSONObject();
            data.put("success", true);
            data.put("errMsg", "ok");
            callback.invoke(data);
        } else {
            JSONObject data = new JSONObject();
            data.put("success", false);
            data.put("errMsg", "Not found id " + id);
            callback.invoke(data);
        }

    }
}
