package com.example.comp90018_project;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.example.comp90018_project.LoginActivity.USERID;

public class HomeActivity extends AppCompatActivity {

    //private Button logoutButton;

    private FirebaseAuth mAuth;

    private EditText content;
    private ImageView image;
    private Uri image_uri;
    private Uri image_downloaded_uri;
    private String image_filename;
    private Button btn_update;
    String currentPhotoPath;
    StorageReference storageReference;
    private FirebaseFirestore mDB;

    String TAG = "home";

    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 105;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        mAuth = FirebaseAuth.getInstance();
        //findAllView();

        storageReference = FirebaseStorage.getInstance().getReference();
        mDB = FirebaseFirestore.getInstance();

        Log.i(TAG, "On Create");
        Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Log.i(TAG, "!!!!!!!!!!!!!" + USERID + "!!!!!!!!!!!!!!!");
        Log.i(TAG,"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        image = (ImageView) findViewById(R.id.edited_avatar2);
        content = (EditText) findViewById(R.id.edited_bio2);
        btn_update = (Button) findViewById(R.id.setting_update_btn);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String bio = content.getText().toString();
                Log.d(TAG, "bio is  =============================" + bio);
                if (image_uri != null) {
                    uploadImageToFirebase(image_filename, image_uri);
                    Log.d(TAG, "image downloaded uri is empty =============================");
                    if (image_downloaded_uri == null) {
                        Log.d(TAG, "image downloaded uri is empty =============================");
                    } else {
                        Log.d(TAG, "image downloaded uri is not empty =============================");
                    }
                } else {
                    Log.d(TAG, "image uri is empty =============================");
                }
            }
        });
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
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
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
                        uploadToFireStore(uri.toString(), content.getText().toString());
                        image_downloaded_uri = uri;


                        Log.d("tag", "onSuccess: Uploaded Image URl is " + uri.toString());

                        if (image_downloaded_uri == null) {
                            Log.d("tag", " image_downloaded_uri is null ))))))))))))))");
                        } else {
                            Log.d("tag", "image_downloaded_uri is not null }}}}}}}}}}} " + image_downloaded_uri);
                        }


                        Toast.makeText(HomeActivity.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
                    }
                });

                Toast.makeText(HomeActivity.this, "Image Is Uploaded.", Toast.LENGTH_SHORT).show();
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

    private void uploadToFireStore(String downloaded_uri, String updated_bio) {
        final DocumentReference sfDocRef = mDB.collection("users").document(USERID);
        Log.d(TAG, "UserID==============" + USERID);

        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sfDocRef);
                transaction.update(sfDocRef, "avatar_url", downloaded_uri);
                transaction.update(sfDocRef, "bio", updated_bio);
                // success
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "Transaction success!");
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Transaction failure.", e);
            }
        });

    }

}
