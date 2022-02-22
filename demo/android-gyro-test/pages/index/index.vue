<template>
	<view class="content">
		<text class="title">获取陀螺仪数据：</text>
		<view class="text-area">
			<text class="text">X:{{gyroValueX}}</text>
			<text class="text">Y:{{gyroValueY}}</text>
			<text class="text">Z:{{gyroValueZ}}</text>
			<text class="text">原始角速度 X:{{gyroValueRawX}}</text>
			<text class="text">原始角速度 Y:{{gyroValueRawY}}</text>
			<text class="text">原始角速度 Z:{{gyroValueRawZ}}</text>
		</view>
	</view>
</template>

<script>
	
	export default {
		data() {
			return {
				gyroUpdateTimer: 0,
				gyroValueX: 0,
				gyroValueY: 0,
				gyroValueZ: 0,
				gyroValueRawX: 0,
				gyroValueRawY: 0,
				gyroValueRawZ: 0,
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
						console.log(res);
						this.gyroValueX = res.x;
						this.gyroValueY = res.y;
						this.gyroValueZ = res.z;
						this.gyroValueRawX = res.rawGyroscopeValue.x;
						this.gyroValueRawY = res.rawGyroscopeValue.y;
						this.gyroValueRawZ = res.rawGyroscopeValue.z;
					});
				}, 1000);
			}, 300)
		},
	}
</script>

<style>
	.content {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
	}

	.logo {
		height: 200rpx;
		width: 200rpx;
		margin-top: 200rpx;
		margin-left: auto;
		margin-right: auto;
		margin-bottom: 50rpx;
	}

	.text-area {
		display: flex;
		justify-content: center;
		flex-direction: column;
		margin-top: 50rpx;
	}
	.text {
		width: 400rpx;
	}
	.title {
		font-size: 36rpx;
		color: #8f8f94;
	}
</style>
