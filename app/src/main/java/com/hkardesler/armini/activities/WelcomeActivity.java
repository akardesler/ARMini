/*
 * *
 *  * Created by Alper Kardesler on 22.05.2022 05:53
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 22.05.2022 05:53
 *
 */

package com.hkardesler.armini.activities;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.TextView;

import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityWelcomeBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;

import java.util.Locale;

public class WelcomeActivity extends AppCompatActivity {

    ActivityWelcomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        } else {
            locale = Resources.getSystem().getConfiguration().locale;
        }

        TypedArray languages = getResources().obtainTypedArray(R.array.language_code);
        int languageIndex = 0;

        for(int i = 0; i < languages.length(); i++ ){
            if(locale.getLanguage().contentEquals(languages.getText(i).toString())){
                languageIndex = i;
                break;
            }
        }
        AppUtils.putInt(this, Global.APP_LANGUAGE_ID_KEY, languageIndex);
        AppUtils.setLocale(this, languages.getText(languageIndex).toString());

        binding = ActivityWelcomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().getDecorView().getWindowInsetsController().hide(
                    WindowInsets.Type.statusBars()
            );
        }else{
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }


        binding.btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = AppUtils.getPrefs(WelcomeActivity.this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(Global.FIRST_SESSION_KEY, false);
                editor.apply();

                Intent i = new Intent(WelcomeActivity.this, SignInActivity.class);
                startActivity(i);
                finish();
            }
        });

        AppUtils.addTextGradient(binding.txtTitle, getString(R.string.welcome));

        String descText = getString(R.string.welcome_desc);
        SpannableStringBuilder str = new SpannableStringBuilder(descText);
        String appName = getString(R.string.app_name);
        str.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), descText.indexOf(appName), descText.indexOf(appName)+6, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.txtDesc.setText(str);
    }

}