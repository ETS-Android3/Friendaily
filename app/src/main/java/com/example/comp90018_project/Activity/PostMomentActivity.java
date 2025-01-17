package com.example.comp90018_project.Activity;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.example.comp90018_project.Util.BitmapTransfer;
import com.example.comp90018_project.model.Moment;
import com.example.comp90018_project.R;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PostMomentActivity extends AppCompatActivity {

    private EditText content;
    private ImageView image;
    private Uri image_downloaded_uri;
    private String image_bitmap_str;
    private Button btn_submit;
    StorageReference storageReference;
    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private String USERID;
    String currentPhotoPath;
    String TAG = "POST_Moment";
    Context context;
    private Uri image_uri = null;
    private String image_filename = null;
    public static final int CAMERA_PERM_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moment_post);

        content = (EditText) findViewById(R.id.moment_content);
        image = (ImageView) findViewById(R.id.moment_image);
        btn_submit = (Button) findViewById(R.id.moment_submit_btn);


        storageReference = FirebaseStorage.getInstance().getReference();
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }
        else{
            context = this;
            USERID = currentUser.getUid();
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImage();
                }
            });

            btn_submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addToFireStore();
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setClass(PostMomentActivity.this, MainActivity.class);
                    startActivity(intent);
                }
            });

        }
    }

    /**
     * Select image from album, set the imageview, get the link
     */
    private void selectImage() {
        final CharSequence[] options = { "Take photo", "Select from gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Please select a photo");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (options[i].equals("Take photo")) {
                    askCameraPermissions();
                } else if (options[i].equals("Select from gallery")) {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    choosePhotoResultLauncher.launch(intent);
                } else if (options[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }

            }
        });
        builder.show();
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }else {
            dispatchTakePictureIntent();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == CAMERA_PERM_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }else {
                Toast.makeText(this, "Camera Permission is Required to Use camera.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ActivityResultLauncher<Intent> takePhotoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d("tag", "reach here !!!!!!! ");
                    if(result.getResultCode() == RESULT_OK)
                    {
                        try
                        {
                            BitmapFactory.Options option = new BitmapFactory.Options();
                            option.inSampleSize = 4;
                            option.inPreferredConfig= Bitmap.Config.RGB_565;
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(image_uri), null, option);
                            bitmap = topSquareScale(bitmap);
                            image_bitmap_str = BitmapTransfer.convertBitmapToString(bitmap);
                            image.setImageBitmap(bitmap);
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    ActivityResultLauncher<Intent> choosePhotoResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Bitmap bitmap = null;
                        Uri contentUri = data.getData();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "JPEG_" + timeStamp +"."+getFileExt(contentUri);
                        Log.d("tag", "onActivityResult: Gallery Image Uri:  " +  imageFileName);
                        ContentResolver cr = PostMomentActivity.this.getContentResolver();
                        try {
                            bitmap = BitmapFactory.decodeStream(cr.openInputStream(contentUri));
                            int round = 2;
                            while (BitmapTransfer.convertBitmapToString(bitmap).length() > 20000) {
                                BitmapFactory.Options option = new BitmapFactory.Options();
                                option.inSampleSize = round;
                                option.inPreferredConfig= Bitmap.Config.RGB_565;
                                bitmap = BitmapFactory.decodeStream(cr.openInputStream(contentUri), null, option);
                                round += 1;
                            }
                            Log.i(TAG, "final size: " + BitmapTransfer.convertBitmapToString(bitmap).length());
                            //设置图片显示，可以看到效果
                        } catch (FileNotFoundException e) {
                            Log.e("Exception", e.getMessage(),e);
                        }

                        bitmap = topSquareScale(bitmap);
                        image.setImageBitmap(bitmap);
                        image_bitmap_str = BitmapTransfer.convertBitmapToString(bitmap);
                    }
                }
            }
    );

    private Bitmap topSquareScale(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }

        Bitmap finalBitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        int length;

        if (widthOrg != heightOrg) {
            if (widthOrg > heightOrg) {
                length = heightOrg;
            }
            else {
                length = widthOrg;
            }

            int xTopLeft = (widthOrg - length) / 2;
            int yTopLeft = (heightOrg - length) / 2;

            try{
                finalBitmap = Bitmap.createBitmap(bitmap, xTopLeft, yTopLeft, length, length);
            }
            catch(Exception e){
                return bitmap;
            }
            return finalBitmap;
        }
        return bitmap;
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = new File(getExternalCacheDir(),"output_image.jpg");
        try {
            if(photoFile.exists())
            {
                photoFile.delete();
            }
            photoFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            image_uri = FileProvider.getUriForFile(this,
                    "com.example.cameraalbumtest.fileprovider",
//                        "net.smallacademy.android.fileprovider",
                    photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
            takePhotoResultLauncher.launch(takePictureIntent);
        }
    }

    private void addToFireStore() {
        if (content != null || image_bitmap_str != null){
            // begin to add data only when user add a picture or write text

            // need to get the user_avtar_url and username
            CollectionReference friendRef = mDB.collection("users");
            Query query = friendRef.whereEqualTo("uid", USERID);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        String moment_text = null;

                        String moment_bitmap = null;
                        if(content != null) moment_text = content.getText().toString();
                        moment_bitmap = image_bitmap_str;

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Long timestamp = System.currentTimeMillis();
                        String date = simpleDateFormat.format(new Date(timestamp));


                        User user = new User(task.getResult().getDocuments().get(0).getData());
                        String username = user.getUsername();
                        String user_avatar_url = user.getAvatarUrl();
                        ArrayList<Map<String, Object>> friendList = (ArrayList<Map<String,Object>>) user.getaddedFriends();
                        Moment newMom = new Moment(USERID,date, moment_text, moment_bitmap, username, user_avatar_url);

                        Map<String, Object> newMonMap = newMom.toMap();

                        for (Map<String, Object> friend : friendList) {
                            String friend_uid = (String) friend.get("uid");
                            String friend_username = (String) friend.get("username");
                            postMoment(friend_uid, newMonMap, friend_username);
                        }
                        postMoment(USERID, newMonMap, username);
                    }
                }
            });

        }else Toast.makeText(PostMomentActivity.this, "Cannot post a empty moment", Toast.LENGTH_LONG).show();
    }

    private void postMoment(String userID, Map<String, Object> newMoment, String userName) {
        DocumentReference momentsRef = mDB.collection("moments").document(userID);
        mDB.runTransaction(new Transaction.Function<Void>() {

            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<Map<String, Object>> existing_moments = (ArrayList<Map<String, Object>>) transaction.get(momentsRef).get("all_friends_moments");
                existing_moments.add(newMoment);
                transaction.update(momentsRef, "all_friends_moments", existing_moments);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, userName + " Post successful!");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, userName + " Post failed!");
                // Toast.makeText(PostMomentActivity.this, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }


    /**
     * if user don't log in, return to login page
     */
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(PostMomentActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}
