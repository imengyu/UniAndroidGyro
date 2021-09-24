## 更新记录
1.0.0
首次发布

## 平台兼容性

|  Android   | iOS  |
|  ----  | ----  |
| API Level 19+  | × |
	
## 简介

该插件为安卓端APP提供了获取陀螺仪数据的能力，功能大致与[官方API](https://uniapp.dcloud.io/api/system/gyroscope)相同（截至本插件开发时，官方API依然不支持APP端，未来可能会支持？）。

## 使用方法

导入插件

```js
const cUniAndroidGyro = uni.requireNativePlugin('GyroModule');
```

使用示例
```js
export default {
  data() {
    return {
      gyroUpdateTimer: 0,
    }
  },
  beforeDestroy() {
    clearInterval(this.gyroUpdateTimer);
    //停止监听陀螺仪数据
    cUniAndroidGyro.stopGyro();
  },
  onLoad(options) {
    //因为uniapp的原因，如果要在页面一进入就监听，需要加一个延时
    setTimeout(() => {
      //开始监听陀螺仪数据
      cUniAndroidGyro.startGyro({
        interval: "normal", //监听速度，可选：normal正常（5次秒），ui较缓慢(约16次秒)，game最快(50次秒)。此数据对应于安卓的SensorManager.SENSOR_DELAY_*
      });

      this.gyroUpdateTimer = setInterval(() => {
        //获取陀螺仪数据
        cUniAndroidGyro.getGyroValue((res) => {
          console.log("x: " + res.x);
          console.log("y: " + res.y);
          console.log("z: " + res.z);
        });
      }, 1000);
    }, 100)
  },
}
```

## API

### startGyro(OBJECT)

监听陀螺仪数据变化事件。

OBJECT参数说明：

|  属性| 类型 |说明  |
|  ----  | ----  | ----  |
| interval | string  |  监听速度，可选 normal、ui、game，默认为normal  |

### stopGyro()

停止监听陀螺仪数据变化事件。

### getGyroValue(CALLBACK)

获取当前陀螺仪数据。该回调不会一直调用，如果需要连续数据，请加一个定时器重复获取。

CALLBACK 参数说明

|  属性| 类型 |说明  |
|  ----  | ----  | ----  |
| res | Object  | res = {x,y,x} |

res 的结构

以下参数都已转换为角度。

|  属性| 类型 |说明  |
|  ----  | ----  | ----  |
| x | number | 当前手机x轴方向角度（手机正面向上旋转轴） |
| y | number | 当前手机y轴方向角度（手机垂直旋转轴），手机平放时为0 |
| z | number | 当前手机z轴方向角度（手机水平旋转轴），手机平放时为0 |
