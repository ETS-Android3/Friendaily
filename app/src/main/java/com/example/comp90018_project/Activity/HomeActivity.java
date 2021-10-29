package com.example.comp90018_project.Activity;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.Util.ImageHolder;
import com.example.comp90018_project.R;
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

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class HomeActivity extends AppCompatActivity {

    //private Button logoutButton;

    private FirebaseAuth mAuth;

    private EditText content;
    private ImageView image;
    private Uri image_uri;
    private Uri image_downloaded_uri = null;
    private String image_filename;
    private Button btn_update;
    String currentPhotoPath;
    StorageReference storageReference;
    private FirebaseFirestore mDB;
    String USERID = null;
    private ImageHolder imageHolder;
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

            image = (ImageView) findViewById(R.id.edited_avatar2);
            content = (EditText) findViewById(R.id.edited_bio2);
            btn_update = (Button) findViewById(R.id.setting_update_btn);
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    imageHolder = new ImageHolder(image);
//                    imageHolder.selectImage(context);
                    selectImage();
                }
            });
            btn_update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(image_uri != null && image_filename != null){
                        //If this user take a new picture
                        //If this user has taken a new image
                        uploadImageToFirebase(image_filename, image_uri);
                        Log.d(TAG, "image downloaded uri is empty =============================");
                        if (image_downloaded_uri == null) {
                            Log.d(TAG, "image downloaded uri is empty =============================");
                        } else {
                            Log.d(TAG, "image downloaded uri is not empty =============================");
                        }
                     }else uploadToFireStore();
                }
            });
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
                    //startActivityForResult(intent, 2);
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
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        File f = new File(currentPhotoPath);
                        image.setImageURI(Uri.fromFile(f));
                        Log.d("tag", "ABsolute Url of Image is " + Uri.fromFile(f));

                        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                        Uri contentUri = Uri.fromFile(f);
                        mediaScanIntent.setData(contentUri);
                        // result.sendBroadcast(mediaScanIntent);

                        // uploadImageToFirebase(f.getName(),contentUri);
                        // uploadImageToFirebase(f.getName(), contentUri);
                        image_uri = contentUri;
                        image_filename = f.getName();

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
                        Uri contentUri = data.getData();
                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "JPEG_" + timeStamp +"."+getFileExt(contentUri);
                        Log.d("tag", "onActivityResult: Gallery Image Uri:  " +  imageFileName);
                        image.setImageURI(contentUri);

                        // uploadImageToFirebase(imageFileName,contentUri);
                        image_uri = contentUri;
                        image_filename = imageFileName;
                    }
                }
            }
    );

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "net.smallacademy.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                //startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
                takePhotoResultLauncher.launch(takePictureIntent);
            }
        }
    }

    /**
     * This functionality is used to upload a avatar to database
     * If it success, global parameter image_downloaded_uri will be the location of this avatar; if not, empty
     * @param name filename
     * @param contentUri the location of avatar in the phone
     */
    private void uploadImageToFirebase(String name, Uri contentUri) {
        Log.d(TAG, "+++++++++++++ image name is +++++++++++++++++++ " + name);
        Log.d(TAG, "+++++++++++++ image uri is +++++++++++++++++++ " + contentUri.toString());
        final StorageReference image = storageReference.child("user_avatar/" + name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Toast.makeText(HomeActivity.this, "Image Is Uploaded. Please wait for updating...", Toast.LENGTH_SHORT).show();
                        image_downloaded_uri = uri;
                        uploadToFireStore();
                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());

                        if (image_downloaded_uri == null) {
                            Log.d("tag", " image_downloaded_uri is null ))))))))))))))");
                        } else {
                            Log.d("tag", "image_downloaded_uri is not null }}}}}}}}}}} " + image_downloaded_uri);
                        }


                        Toast.makeText(HomeActivity.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(HomeActivity.this, "Image failed to Upload.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(HomeActivity.this, "Upload Failled.", Toast.LENGTH_SHORT).show();
            }
        });

        Log.d(TAG, "image_downloaded_uri is " + image_downloaded_uri);

//        if (image_downloaded_uri != null) {
//            // upadate image downloaded uri to firestore
    }

    private void uploadToFireStore() {
        DocumentReference sfDocRef = mDB.collection("users").document(USERID);
        Log.d(TAG, "UserID==============" + USERID);

        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sfDocRef);
                if(image_downloaded_uri != null){
                    transaction.update(sfDocRef, "avatar_url", image_downloaded_uri.toString());
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
