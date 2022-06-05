/*
 * *
 *  * Created by Haydar Kardesler on 20.05.2022 22:48
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 20.05.2022 22:48
 *
 */

package com.hkardesler.armini.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Handler;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hkardesler.armini.R;
import com.hkardesler.armini.models.Position;
import com.hkardesler.armini.models.User;

import java.lang.reflect.Type;
import java.util.Locale;

import jp.wasabeef.blurry.Blurry;

public class AppUtils {

    public static SharedPreferences getPrefs(Context context){
        return context.getSharedPreferences(Global.SHARED_PREFERENCES_ID, Context.MODE_PRIVATE);
    }

    public static void setUser(Context context, User user){
        SharedPreferences prefs = getPrefs(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString(Global.USER_KEY, json);
        editor.apply();
    }

    public static User getUser(Context context){
        SharedPreferences prefs = getPrefs(context);
        Gson gson = new Gson();
        String json = prefs.getString(Global.USER_KEY, null);
        Type type = new TypeToken<User>() {
        }.getType();
        return gson.fromJson(json, type);
    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);

        if (activity.getCurrentFocus() == null) {
            return;
        }
        inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void showToastMessage(Activity activity, String message, int icon, int color) {
        Toast toast = new Toast(activity);
        toast.setDuration(Toast.LENGTH_LONG);
        View custom_view = activity.getLayoutInflater().inflate(R.layout.layout_toast, null);
        ((TextView) custom_view.findViewById(R.id.message)).setText(message);
        ((ImageView) custom_view.findViewById(R.id.icon)).setImageResource(icon);
        ((CardView) custom_view.findViewById(R.id.parent_view)).setCardBackgroundColor(activity.getResources().getColor(color));

        toast.setView(custom_view);
        toast.show();
    }

    public static void showLoading(Activity activity, ViewGroup layout){

        ViewGroup rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        layout.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                layout.getViewTreeObserver().removeOnPreDrawListener(this);
                Blurry.with(activity).radius(10).sampling(2).animate(50).onto(layout);
                return true;
            }
        });

        View v = activity.getLayoutInflater().inflate(R.layout.layout_loading, null);
        v.setTag("loadingLayout");
        rootView.addView(v);
    }

    public static void hideLoading(Activity activity, ViewGroup viewGroup){
        ViewGroup rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
        View layout = rootView.findViewWithTag("loadingLayout");
        Blurry.delete(viewGroup);
        if(layout != null){
            new Handler().postDelayed(() -> {
                rootView.removeView(layout);
            }, 50);
        }
    }

    public static int dpToPx(Context mContext,int dp) {
        Resources r = mContext.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    public static void setLocale(Activity activity, String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }
    public static void addTextGradient(TextView textView, String text) {
        TextPaint paint = textView.getPaint();
        float width = paint.measureText(text);

        Shader textShader = new LinearGradient(0, 0, width, textView.getTextSize(),
                new int[]{
                        Color.parseColor("#478AEA"),
                        Color.parseColor("#8446CC"),
                }, null, Shader.TileMode.CLAMP);
        textView.getPaint().setShader(textShader);

    }

    public static String convertJson(Object data){
        Gson gson = new Gson();
        return gson.toJson(data);
    }

    public static void homePosition(Position position){
        position.setBaseValue(90);
        position.setShoulderValue(0);
        position.setElbowVerticalValue(90);
        position.setElbowHorizontalValue(90);
        position.setWristVerticalValue(90);
        position.setWristHorizontalValue(90);
        position.setGripperValue(45);
    }
}
