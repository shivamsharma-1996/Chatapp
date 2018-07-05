package com.shivam.chatapp.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shivam.chatapp.Models.Messages;
import com.shivam.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by shivam sharma on 12/25/2017.
 */




//------------------------ Here we used Custom RecyclerAdapter instead of FirebaseRecyclerAdapter bcoz, the "Pagination" with FirebaseRecyclerAdapter is not possible
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHOlder>
{
    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    RelativeLayout relLayout_of_message_single_layout;


    public MessageAdapter(List<Messages> mMessageList)
    {
        this.mMessageList = mMessageList;
    }


    //onCreateViewHolder() inflates the  message_single_layout.xml file & return the view & show them in RecycleraView
    @Override
    public MessageAdapter.MessageViewHOlder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        Log.i("CallingTest","onCreateViewHolder");
        Log.i("v",v.toString());   //https://medium.com/@pranitsawant01/about-androids-layoutinflater-inflate-5188a247c8fc
        relLayout_of_message_single_layout = (RelativeLayout) v;  //for understand the concept of layoutinflator


        return new MessageViewHOlder(v);
    }


    //onBindViewHolder() binds the data to (messageText & profileImage) i.e. viewholder
    @Override
    public void onBindViewHolder(final MessageViewHOlder messageHolder, int position)
    {
        Log.i("CallingTest","onBindViewHolder");
        Log.i("onBindViewHolder", String.valueOf(position));
        mAuth =FirebaseAuth.getInstance();
        String currentUser_id = mAuth.getCurrentUser().getUid();


        Messages c = mMessageList.get(position);
        String from_user = c.getFrom();
        String message_type = c.getType();
        long message_timeStamp = c.getTime();

        //geting time from timeStamp value
        final String message_time = DateUtils.formatDateTime(messageHolder.mView.getContext(), message_timeStamp, DateUtils.FORMAT_SHOW_TIME);





        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("thumb_img").getValue().toString();


                messageHolder.displayName.setText(name);
                messageHolder.messageTime.setText(message_time);


                Picasso.with(messageHolder.profileImage.getContext()).load(image)
                        .placeholder(R.drawable.default_avatar).into(messageHolder.profileImage);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(message_type.equals("text"))
        {
            messageHolder.messageText.setText(c.getMessage());
            messageHolder.messageImage.setVisibility(View.INVISIBLE);
        }
        else if(message_type.equals("image"))
        {
            messageHolder.messageImage.setVisibility(View.VISIBLE);
            Picasso.with(messageHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(messageHolder.messageImage);

            messageHolder.messageText.setVisibility(View.GONE);
        }

        //Changing the layout of retrieved message from firebase according to "from_user"
        if(from_user.equals(currentUser_id))
        {
            //viewHolder.messageText.setBackgroundColor(Color.WHITE);
            messageHolder.messageText.setBackgroundResource(R.drawable.message_text_background2);
            messageHolder.messageText.setTextColor(Color.BLACK);
            relLayout_of_message_single_layout.setGravity(Gravity.RIGHT);
        }
        else
        {
            messageHolder.messageText.setBackgroundResource(R.drawable.message_text_background);
            messageHolder.messageText.setTextColor(Color.WHITE);
            relLayout_of_message_single_layout.setGravity(Gravity.LEFT);
        }

    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }

    public class MessageViewHOlder extends RecyclerView.ViewHolder
    {
        public View mView;
        public TextView messageText;
        public CircleImageView profileImage;
        public TextView displayName;
        public TextView messageTime;
        public ImageView messageImage;


        public MessageViewHOlder(View view)
        {
            super(view);
            this.mView =view;
            messageText = (TextView) view.findViewById(R.id.message_text_layout);
            profileImage = (CircleImageView) view.findViewById(R.id.message_profile_layout);
            displayName = (TextView) view.findViewById(R.id.name_text_layout);
            messageTime = (TextView) view.findViewById(R.id.time_text_layout);
            messageImage = (ImageView) view.findViewById(R.id.message_image_layout);
        }
    }
}







