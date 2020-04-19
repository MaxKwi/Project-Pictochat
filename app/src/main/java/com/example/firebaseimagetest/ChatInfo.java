package com.example.firebaseimagetest;

import java.util.ArrayList;

public class ChatInfo
{

    public String chatId = "";
    public String displayName = "";
    public ArrayList<String> tempUidFriendsInChat;
    public ArrayList<String> currentFriendsInChat;

    public ChatInfo(String chatId)
    {
        this.chatId = chatId;
        tempUidFriendsInChat = new ArrayList<>();
        currentFriendsInChat = new ArrayList<>();
    }


}
