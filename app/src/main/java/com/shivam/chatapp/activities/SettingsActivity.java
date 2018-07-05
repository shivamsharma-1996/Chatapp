package com.shivam.chatapp.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shivam.chatapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity {

    private DatabaseReference mUserDatabaseRef;
    private FirebaseUser mCurrentUser;
    private StorageReference mImageStorage;


    private CircleImageView mDisplayImage;
    private TextView mName;
    private TextView mStatus;

    private Button mStatusbtn;
    private Button mChangeImg;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);


        mName = (TextView) findViewById(R.id.settings_display_name);
        mStatus = (TextView) findViewById(R.id.settings_status);
        mDisplayImage = (CircleImageView) findViewById(R.id.settings_image);

        mStatusbtn = (Button) findViewById(R.id.settings_status_btn);
        mChangeImg = (Button) findViewById(R.id.settings_img_btn);

        mImageStorage = FirebaseStorage.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid =  mCurrentUser.getUid();


        mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mUserDatabaseRef.keepSynced(true);

        mUserDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name =  dataSnapshot.child("name").getValue().toString();
                String status =   dataSnapshot.child("status").getValue().toString();
                final String image =   dataSnapshot.child("image").getValue().toString(); x
                String thumb_img  =   dataSnapshot.child("thumb_img").getValue().toString();


                mName.setText(name);
                mStatus.setText(status);

                if(!image.equals("default"))
                {
                   // Picasso.with(SettingsActivity.this).load(image).into(mDisplayImage);


                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.default_avatar).into(mDisplayImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.default_avatar).into(mDisplayImage);
                        }
                    });
                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SettingsActivity.this,"can't listen value",Toast.LENGTH_LONG).show();
            }
        });


        mStatusbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String status_value = mStatus.getText().toString();

                Intent status_intent = new Intent(SettingsActivity.this,StatusActivity.class);
                status_intent.putExtra("status_value",status_value);
                startActivity(status_intent);
            }
        });


        mChangeImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


               /* Intent gallery_intent = new Intent();
                gallery_intent.setType("image");
                gallery_intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallery_intent,"SELECT IMAGE"),GALLERY_PICK);
*/

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
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
            sendToStart();
        }
        else
        {
            mUserDatabaseRef.child("online").setValue(true);
        }
    }


    private void sendToStart()
    {
        Intent startIntent = new Intent(SettingsActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }





    @Override
    protected void onStop() {
        super.onStop();
        mUserDatabaseRef.child("online").setValue(false);
    }





    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {






        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                mProgressDialog = new ProgressDialog((SettingsActivity.this));
                mProgressDialog.setTitle("Uploading Image...");
                mProgressDialog.setMessage("Please wait while we upload and process the image");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();


                Uri resultUri = result.getUri();
                File thumb_filePath = new File(resultUri.getPath());

                String current_user_id = mCurrentUser.getUid();

                // image compression code for selected image

                Bitmap compressedImageBitmap = null;
                try
                {
                    compressedImageBitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(40)
                            .compressToBitmap(thumb_filePath);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();




                StorageReference filePath = mImageStorage.child("profile_images").child( current_user_id  +  ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child( current_user_id  +  ".jpg");


                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {


                        if(task.isSuccessful())
                        {
                            final String image_downlaodUrl = task.getResult().getDownloadUrl().toString(); //here we get the download url for the image

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {

                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if (thumb_task.isSuccessful())
                                    {
                                        Map update_hashmap = new HashMap<>();                 //if hashmap is used instead of Map object then current user storage only contains image & thumb_img then we get null pointer error so created a Map object
                                        update_hashmap.put("image",image_downlaodUrl);
                                        update_hashmap.put("thumb_img",thumb_downloadUrl);

                                        mUserDatabaseRef.updateChildren(update_hashmap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(SettingsActivity.this, "successfully uploaded", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(SettingsActivity.this, "not uploaded", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(SettingsActivity.this,"error in uploading thumbnail",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this,"error in uploading",Toast.LENGTH_LONG).show();
                        }
                    }
                });


                Toast.makeText(SettingsActivity.this,resultUri.toString(),Toast.LENGTH_LONG).show();
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(SettingsActivity.this,error.toString(),Toast.LENGTH_LONG).show();
            }
        }
    }

}
