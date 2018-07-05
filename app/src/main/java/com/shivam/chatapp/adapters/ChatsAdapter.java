package com.shivam.chatapp.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.shivam.chatapp.Models.Messages;
import com.shivam.chatapp.R;
import com.shivam.chatapp.activities.ChatActivity;
import com.shivam.chatapp.activities.ProfileActivity;
import com.shivam.chatapp.activities.UsersActivity;
import com.squareup.picasso.Picasso;

import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shivam sharma on 12/28/2017.
 */




public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder>
{
    private List<String> mChatList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;
    

    public ChatsAdapter(List<String> mChatList)
    {
        this.mChatList = mChatList;

        mAuth =FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
    }


    @Override
    public ChatsAdapter.ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.users_single_layout, parent, false);

        //Log.i("CallingTest","onCreateViewHolder");
        return new ChatViewHolder(v);
    }



    public class ChatViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameView;
        TextView userStatusView;
        CircleImageView userImageView;

        public ChatViewHolder(View view)
        {
            super(view);
            userNameView = (TextView) view.findViewById(R.id.user_single_name);
            userStatusView = (TextView) view.findViewById(R.id.user_single_status);
            userImageView = (CircleImageView) view.findViewById(R.id.user_single_image);


           // Query q = mUserDatabase.orderByChild("name").equalTo();

            //onclicklistener for recyclerView items(i.e. all viewHolders)
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Toast.makeText(view.getContext(), "position = " + getPosition() + " view: " + view, Toast.LENGTH_SHORT).show();
                    Log.i("CallingTest", String.valueOf(mChatList.get(getAdapterPosition())));

                    String mChatUserId = mChatList.get(getAdapterPosition());
                    Intent chatIntent = new Intent(view.getContext(),ChatActivity.class);
                    chatIntent.putExtra("user_id",mChatUserId);
                    //chatIntent.putExtra("user_name",userName);
                    //startActivity(chatIntent);
                    //view.getContext().startActivity(profileIntent);
                }
            });
        }


        public void setUserName(String userName)
        {
            userNameView.setText(userName);
        }

        public void setUserStatus(String userStatus)
        {
            userStatusView.setText(userStatus);
        }

        public void setUserImage(String userImage)
        {
            Picasso.with(userImageView.getContext()).load(userImage)
                    .placeholder(R.drawable.default_avatar).into(userImageView);
        }


    }





    @Override
    public void onBindViewHolder(final ChatViewHolder viewHolder, int position)
    {

       // Log.i("CallingTest","onBindViewHolder");
       // Log.i("onBindViewHolder", String.valueOf(position));


        String chatUser_id = mChatList.get(position);
        Log.i("onBindViewHolder" ,chatUser_id );

        mUserDatabase.child(chatUser_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("thumb_img").getValue().toString();

                viewHolder.setUserName(name);
                viewHolder.setUserStatus(status);
                viewHolder.setUserImage(image);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });






    }

    @Override
    public int getItemCount()
    {
        return mChatList.size();
    }




}







