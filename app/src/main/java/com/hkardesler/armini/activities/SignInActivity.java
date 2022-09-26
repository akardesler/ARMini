/*
 * *
 *  * Created by Alper Kardesler on 19.05.2022 18:35
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 20.05.2022 22:42
 *
 */

package com.hkardesler.armini.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivitySignInBinding;
import com.hkardesler.armini.models.User;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;

import java.util.Objects;

public class SignInActivity extends BaseActivity {

    boolean isRemember;
    private DatabaseReference mDatabase;
    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mDatabase = firebaseDatabase.getReference();
        isRemember = prefs.getBoolean(Global.REMEMBER_ME_KEY, false);
        binding.chkRemember.setChecked(isRemember);

        int appLanguageId = prefs.getInt(Global.APP_LANGUAGE_ID_KEY, 0);
        if(appLanguageId != 0){
            binding.txtLoginAppName.setVisibility(View.GONE);
        }else{
            binding.txtLoginAppName.setVisibility(View.VISIBLE);
        }

        if(isRemember){
            if(user != null){
                binding.inputEmail.setText(user.getEmail());
                binding.inputPassword.setText(user.getPassword());
            }
        }
    }

    @Override
    protected void setListeners() {
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });

        binding.btnForgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(SignInActivity.this, ResetPasswordActivity.class);
                startActivity(i);
            }
        });

        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
    }

    private void signIn(){
        AppUtils.showLoading(SignInActivity.this, binding.layoutMain);
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        if(email.isEmpty()){
            binding.inputEmail.setError(getString(R.string.email_warning));
            binding.inputEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.inputEmail.setError(getString(R.string.valid_email_error));
            binding.inputEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            binding.inputPassword.setError(getString(R.string.pass_warning));
            binding.inputPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    mDatabase.child("users").child(userId).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                            if (task.isSuccessful()) {
                                DataSnapshot data = task.getResult();
                                AppUtils.putBool(SignInActivity.this,Global.SIGNED_IN_KEY, true);
                                AppUtils.putBool(SignInActivity.this,Global.REMEMBER_ME_KEY, binding.chkRemember.isChecked());
                                User user_login = new User(userId, data.child(Global.FIREBASE_FULL_NAME_KEY).getValue(String.class), data.child(Global.FIREBASE_EMAIL_KEY).getValue(String.class), binding.inputPassword.getText().toString());
                                AppUtils.setUser(SignInActivity.this, user_login);

                                Intent i = new Intent(SignInActivity.this, MainActivity.class);

                                startActivity(i);
                                finish();
                                AppUtils.hideLoading(SignInActivity.this, binding.layoutMain);

                            }
                            else {
                                AppUtils.hideLoading(SignInActivity.this, binding.layoutMain);

                                AppUtils.showToastMessage(SignInActivity.this, getString(R.string.sign_in_error), R.drawable.ic_close, R.color.red);
                            }
                        }
                    });
                }else {
                    AppUtils.hideLoading(SignInActivity.this, binding.layoutMain);
                    AppUtils.showToastMessage(SignInActivity.this, getString(R.string.sign_in_error), R.drawable.ic_close, R.color.red);
                }
            }
        });
    }


}