package com.shivam.chatapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shivam.chatapp.Models.Users;
import com.shivam.chatapp.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUserList;

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);

        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mAuth = FirebaseAuth.getInstance();
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users");



        mUserDatabaseRef.keepSynced(true);



        mUserList = (RecyclerView) findViewById(R.id.users_list);
        mUserList.setHasFixedSize(true);
        mUserList.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onStart()
    {
        super.onStart();


        FirebaseRecyclerAdapter<Users,UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(

                Users.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUserDatabaseRef
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users model, int position) {

                usersViewHolder.setDisplayName(model.getName());
                usersViewHolder.setUserStatus(model.getStatus());
                usersViewHolder.setUserImage(model.getThumb_image(),getApplicationContext());

                final String user_id = getRef(position).getKey();

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id",user_id);


                        startActivity(profileIntent);

                    }
                });

            }
        };

        mUserList.setAdapter(firebaseRecyclerAdapter);
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        if(mAuth.getCurrentUser()!=null)
        {

            mUserDatabaseRef.child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
            Log.i("Users","onStart()");
        }
    }





    @Override
    protected void onRestart()
    {
        super.onRestart();
    }





    @Override
    protected void onStop()
    {
        super.onStop();
        if(mAuth.getCurrentUser()!=null)
        {

        }
    }






    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public UsersViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        private void setDisplayName(String name)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }
        private void setUserStatus(String status)
        {
            TextView userNameView = (TextView) mView.findViewById(R.id.user_single_status);
            userNameView.setText(status);
        }
        private void setUserImage(String thumb_image, final Context context)
        {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);

            Picasso.with(context)
                    .load(thumb_image)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.mipmap.ic_launcher)
                    .into(userImageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess()
                        {
                            Toast.makeText(context,"thumb_image downloaded",Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onError()
                        {
                            Toast.makeText(context,"thumb_image can't downloaded",Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }
}
