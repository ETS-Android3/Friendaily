package com.example.comp90018_project.Activity;


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
import android.graphics.Paint;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.example.comp90018_project.R;
import com.example.comp90018_project.Util.BitmapTransfer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private EditText content;
    private TextView editAvatar;
    private ImageView image;
    private ImageView backMain;
    private String image_bitmap_str;
    private Uri image_uri;
    private Button btn_update;
    StorageReference storageReference;
    private FirebaseFirestore mDB;
    String USERID = null;
    Context context;
    String TAG = "home";

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        //Check whether this user has login; if not, return to login page
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }
        else{
            context = this;
            USERID = currentUser.getUid();
            setContentView(R.layout.home_page);
            //findAllView();

            storageReference = FirebaseStorage.getInstance().getReference();
            mDB = FirebaseFirestore.getInstance();

            Log.i(TAG, "On Create");

            image = (ImageView) findViewById(R.id.profileAvatarView);
            backMain = (ImageView) findViewById(R.id.editProfileBackMain);
            content = (EditText) findViewById(R.id.edited_bio);
            btn_update = (Button) findViewById(R.id.setting_update_btn);
            editAvatar = (TextView) findViewById(R.id.editAvatar);
            editAvatar.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
            editAvatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectImage();
                }
            });
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    uploadToFireStore();
                }
            });
            backMain.setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }
            );
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /*
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
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
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
                        ContentResolver cr = HomeActivity.this.getContentResolver();
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

                        // uploadImageToFirebase(imageFileName,contentUri);
//                        image.setImageURI(contentUri);

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

    private void uploadToFireStore() {
        DocumentReference sfDocRef = mDB.collection("users").document(USERID);
        Log.d(TAG, "UserID==============" + USERID);
        Log.d(TAG, "avatar_bitmap_str==============" + image_bitmap_str);

        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sfDocRef);
                if (image_bitmap_str != null) {
                    transaction.update(sfDocRef, "avatar_url", image_bitmap_str);
                }
                if(content != null){
                    transaction.update(sfDocRef, "bio", content.getText().toString());
                }

                // success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Transaction failure.", e);
            }
        });
    }

    /**
     * if user don't log in, return to login page
     */
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(HomeActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

}
