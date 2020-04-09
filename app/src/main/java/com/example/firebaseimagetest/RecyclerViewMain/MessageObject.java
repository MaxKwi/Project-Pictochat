package com.example.firebaseimagetest.RecyclerViewMain;

import java.util.ArrayList;

public class MessageObject
{

    String messageId, text, senderId;
    ArrayList<String> mediaUrlList;

    public MessageObject(String msgId, String txt, String sndId, ArrayList<String> mediaUrlList)
    {
        messageId = msgId;
        text = txt;
        senderId = sndId;
        this.mediaUrlList = mediaUrlList;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getText() {
        return text;
    }

    public ArrayList<String> getMediaUrlList()
    {
        return mediaUrlList;
    }
}
