## 简介

该插件为安卓端/IOS APP提供了获取陀螺仪数据的能力，功能大致与[官方API](https://uniapp.dcloud.io/api/system/gyroscope)相同（截至本插件开发时，官方API依然不支持APP端，未来可能会支持？）。

## 使用方法

```js
export default {
  data() {
    return {
      gyroUpdateTimer: 0,
      gyroModule: null,
    }
  },
  beforeDestroy() {
    clearInterval(this.gyroUpdateTimer);
    //停止监听陀螺仪数据
    this.gyroModule.stopGyro();
  },
  onLoad(options) {
    this.gyroModule = uni.requireNativePlugin('imengyu-UniAndroidGyro-GyroModule');
    //因为uniapp的原因，如果要在页面一进入就监听，需要加一个延时
    setTimeout(() => {
      this.gyroModule.startGyro({
        interval: "normal", //监听速度，可选：normal正常（5次秒），ui较缓慢(约16次秒)，game最快(50次秒)。此数据对应于安卓的SensorManager.SENSOR_DELAY_*
      }, (res) => {
        console.log(res);
        if(res.success) {
          //定时器获取陀螺仪数据
          this.gyroUpdateTimer = setInterval(() => {
            //获取陀螺仪数据
            this.gyroModule.getGyroValue((res) => {
              console.log(res);
              this.gyroValueX = res.x;
              this.gyroValueY = res.y;
              this.gyroValueZ = res.z;
              this.gyroValueRawX = res.rawGyroscopeValue.x;
              this.gyroValueRawY = res.rawGyroscopeValue.y;
              this.gyroValueRawZ = res.rawGyroscopeValue.z;
            });
          }, 1000);
        } else {
          //失败
          if(res.notSupport) {
            //如果notSupport为true表示当前设备不支持陀螺仪
            uni.showModal({
              title: '提示',
              content: '当前设备不支持陀螺仪！',
              showCancel: false
            });
          }
        }
      });
    }, 100)
  },
}
```

## API 说明文档

* `startGyro(options, callback)`

  开始监听陀螺仪数据变化事件，需要手动调用获取当前数据。
  * 此函数回调仅为返回执行是否成功以及错误信息，不会返回陀螺仪数据，要使用回调模式获取陀螺仪数据，请使用 `startGyroWithCallback` 方法
  * 此函数不能重复调用开启。开启之前须使用 `getGyroStarted` 检查当前状态。
  * 如果设备不支持陀螺仪，则会自动停止。

  * **参数**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | interval | string  |  监听速度，可选 normal、ui、game、fastest，默认为normal。此数据对应于安卓的SensorManager.SENSOR_DELAY_*。在IOS上可以设置浮点数字符串，将会直接传递到`CMMotionManager.gyroUpdateInterval` 上，单位是秒。 |
  
  * **回调返回数据**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | success | boolean | 返回是否成功 |
    | errMsg | string | 返回错误信息 |
    | notSupport | boolean | 如果当前设备不支持陀螺仪，则为true |

* `startGyroWithCallback(options, callback)`

  使用自动回调模式, 开始监听陀螺仪数据变化事件。
  * 此函数不能重复调用开启。开启之前须使用 `isGyroStarted` 检查当前状态，如果要更改回调，必须先停止再重新开启。
  * 如果设备不支持陀螺仪，则会自动停止。
  * 第一次回调没有数据，只表示启动是否成功。

  * **参数**

  *与 `startGyro` 函数的参数相同。*

  * **回调返回数据**
  
    *第一次回调返回的数据与 `startGyro` 函数的返回数据相同。*

    *第二次回调返回数据：*

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | success | boolean | 返回是否成功 |
    | errMsg | string | 返回错误信息 |
    | x | number | 当前手机x轴方向角度（手机正面向上旋转轴）（这三个xyz数据经过处理，单位是角度，带偏移修正） |
    | y | number | 当前手机y轴方向角度（手机垂直旋转轴），手机平放时为0 |
    | z | number | 当前手机z轴方向角度（手机水平旋转轴），手机平放时为0 |
    | rawGyroscopeValue | object | 陀螺仪原始数据 (见下方)，未经处理，您可以自己进行相关计算 (同 uni.startGyroscope 的数据) |
    | rotationVector | object | **【仅Android】** 旋转向量原始数据 (见下方)，未经处理，您可以自己进行相关计算 |

  * **rawGyroscopeValue 格式**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | x | number | 手机绕x轴旋转的速率。(rad/s 弧度/秒) |
    | y | number | 手机绕y轴旋转的速率。(rad/s 弧度/秒) |
    | z | number | 手机绕z轴旋转的速率。(rad/s 弧度/秒) |

  * **rotationVector 格式**

    旋转向量，方便您自由计算，更多信息可以参考 [android 官方文档](https://developer.android.google.cn/guide/topics/sensors/sensors_motion)。

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | x | number | 沿x轴的旋转矢量分量 (x*sin(θ/2))。 |
    | y | number | 沿y轴的旋转矢量分量 (y*sin(θ/2))。 |
    | z | number | 沿z轴的旋转矢量分量 (z*sin(θ/2))。|
    | w | number | 旋转向量的标量分量 (cos(θ/2))。|

* `stopGyro(callback)`

  停止监听陀螺仪数据变化事件。

  * **回调返回数据**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | success | boolean | 返回是否成功 |
    | errMsg | string | 返回错误信息 |

* `getGyroValue(callback)`

  手动异步获取当前陀螺仪数据。该回调不会一直调用，如果需要连续数据，请加一个定时器重复获取。或者使用 `startGyroWithCallback` 获取连续数据。

  * **回调返回数据**
  
    *回调返回的数据与 `startGyroWithCallback` 第二次回调返回数据相同*

* `getGyroValueSync()`

  手动同步模式获取当前陀螺仪数据。

  * **函数返回数据**

    *返回的数据与 `startGyroWithCallback` 第二次回调返回数据相同*

* `getGyroStarted(callback)`

  获取当前是否开启了监听。

  * **回调返回数据**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | started | boolean | 当前是否开启了监听 |

* `isGyroAvailable()` （已弃用）

  获取当前设备是否支持陀螺仪。 (已弃用, 这个函数有问题，一般用不到，调用 startGyro 如果设备不支持也可以返回错误信息。 )。

* `isGyroStarted()` （已弃用）

  获取当前是否开启了监听 (已弃用, 请使用 getGyroStarted )。

### 附加 API 说明文档

* `startCustomSensor(options, callback)`

  开始自定义传感器监测 **【仅Android】**。
  返回原始数据，方便您自由处理。

  * 请参考 [Android 官方文档](https://developer.android.google.cn/guide/topics/sensors/sensors_motion) 。
  * 开启后不会自动停止，切记在页面返回之前调用 `stopCustomSensor` 停止监听, 否则会一直占用内存。

  * **参数**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | interval | string  |  监听速度，可选 normal、ui、game、fastest，默认为normal。 |
    | type | string | 表示要监听的数据类型，此数据对应于Android的 `Sensor.TYPE_*`，关于数据的说明，请参考 [Android 官方文档](https://developer.android.google.cn/guide/topics/sensors/sensors_motion) 。一共支持以下几个类型： |
    |  | 'TYPE_ACCELEROMETER' | |
    |  | 'TYPE_ACCELEROMETER_UNCALIBRATED' |（需要 API Level 26+）  |
    |  | 'TYPE_GRAVITY' | |
    |  | 'TYPE_GYROSCOPE' | |
    |  | 'TYPE_GYROSCOPE_UNCALIBRATED' | （需要 API Level 26+） |
    |  | 'TYPE_LINEAR_ACCELERATION' | |
    |  | 'TYPE_ROTATION_VECTOR' | |
    |  | 'TYPE_STEP_COUNTER' |  |
  
  * **回调返回数据**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | success | boolean | 返回是否成功 |
    | errMsg | string | 返回错误信息 |
    | values | number[] | 系统返回的原始数据 |
    | customSensorId | number | 当前监听器的ID，可以调用 `stopCustomSensor` 停止当前监听器 |

* `stopCustomSensor(options, callback)`

  停止自定义传感器监测 **【仅Android】**。

  * **参数**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | customSensorId | number | 要停止的监听器的ID |

  * **回调返回数据**

    |  属性| 类型 |说明  |
    |  ----  | ----  | ----  |
    | success | boolean | 返回是否成功 |
    | errMsg | string | 返回错误信息 |

### 兼容性特殊说明

* 页面切换并不会停止监听。您需要在页面的 OnUnload 函数中手动停止监听。
* 应用切换至后台之后会自动停止获取陀螺仪数据，重新换回前台后会自动重新开始。
* IOS 无法返回BOOL, 使用字符串代替，返回 `'true'` 表示true，返回 `''` 表示false (可使用if判断)。
