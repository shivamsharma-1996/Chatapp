package com.shivam.chatapp.Models;

import java.util.HashMap;

/**
 * Created by shivam sharma on 12/29/2017.
 */

public class Chats
{
    private HashMap<String, Messages> chat;

    public Chats()
    {
        //empty constructor required
    }

    public HashMap<String, Messages> getChat()
    {
        return chat;
    }

    public void setChats(HashMap<String, Messages> chat)
    {
        this.chat = chat;
    }
}
