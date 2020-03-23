package com.example.firebaseimagetest;

import com.google.firebase.database.Exclude;

public class Upload
{

    private String mName;
    private String mImageUrl;
    private String mUid;

    private String mKey;

    public Upload()
    {

    }

    public Upload(String name, String imageUrl, String uid)
    {
        if(name.trim().equals(""))
        {
            name = "No Name";
        }

        mName = name;
        mImageUrl = imageUrl;
        mUid = uid;
    }

    public String getName()
    {
        return mName;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public String getImageUrl()
    {
        return mImageUrl;
    }

    public void setImageUrl(String imageUrl)
    {
        mImageUrl = imageUrl;
    }

    public String getUid() {
        return mUid;
    }

    public void setUid(String uid){
        mUid = uid;
    }

    @Exclude
    public String getKey()
    {
        return mKey;
    }

    @Exclude
    public void setKey(String key)
    {
        mKey = key;
    }

}
