<template>
	<view class="content">
		<text class="title">获取陀螺仪数据：</text>
		<view class="text-area">
			<text class="text">X:{{gyroValueX}}</text>
			<text class="text">Y:{{gyroValueY}}</text>
			<text class="text">Z:{{gyroValueZ}}</text>
		</view>
		<text class="title">获取陀螺仪原始数据：</text>
		<view class="text-area">
			<text class="text">角速度 X:{{gyroValueRawX}}</text>
			<text class="text">角速度 Y:{{gyroValueRawY}}</text>
			<text class="text">角速度 Z:{{gyroValueRawZ}}</text>
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
				
				//模式1：使用回调监听陀螺仪数据，将会按设置的interval定时回调
				/*
				this.gyroModule.startGyroWithCallback({
					interval: "normal", //监听速度，可选：normal正常（5次秒），ui较缓慢(约16次秒)，game最快(50次秒)。此数据对应于安卓的SensorManager.SENSOR_DELAY_*
				}, (res) => {
					console.log(res);
					if(res.success) {
						//成功，获取陀螺仪数据
						if(res.rawGyroscopeValue) {
							this.gyroValueX = res.x;
							this.gyroValueY = res.y;
							this.gyroValueZ = res.z;
							this.gyroValueRawX = res.rawGyroscopeValue.x;
							this.gyroValueRawY = res.rawGyroscopeValue.y;
							this.gyroValueRawZ = res.rawGyroscopeValue.z;
						}
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
				*/
				
				
				//模式2：使用定时器手动获取陀螺仪数据
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
