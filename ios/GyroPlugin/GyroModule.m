//
//  TestModule.m
//  DCTestUniPlugin
//
//  Created by XHY on 2020/4/22.
//  Copyright © 2020 DCloud. All rights reserved.
//

#import "GyroModule.h"

@implementation GyroModule

// 通过宏 UNI_EXPORT_METHOD 将异步方法暴露给 js 端
UNI_EXPORT_METHOD(@selector(startGyro:callback:))
UNI_EXPORT_METHOD(@selector(startGyroWithCallback:callback:))
UNI_EXPORT_METHOD(@selector(getGyroValue:))
UNI_EXPORT_METHOD(@selector(stopGyro:))
UNI_EXPORT_METHOD_SYNC(@selector(isGyroAvailable))
UNI_EXPORT_METHOD_SYNC(@selector(isGyroStarted))
UNI_EXPORT_METHOD_SYNC(@selector(getGyroValueSync))

//创建CMMotionManager
- (void)checkAndCreateManager {
    if (!_motionGyro)
        _motionGyro = [[CMMotionManager alloc] init];
}
//设置延时
- (void)setManagerUpdateInterval:(NSDictionary *)options {
    NSString *interval = [options objectForKey:@"interval"];
    if(interval) {
        NSArray *items = @[@"normal", @"ui", @"game", @"fastest"];
        long item = [items indexOfObject:interval];
        switch (item)
        {
            default:
                if([self isPureFloat: interval])
                    _motionGyro.gyroUpdateInterval = [interval floatValue];
                else
                    _motionGyro.gyroUpdateInterval = 0.2;
                break;
            case 0:
                _motionGyro.gyroUpdateInterval = 0.2;
                break;
            case 1:
                _motionGyro.gyroUpdateInterval = 0.0625;
                break;
            case 2:
                _motionGyro.gyroUpdateInterval = 0.02;
                break;
            case 3:
                _motionGyro.gyroUpdateInterval = 0.016;
                break;
        }
    }
}
//判断nsstring是不是浮点数
- (BOOL)isPureFloat:(NSString *)string{
    NSScanner* scan = [NSScanner scannerWithString:string];
    float val;
    return [scan scanFloat:&val] && [scan isAtEnd];
}
//构造gyro返回值
- (NSDictionary *)makeRetValue:(NSString*)err {
    if(err) {
        return [NSDictionary dictionaryWithObjectsAndKeys:
                @"", @"success",
                err, @"errMsg", nil];
    } else {
        NSDictionary * rawGyroscopeValue = [NSDictionary dictionaryWithObjectsAndKeys:
                                         @"true", @"success",
                                         @"ok", @"errMsg",
                                         _numberGyroX, @"x",
                                         _numberGyroY, @"y",
                                         _numberGyroZ, @"z",
                                         nil];
        
        return [NSDictionary dictionaryWithObjectsAndKeys:
                @"true", @"success",
                @"ok", @"errMsg",
                _numberRotX, @"x",
                _numberRotY, @"y",
                _numberRotZ, @"z",
                rawGyroscopeValue, @"rawGyroscopeValue",
                nil];
    }
}
//构造文字返回值
- (NSDictionary *)makeSuccessRetValue {
    return [NSDictionary dictionaryWithObjectsAndKeys:
            @"ok",@"errMsg",
            @"true", @"success", nil];
}
//构造不支持的返回值
- (NSDictionary *)makeNotSupportRetValue {
    return [NSDictionary dictionaryWithObjectsAndKeys:
            @"", @"success",
            @"true", @"notSupport",
            @"This device does not support gyroscopes", @"errMsg", nil];
}

//开启motion，用于计算手机旋转角度
- (void)startMotion {
    if ([self->_motionGyro isDeviceMotionAvailable]) {
        [self->_motionGyro startDeviceMotionUpdatesToQueue:[NSOperationQueue mainQueue]
                                                        withHandler:^(CMDeviceMotion * _Nullable motion,
                                                                      NSError * _Nullable error) {
                    
            // Gravity 获取手机的重力值在各个方向上的分量，根据这个就可以获得手机的空间位置，倾斜角度等
            double gravityX = motion.gravity.x;
            double gravityY = motion.gravity.y;
            double gravityZ = motion.gravity.z;
            
            // 获取手机的倾斜角度(zTheta是手机与水平面的夹角， xyTheta是手机绕自身旋转的角度)：
            double zTheta = atan2(gravityZ,sqrtf(gravityX * gravityX + gravityY * gravityY)) / M_PI * 180.0;
            double xTheta = atan2(gravityX, gravityY) / M_PI * 180.0;
            double yTheta = atan2(gravityY, gravityZ) / M_PI * 180.0;
            
            self->_numberGyroX = [NSNumber numberWithDouble:zTheta];
            self->_numberGyroY = [NSNumber numberWithDouble:yTheta];
            self->_numberGyroZ = [NSNumber numberWithDouble:xTheta];
        }];
    }
}


/**
 * 返回当前设备是否支持陀螺仪。(弃用)
 * @return 是否支持
 */
- (NSString *)isGyroAvailable {
    [self checkAndCreateManager];
    if ([_motionGyro isGyroAvailable])
        return @"true";
    else
        return @"";
}

/**
 * 获取当前是否开启了监听。(弃用)
 * @return 是否开启
 */
- (NSString *)isGyroStarted {
    [self checkAndCreateManager];
    if (_status)
        return @"true";
    else
        return @"";
}

/**
 * 获取当前是否开启了监听
 * @param callback
 * {
 *     started: boolean
 * }
 */
- (void)startGyro:(UniModuleKeepAliveCallback)callback {
    NSDictionary *result = [NSDictionary
                            dictionaryWithObject:(self->_status ? @"true" : @"")
                            forKey:@"started"];
    callback(result, NO);
}

/**
 * 启动非回调式陀螺仪，需要手动调用获取当前数据。
 * @param options
 * {
 *     interval: 'fastest'|'game'|'ui'|'normal', 监听速度，可选：normal正常（5次秒），ui较缓慢(约16次秒)，game游戏(50次秒)，fastest最快。此数据对应于安卓的SensorManager.SENSOR_DELAY_*
 * }
 * @param callback 此回调仅回传表示调用是否成功
 */
- (void)startGyro:(NSDictionary *)options
         callback:(UniModuleKeepAliveCallback)callback {
    [self checkAndCreateManager];
    
    //Check state
    if(self->_status) {
        if(callback)
            callback([self makeRetValue:@"The listener is running. Please call stopGyro stop it first!"], NO);
        return;
    }
    //Check Available
    if(![_motionGyro isGyroAvailable]) {
        if(callback)
            callback([self makeNotSupportRetValue], NO);
        return;
    }
    
    self->_status = TRUE;
    
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    
    [self setManagerUpdateInterval:options];
    [self->_motionGyro startGyroUpdatesToQueue:queue withHandler:^(CMGyroData *gyroData, NSError *error) {
        
        CMRotationRate rotate = gyroData.rotationRate;
        self->_numberGyroX = [NSNumber numberWithDouble:rotate.x];
        self->_numberGyroY = [NSNumber numberWithDouble:rotate.y];
        self->_numberGyroZ = [NSNumber numberWithDouble:rotate.z];
    }];
    //Start montion for calc
    [self startMotion];
    
    if(callback)
        callback([self makeSuccessRetValue], NO);
}

- (void)startGyroWithCallback:(NSDictionary *)options
                     callback:(UniModuleKeepAliveCallback)callback{
    [self checkAndCreateManager];
    
    //Check state
    if(self->_status) {
        if(callback)
            callback([self makeRetValue:@"The listener is running. Please call stopGyro stop it first!"], NO);
        return;
    }
    //Check Available
    if(![_motionGyro isGyroAvailable]) {
        if(callback) callback([self makeNotSupportRetValue], NO);
        return;
    }
    self->_status = TRUE;
    
    NSOperationQueue *queue = [[NSOperationQueue alloc] init];
    [self setManagerUpdateInterval:options];
    [self->_motionGyro startGyroUpdatesToQueue:queue withHandler:^(CMGyroData *gyroData, NSError *error) {
        
        CMRotationRate rotate = gyroData.rotationRate;
        self->_numberGyroX = [NSNumber numberWithDouble:rotate.x];
        self->_numberGyroY = [NSNumber numberWithDouble:rotate.y];
        self->_numberGyroZ = [NSNumber numberWithDouble:rotate.z];
            
        if (callback) {
            if (error)
                callback([self makeRetValue:[error localizedDescription]], YES);
            else
                callback([self makeRetValue:NULL], YES);
        }
    }];
    //Start montion for calc
    [self startMotion];
}

- (void)getGyroValue:(UniModuleKeepAliveCallback)callback {
    if (callback) {
        if(!self->_status) callback([self makeRetValue:@"The listener is not running."], NO);
        else callback([self makeRetValue:NULL], NO);
    }
}

- (NSDictionary *)getGyroValueSync {
    if(!self->_status) return[self makeRetValue:@"The listener is not running."];
    else return [self makeRetValue:NULL];
}

- (void)stopGyro:(UniModuleKeepAliveCallback)callback {
    
    if(!self->_status) {
        if(callback)
            callback([self makeRetValue:@"The listener is not running."], NO);
        return;
    }
    
    [self checkAndCreateManager];
    
    [_motionGyro stopGyroUpdates];
    [_motionGyro stopDeviceMotionUpdates];
    
    if(callback)
        callback([self makeSuccessRetValue], NO);
}

@end
