## 简介

该插件为安卓端APP提供了获取陀螺仪数据的能力，功能大致与[官方API](https://uniapp.dcloud.io/api/system/gyroscope)相同（截至本插件开发时，官方API依然不支持APP端，未来可能会支持？）。

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
      //开始监听陀螺仪数据
      this.gyroModule.startGyro({
        interval: "normal", //监听速度，可选：normal正常（5次秒），ui较缓慢(约16次秒)，game最快(50次秒)。此数据对应于安卓的SensorManager.SENSOR_DELAY_*
      });

      this.gyroUpdateTimer = setInterval(() => {
        //获取陀螺仪数据
        this.gyroModule.getGyroValue((res) => {
          console.log("x: " + res.x);
          console.log("y: " + res.y);
          console.log("z: " + res.z);
        });
      }, 1000);
    }, 100)
  },
}
```

## API 说明文档

### 陀螺仪参数结构

|  属性| 类型 |说明  |
|  ----  | ----  | ----  |
| interval | string  |  监听速度，可选 normal、ui、game、fastest，默认为normal  |

```ts
/**
 * 陀螺仪参数结构定义
 */
interface GyroOptions {
  /**
   * 监听速度，可选 normal、ui、game、fastest，默认为normal，
   * 此数据对应于安卓的SensorManager.SENSOR_DELAY_*
   */
  interval: 'fastest'|'game'|'ui'|'normal'
}
```

### 陀螺仪返回参数结构

|  属性| 类型 |说明  |
|  ----  | ----  | ----  |
| x | number | 当前手机x轴方向角度（手机正面向上旋转轴） |
| y | number | 当前手机y轴方向角度（手机垂直旋转轴），手机平放时为0 |
| z | number | 当前手机z轴方向角度（手机水平旋转轴），手机平放时为0 |
| success | boolean | 返回是否成功 |
| errMsg | string | 返回错误信息 |

```ts
/**
 * 陀螺仪返回参数结构定义
 */
interface GyroReturnValues {
  /**
   * 当前手机x轴方向角度（手机正面向上旋转轴）
   */
  x: number,
  /**
   * 当前手机y轴方向角度（手机垂直旋转轴），手机平放时为0
   */
  y: number,
  /**
   * 当前手机z轴方向角度（手机水平旋转轴），手机平放时为0
   */
  z: number,
  /**
   * 返回是否成功
   */
  success: boolean,
  /**
   * 返回错误信息
   */
  errMsg: string,
}
/**
 * 执行返回参数结构定义
 */
interface GyroExecuteReturnValues {
  /**
   * 返回是否成功
   */
  success: boolean,
  /**
   * 返回错误信息
   */
  errMsg: string,
}
```

### GyroModule API

* `isGyroAvailable() : boolean`

  获取当前设备是否支持陀螺仪。

* `isGyroStarted() : boolean`

  获取当前是否开启了监听。

* `startGyro(options: GyroOptions, callback: (res: GyroExecuteReturnValues) => void) : void`

  开始监听陀螺仪数据变化事件，需要手动调用获取当前数据。
  * 此函数回调仅为返回执行是否成功以及错误信息，不会返回陀螺仪数据，要使用回调模式获取陀螺仪数据，请使用 `startGyroWithCallback` 方法
  * 此函数不能重复调用开启。开启之前须使用 `isGyroStarted` 检查当前状态。

* `startGyroWithCallback(options: GyroOptions, callback: (res: GyroReturnValues) => void) : void`

  使用自动回调模式, 开始监听陀螺仪数据变化事件。
  * 此函数不能重复调用开启。开启之前须使用 `isGyroStarted` 检查当前状态，如果要更改回调，必须先停止再重新开启。

* `stopGyro(callback: (res: GyroExecuteReturnValues) => void) : void`

  停止监听陀螺仪数据变化事件。

* `getGyroValue(callback: (res: GyroReturnValues) => void)) : void`

  手动异步获取当前陀螺仪数据。该回调不会一直调用，如果需要连续数据，请加一个定时器重复获取。或者使用 `startGyroWithCallback` 获取连续数据。

* `getGyroValueSync() : GyroReturnValues`

  手动同步模式获取当前陀螺仪数据。

### 兼容性特殊说明

* IOS平台上页面切换并不会停止监听。您需要在页面的 OnUnload 函数中手动停止监听。
* IOS 无法返回BOOL, 使用字符串代替，返回 `'true'` 表示true，返回 `'false'` 表示false。
