package com.shivam.chatapp.Models;

/**
 * Created by shivam sharma on 12/29/2017.
 */

public class Requests
{
    public String request_type;

    public Requests()
    {
        //empty constructor required
    }

    public Requests(String request_type)
    {
        this.request_type = request_type;
    }

    public String getRequest_type()
    {
        return request_type;
    }

    public void setRequest_type(String request_type)
    {
        this.request_type = request_type;
    }

    @Override
    public String toString() {
        return "request_type : " + request_type  ;
    }
}
