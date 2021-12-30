//
//  TestModule.h
//  DCTestUniPlugin
//
//  Created by XHY on 2020/4/22.
//  Copyright © 2020 DCloud. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreMotion/CoreMotion.h>
#import "DCUniModule.h"

NS_ASSUME_NONNULL_BEGIN

@interface GyroModule : DCUniModule

@property (strong, nonatomic) CMMotionManager *motionGyro;
@property (strong, nonatomic) NSNumber *numberGyroX;
@property (strong, nonatomic) NSNumber *numberGyroY;
@property (strong, nonatomic) NSNumber *numberGyroZ;
@property BOOL status;

@end

NS_ASSUME_NONNULL_END
