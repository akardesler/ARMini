/*
 * *
 *  * Created by Alper Kardesler on 20.05.2022 22:48
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.helpers;
import com.hkardesler.armini.models.ControllerModeEnum;
import com.hkardesler.armini.models.MotorSpeedEnum;

public class Global {

    public static final String MQTT_HOST = "b35fc3f300ac47009225f3a7df3098b9.s1.eu.hivemq.cloud";
    public static final int MQTT_PORT = 8883;
    public static final String MQTT_USERNAME = "hkardesler";
    public static final String MQTT_PASSWORD = "Haydar.Kardesler19";
    public static final String MQTT_JOYSTICK_1_TOPIC = "armini/joystick1";
    public static final String MQTT_JOYSTICK_2_TOPIC = "armini/joystick2";
    public static final String MQTT_JOYSTICK_3_TOPIC = "armini/joystick3";
    public static final String MQTT_GRIPPER_TOPIC = "armini/gripper";
    public static final String MQTT_SERVO_POSITIONS_TOPIC = "armini/servopositions";
    public static final String MQTT_MOTOR_SPEED_TOPIC = "armini/motorspeed";
    public static final String MQTT_FUNCTIONS_TOPIC = "armini/functions";
    public static final String MQTT_TRY_POSITION_TOPIC = "armini/tryposition";
    public static final String MQTT_RUN_SCENARIO_TOPIC = "armini/runscenario";
    public static final String MQTT_STOP_SCENARIO_TOPIC = "armini/stopscenario";
    public static final String MQTT_GO_TO_POSITION_TOPIC = "armini/gotoposition";
    public static final String MQTT_FUNCTIONS_GO_TO_HOME_KEY = "goToHome";
    public static final String MQTT_FUNCTIONS_ON_LIGHT_KEY = "onLight";
    public static final String MQTT_FUNCTIONS_OFF_LIGHT_KEY = "offLight";
    public static final String MQTT_ARM_STATUS_REQUEST_TOPIC = "armini/armStatusRequest";
    public static final String MQTT_ARM_STATUS_CALLBACK_TOPIC = "armini/armStatusCallback";
    public static final String MQTT_SCENARIO_STARTED_CALLBACK_TOPIC = "armini/scenarioStartedCallback";

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

    public static final String FIREBASE_INFO_KEY = "info";
    public static final String FIREBASE_INFO_ARM_STATUS_KEY = "armStatus";
    public static final String FIREBASE_INFO_OPERATOR_ID_KEY = "operatorId";
    public static final String FIREBASE_INFO_SCENARIO_ID_KEY = "scenarioId";
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
    public static final String FIREBASE_WAITING_TIME_KEY = "waitingTime";

    public static final String MOTOR_SPEED_POSITION_KEY = "motorSpeedPosition";
    public static final String MOTOR_SPEED_MANUAL_KEY = "motorSpeedManual";
    public static final String CONTROLLER_MODE_POSITION_KEY = "controllerModePosition";
    public static final String CONTROLLER_MODE_CHANGED = "controllerModeChanged";

    public static MotorSpeedEnum MOTOR_SPEED_POSITION_VALUE = MotorSpeedEnum.NORMAL;
    public static MotorSpeedEnum MOTOR_SPEED_MANUAL_VALUE = MotorSpeedEnum.NORMAL;
    public static ControllerModeEnum CONTROLLER_MODE_POSITION_VALUE = ControllerModeEnum.SLIDER;
    public static int POSITION_WAITING_TIME_VALUE = 100;
    public static boolean IS_USER_OPERATOR = false;

}
