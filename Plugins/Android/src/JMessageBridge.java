package com.example.unity3d_jpush_demo;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.unity3d.player.UnityPlayer;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * JMessage 插件类，如有需要可自定义修改，再重新生成 jar 包。
 */
public class JMessageBridge {
    private final String TAG = "JMessageBridge";
    private Activity mActivity;
    private Gson mGson;

    public static JMessageBridge instance = new JMessageBridge();
    public static String gameObjectName = "Main Camera";
    public static boolean isQuit = true;

    public JMessageBridge() {
        mGson = new Gson();
    }

    public static JMessageBridge getInstance() {
        if (instance == null) {
            instance = new JMessageBridge();
        }
        isQuit = false;
        return instance;
    }

    public void quit() {
        isQuit = true;
    }

    public void initJMessage(String gameObjectName) {
        if (TextUtils.isEmpty(gameObjectName)) {
            Log.e(TAG, "Game object name ");
        }
        JMessageBridge.gameObjectName = gameObjectName;
        JMessageClient.init(getActivity());
        JMessageClient.registerEventReceiver(this); // 注册消息处理事件。
    }

    public void registerEventReceiver() {
        JMessageClient.registerEventReceiver(this);
    }

    public void unregisterEventReceiver() {
        JMessageClient.unRegisterEventReceiver(this);
    }

    // 消息事件。
    public void onEvent(MessageEvent event) {
        Message msg = event.getMessage();
        String msgJson = mGson.toJson(msg);
        UnityPlayer.UnitySendMessage(gameObjectName, "onReceiveMessage", msgJson);

        switch (msg.getContentType()) {
            case eventNotification:
                EventNotificationContent content = (EventNotificationContent)
                        msg.getContent();
                switch (content.getEventNotificationType()) {
                    case group_member_added:    // 添加群成员。
                        UnityPlayer.UnitySendMessage(gameObjectName,
                                "onGroupMemberAdded", null);
                        break;
                    case group_member_removed:  // 移除群成员（只有被移除的用户能收到该事件）。
                        UnityPlayer.UnitySendMessage(gameObjectName,
                                "onGroupMemberRemoved", null);
                        break;
                    case group_member_exit:     // 退群。
                        UnityPlayer.UnitySendMessage(gameObjectName,
                                "onGroupMemberExit", null);
                        break;
                    default:
                }
                break;
        }
    }

    public void onEvent(LoginStateChangeEvent event) {
        LoginStateChangeEvent.Reason reason = event.getReason();
        switch (reason) {
            case user_password_change:
                UnityPlayer.UnitySendMessage(gameObjectName,
                        "onUserPasswordChanged", null);
                break;
            case user_logout:
                UnityPlayer.UnitySendMessage(gameObjectName,
                        "onUserLogout", null);
                break;
            case user_deleted:
                UnityPlayer.UnitySendMessage(gameObjectName,
                        "onUserDeleted", null);
                break;
            default:
        }
    }

    public void onEvent(NotificationClickEvent event) {
        Message msg = event.getMessage();
        String json = mGson.toJson(msg);
        UnityPlayer.UnitySendMessage(gameObjectName, "onOpenMessage", json);

        Intent intent = getActivity().getPackageManager()
                .getLaunchIntentForPackage(getActivity().getPackageName());
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        getActivity().startActivity(intent);
    }

    // Login and register API.
    public void register(String username, String password, final String successFuncName,
            final String errorFuncName) {
        JMessageClient.register(username, password, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                handleResult(status, desc, successFuncName, errorFuncName);
            }
        });
    }

    public void login(String username, String password, final String successFuncName,
            final String errorFuncName) {
        JMessageClient.login(username, password, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                handleResult(status, desc, successFuncName, errorFuncName);
            }
        });
    }

    public void logout() {
        JMessageClient.logout();
    }


    // User info API.
    public String getUserInfo(String username, String appKey) {
        if (appKey == null) {
            appKey = "";
        }
        final String[] result = new String[0];
        JMessageClient.getUserInfo(username, appKey, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, UserInfo userInfo) {
                if (status == 0) {
                    result[0] = mGson.toJson(userInfo);
                } else {
                    Log.w(TAG, "getUserInfo: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    public boolean updateUserInfo(String username, String appKey, final String field,
            final String value) {
        final boolean[] result = new boolean[1];
        JMessageClient.getUserInfo(username, appKey, new GetUserInfoCallback() {
            @Override
            public void gotResult(int status, String desc, UserInfo userInfo) {
                if (status == 0) {
                    result[0] = updateUserInfo(userInfo, field, value);
                } else {
                    result[0] = false;
                    Log.w(TAG, "updateUserInfo: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    public String getMyInfo() {
        UserInfo myInfo = JMessageClient.getMyInfo();
        if (myInfo != null) {
            return mGson.toJson(myInfo);
        }
        return null;
    }

    public boolean updateMyInfo(String field, String value){
        UserInfo myInfo = JMessageClient.getMyInfo();
        return updateUserInfo(myInfo, field, value);
    }

    private void handleResult(int status, String desc, String successFuncName,
            String errorFuncName) {
        if (status == 0) {  // 操作成功。
            UnityPlayer.UnitySendMessage(gameObjectName, successFuncName, null);
        } else {    // 操作异常。
            UnityPlayer.UnitySendMessage(gameObjectName, errorFuncName, desc);
        }
    }

    private Activity getActivity() {
        if (mActivity == null) {
            mActivity = UnityPlayer.currentActivity;
        }
        return mActivity;
    }

    private boolean updateUserInfo(UserInfo userInfo, String field, String value) {
        final boolean[] result = {false};
        switch (field) {
            case "nickname":
                userInfo.setNickname(value);
                result[0] = true;
                break;
            case "birthday":
                long birthday = Long.parseLong(value);
                userInfo.setBirthday(birthday);
                result[0] = true;
                break;
            case "gender":
                switch (value) {
                    case "male":
                        userInfo.setGender(UserInfo.Gender.male);
                        break;
                    case "female":
                        userInfo.setGender(UserInfo.Gender.female);
                        break;
                    default:
                        userInfo.setGender(UserInfo.Gender.unknown);
                        break;
                }
                result[0] = true;
                break;
            case "signature":
                userInfo.setSignature(value);
                result[0] = true;
                break;
            case "region":
                userInfo.setRegion(value);
                result[0] = true;
                break;
            default:
                return false;
        }

        JMessageClient.updateMyInfo(UserInfo.Field.valueOf(field), userInfo,
                new BasicCallback() {
                    @Override
                    public void gotResult(int status, String desc) {
                        result[0] = status == 0;
                        if (status != 0) {
                            Log.w(TAG, "updateMyInfo: " + status + " - " + desc);
                        }
                    }
                });
        return result[0];
    }

}
