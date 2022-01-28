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

- (void)checkAndCreateManager {
    if (!_motionGyro)
        _motionGyro = [[CMMotionManager alloc] init];
}
- (void)setManagerUpdateInterval:(NSDictionary *)options {
    NSString *interval = [options objectForKey:@"interval"];
    if(interval) {
        NSArray *items = @[@"normal", @"ui", @"game", @"fastest"];
        long item = [items indexOfObject:interval];
        switch (item)
        {
            default:
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
- (NSDictionary *)makeRetValue:(NSString*)err {
    if(err) {
        return [NSDictionary dictionaryWithObjectsAndKeys:
                @"", @"success",
                err, @"errMsg", nil];
    } else {
        return [NSDictionary dictionaryWithObjectsAndKeys:
                @"true", @"success",
                @"ok", @"errMsg",
                _numberGyroX, @"x",
                _numberGyroY, @"y",
                _numberGyroZ, @"z",
                nil];
    }
}
- (NSDictionary *)makeSuccessRetValue {
    return [NSDictionary dictionaryWithObjectsAndKeys:
            @"ok",@"errMsg",
            @"true", @"success", nil];
}

- (NSString *)isGyroAvailable {
    [self checkAndCreateManager];
    if ([_motionGyro isGyroAvailable])
        return @"true";
    else
        return @"";
}
- (NSString *)isGyroStarted {
    [self checkAndCreateManager];
    if (_status)
        return @"true";
    else
        return @"";
}

- (void)startGyro:(NSDictionary *)options
         callback:(UniModuleKeepAliveCallback)callback {
    [self checkAndCreateManager];
    
    if(self->_status) {
        if(callback)
            callback([self makeRetValue:@"The listener is running. Please call stopGyro stop it first!"], NO);
        return;
    }
    if(![_motionGyro isGyroAvailable]) {
        if(callback)
            callback([self makeRetValue:@"Gyroscope is not available!"], NO);
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
    
    if(callback)
        callback([self makeSuccessRetValue], NO);
}

- (void)startGyroWithCallback:(NSDictionary *)options
                     callback:(UniModuleKeepAliveCallback)callback{
    [self checkAndCreateManager];
    
    if(self->_status) {
        if(callback)
            callback([self makeRetValue:@"The listener is running. Please call stopGyro stop it first!"], NO);
        return;
    }
    if(![_motionGyro isGyroAvailable]) {
        if(callback)
            callback([self makeRetValue:@"Gyroscope is not available!"], NO);
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
    if(callback)
        callback([self makeSuccessRetValue], NO);
}

@end
