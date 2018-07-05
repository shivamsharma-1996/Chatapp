package com.shivam.chatapp.main_tabs;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shivam.chatapp.Models.Friends;
import com.shivam.chatapp.Models.Messages;
import com.shivam.chatapp.Models.Users;
import com.shivam.chatapp.R;
import com.shivam.chatapp.activities.ChatActivity;
import com.shivam.chatapp.activities.ProfileActivity;
import com.shivam.chatapp.activities.UsersActivity;
import com.shivam.chatapp.adapters.ChatsAdapter;
import com.shivam.chatapp.adapters.MessageAdapter;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment {

    private DatabaseReference  mMessageRef;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    
    private View mMainView;

    //Cuatom RecyclerView & its ChatsAdapter
    private RecyclerView mRecycler_chatUsersList;
    private final List<String> chatUsersList = new ArrayList<>();
    private LinearLayoutManager  mLinearLayout;
    private ChatsAdapter mAdapter;

    String message_type, lastMsg = "";



    public ChatsFragment()
    {
        // Required empty p2ublic constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);
       

        //RecyclerView
        mRecycler_chatUsersList = (RecyclerView) mMainView.findViewById(R.id.chat_list);
        mAdapter = new ChatsAdapter(chatUsersList);
        mLinearLayout = new LinearLayoutManager(getContext());
        mRecycler_chatUsersList.setHasFixedSize(true);
        mRecycler_chatUsersList.setLayoutManager(mLinearLayout);   //this line is important in both of firebaseRecyclerAdapter as well as Custom RecyclerViewAdapter
        //mRecycler_chatUsersList.setAdapter(mAdapter);


       //Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mMessageRef = FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);
        mMessageRef.keepSynced(true);
        mUserDatabase =  FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);

        return mMainView;
    }



    @Override
    public void onStart()
    {
        super.onStart();

        //this commented code used for custom firebase recyclerViewAdapter
               /*mMessageRef.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                Log.i("mMessageRef" ,dataSnapshot.getKey());
                //These 2 lines I used in the case of custom recyclerAdapter
                chatUsersList.add(dataSnapshot.getKey());
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/







        FirebaseRecyclerAdapter<Friends, ChatViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, ChatViewHolder>(
                Friends.class,
                R.layout.users_single_layout,
                ChatViewHolder.class,
                mMessageRef)
        {
            @Override
            protected void populateViewHolder(final ChatViewHolder ChatViewHolder, Friends friends, int position) {


                final String mChatUserId = getRef(position).getKey();

                Query q = mMessageRef.child(mChatUserId).limitToLast(1);
                q.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        Messages messages = dataSnapshot.getValue(Messages.class);
                        lastMsg = messages.getMessage();
                        message_type = messages.getType();
                        Log.i("lastMsg" , lastMsg);
                        Log.i("message_type" , message_type);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                
                

                mUserDatabase.child(mChatUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String userName =  dataSnapshot.child("name").getValue().toString();
                        String userStatus =  dataSnapshot.child("status").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_img").getValue().toString();

                        ChatViewHolder.setUserName(userName);
                        if(message_type.equals("text"))
                        {
                            ChatViewHolder.userLastPhotoMsg.setVisibility(View.GONE);
                            ChatViewHolder.setLastMsg(lastMsg);
                        }
                        else if (message_type.equals("image"))
                        {
                            ChatViewHolder.userLastPhotoMsg.setVisibility(View.VISIBLE);
                            ChatViewHolder.setLastMsg("      photo");
                        }

                        ChatViewHolder.setUserImage(userThumb);

                        ChatViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view)
                            {
                                Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                chatIntent.putExtra("user_id",mChatUserId);
                                chatIntent.putExtra("user_name",userName);
                                startActivity(chatIntent);
                            }
                        });
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
        };
          mRecycler_chatUsersList.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("ChatFragment","ChatFragment resumed");
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        TextView userNameView;
        TextView userLastMsgView;
        ImageView userLastPhotoMsg;
        CircleImageView userImageView;

        public ChatViewHolder(View view)
        {
            super(view);
            mView = view;
            userNameView = (TextView) view.findViewById(R.id.user_single_name);
            userLastMsgView = (TextView) view.findViewById(R.id.user_single_status);
            userLastPhotoMsg = (ImageView) view.findViewById(R.id.last_photo_msg);
            userImageView = (CircleImageView) view.findViewById(R.id.user_single_image);

            //onclicklistener for recyclerView items(i.e. all viewHolders)
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(view.getContext(), "position = " + getPosition() + " view: " + view, Toast.LENGTH_SHORT).show();
                  //  Log.i("CallingTest", String.valueOf(mChatList.get(getAdapterPosition())));
                }
            });
        }


        public void setUserName(String userName)
        {
            userNameView.setText(userName);
        }

        public void setLastMsg(String lastMsg)
        {
            userLastMsgView.setText(lastMsg);
        }

        public void setUserImage(String userImage)
        {
            Picasso.with(userImageView.getContext()).load(userImage)
                    .placeholder(R.drawable.default_avatar).into(userImageView);
        }


    }
}
