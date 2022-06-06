/*
 * *
 *  * Created by Haydar Kardesler on 1.06.2022 00:27
 *  * Copyright (c) 2022 . All rights reserved.
 *
 */

package com.hkardesler.armini.activities;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.hkardesler.armini.R;
import com.hkardesler.armini.databinding.ActivityProfileBinding;
import com.hkardesler.armini.helpers.Global;
import com.hkardesler.armini.adapters.LanguageListViewAdapter;
import com.hkardesler.armini.helpers.RelativePopupWindow;
import com.hkardesler.armini.helpers.AppUtils;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends BaseActivity {
    private ActivityProfileBinding binding;
    private RelativePopupWindow relativePopupWindow;
    TypedArray langDrawableIds;
    TypedArray langNames;
    int appLanguageId;
    boolean refreshMainActivity = false;
    boolean refreshProfilePhoto = false;
    private BottomSheetBehavior<View> mBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private Uri photoUri;
    ActivityResultLauncher<Intent> takePhotoResultLauncher;
    ActivityResultLauncher<String[]> galleryActivityLauncher;
    boolean userHasImage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.setUser(user);
        relativePopupWindow = new RelativePopupWindow(this);
        langDrawableIds = getResources().obtainTypedArray(R.array.language_image);
        langNames = getResources().obtainTypedArray(R.array.language_name);
        appLanguageId = prefs.getInt(Global.APP_LANGUAGE_ID_KEY, 0);
        binding.txtLang.setText(langNames.getText(appLanguageId));
        binding.imgFlag.setImageDrawable(langDrawableIds.getDrawable(appLanguageId));
        storageReference = FirebaseStorage.getInstance().getReference();
        getUserImageFromFirebase();
        View bottom_sheet = findViewById(R.id.bottom_sheet);
        mBehavior = BottomSheetBehavior.from(bottom_sheet);
        initActivityResultLauncher();
        setListeners();
    }

    @Override
    protected void setListeners() {
        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishActivity();
            }
        });

        binding.rltLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLangDropList();
            }
        });

        binding.btnEditPp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheetDialog();
            }
        });

        binding.btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!binding.inputCurrentPassword.getText().toString().trim().isEmpty()){
                    if(binding.inputCurrentPassword.getText().toString().equals(user.getPassword()) && binding.inputNewPassword.getText().toString().equals(binding.inputNewPasswordConfirm.getText().toString())){
                        FirebaseUser userFirebase = FirebaseAuth.getInstance().getCurrentUser();

                        if(userFirebase == null)
                            return;

                        userFirebase.updatePassword(binding.inputNewPassword.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.profile_updated), R.drawable.ic_done, R.color.green_500);
                                            user.setPassword(binding.inputNewPassword.getText().toString());
                                            AppUtils.setUser(ProfileActivity.this, user);
                                        }else{
                                            AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
                                        }
                                    }
                                });

                        if(!binding.inputFullName.getText().toString().equals(user.getFullName())){
                            FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY).child(user.getUserId())
                                    .child(Global.FIREBASE_FULL_NAME_KEY).setValue(binding.inputFullName.getText().toString());

                            user.setFullName(binding.inputFullName.getText().toString());
                            AppUtils.setUser(ProfileActivity.this, user);
                        }

                    }else{
                        AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
                    }
                }else if(!binding.inputFullName.getText().toString().equals(user.getFullName())){
                    FirebaseDatabase.getInstance().getReference(Global.FIREBASE_USERS_KEY).child(user.getUserId())
                            .child(Global.FIREBASE_FULL_NAME_KEY).setValue(binding.inputFullName.getText().toString());
                    user.setFullName(binding.inputFullName.getText().toString());
                    AppUtils.setUser(ProfileActivity.this, user);
                    AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.profile_updated), R.drawable.ic_done, R.color.green_500);
                }
            }
        });
    }


    private void showLangDropList() {

        if (relativePopupWindow.isShowing()) {
            relativePopupWindow.dismiss();
            return;
        }
        View v = ((LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_dropdown, null, false);
        relativePopupWindow.setContentView(v);
        relativePopupWindow.setWidth((getResources().getDisplayMetrics().widthPixels));
        relativePopupWindow.setHeight(400);
        relativePopupWindow.setFocusable(true);
        relativePopupWindow.setOutsideTouchable(true);
        relativePopupWindow.setElevation(20.0f);

        ListView lv = v.findViewById(R.id.spinlist);
        lv.setAdapter(new LanguageListViewAdapter(this, langNames, langDrawableIds));
        lv.setOnItemClickListener((adapterView, view, pos, l) -> {
            binding.txtLang.setText(langNames.getText(pos));
            binding.imgFlag.setImageDrawable(langDrawableIds.getDrawable(pos));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(Global.APP_LANGUAGE_ID_KEY, pos);
            editor.apply();
            appLanguageId = pos;

            String langCode = getResources().obtainTypedArray(R.array.language_code).getText(appLanguageId).toString();
            AppUtils.setLocale(this, langCode);
            refreshMainActivity = true;
            updateStrings();
            relativePopupWindow.dismiss();


        });
        lv.setSelection(appLanguageId);


        relativePopupWindow.showOnAnchor(binding.rltLang, -1, 0);

    }
    private void updateStrings(){
        binding.inputFullName.setHint(getString(R.string.full_name));
        binding.inputEmail.setHint(getString(R.string.email_hint));
        binding.txtLanguage.setText(getString(R.string.language));
        binding.txtChangePass.setText(getString(R.string.change_pass));
        binding.inputCurrentPassword.setHint(getString(R.string.current_pass));
        binding.inputNewPassword.setHint(getString(R.string.new_pass));
        binding.inputNewPasswordConfirm.setHint(getString(R.string.confirm_new_pass));
        binding.btnUpdate.setText(getString(R.string.update));
    }
    @Override
    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    private void finishActivity(){
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Global.REFRESH_MAIN_ACTIVITY_KEY, refreshMainActivity);
        resultIntent.putExtra(Global.REFRESH_PROFILE_PHOTO, refreshProfilePhoto);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void showBottomSheetDialog() {
        if (mBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View view = getLayoutInflater().inflate(R.layout.layout_profile_photo_menu, null);

        if(userHasImage){
            view.findViewById(R.id.ln_remove).setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.ln_take_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
                mBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.ln_gallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                galleryActivityLauncher.launch(new String[]{"image/*"});
                mBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.ln_remove)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteImageFromFirebase();
                mBottomSheetDialog.dismiss();
            }
        });

        ((View) view.findViewById(R.id.ln_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.dismiss();
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;
            }
        });
    }

    public void takePhoto() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA ) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.CAMERA}, 1000);
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);


        takePhotoResultLauncher.launch(cameraIntent);

    }

    private void initActivityResultLauncher() {
         takePhotoResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if(result.getResultCode() == Activity.RESULT_OK){
                        binding.imgProfilePhoto.setImageURI(photoUri);
                        uploadImageToFirebase(photoUri);

                    }
                });

       galleryActivityLauncher = registerForActivityResult(new ActivityResultContracts.OpenDocument(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                if (result != null) {
                    binding.imgProfilePhoto.setImageURI(result);
                    uploadImageToFirebase(result);
                }
            }
        });
    }

    private void uploadImageToFirebase(Uri imageUri){
        binding.progressBar.setVisibility(View.VISIBLE);

        StorageReference fileRef = storageReference.child(Global.FIREBASE_USER_IMAGES_KEY).child(user.getUserId()+".jpg");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.profile_photo_updated), R.drawable.ic_done, R.color.green_500);
                binding.progressBar.setVisibility(View.GONE);
                userHasImage = true;
                refreshProfilePhoto = true;

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);
                binding.progressBar.setVisibility(View.GONE);

            }
        });
    }

    private void getUserImageFromFirebase(){

        StorageReference fileRef = storageReference.child(Global.FIREBASE_USER_IMAGES_KEY).child(user.getUserId()+".jpg");

        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(binding.imgProfilePhoto);
                userHasImage = true;
                binding.progressBar.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.imgProfilePhoto.setImageDrawable(AppCompatResources.getDrawable(ProfileActivity.this, R.drawable.ic_profile_person));
                binding.progressBar.setVisibility(View.GONE);

            }
        });
    }

    private void deleteImageFromFirebase(){
        StorageReference fileRef = storageReference.child(Global.FIREBASE_USER_IMAGES_KEY).child(user.getUserId()+".jpg");

        fileRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    binding.imgProfilePhoto.setImageDrawable(AppCompatResources.getDrawable(ProfileActivity.this, R.drawable.ic_profile_person));

                    AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.profile_photo_updated), R.drawable.ic_done, R.color.green_500);
                    userHasImage = false;
                    refreshProfilePhoto = true;

                }else{
                    AppUtils.showToastMessage(ProfileActivity.this,getString(R.string.sth_wrong), R.drawable.ic_close, R.color.red);

                }
            }
        });
    }
}