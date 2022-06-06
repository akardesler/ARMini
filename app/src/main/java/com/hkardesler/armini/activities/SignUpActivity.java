/*
 * *
 *  * Created by Haydar Kardesler on 19.05.2022 19:01
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 20.05.2022 22:42
 *
 */

package com.hkardesler.armini.activities;

import androidx.annotation.NonNull;
import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.hkardesler.armini.databinding.ActivitySignUpBinding;
import com.hkardesler.armini.helpers.AppUtils;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.R;

import java.util.Objects;

public class SignUpActivity extends BaseActivity {

    ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void setListeners() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(binding.inputSecretKey.getText().toString().equals(Global.SECRET_KEY)){
                    dismissKeyboard();
                    binding.layoutSecretKey.setVisibility(View.GONE);
                    binding.layoutSignUp.setVisibility(View.VISIBLE);
                }else{
                    AppUtils.showToastMessage(SignUpActivity.this, getString(R.string.secret_key_error), R.drawable.ic_close, R.color.red);
                }
            }
        });

        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        binding.inputSecretKey.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId== EditorInfo.IME_ACTION_DONE ))
                {
                    binding.inputSecretKey.clearFocus();
                    dismissKeyboard();
                    binding.btnBack.clearFocus();

                    return true;
                }
                return false;
            }
        });
    }

    private void registerUser(){
        AppUtils.showLoading(SignUpActivity.this, binding.layoutMain);
        String fullName = binding.inputFullName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();
        String confirmPassword = binding.inputPassConfirm.getText().toString().trim();

        if(fullName.isEmpty()){
            binding.inputFullName.setError(getString(R.string.fullname_warning));
            binding.inputFullName.requestFocus();
            return;
        }

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

        if( confirmPassword.isEmpty()){
            binding.inputPassConfirm.setError(getString(R.string.pass_warning));
            binding.inputPassConfirm.requestFocus();
            return;
        }

        if(!password.equals(confirmPassword)){
            binding.inputPassConfirm.setError(getString(R.string.password_match_error));
            binding.inputPassConfirm.requestFocus();
            return;
        }

        if(password.length() < 6){
            binding.inputPassword.setError(getString(R.string.password_min_char_error));
            binding.inputPassword.requestFocus();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                    FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY).child(userId).child(Global.FIREBASE_FULL_NAME_KEY).setValue(fullName);
                    FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY).child(userId).child(Global.FIREBASE_EMAIL_KEY).setValue(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                AppUtils.showToastMessage(SignUpActivity.this,getString(R.string.register_success), R.drawable.ic_done, R.color.green_500);
                                AppUtils.hideLoading(SignUpActivity.this, binding.layoutMain);
                                finish();
                            }else{
                                AppUtils.hideLoading(SignUpActivity.this, binding.layoutMain);
                                AppUtils.showToastMessage(SignUpActivity.this,getString(R.string.register_error), R.drawable.ic_close, R.color.red);
                            }
                        }
                    });
                }else{
                    AppUtils.hideLoading(SignUpActivity.this, binding.layoutMain);
                    AppUtils.showToastMessage(SignUpActivity.this,getString(R.string.register_error), R.drawable.ic_close, R.color.red);
                }

            }
        });
    }


    public void dismissKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.inputSecretKey.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}