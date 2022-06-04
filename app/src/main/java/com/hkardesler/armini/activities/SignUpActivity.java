/*
 * *
 *  * Created by Haydar Kardesler on 19.05.2022 19:01
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 20.05.2022 22:42
 *
 */

package com.hkardesler.armini.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

public class SignUpActivity extends AppCompatActivity {

    ConstraintLayout layout_sign_up, layout_secret_key;
    ImageView btnBack;
    EditText edtSecretKey, edtFullName, edtEmail, edtPassword, edtConfirmPassword;
    Button btnContinue, btnSignUp;
    private FirebaseAuth mAuth;
    RelativeLayout layout_main;
    ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        layout_main = binding.layoutMain;
        layout_sign_up = binding.layoutSignUp;
        layout_secret_key = binding.layoutSecretKey;
        edtSecretKey = binding.inputSecretKey;
        edtFullName = binding.inputFullName;
        edtEmail = binding.inputEmail;
        edtPassword = binding.inputPassword;
        edtConfirmPassword = binding.inputPassConfirm;
        btnContinue = binding.btnContinue;
        btnSignUp = binding.btnSignUp;
        btnBack = binding.btnBack;
        mAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(edtSecretKey.getText().toString().equals(Global.SECRET_KEY)){
                    dismissKeyboard();
                    layout_secret_key.setVisibility(View.GONE);
                    layout_sign_up.setVisibility(View.VISIBLE);
                }else{
                    AppUtils.showToastMessage(SignUpActivity.this, getString(R.string.secret_key_error), R.drawable.ic_close, R.color.red);
                }
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        edtSecretKey.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId== EditorInfo.IME_ACTION_DONE ))
                {
                    edtSecretKey.clearFocus();
                    dismissKeyboard();
                    btnBack.clearFocus();

                    return true;
                }
                return false;
            }
        });


    }

    private void registerUser(){
        AppUtils.showLoading(SignUpActivity.this, layout_main);
        String fullName = edtFullName.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if(fullName.isEmpty()){
            edtFullName.setError(getString(R.string.fullname_warning));
            edtFullName.requestFocus();
            return;
        }

        if(email.isEmpty()){
            edtEmail.setError(getString(R.string.email_warning));
            edtEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            edtEmail.setError(getString(R.string.valid_email_error));
            edtEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            edtPassword.setError(getString(R.string.pass_warning));
            edtPassword.requestFocus();
            return;
        }

        if( confirmPassword.isEmpty()){
            edtConfirmPassword.setError(getString(R.string.pass_warning));
            edtConfirmPassword.requestFocus();
            return;
        }

        if(!password.equals(confirmPassword)){
            edtConfirmPassword.setError(getString(R.string.password_match_error));
            edtConfirmPassword.requestFocus();
            return;
        }

        if(password.length() < 6){
            edtPassword.setError(getString(R.string.password_min_char_error));
            edtPassword.requestFocus();
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
                                AppUtils.hideLoading(SignUpActivity.this, layout_main);
                                finish();
                            }else{
                                AppUtils.hideLoading(SignUpActivity.this, layout_main);
                                AppUtils.showToastMessage(SignUpActivity.this,getString(R.string.register_error), R.drawable.ic_close, R.color.red);
                            }
                        }
                    });
                }else{
                    AppUtils.hideLoading(SignUpActivity.this, layout_main);
                    AppUtils.showToastMessage(SignUpActivity.this,getString(R.string.register_error), R.drawable.ic_close, R.color.red);
                }

            }
        });
    }


    public void dismissKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtSecretKey.getWindowToken(), 0);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}