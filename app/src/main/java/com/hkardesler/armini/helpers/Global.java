/*
 * *
 *  * Created by Haydar Kardesler on 20.05.2022 22:48
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 20.05.2022 22:43
 *
 */

package com.hkardesler.armini.helpers;

import com.hkardesler.armini.models.ArmStatus;
import com.hkardesler.armini.models.ControllerMode;
import com.hkardesler.armini.models.MotorSpeed;

public class Global {

    public static final String MQTT_HOST = "b35fc3f300ac47009225f3a7df3098b9.s1.eu.hivemq.cloud";
    public static final String MQTT_USERNAME = "hkardesler";
    public static final String MQTT_PASSWORD = "Haydar.Kardesler19";
    public static final String MQTT_JOYSTICK_1_TOPIC = "armini/joystick1";
    public static final String MQTT_JOYSTICK_2_TOPIC = "armini/joystick2";
    public static final String MQTT_JOYSTICK_3_TOPIC = "armini/joystick3";
    public static final String MQTT_GRIPPER_TOPIC = "armini/gripper";
    public static final String MQTT_SERVO_POSITIONS_TOPIC = "armini/servopositions";
    public static final String MQTT_MOTOR_SPEED_TOPIC = "armini/motorspeed";
    public static final String MQTT_FUNCTIONS_TOPIC = "armini/functions";
    public static final String MQTT_FUNCTIONS_GO_TO_HOME_KEY = "goToHome";
    public static final String MQTT_FUNCTIONS_ON_LIGHT_KEY = "onLight";
    public static final String MQTT_FUNCTIONS_OFF_LIGHT_KEY = "offLight";

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
    public static final String DELETE_POSITION_ID = "deletePositionId";

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
    public static final String CONTROLLER_MODE_CHANGED = "controllerModeChanged";

    public static MotorSpeed MOTOR_SPEED_POSITION_VALUE = MotorSpeed.NORMAL;
    public static MotorSpeed MOTOR_SPEED_MANUAL_VALUE = MotorSpeed.NORMAL;
    public static ControllerMode CONTROLLER_MODE_POSITION_VALUE = ControllerMode.SLIDER;
    public static ArmStatus ARMINI_STATUS = ArmStatus.OFFLINE;
    public static boolean IS_USER_OPERATOR = false;



}
