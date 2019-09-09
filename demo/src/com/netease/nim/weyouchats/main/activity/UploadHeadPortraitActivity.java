package com.netease.nim.weyouchats.main.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.bhm.sdk.bhmlibrary.views.TitleBar;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.netease.nim.uikit.business.session.actions.PickImageAction;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.media.imagepicker.Constants;
import com.netease.nim.uikit.common.media.imagepicker.ImagePickerLauncher;
import com.netease.nim.uikit.common.media.model.GLImage;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.weyouchats.R;
import com.netease.nim.weyouchats.config.preference.Preferences;
import com.netease.nim.weyouchats.contact.ContactHttpClient;
import com.netease.nim.weyouchats.contact.helper.UserUpdateHelper;
import com.netease.nim.weyouchats.login.LoginActivity;
import com.netease.nim.weyouchats.login.User;
import com.netease.nimlib.sdk.AbortableFuture;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallbackWrapper;
import com.netease.nimlib.sdk.ResponseCode;
import com.netease.nimlib.sdk.nos.NosService;
import com.netease.nimlib.sdk.uinfo.constant.UserInfoFieldEnum;

import java.io.File;
import java.util.ArrayList;

public class UploadHeadPortraitActivity extends UI {

    private final static int REQUEST_PICK_ICON = 104;
    private String imagePath = "";
    private User user;
    private AbortableFuture<String> uploadAvatarFuture = null;

    private TitleBar titleBar;
    private ImageView iv_head;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_head);
        initView();
        initEvent();
    }

    private void initView(){
        iv_head = findView(R.id.iv_head);
        titleBar = findView(R.id.titleBar);
        user = new Gson().fromJson(Preferences.getUserInfo(), User.class);
        imagePath = user.getIcon();
        Glide.with(this).load(user.getIcon()).into(iv_head);
    }

    private void initEvent(){
        titleBar.setLeftOnClickListener(v -> finish());

        if (!getIntent().getBooleanExtra("edit", false)) {
            titleBar.setRightText("");
        }
        titleBar.setRightOnClickListener(v -> {
            if (!getIntent().getBooleanExtra("edit", false)) {
                return;
            }
            ImagePickerLauncher.pickImage(UploadHeadPortraitActivity.this, REQUEST_PICK_ICON, R.string.head);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_ICON) {
            if (data == null) {
                return;
            }
            ArrayList<GLImage> images = (ArrayList<GLImage>) data.getSerializableExtra(Constants.EXTRA_RESULT_ITEMS);
            if (images == null || images.isEmpty()) {
                return;
            }
            GLImage image = images.get(0);
            imagePath = image.getPath();
            save();
        }
    }

    private void save(){
        File file = new File(imagePath);
        DialogMaker.showProgressDialog(this, null, null, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancelUpload(R.string.user_info_update_cancel);
            }
        }).setCanceledOnTouchOutside(true);

        new Handler().postDelayed(outimeTask, 30000);

        uploadAvatarFuture = NIMClient.getService(NosService.class).upload(file, PickImageAction.MIME_JPEG);
        uploadAvatarFuture.setCallback(new RequestCallbackWrapper<String>() {
            @Override
            public void onResult(int code, String url, Throwable exception) {
                if (code == ResponseCode.RES_SUCCESS && !TextUtils.isEmpty(url)) {
                    UserUpdateHelper.update(UserInfoFieldEnum.AVATAR, url, new RequestCallbackWrapper<Void>() {
                        @Override
                        public void onResult(int code, Void result, Throwable exception) {
                            if (code == ResponseCode.RES_SUCCESS) {
                                user.setIcon(url);
                                Preferences.saveUserInfo(new Gson().toJson(user));
                                Glide.with(UploadHeadPortraitActivity.this).load(url).into(iv_head);
                                ToastHelper.showToast(UploadHeadPortraitActivity.this, R.string.head_update_success);
                                //通过云信上传头像成功后通知后台同步云信的头像到服务器
                                updateUserinfoByYx();
                                onUpdateDone();
                            } else {
                                imagePath = "";
                                ToastHelper.showToast(UploadHeadPortraitActivity.this, R.string.head_update_failed);
                            }
                        }
                    }); // 更新资料
                } else {
                    imagePath = "";
                    ToastHelper.showToast(UploadHeadPortraitActivity.this, R.string.user_info_update_failed);
                    onUpdateDone();
                }
            }
        });
    }

    private void updateUserinfoByYx() {
        final String token = Preferences.getUserToken();
        ContactHttpClient.getInstance().updateUserinfoByYx(token, new ContactHttpClient.ContactHttpCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.e("999999","服务器同步云信成功");
            }

            @Override
            public void onFailed(int code, String errorMsg) {
                Log.e("999999","服务器同步云信失败==code==errorMsg"+code+errorMsg);
            }
        });
    }

    private void cancelUpload(int resId) {
        if (uploadAvatarFuture != null) {
            uploadAvatarFuture.abort();
            ToastHelper.showToast(UploadHeadPortraitActivity.this, resId);
            onUpdateDone();
        }
    }

    private Runnable outimeTask = () -> cancelUpload(R.string.user_info_update_failed);

    private void onUpdateDone() {
        uploadAvatarFuture = null;
        DialogMaker.dismissProgressDialog();
    }

    @Override
    public void finish() {
        Intent intent =  new Intent();
        intent.putExtra("head", imagePath);
        setResult(0, intent);
        super.finish();
    }
}
