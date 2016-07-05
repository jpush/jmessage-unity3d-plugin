using UnityEngine;
using System.Collections;

namespace JMessage {

    public class JMessageBinding : MonoBehaviour
    {
        #if UNITY_ANDROID
        private static AndroidJavaObject jmessagePlugin;
        public static string gameObjectName = "";

        static JMessageBinding()
        {
            using(AndroidJavaClass clazz = new AndroidJavaClass("com.jiguang.jmessage.JMessageBridge"))
			{
				jmessagePlugin = clazz.CallStatic<AndroidJavaObject>("getInstance");
			}
        }

        public static void InitJMessage(string gameObjectName)
        {
            this.gameObjectName = gameObjectName;
            jmessagePlugin.Call("initJMessage", gameObjectName);
        }

        public static void RegisterEventReceiver()
        {
            jmessagePlugin.Call("RegisterEventReceiver");
        }

        public static void UnregisterEventReceiver()
        {
            jmessagePlugin.Call("UnregisterEventReceiver");
        }

        // Login and register API.
        public static bool Register(string username, string password)
        {
            return jmessagePlugin.Call<bool>("register", username, password);
        }

        public static bool Login(string username, string password)
        {
            return jmessagePlugin.Call<bool>("login", username, password);
        }

        public static void Logout()
        {
            return jmessagePlugin.Call("logout");
        }

        // User info API.
        public static string GetUserInfo(string username, string appKey)
        {
            return jmessagePlugin.Call<string>("getUserInfo");
        }

        public static string GetMyInfo()
        {
            return jmessagePlugin.Call<string>("getMyInfo");
        }

        public static bool UpdateUserInfo(string username, string appKey,
                string field, string value)
        {
            return jmessagePlugin.Call<bool>("updateUserInfo", username, appKey,
                field, value);
        }

        public static bool UpdateMyInfo(string field, string value)
        {
            return jmessagePlugin.Call<bool>("updateMyInfo", field, value);
        }

        public static bool UpdateMyAvatar(string avatarFileUrl)
        {
            return jmessagePlugin.Call<bool>("UpdateMyAvatar", avatarFileUrl);
        }

        // Single message API.
        public static void SendSingleTextMessage(string username, string text,
                string appKey)
        {
            jmessagePlugin.Call("sendSingleTextMessage", username, text, appKey);
        }

        public static void SendSingleImageMessage(string username, string imgFileUrl,
                string appKey)
        {
            jmessagePlugin.Call("sendSingleImageMessage", username, imgFileUrl, appKey);
        }

        public static void SendSingleVoiceMessage(string username, string voiceFileUrl,
                string appKey)
        {
            jmessagePlugin.Call("sendSingleVoiceMessage", username, voiceFileUrl, appKey);
        }

        public static void SendSingleCustomMessage(string username, string jsonStr,
                string appKey)
        {
            jmessagePlugin.Call("sendSingleCustomMessage", username, jsonStr, appKey);
        }

        // Group message API.
        public static void SendGroupTextMessage(long groupId, string text)
        {
            jmessagePlugin.Call("sendGroupTextMessage", groupId, text);
        }

        public static void SendGroupImageMessage(long groupId, string imgFileUrl)
        {
            jmessagePlugin.Call("sendGroupImageMessage", groupId, imgFileUrl);
        }

        public static void SendGroupVoiceMessage(long groupId, string voiceFileUrl)
        {
            jmessagePlugin.Call("sendGroupVoiceMessage", groupId, voiceFileUrl);
        }

        public static void SendGroupCustomMessage(long groupId, string jsonStr)
        {
            jmessagePlugin.Call("sendGroupCustomMessage", groupId, jsonStr);
        }

        public static string GetLatestMessage(string conType, string value, string appKey)
        {
            return jmessagePlugin.Call<string>("getLatestMessage", conType, value, appKey);
        }

        public static string GetHistoryMessages(string conType, string value,
                string appKey, int start, int end)
        {
            return jmessagePlugin.Call<string>("getHistoryMessages", conType, value,
                appKey, start, end);
        }

        public static string GetAllMessages(string conType, string value,
                string appKey)
        {
            return jmessagePlugin.Call<string>("getAllMessages", conType, value, appKey);
        }

        // Conversation API.
        public static string GetConversationList()
        {
            return jmessagePlugin.Call<string>("getConversationList");
        }

        public static void SetSingleConversationUnreadMessageCount(string username,
                string appKey, int count)
        {
            jmessagePlugin.Call("setSingleConversationUnreadMessageCount",
                username, appKey, count);
        }

        public static void SetGroupConversationUnreadMessageCount(long groupId,
                int count)
        {
            jmessagePlugin.Call("setGroupConversationUnreadMessageCount",
                groupId, count);
        }

        public static void EnterSingleConversation(string username, string appKey)
        {
            jmessagePlugin.Call("enterSingleConversation", username, appkey);
        }

        public static void EnterGroupConversation(long groupId)
        {
            jmessagePlugin.Call("enterGroupConversation", groupId);
        }

        public static string GetSingleConversation(string username, string appKey)
        {
            return jmessagePlugin.Call<string>("getSingleConversation", username, appKey);
        }

        public static string GetGroupConversation(long groupId)
        {
            return jmessagePlugin.Call<string>("getGroupConversation", groupId);
        }

        public static void DeleteSingleConversation(string username, string appKey)
        {
            jmessagePlugin.Call("deleteSingleConversation", username, appKey);
        }

        public static void DeleteGroupConversation(long groupId)
        {
            jmessagePlugin.Call("deleteGroupConversation", groupId);
        }

        // Group API.
        public static long CreateGroup(string groupName, string groupDesc)
        {
            return jmessagePlugin.Call<long>("createGroup", groupName, groupDesc);
        }

        public static string GetGroupIDList()
        {
            return jmessagePlugin.Call<string>("getGroupIDList");
        }

        public static string GetGroupInfo(long groupId)
        {
            return jmessagePlugin.Call<string>("getGroupInfo", groupId);
        }

        public static bool UpdateGroupName(long groupId, string newName)
        {
            return jmessagePlugin.Call<bool>("updateGroupName", groupId, newName);
        }

        public static bool UpdateGroupDesc(long groupId, string newDesc)
        {
            return jmessagePlugin.Call<bool>("updateGroupDesc", groupId, newDesc);
        }

        public static bool RemoveGroupMembers(long groupId, string username)
        {
            return jmessagePlugin.Call<bool>("removeGroupMembers", groupId, username);
        }

        public static string GetGroupMembers(long groupId)
        {
            return jmessagePlugin.Call<bool>("getGroupMembers", groupId);
        }

        public static bool ExitGroup(long groupId)
        {
            return jmessagePlugin.Call<bool>("exitGroup", groupId);
        }

        // Blacklist API.
        public static bool AddUsersToBlacklist(string usernames)
        {
            return jmessagePlugin.Call<bool>("addUsersToBlacklist", usernames);
        }

        public static bool DelUsersFromBlacklist(string usernames)
        {
            return jmessagePlugin.Call<bool>("delUsersFromBlacklist", usernames);
        }

        public static string GetBlacklist()
        {
            return jmessagePlugin.Call<string>("getBlacklist");
        }

        // Handle event.
        public static void onGroupMemberAdded()
        {
            
        }

        #endif
    }
}
