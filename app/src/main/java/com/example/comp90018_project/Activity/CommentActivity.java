//package com.example.comp90018_project.Activity;
//
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.example.comp90018_project.model.Comment;
//import com.example.comp90018_project.R;
//import com.google.android.gms.tasks.OnFailureListener;
//import com.google.android.gms.tasks.OnSuccessListener;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.Date;
//
//public class CommentActivity extends AppCompatActivity {
//    private EditText content;
//    private FirebaseAuth mAuth;
//    private FirebaseFirestore mDB;
//    private String USERID;
//    // TODO: 2021/10/23 "mid" is been delivered from moment list
//    private String mid;
//    private static final String TAG = "Comment activity";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_comment);
//        mAuth = FirebaseAuth.getInstance();
//        setContentView(R.layout.activity_find_new_friend);
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser == null){
//            reload();
//        }else {
//            // TODO: 2021/10/23 Add construction of layout
//            USERID = currentUser.getUid();
//            // Get the id of moment from last activity
//            Intent intent = getIntent();
//            if(intent != null){
//                mid = intent.getStringExtra("mid");
//            }
//        }
//    }
//
//    /**
//     * Store comment to the firebase
//     */
//    private void addToFirebase(){
//        if(content != null){
//            Long timestamp = System.currentTimeMillis();
//            Date date = new Date(timestamp);
//            Comment newCom = new Comment(USERID,mid,timestamp,date, content.getText().toString());
//
//            // Task<DocumentReference> addedDocRef = mDB.collection("cities").add(newMoment);
//            mDB.collection("comments").document().set(newCom.toMap()).addOnSuccessListener(new OnSuccessListener<Void>(){
//                @Override
//                public void onSuccess(Void unused) {
//                    Log.i(TAG, "Post successful!");
//                    Intent intent = new Intent();
//                    intent.setClass(CommentActivity.this, MainActivity.class);
//                    finish();
//                    startActivity(intent);
//                }
//            }).addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    Log.i(TAG, "Post failed!");
//                    Toast.makeText(CommentActivity.this, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
//                }
//            });
//
//        }else Toast.makeText(CommentActivity.this, "This comment is empty!", Toast.LENGTH_LONG).show();
//
//    }
//
//    //if user has log in, return to the homepage
//    private void reload(){
//        Intent intent = new Intent();
//        intent.setClass(CommentActivity.this, MainActivity.class);
//        finish();
//        startActivity(intent);
//    }
//}