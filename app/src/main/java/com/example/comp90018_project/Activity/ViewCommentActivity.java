package com.example.comp90018_project.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.adapter.CommentAdapter;
import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.adapter.MomentAdapter;
import com.example.comp90018_project.model.Comment;
import com.example.comp90018_project.model.Moment;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.example.comp90018_project.R;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.OnCompleteListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.comp90018_project.Activity.LoginActivity.USERID;

public class ViewCommentActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private FirebaseUser currentUser;
    private String moment_username;
    private String moment_date;
    private String moment_UserID;
    private EditText my_comment_content;
    private String commentor_userid;
    private Button post_my_comment;
    private ListView commentListView;
    private ImageView backMain;
    private CommentAdapter adapter;
    String TAG = "View Comment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_comment);
        Intent intent = getIntent();
        // get data from last activity (adapter)
        moment_date = intent.getStringExtra("date");
        moment_UserID = intent.getStringExtra("uid");
        moment_username = intent.getStringExtra("username");
        backMain = findViewById(R.id.commentBackMain);
        currentUser = mAuth.getCurrentUser();
        // initialize widget
        my_comment_content = (EditText) findViewById(R.id.leave_comment);
        post_my_comment = findViewById(R.id.post_comment);
        commentListView = (ListView) findViewById(R.id.comment_List);
        // initialize adapter
        adapter = new CommentAdapter(ViewCommentActivity.this);
        // commentListView.setAdapter(adapter);

        post_my_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentUser != null && my_comment_content.getText() != null && !my_comment_content.getText().toString().equals("")) {
                    Long timestamp = System.currentTimeMillis();
                    String content = my_comment_content.getText().toString();
                    String cid = moment_username + "_" + moment_date;
                    commentor_userid = currentUser.getUid();

                    // Now, need to find current user's username and avatar_url
                    CollectionReference userRef = mDB.collection("users");
                    Query query = userRef.whereEqualTo("uid", commentor_userid);
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                // get the user, should be me
                                User user = new User(task.getResult().getDocuments().get(0).getData());
                                String commentor_username = user.getUsername();
                                String commentor_user_avatar = user.getAvatarUrl();

                                Comment newComment = new Comment(commentor_userid, cid, timestamp, content, commentor_username, commentor_user_avatar);
                                postComment(commentor_userid, newComment.toMap(), commentor_username);

                            }

                        }
                    });
                }
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


        if (currentUser == null) {
            reload();
        } else {
            commentView();
        }
    }

    private void postComment(String commentor_userid, Map<String, Object> newComment, String commentor_username) {
        DocumentReference ref = mDB.collection("comments").document((String)newComment.get("cid"));
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                ArrayList<Map<String, Object>> existing_comments = (ArrayList<Map<String, Object>>) transaction.get(ref).get("comment_list");
                existing_comments.add(newComment);
                transaction.update(ref, "comment_list", existing_comments);
                return null;
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, commentor_username + " comment successful!");
                // notify the adapter to refresh
                commentListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                commentView();
                //Toast.makeText(ViewMomentActivity.class, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, commentor_username + " comment failed!");
                // Toast.makeText(PostMomentActivity.this, "Post failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void commentView() {
        String UserID = currentUser.getUid();
        Log.d(TAG, UserID);
        String cid = moment_username + "_" + moment_date;
        DocumentReference docRef = mDB.collection("comments").document(cid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        ArrayList<Map<String, Object>> comments_list = (ArrayList<Map<String, Object>>) task.getResult().getData().get("comment_list");
                        if (comments_list != null) {
                            Log.d(TAG, "comments list get");
                            // ListView commentListview = (ListView) findViewById(R.id.comment_List);
                            List<Map<String, Object>> commentfound_list = new ArrayList<Map<String, Object>>();
                            for (int i=0; i < comments_list.size(); i++) {
                                Map<String, Object> map = new HashMap<String, Object>();
                                Map<String, Object> comment_map = comments_list.get(i);
                                String avatar_url = (String) comment_map.get("user_avatar_url");
                                if (avatar_url == null) {
                                    map.put("avatar", "");
                                } else {
                                    map.put("avatar", comment_map.get("user_avatar_url"));
                                }
                                map.put("uid", comment_map.get("uid"));
                                map.put("cid", comment_map.get("cid"));
                                map.put("name", comment_map.get("username"));
                                map.put("content", comment_map.get("content"));
                                map.put("timestamp", comment_map.get("date"));
                                commentfound_list.add(map);
                            }
                            //CommentAdapter adapter = new CommentAdapter(ViewCommentActivity.this);
                            adapter.setCommentList(commentfound_list);
                            commentListView.setAdapter(adapter);
                        }
                    } else {
                        // if document not existed, set the document
                        Map<String, Object> comment_list = new HashMap<>();
                        comment_list.put("comment_list", new ArrayList<Comment>());
                        mDB.collection("comments").document(cid).set(comment_list);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void reload() {
        Intent intent = new Intent();
        intent.setClass(ViewCommentActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }
}
