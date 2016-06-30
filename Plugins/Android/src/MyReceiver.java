package com.example.unity3d_jpush_demo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.unity3d.player.UnityPlayer;

import cn.jpush.android.api.JPushInterface;

/**
 * 自定义接收器
 * <p>
 * 如果不定义这个 Receiver，则：
 * 1) 默认用户会打开主界面
 * 2) 接收不到自定义消息
 */
public class MyReceiver extends BroadcastReceiver {
    private static final String TAG = "JPush";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        Log.d(TAG, "[MyReceiver] onReceive - " + intent.getAction() +
                ", extras: " + printBundle(bundle));

        if (JPushInterface.ACTION_REGISTRATION_ID.equals(intent.getAction())) {
            String regId = bundle.getString(JPushInterface.EXTRA_REGISTRATION_ID);
            Log.d(TAG, "[MyReceiver] 接收Registration Id : " + regId);
            // Todo:send the Registration Id to your server...
        } else if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的自定义消息: "
                    + bundle.getString(JPushInterface.EXTRA_MESSAGE));

            //把数据打包，交给用户自己处理
            if (!JPushBridge.isQuit) {
                Log.d(TAG, "coming in---------message");
                String message = bundle.getString(JPushInterface.EXTRA_MESSAGE);
                String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
                try{
                    UnityPlayer.UnitySendMessage(JPushBridge.gameObjectName,
                            "recvMessage", msg2str(message, extras));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "coming out---------message");
            }
        } else if (JPushInterface.ACTION_NOTIFICATION_RECEIVED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 接收到推送下来的通知");
            if (!JPushBridge.isQuit) {
                String content = bundle.getString(JPushInterface.EXTRA_ALERT);
                String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
                String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
                try{
                    UnityPlayer.UnitySendMessage(JPushBridge.gameObjectName,
                            "recvNotification", noti2str(title, content, extras));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (JPushInterface.ACTION_NOTIFICATION_OPENED.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户点击打开了通知");
            JPushInterface.reportNotificationOpened(context,
                    bundle.getString(JPushInterface.EXTRA_MSG_ID));

            Intent launch = context.getPackageManager().
                    getLaunchIntentForPackage(context.getPackageName());
            launch.addCategory(Intent.CATEGORY_LAUNCHER);
            launch.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(launch);

            //把数据打包，交给用户自己处理
            if (!JPushBridge.isQuit) {
                Log.d(TAG, "coming in---------no quit");
                String title = bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
                String content = bundle.getString(JPushInterface.EXTRA_ALERT);
                String extras = bundle.getString(JPushInterface.EXTRA_EXTRA);
                try{
                    UnityPlayer.UnitySendMessage(JPushBridge.gameObjectName,
                            "openNotification", noti2str(title, content, extras));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Log.d(TAG, "coming in---------is_quit");
            }
        } else if (JPushInterface.ACTION_RICHPUSH_CALLBACK.equals(intent.getAction())) {
            Log.d(TAG, "[MyReceiver] 用户收到到RICH PUSH CALLBACK: "
                    + bundle.getString(JPushInterface.EXTRA_EXTRA));
            //在这里根据 JPushInterface.EXTRA_EXTRA 的内容处理代码，比如打开新的Activity， 打开一个网页等..
        } else {
            Log.d(TAG, "[MyReceiver] Unhandled intent - " + intent.getAction());
        }
    }

    // 打印所有的 intent extra 数据
    private static String printBundle(Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) {
            if (key.equals(JPushInterface.EXTRA_NOTIFICATION_ID)) {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getInt(key));
            } else {
                sb.append("\nkey:").append(key).append(", value:").append(bundle.getString(key));
            }
        }
        return sb.toString();
    }

    /**
     * {
     * "title": "JPush SDK Demo",
     * "message": "sdf",
     * "extras": {
     * "q": "ddd",
     * "a": "aaa"
     * }
     * }
     */
    private static String noti2str(String title, String content, String extras) {
        String sb = ("{\"title\":\"" + title + "\",\"content\":\""
                + content + "\",\"extras\":" + extras + "}");
        return sb;
    }

    /**
     * {
     * "message": "hhh",
     * "extras": {
     * "f": "fff",
     * "q": "qqq",
     * "a": "aaa"
     * }
     * }
     *
     */
    private static String msg2str(String content, String extras) {
        return ("{\"message\":\"" + content + "\",\"extras\":" + extras + "}");
    }

}
