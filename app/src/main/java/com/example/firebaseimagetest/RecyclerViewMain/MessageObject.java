package com.example.firebaseimagetest.RecyclerViewMain;

public class MessageObject
{

    String messageId, text, senderId;

    public MessageObject(String msgId, String txt, String sndId)
    {
        messageId = msgId;
        text = txt;
        senderId = sndId;
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
}
