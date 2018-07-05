package com.shivam.chatapp.main_tabs;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.shivam.chatapp.Models.Requests;
import com.shivam.chatapp.R;
import com.shivam.chatapp.activities.ChatActivity;
import com.shivam.chatapp.activities.ProfileActivity;
import com.shivam.chatapp.adapters.ChatsAdapter;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestsFragment extends Fragment {

    private View mMainView;

    //Firebase
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendReqRef;

    private boolean ZERO_REQUESTS = true;
    TextView zeroRequestView;
    private ProgressBar mReqProgressBar;


    //RecyclerView
    private RecyclerView mRequestList;


    String lastMsg = "";
    public RequestsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);


        mReqProgressBar = (ProgressBar) mMainView.findViewById(R.id.progressBar);
        zeroRequestView = (TextView) mMainView.findViewById(R.id.request_zeroRequest);


        //RecyclerView
        mRequestList = (RecyclerView) mMainView.findViewById(R.id.request_list);
        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));     //this line is important in both of firebaseRecyclerAdapter as well as Custom RecyclerViewAdapter


        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mCurrent_user_id = mAuth.getCurrentUser().getUid();
        mUserDatabase =  FirebaseDatabase.getInstance().getReference().child("Users");
        mUserDatabase.keepSynced(true);
        mFriendReqRef = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);
        mFriendReqRef.keepSynced(true);

        return mMainView;
    }


    @Override
    public void onStart()
    {
        super.onStart();


        mReqProgressBar.setVisibility(View.VISIBLE);


        Query requestQuery = mFriendReqRef.orderByChild("request_type").equalTo("received");
        requestQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue()!= null)
                {
                    //ZERO_REQUESTS = false;
                    zeroRequestView.setVisibility(View.GONE);


                }
                else
                {
                    //ZERO_REQUESTS = true;
                    zeroRequestView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseRecyclerAdapter<Requests, RequestHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Requests, RequestHolder>(
                Requests.class,
                R.layout.request_single_layout,
                RequestHolder.class,
                requestQuery)
        {
            @Override
            protected void populateViewHolder(final RequestHolder requestHolder, Requests requests, int position) {
                Log.i("model",requests.toString());

                    ZERO_REQUESTS = false;

                    Log.i("request_type", "recieved");
                    final String mRequestUserId = getRef(position).getKey();
                    Log.i("mRequestUserId",mRequestUserId);

                    mUserDatabase.child(mRequestUserId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            String userName = dataSnapshot.child("name").getValue().toString();
                            String userImage = dataSnapshot.child("thumb_img").getValue().toString();



                            requestHolder.setUserName(userName);
                            requestHolder.setUserImage(userImage);



                            requestHolder.mView.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View view) {
                                    Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                    profileIntent.putExtra("user_id",mRequestUserId);
                                    startActivity(profileIntent);
                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

            @Override
            protected void onDataChanged()
            {
                if (mReqProgressBar.getVisibility() == View.VISIBLE)
                {
                    mReqProgressBar.setVisibility(View.GONE);
                }
            }


        };

             mRequestList.setAdapter(firebaseRecyclerAdapter);


    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("RequestFragment","RequestFragment resumed");
    }


    public static class RequestHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        View mView;
        TextView userNameView;

        CircleImageView userImageView;

      
      
        public RequestHolder(View itemView) 
        {
            super(itemView);
            this.mView = itemView;

            userNameView = (TextView) mView.findViewById(R.id.request_single_name);
            userImageView = (CircleImageView) mView.findViewById(R.id.request_single_image);

            //Listeners
            userNameView.setOnClickListener(this);
            userImageView.setOnClickListener(this);

        }

        public void setUserName(String userName)
        {
            userNameView.setText(userName);
        }

        public void setUserImage(String userImage)
        {
            Picasso.with(userImageView.getContext()).load(userImage)
                    .placeholder(R.drawable.default_avatar).into(userImageView);
        }

        @Override
        public void onClick(View view)
        {
            //            switch (view.getId())
//            {
//                case R.id.request_acceptBtn:
//                    break;
//
//                case R.id.request_declineBtn:
//                    break;
//
//                case R.id.request_single_name:
//                    break;
//            }
        }
    }
}
