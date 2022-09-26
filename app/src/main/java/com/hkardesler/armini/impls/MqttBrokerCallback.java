/*
 * *
 *  * Created by Alper Kardesler on 15.07.2022 15:45
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.impls;

public interface MqttBrokerCallback {
    void onComplete();
    void onError();
}
