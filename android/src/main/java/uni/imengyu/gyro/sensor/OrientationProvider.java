package uni.imengyu.gyro.sensor;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import uni.imengyu.gyro.representation.MatrixF4x4;
import uni.imengyu.gyro.representation.Quaternion;

/**
 * Classes implementing this interface provide an orientation of the device
 * either by directly accessing hardware, using Android sensor fusion or fusing
 * sensors itself.
 * 
 * The orientation can be provided as rotation matrix or quaternion.
 * 
 * @author Alexander Pacha
 * 
 */
public abstract class OrientationProvider implements SensorEventListener {
    /**
     * Sync-token for syncing read/write to sensor-data from sensor manager and
     * fusion algorithm
     */
    protected final Object synchronizationToken = new Object();

    /**
     * The list of sensors used by this provider
     */
    protected List<Sensor> sensorList = new ArrayList<Sensor>();

    /**
     * The matrix that holds the current rotation
     */
    protected final MatrixF4x4 currentOrientationRotationMatrix;

    /**
     * The quaternion that holds the current rotation
     */
    protected final Quaternion currentOrientationQuaternion;

    /**
     * The sensor manager for accessing android sensors
     */
    protected SensorManager sensorManager;

    /**
     * Initialises a new OrientationProvider
     * 
     * @param sensorManager
     *            The android sensor manager
     */
    public OrientationProvider(SensorManager sensorManager) {
        this.sensorManager = sensorManager;

        // Initialise with identity
        currentOrientationRotationMatrix = new MatrixF4x4();

        // Initialise with identity
        currentOrientationQuaternion = new Quaternion();
    }

    private int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;

    /**
     * Get the sensorDelay, SensorManager.SENSOR_DELAY_
     * @return SensorManager.SENSOR_DELAY_
     */
    public int getSensorDelay() {
        return sensorDelay;
    }

    /**
     * Set the sensorDelay,
     * @param sensorDelay SensorManager.SENSOR_DELAY_
     */
    public void setSensorDelay(int sensorDelay) {
        this.sensorDelay = sensorDelay;
    }

    private boolean running = false;

    protected OnSensorChangedListener onSensorChangedListener = null;

    public interface OnSensorChangedListener {
        void onSensorChanged(float[] values);
    }

    public void setOnSensorChangedListener(OnSensorChangedListener onSensorChangedListener) {
        this.onSensorChangedListener = onSensorChangedListener;
    }

    /**
     * Get running status
     * @return Is running?
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Starts the sensor fusion (e.g. when resuming the activity)
     */
    public void start() {
        running = true;
        // enable our sensor when the activity is resumed, ask for
        // 10 ms updates.
        for (Sensor sensor : sensorList) {
            // enable our sensors when the activity is resumed, ask for
            // 20 ms updates (Sensor_delay_game)
            sensorManager.registerListener(this, sensor, getSensorDelay());
        }
    }

    /**
     * Stops the sensor fusion (e.g. when pausing/suspending the activity)
     */
    public void stop() {
        running = false;
        // make sure to turn our sensors off when the activity is paused
        for (Sensor sensor : sensorList) {
            sensorManager.unregisterListener(this, sensor);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not doing anything
    }

    /**
     * Get the current rotation of the device in the rotation matrix format (4x4 matrix)
     */
    public void getRotationMatrix(MatrixF4x4 matrix) {
        synchronized (synchronizationToken) {
            matrix.set(currentOrientationRotationMatrix);
        }
    }

    /**
     * Get the current rotation of the device in the quaternion format (vector4f)
     */
    public void getQuaternion(Quaternion quaternion) {
        synchronized (synchronizationToken) {
            quaternion.set(currentOrientationQuaternion);
        }
    }

    /**
     * Get the current rotation of the device in the Euler angles
     */
    public void getEulerAngles(float angles[]) {
        synchronized (synchronizationToken) {
            SensorManager.getOrientation(currentOrientationRotationMatrix.matrix, angles);
        }
    }
}
