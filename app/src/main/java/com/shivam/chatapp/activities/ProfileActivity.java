package com.shivam.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shivam.chatapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView mProfileImage;
    private TextView mProfileName, mProfileStatus, mProfileFriendsCount;
    private Button mProfileSendReqBtn, mDeclineBtn;

    private DatabaseReference mRootRef, mUserDatabse, mFriendReqDatabase, mFriendDatabase;

    private FirebaseUser mCurrent_user;

    private ProgressDialog mProgressDialog;

    private String mCurrent_state;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

       final String user_id = getIntent().getStringExtra("user_id");
//        mDisplayID = (TextView) findViewById(R.id.profile_display_name);
//        mDisplayID.setText(user_id);


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mUserDatabse = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser();

        mProfileImage = (ImageView) findViewById(R.id.profile_image);
        mProfileName = (TextView) findViewById(R.id.profile_display_name);
        mProfileStatus = (TextView) findViewById(R.id.profile_status);
        mProfileFriendsCount = (TextView) findViewById(R.id.profile_totalFriends);
        mProfileSendReqBtn = (Button) findViewById(R.id.profile_sendReqBtn);
        mDeclineBtn = (Button) findViewById(R.id.profile_decline_Btn);


        mCurrent_state = "not_friends";
        mDeclineBtn.setVisibility(View.INVISIBLE);
        mDeclineBtn.setEnabled(false);



        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading User Data");
        mProgressDialog.setMessage("Please wait while load the user data");
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mUserDatabse.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                mProfileName.setText(display_name);
                mProfileStatus.setText(status);

                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

                if(mCurrent_user.getUid().equals(user_id)){

                    mDeclineBtn.setEnabled(false);
                    mDeclineBtn.setVisibility(View.INVISIBLE);

                    mProfileSendReqBtn.setEnabled(false);
                    mProfileSendReqBtn.setVisibility(View.INVISIBLE);

                }


                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        //means request is either sen
                        if (dataSnapshot.hasChild(user_id)) {
                            String req_type = dataSnapshot.child(user_id).child("request_type").getValue().toString();


                            if (req_type.equals("received"))
                            {
                                mCurrent_state = "req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");

                                mDeclineBtn.setVisibility(View.VISIBLE);
                                mDeclineBtn.setEnabled(true);
                            }
                            else if (req_type.equals("sent"))
                            {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }


                            mProgressDialog.dismiss();

                        }
                        else
                        {
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id))
                                    {
                                        mCurrent_state = "friends";
                                        mProfileSendReqBtn.setText("Unfriend this Person");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }

                                    else
                                    {
                                        mCurrent_state = "not_friends";
                                        mProfileSendReqBtn.setText("Send Friend Request");

                                        mDeclineBtn.setVisibility(View.INVISIBLE);
                                        mDeclineBtn.setEnabled(false);
                                    }

                                    mProgressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();

                                }
                            });
                        }

                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {


            }
        });


















        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProfileSendReqBtn.setEnabled(false);  //Once the user tap on button ,the button will set to be disabled


                if(mCurrent_state.equals("not_friends"))                      //MEANS USER CLICK ON "Send Friend Request" BUTTON
                {

                    DatabaseReference newNotificationRef = mRootRef.child("notifications").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrent_user.getUid());
                    notificationData.put("type", "request");

                    //                   mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type")
//                           .setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
//                       @Override
//                       public void onComplete(@NonNull Task<Void> task) {
//                           if(task.isSuccessful())
//                           {
//                               mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type")
//                                       .setValue("recieved").addOnSuccessListener(new OnSuccessListener<Void>() {
//                                   @Override
//                                   public void onSuccess(Void aVoid)
//                                   {
//
//                                       HashMap<String,String> notficationData = new HashMap<>();
//                                       notficationData.put("from", mCurrent_user.getUid());
//                                       notficationData.put("type", "request");
//
//
//                                       /*mNotificationDatabase.child(user_id).push().setValue(notficationData).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                           @Override
//                                           public void onSuccess(Void aVoid) {
//
//                                           }
//                                       });
//*/
//
//                                       //mProfileSendReqBtn.setEnabled(true);
//                                       mCurrent_state = "req_sent";   //NOW, the state change from "not_friends" to "req_sent"
//                                       mProfileSendReqBtn.setText("Cancel Friend Request");
//
//
//                                       mDeclineBtn.setVisibility(View.INVISIBLE);
//                                       mDeclineBtn.setEnabled(false);
//
//                                       //Toast.makeText(ProfileActivity.this,"Sent Request Successfully",Toast.LENGTH_LONG).show();
//                                   }
//                               });
//                           }
//                           else
//                           {
//                               Toast.makeText(ProfileActivity.this,"Failed Sending Request",Toast.LENGTH_LONG).show();
//                           }
//
//                           mProfileSendReqBtn.setEnabled(true);
//
//                       }
//                   });




                    Map requestMap = new HashMap();
                    requestMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id + "/request_type", "sent");
                    requestMap.put("Friend_req/" + user_id  + "/" + mCurrent_user.getUid() + "/request_type", "received");
                    requestMap.put("notifications/" + user_id + "/" + newNotificationId, notificationData);


                    mRootRef.updateChildren(requestMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null)
                            {
                                Toast.makeText(ProfileActivity.this, "There was some error in sending request",Toast.LENGTH_LONG).show();
                            }

                            else
                            {
                                mCurrent_state = "req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                            }
                            mProfileSendReqBtn.setEnabled(true);


                     //                            HashMap<String,String> notificationData = new HashMap<>();
//                            notificationData.put("from",mCurrent_user.getUid());
//                            notificationData.put("type","request");
//
//                            mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    mCurrent_state = "req_sent";
//                                    mProfileSendReqBtn.setText("Cancel Friend Request");
//
//                                    mDeclineBtn.setVisibility(View.INVISIBLE);
//                                    mDeclineBtn.setEnabled(false);
//                                }
//                            });

                        }
                    });

                }



                if(mCurrent_state.equals("req_sent"))                            //MEANS USER CLICK ON "Cancel Friend Request" BUTTON
                {
                    mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state = "not_friends";
                                    mProfileSendReqBtn.setText("Send Friend Request");

                                    mDeclineBtn.setVisibility(View.INVISIBLE);
                                    mDeclineBtn.setEnabled(false);
                                }
                            });
                        }
                    });

                }




                if(mCurrent_state.equals("req_received"))            //USER CLICK ON "Accept Friend Request" BUTTON
                {
                    final String currenDate = java.text.DateFormat.getDateTimeInstance().format(new Date());
                    //                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).setValue(currenDate).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//
//                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).setValue(currenDate)
//                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                                        @Override
//                                        public void onSuccess(Void aVoid)
//                                        {
//                                           mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                               @Override
//                                               public void onSuccess(Void aVoid) {
//
//                                                   mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                                       @Override
//                                                       public void onSuccess(Void aVoid) {
//
//                                                           mProfileSendReqBtn.setEnabled(true);
//                                                           mCurrent_state = "friends";                                 //NOW, the state changes from "req_recieved" to "friends"
//                                                           mProfileSendReqBtn.setText("Unfriend this Person");
//
//
//                                                           mDeclineBtn.setVisibility(View.INVISIBLE);
//                                                           mDeclineBtn.setEnabled(false);
//                                                       }
//                                                   });
//                                               }
//                                           });
//                                        }
//                                    });
//                        }
//                    });
                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date" , currenDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date" , currenDate);
                    friendsMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id, null);
                    friendsMap.put("Friend_req/" + user_id +  "/" + mCurrent_user.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError == null)
                            {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "friends";
                                mProfileSendReqBtn.setText("Unfriend this Person");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();
                                Toast.makeText(ProfileActivity.this, error , Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }





                //                if(mCurrent_state.equals("friends"))                  //MEANS USER CLICK ON  "Unfriend this Person" BUTTON
//                {
//                    mFriendDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            mFriendDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    mProfileSendReqBtn.setEnabled(true);
//                                    mCurrent_state = "not_friends";                                  //NOW, the state changes from "friends" to "not_friends"
//                                    mProfileSendReqBtn.setText("Send Friend Request");
//
//
//                                    mDeclineBtn.setVisibility(View.INVISIBLE);
//                                    mDeclineBtn.setEnabled(false);
//                                }
//                            });
//                        }
//                    });
//                }
                if(mCurrent_state.equals("friends"))                        {
                    Map unfriendMap = new HashMap();
                    unfriendMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id , null);
                    unfriendMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() , null);

                    mRootRef.updateChildren(unfriendMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if(databaseError == null)
                            {
                                mProfileSendReqBtn.setEnabled(true);
                                mCurrent_state = "not_friends";
                                mProfileSendReqBtn.setText("Send Friend Request");

                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mDeclineBtn.setEnabled(false);
                            }
                            else
                            {
                                String error = databaseError.getMessage();

                                Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                            }

                            mProfileSendReqBtn.setEnabled(true);
                        }
                    });
                }}
        });
















        //        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//
//            mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                @Override
//                public void onSuccess(Void aVoid) {
//                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            mProfileSendReqBtn.setEnabled(true);
//                            mCurrent_state = "not_friends";                                  //NOW, the state changes to "not_friends"
//                            mProfileSendReqBtn.setText("Send Friend Request");
//
//                            mDeclineBtn.setVisibility(View.INVISIBLE);
//                            mDeclineBtn.setEnabled(false);
//                        }
//                    });
//                }
//            });
//
//        }
//    });
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               Map declineReqMap = new HashMap();
               declineReqMap.put("Friend_req/" + mCurrent_user.getUid() + "/" + user_id , null);
               declineReqMap.put("Friend_req/" + user_id + "/" + mCurrent_user.getUid() , null);

               mRootRef.updateChildren(declineReqMap, new DatabaseReference.CompletionListener() {
                   @Override
                   public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                       if(databaseError == null)
                       {
                           mProfileSendReqBtn.setEnabled(true);
                           mCurrent_state = "not_friends";
                           mProfileSendReqBtn.setText("Send Friend Request");

                           mDeclineBtn.setVisibility(View.INVISIBLE);
                           mDeclineBtn.setEnabled(false);
                       }
                       else
                       {
                           String error = databaseError.getMessage();
                           Toast.makeText(ProfileActivity.this, error, Toast.LENGTH_LONG).show();
                       }

                       mProfileSendReqBtn.setEnabled(true);
                   }
               });
           }
       });
}






    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null)
        {
            //user is not signed in so navigate to startActivity
            sendToStart();
        }
        else
        {
            //means currentUser!= null & a user is already signed in
            mUserDatabse.child("online").setValue(true);
        }
    }


    private void sendToStart()
    {
        Intent startIntent = new Intent(ProfileActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }





    @Override
    protected void onStop() {
        super.onStop();
        mUserDatabse.child("online").setValue(false);

    }
}
