/*
 * *
 *  * Created by Haydar Kardesler on 20.05.2022 22:48
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 20.05.2022 22:43
 *
 */

package com.hkardesler.armini.helpers;

import com.hkardesler.armini.models.ControllerMode;
import com.hkardesler.armini.models.MotorSpeed;

public class Global {

    public static final String SECRET_KEY = "123456";
    public static final String SHARED_PREFERENCES_ID = "com.hkardesler.armini.shared.prefs";
    public static final String USER_KEY = "user";
    public static final String REMEMBER_ME_KEY = "rememberMe";
    public static final String FIRST_SESSION_KEY = "firstSession";
    public static final String SIGNED_IN_KEY = "signedIn";
    public static final String APP_LANGUAGE_ID_KEY = "appLanguageId";
    public static final String REFRESH_MAIN_ACTIVITY_KEY = "refreshMainActivity";
    public static final String REFRESH_PROFILE_PHOTO = "refreshProfilePhoto";
    public static final String SCENARIO_KEY = "scenario";
    public static final String SCENARIO_ID_KEY = "scenarioId";
    public static final String SCENARIO_DELETED_KEY = "scenarioDeleted";
    public static final String POSITION_KEY = "position";
    public static final String NEW_POSITION_KEY = "newPosition";
    public static final String POSITION_DELETED_KEY = "positionDeleted";

    public static final String FIREBASE_USERS_KEY = "users";
    public static final String FIREBASE_FULL_NAME_KEY = "fullName";
    public static final String FIREBASE_EMAIL_KEY = "email";
    public static final String FIREBASE_SCENARIOS_KEY = "scenarios";
    public static final String FIREBASE_SCENARIO_NAME_KEY = "name";
    public static final String FIREBASE_WORKING_MODE_KEY = "workingMode";
    public static final String FIREBASE_LOOP_COUNT_KEY = "loopCount";
    public static final String FIREBASE_LIGHT_OPEN_KEY = "isLightOpen";
    public static final String FIREBASE_USER_IMAGES_KEY = "userImages";
    public static final String FIREBASE_POSITIONS_KEY = "positions";
    public static final String FIREBASE_BASE_KEY = "base";
    public static final String FIREBASE_SHOULDER_KEY = "shoulder";
    public static final String FIREBASE_ELBOW_VER_KEY = "elbowVertical";
    public static final String FIREBASE_ELBOW_HOR_KEY = "elbowHorizontal";
    public static final String FIREBASE_WRIST_VER_KEY = "wristVertical";
    public static final String FIREBASE_WRIST_HOR_KEY = "wristHorizontal";
    public static final String FIREBASE_GRIPPER_KEY = "gripper";
    public static final String FIREBASE_MOTOR_SPEED_KEY = "motorSpeed";

    public static final String MOTOR_SPEED_POSITION_KEY = "motorSpeedPosition";
    public static final String MOTOR_SPEED_MANUAL_KEY = "motorSpeedManual";
    public static final String CONTROLLER_MODE_POSITION_KEY = "controllerModePosition";
    public static final String CONTROLLER_MODE_MANUAL_KEY = "controllerModeManual";

    public static MotorSpeed MOTOR_SPEED_POSITION_VALUE = MotorSpeed.NORMAL;
    public static MotorSpeed MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.NORMAL;
    public static ControllerMode CONTROLLER_MODE_POSITION_VALUE = ControllerMode.SLIDER;
    public static ControllerMode CONTROLLER_MODE_MANUAL_VALUE = ControllerMode.JOYSTICK;


}
