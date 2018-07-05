package com.shivam.chatapp.Application;

import android.app.Application;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by shivam sharma on 8/30/2017.
 */


// This class gonna handle the disconnection from internet for application
public class InstantChat extends Application
{
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate()
    {
        super.onCreate();

        //Adding Firebase Offline Capabilities
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);   //now  we've to still add it to activity as well while working with any QUERIES at there.

        //Adding PICASSO Offline Capabilities
        Picasso.Builder builder = new Picasso.Builder((this));
        builder.downloader(new OkHttpDownloader(this , Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null)
        {
            mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            //adding presence functionality using onDisconnect() method

            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot != null)
                    {
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);  //if this query gets disconnect from firebase then its value is be set by false
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }



    }


}

  /*  There are two cases that may happen when the client and server are disconnected:

        1. the client explicitly disconnects from the server
        2. the client simply disappears

        case 1:  When you kill the app, you are triggering an explicit disconnect. In that case the client will send a signal to the server that it is disconnecting and the server will immediately execute the onDisconnect callback.

        case 2:  When you switch off wifi, your app doesn't get a chance to tell the server that it is disconnecting. In that case the onDisconnect will fire once, so the server detects that the client is gone. This may take a few minutes, depending on how the time-outs for sockets are configured. So in that case you just have to be a bit more patient.*/