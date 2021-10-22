package com.example.comp90018_project;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This create a instance which is used to change image
 * Uri and image_filename is the information about the new image
 * image is the current picture
 */
public class ImageHolder extends AppCompatActivity {
    private Uri image_uri = null;
    private String image_filename = null;
    private ImageView image;
    String currentPhotoPath;
    public static final int CAMERA_PERM_CODE = 101;

    public ImageHolder(ImageView image){
        this.image = image;
    }

    /**
     * Select image from album, set the imageview, get the link
     */
    public void selectImage(Context context) {
        final CharSequence[] options = { "Take photo", "Select from gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

    public void askCameraPermissions() {
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

    public String getName(){
        return image_filename;
    }

    public Uri getUri(){
        return image_uri;
    }
}
