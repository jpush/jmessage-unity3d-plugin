package com.example.unity3d_jpush_demo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;
import com.unity3d.player.UnityPlayer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
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

    public boolean updateMyInfo(String field, String value) {
        UserInfo myInfo = JMessageClient.getMyInfo();
        return updateUserInfo(myInfo, field, value);
    }

    public boolean updateMyPassword(String oldPwd, String newPwd) {
        final boolean[] result = {false};
        JMessageClient.updateUserPassword(oldPwd, newPwd, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                if (status == 0) {
                    result[0] = true;
                } else {
                    Log.w(TAG, "updateMyPassword: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    public boolean updateMyAvatar(String avatarPath) {
        final boolean[] result = {false};
        File avatarFile = new File(avatarPath);
        JMessageClient.updateUserAvatar(avatarFile, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                if (status == 0) {
                    result[0] = true;
                } else {
                    Log.w(TAG, "updateMyAvatar: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    // Single message API.
    public void sendSingleTextMessage(String username, String text, String appKey) {
        if (appKey == null) {
            appKey = "";
        }
        Conversation con = getSingleCon(username, appKey);
        if (con == null) {
            return;
        }
        Message msg = con.createSendTextMessage(text);
        JMessageClient.sendMessage(msg);
    }

    public void sendSingleImageMessage(String username, String imgFileUrl,
            String appKey) {
        if (appKey == null) {
            appKey = "";
        }
        Conversation con = getSingleCon(username, appKey);
        if (con == null) {
            return;
        }
        try {
            URL imgUrl = new URL(imgFileUrl);
            File imgFile = new File(imgUrl.getPath());
            Message msg = con.createSendImageMessage(imgFile);
            JMessageClient.sendMessage(msg);
        } catch (MalformedURLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendSingleVoiceMessage(String username, String voiceFileUrl,
            String appKey) {
        if (appKey == null) {
            appKey = "";
        }
        Conversation con = getSingleCon(username, appKey);
        if (con == null) {
            return;
        }
        try {
            URL url = new URL(voiceFileUrl);
            String voicePath = url.getPath();
            File file = new File(voicePath);

            MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(),
                    Uri.parse(voicePath));
            int duration = mediaPlayer.getDuration();

            Message msg = con.createSendVoiceMessage(file, duration);
            JMessageClient.sendMessage(msg);
            mediaPlayer.release();
        } catch (MalformedURLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendSingleCustomMessage(String username, String jsonStr,
            String appKey) {
        if (appKey == null) {
            appKey = "";
        }
        Conversation con = getSingleCon(username, appKey);
        if (con == null) {
            return;
        }
        try {
            JSONObject values = new JSONObject(jsonStr);
            Iterator keys = values.keys();
            Map<String, String> valueMap = new HashMap<>();

            String key, value;
            while (keys.hasNext()) {
                key = (String) keys.next();
                value = values.getString(key);
                valueMap.put(key, value);
            }
            Message msg = con.createSendCustomMessage(valueMap);
            JMessageClient.sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Group message API.
    public void sendGroupTextMessage(long groupId, String text) {
        Message msg = JMessageClient.createGroupTextMessage(groupId, text);
        JMessageClient.sendMessage(msg);
    }

    public void sendGroupImageMessage(long groupId, String imgFileUrl) {
        try {
            URL imgUrl = new URL(imgFileUrl);
            File imgFile = new File(imgUrl.getPath());
            Message msg = JMessageClient.createGroupImageMessage(groupId, imgFile);
            JMessageClient.sendMessage(msg);
        } catch (MalformedURLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendGroupVoiceMessage(long groupId, String voiceFileUrl) {
        try {
            URL url = new URL(voiceFileUrl);
            String path = url.getPath();
            File file = new File(path);

            MediaPlayer mediaPlayer = MediaPlayer.create(getActivity(),
                    Uri.parse(path));
            int duration = mediaPlayer.getDuration();

            Message msg = JMessageClient.createGroupVoiceMessage(groupId, file, duration);
            JMessageClient.sendMessage(msg);
        } catch (MalformedURLException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendGroupCustomMessage(long groupId, String jsonStr) {
        try {
            JSONObject values = new JSONObject(jsonStr);
            Iterator keys = values.keys();
            Map<String, String> valueMap = new HashMap<>();
            String key, value;
            while (keys.hasNext()) {
                key = (String) keys.next();
                value = values.getString(key);
                valueMap.put(key, value);
            }
            Message msg = JMessageClient.createGroupCustomMessage(groupId,
                    valueMap);
            JMessageClient.sendMessage(msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getLatestMessage(String conType, String value, String appKey) {
        Conversation con = null;
        if (conType.equals("single")) {
            con = getSingleCon(value, appKey);
        } else if (conType.equals("group")) {
            con = getGroupCon(Long.parseLong(value));
        }
        if (con == null) {
            return null;
        }
        Message msg = con.getLatestMessage();
        return mGson.toJson(msg);
    }

    public String getHistoryMessages(String conType, String value, String appKey,
            int from, int limit) {
        Conversation con = null;
        if (conType.equals("single")) {
            con = getSingleCon(value, appKey);
        } else if (conType.equals("group")) {
            con = getGroupCon(Long.parseLong(value));
        }
        if (con == null) {
            return null;
        }
        List<Message> messages = con.getMessagesFromNewest(from, limit);
        return mGson.toJson(messages);
    }

    /**
     * @param conType: Conversation type. 'single' or 'group'.
     * @param value: Username if conType = 'single'; groupId if conType = 'group'.
     * @param appKey: Get cross-application single conversation by app key.
     * @return JSON String.
     */
    public String getAllMessages(String conType, String value, String appKey) {
        Conversation con = null;
        if (conType.equals("single")) {
            con = getSingleCon(value, appKey);
        } else if (conType.equals("group")) {
            con = getGroupCon(Long.parseLong(value));
        }
        if (con == null) {
            return null;
        }
        List<Message> allMessage = con.getAllMessage();
        return mGson.toJson(allMessage);
    }

    // Conversation API.
    public String getConversationList() {
        List<Conversation> conList = JMessageClient.getConversationList();
        if (conList == null) {
            return null;
        } else {
            return mGson.toJson(conList);
        }
    }

    public void setSingleConversationUnreadMessageCount(String username,
            String appKey, int count) {
        Conversation con = JMessageClient.getSingleConversation(username, appKey);
        if (con == null) {
            Log.w(TAG, "setSingleConversationUnreadMessageCount: Conversation isn't exist.");
            return;
        }
        con.setUnReadMessageCnt(count);
    }

    public void setGroupConversationUnreadMessageCount(long groupId, int count) {
        Conversation con = JMessageClient.getGroupConversation(groupId);
        if (con == null) {
            Log.w(TAG, "setGroupConversationUnreadMessageCount: Conversation isn't exist.");
            return;
        }
        con.setUnReadMessageCnt(count);
    }

    public void enterSingleConversation(String username, String appKey) {
        JMessageClient.enterSingleConversation(username, appKey);
    }

    public void enterGroupConversation(long groupId) {
        JMessageClient.enterGroupConversation(groupId);
    }

    public void exitConversation() {
        JMessageClient.exitConversation();
    }

    public String getSingleConversation(String username, String appKey) {
        Conversation con = getSingleCon(username, appKey);
        return mGson.toJson(con);
    }

    public String getGroupConversation(long groupId) {
        Conversation con = getGroupCon(groupId);
        return mGson.toJson(con);
    }

    public void deleteSingleConversation(String username, String appKey) {
        JMessageClient.deleteSingleConversation(username, appKey);
    }

    public void deleteGroupConversation(long groupId) {
        JMessageClient.deleteGroupConversation(groupId);
    }

    // Group API.
    /**
     * @param groupName: Group name.
     * @param groupDesc: Group description.
     * @return groupId.
     */
    public long createGroup(String groupName, String groupDesc) {
        final long[] result = {-1};
        JMessageClient.createGroup(groupName, groupDesc, new CreateGroupCallback() {
            @Override
            public void gotResult(int status, String desc, long groupId) {
                if (status == 0) {
                    result[0] = groupId;
                } else {
                    Log.w(TAG, "createGroup: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    public String getGroupIDList() {
        final String[] result = {null};
        JMessageClient.getGroupIDList(new GetGroupIDListCallback() {
            @Override
            public void gotResult(int status, String desc, List<Long> list) {
                if (status == 0) {
                    result[0] = mGson.toJson(list);
                } else {
                    Log.w(TAG, "getGroupIDList: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    public String getGroupInfo(long groupId) {
        final String[] result = {null};
        JMessageClient.getGroupInfo(groupId, new GetGroupInfoCallback() {
            @Override
            public void gotResult(int status, String desc, GroupInfo groupInfo) {
                if (status == 0) {
                    result[0] = mGson.toJson(groupInfo);
                } else {
                    Log.w(TAG, "getGroupInfo: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    public boolean updateGroupName(long groupId, String newName) {
        final boolean[] result = {false};
        JMessageClient.updateGroupName(groupId, newName, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                if (status == 0) {
                    result[0] = true;
                } else {
                    Log.w(TAG, "updateGroupName: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    public boolean updateGroupDesc(long groupId, String newDesc) {
        final boolean[] result = {false};
        JMessageClient.updateGroupDescription(groupId, newDesc, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                if (status == 0) {
                    result[0] = true;
                } else {
                    Log.w(TAG, "updateGroupDesc: " + status + " - " + desc);
                }
            }
        });
        return result[0];
    }

    /**
     * @param groupId: Group ID.
     * @param username: Username Str. eg: 'username1, username2'.
     */
    public boolean removeGroupMembers(long groupId, String username) {
        final boolean[] result = {false};
        String[] usernameArr = username.split(",");
        List<String> usernameList = Arrays.asList(usernameArr);
        JMessageClient.removeGroupMembers(groupId, usernameList, new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
                if (status == 0) {
                    result[0] = true;
                } else {
                    Log.w(TAG, "removeGroupMembers: " + status + " - " + desc);
                }
            }
        });
        return result[0];
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

    private Conversation getSingleCon(String username, String appKey) {
        if (appKey == null) {
            appKey = "";
        }
        Conversation con = JMessageClient.getSingleConversation(username, appKey);
        if (con == null) {
            con = Conversation.createSingleConversation(username, appKey);
            if (con == null) {
                Log.w(TAG, "Get single conversation error.");
                return null;
            }
        }
        return con;
    }

    private Conversation getGroupCon(long groupId) {
        Conversation con = JMessageClient.getGroupConversation(groupId);
        if (con == null) {
            con = Conversation.createGroupConversation(groupId);
            if (con == null) {
                Log.w(TAG, "Get group conversation error.");
                return null;
            }
        }
        return con;
    }

}
