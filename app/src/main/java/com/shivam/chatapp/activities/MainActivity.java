package com.shivam.chatapp.activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.shivam.chatapp.R;
import com.shivam.chatapp.adapters.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private Toolbar mToolbar;


    ImageView imageView;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TabLayout mTabLayout;

    private boolean NewActivtiyTriger=false;             //onPause() & onStop() called together if another activty comes to foreground. But I  dont want to setValue(false) in  the case of newActivityTrigger is true


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null)
        {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
        }


        mToolbar = (Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Instant Chat");


        imageView = (ImageView) findViewById(R.id.user_single_online_icon);

        //ViewPager
        mViewPager = (ViewPager) findViewById(R.id.tabPager);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = (TabLayout) findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);


    }




    @Override
    protected void onResume() {
        super.onResume();

        //Check if user is signed in(non-null) & update UI accordingly
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
        {
            //user is not signed in so navigate to startActivity
           sendToStart();
        }
        else
        {
            //means currentUser!= null & a user is already signed in
                mUserRef.child("online").setValue("true");
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //NewActivtiyTriger = true;
    }



    //onPause() & onStop() called together if another activty comes to foreground. But I  dont want to setValue(false) in  the case of newActivityTrigger is true
    @Override
    protected void onStop()
    {
        super.onStop();
        if(mAuth.getCurrentUser()!=null)  /*NewActivtiyTriger == false*/
        {
          //mUserRef.child("online").setValue(false);
            mUserRef.child("online").setValue(ServerValue.TIMESTAMP);    //means user is Offline
            Log.i("Main","onStop()");
        }
    }



    private void sendToStart()
    {
        Intent startIntent = new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.main_menu,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.main_logout_btn)
        {
           FirebaseAuth.getInstance().signOut();
            sendToStart();
        }
        if(item.getItemId() == R.id.main_settings_btn)
        {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if(item.getItemId() == R.id.main_all_btn)
        {
            Intent settingsIntent = new Intent(MainActivity.this, UsersActivity.class);
            Log.i("Main","onOptions1()");
            startActivity(settingsIntent);
            Log.i("Main","onOptions2()");
            //NewActivtiyTriger = true;
        }

        return true;
    }
}
