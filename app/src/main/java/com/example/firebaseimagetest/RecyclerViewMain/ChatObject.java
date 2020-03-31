package com.example.firebaseimagetest.RecyclerViewMain;

import com.example.firebaseimagetest.Users;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class ChatObject implements Serializable
{

    public String chatId;

    private ArrayList<Users> userObjectArrayList = new ArrayList<>();

    public ChatObject(String tempChatId)
    {
        chatId = tempChatId;
    }

    public String getChatId()
    {
        return chatId;
    }

    public ArrayList<Users> getUserObjectArrayList()
    {
        return userObjectArrayList;
    }

    public void addUserToArrayList(Users mUser)
    {
        userObjectArrayList.add(mUser);
    }

}
