/*
 * *
 *  * Created by Haydar Kardesler on 22.05.2022 04:28
 *  * Copyright (c) 2022 . All rights reserved.
 *  * Last modified 22.05.2022 04:28
 *
 */

package com.hkardesler.armini.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityResetPasswordBinding;
import com.hkardesler.armini.helpers.AppUtils;

public class ResetPasswordActivity extends AppCompatActivity {

    EditText edtEmail;
    Button btnSend;
    ImageView btnBack;
    private FirebaseAuth mAuth;
    ActivityResetPasswordBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        edtEmail = binding.inputEmail;
        btnBack = binding.btnBack;
        btnSend = binding.btnSend;
        mAuth = FirebaseAuth.getInstance();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edtEmail.getText().toString().trim();
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

                AppUtils.hideSoftKeyboard(ResetPasswordActivity.this);
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    AppUtils.showToastMessage(ResetPasswordActivity.this,getString(R.string.email_sent), R.drawable.ic_info, R.color.blue_500);
                                }else{
                                    AppUtils.showToastMessage(ResetPasswordActivity.this,getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);

                                }
                            }
                        });
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}