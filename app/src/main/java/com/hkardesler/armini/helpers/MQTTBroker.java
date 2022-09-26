/*
 * *
 *  * Created by Alper Kardesler on 15.07.2022 15:36
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.helpers;

import static java.nio.charset.StandardCharsets.UTF_8;
import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hkardesler.armini.impls.MqttBrokerCallback;

import java.util.concurrent.Callable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MQTTBroker {
    private MqttBrokerCallback callback;
    private Mqtt5BlockingClient client;

    public MQTTBroker() {
        connectMQTTBroker();
    }

    public void setCallback(MqttBrokerCallback listener) {
        callback = listener;
    }

    private void connectMQTTBroker(){
        Observable.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        client = MqttClient.builder()
                                .useMqttVersion5()
                                .serverHost(Global.MQTT_HOST)
                                .serverPort(Global.MQTT_PORT)
                                .sslWithDefaultConfig()
                                .buildBlocking();

                        client.connectWith()
                                .simpleAuth()
                                .username(Global.MQTT_USERNAME)
                                .password(UTF_8.encode(Global.MQTT_PASSWORD))
                                .applySimpleAuth()
                                .send();
                        return true;
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError();
                    }

                    @Override
                    public void onComplete() {
                        callback.onComplete();
                    }
                });
    }

    public Mqtt5BlockingClient getClient(){
        return client;
    }

}
