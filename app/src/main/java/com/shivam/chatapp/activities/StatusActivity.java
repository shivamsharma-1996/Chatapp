package com.shivam.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shivam.chatapp.R;

public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatusInput;
    private Button mSaveBtn;
    private ProgressDialog mProgress;

    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.status_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatusInput = (TextInputLayout) findViewById(R.id.status_input);
        mSaveBtn = (Button) findViewById(R.id.status_save_btn);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid =  currentUser.getUid();
        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);


        String status_value = getIntent().getStringExtra("status_value");
        mStatusInput.getEditText().setText(status_value);


        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProgress = new ProgressDialog(StatusActivity.this);
                mProgress.setTitle("Saving Changes");
                mProgress.setMessage("plz wait while we save the changes");
                mProgress.show();

                String status = mStatusInput.getEditText().getText().toString();

                mUserDatabaseRef.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(StatusActivity.this,task.toString(),Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                        else
                        {
                            Toast.makeText(StatusActivity.this,"There was error in saving changes",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }


    @Override
    protected void onStart()
    {
        super.onStart();
        mUserDatabaseRef.child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUserDatabaseRef.child("online").setValue(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
