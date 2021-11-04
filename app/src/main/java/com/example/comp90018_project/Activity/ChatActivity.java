package com.example.comp90018_project.Activity;

import static java.lang.Double.max;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.R;
import com.example.comp90018_project.model.Message;
import com.example.comp90018_project.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore mDB=FirebaseFirestore.getInstance();;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference messageRef = FirebaseFirestore.getInstance().collection("messages");
    //It connect with message - [Another user's ID] - [My user ID] in database
    private CollectionReference otherMsgRef;
    //It connect with message - [My user ID] - [Another user's ID] in database
    private  CollectionReference myMsgRef;
    private TextView username;
    private Button send;
    private EditText content;
    private ImageView backMain;
    private ArrayList<Message> msgList;
    private User chatUser;
    private User user;
    private String userId;
    private String message;

    private ListenerRegistration recieveMsg;
    //An indicator that whether this listener is working
    private Boolean isListening;
    //An indicator for whether chat history can be get successfully
    private  Boolean gotSendMsg;
    private  Boolean gotReceivedMsg;
    //An indicator for finish initialization;
    private  Boolean hasInitialized;

    private static final String TAG = "Chat";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        hasInitialized = false;
        Intent intent = getIntent();
        message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
//        message = "CehhWfxzBBZ01Fz2MyNR7rAyAAq1";
//        mAuth.signInWithEmailAndPassword("111@111.com","123456");
//        message = "2TgJhepgxFSgDhZ1JpmOFgRlZ6j2";
//        mAuth.signInWithEmailAndPassword("jack@qq.com","1234567");
        FirebaseUser currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userId = currentUser.getUid();
        if (message == null) {
            //return to the last page
            finish();

        }else{
            isListening = false;
            gotSendMsg = false;
            gotReceivedMsg = false;
            setContentView(R.layout.activity_chat);
            send = findViewById(R.id.sendButton);
            content = findViewById(R.id.sentMessage);
            content.setText(null);
            backMain = findViewById(R.id.chatBackMain);
            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: You click the send button");
                    if(content.getText().toString().length()==0){
                        Toast.makeText(ChatActivity.this, "You can't send a empty message", Toast.LENGTH_LONG).show();
                    }else{
                        Message msg = new Message(userId, message, content.getText().toString());
                        sendMessage(msg);
                    }
                    content.setText(null);
                }
            });
            backMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            findChatUser();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isListening && hasInitialized) {
            //resume the Listener
            setChatListener();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop listening for receiving
        if(recieveMsg != null && isListening){
            recieveMsg.remove();
        }
    }

    /**
     * Find the user we want to chat with
     * This page can be used only when this user is found
     */
    private void findChatUser() {
        CollectionReference userRef = mDB.collection("users");
        Log.i(TAG, "findChatUser: find user " + message);
        userRef.document(message).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.getResult().getData() == null){
                        //Back to the last page
                        finish();
                    }else{
                        otherMsgRef = messageRef.document(message).collection(userId);
                        myMsgRef = messageRef.document(userId).collection(message);
                        chatUser = new User(task.getResult().getData());
                        username = (TextView) findViewById(R.id.chatName);
                        username.setText(chatUser.getUsername());
                        getChatHistory();
                }
            }
        });
    }

    /**
     * Send message to the database
     */
    private void sendMessage(Message msg) {
        //This message will be stored in messages-[other user' ID]-[this user's ID]
        //And the unread count under [this user's ID] will be updated
        mDB.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                //Send a message
                Long date = msg.getDate();
                DocumentReference newChatRef = otherMsgRef.document();
                newChatRef.set(msg.toMap()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(@NonNull Void unused) {
                        Log.i(TAG, "onSuccess: send successful!");
                        //Add this to msglist
                        msg.setMsgid(newChatRef.getId());
                        msgList.add(msg);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ChatActivity.this, "Send failed!", Toast.LENGTH_LONG).show();
                    }
                });
                DocumentSnapshot unread = transaction.get(otherMsgRef.document("unread"));
                if (unread.exists()) {
                    Double newCount = unread.getDouble("count") + 1.0;
                    transaction.update(otherMsgRef.document("unread"), "count", newCount);
                    transaction.update(otherMsgRef.document("unread"), "date", date);
                } else {
                    Map<String, Object> count = new HashMap<>();
                    count.put("count", 1.0);
                    count.put("date",date);
                    transaction.set(otherMsgRef.document("unread"), count);
                }
                Log.i(TAG, "update count successful!");
                return null;
            }
        });
    }

    /**
     * Get chat history
     * Add old message to message list
     */
    public void getChatHistory(){
        msgList = new ArrayList<>();
        //Get Chat history from the database
        //Set chat listener for receiving
        setChatListener();
        //Get the msg history send by myself
        otherMsgRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot msgs) {
                for (QueryDocumentSnapshot msg: msgs){
                    Message newMsg = new Message(msg.getData());
                    newMsg.setMsgid(msg.getId());
                    if(!msgList.contains(newMsg)){
                        msgList.add(newMsg);
                    }
                }
                gotSendMsg = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                gotSendMsg = false;
            }
        });
        hasInitialized = true;
    }

    public void setChatListener (){
        recieveMsg = myMsgRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error!=null){
                    Log.i(TAG,"Error happens when set up listener!");
                    gotReceivedMsg = false;
                    finish();
                }else{
                    for (DocumentChange dc : value.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                Log.d(TAG, "Add a new document");
                                if(!dc.getDocument().getId().equals("unread") ){
                                    Log.d(TAG, "Get a msg!!!");
                                    QueryDocumentSnapshot msg = dc.getDocument();
                                    Message newMsg = new Message(msg.getData());
                                    newMsg.setMsgid(msg.getId());
                                    mDB.runTransaction(new Transaction.Function<Void>() {
                                        @Nullable
                                        @Override
                                        public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                            DocumentSnapshot unread = transaction.get(myMsgRef.document("unread"));
                                            if (unread.exists()) {
                                                Double newCount = max(unread.getDouble("count") - 1.0,0.0);
                                                transaction.update(myMsgRef.document("unread"), "count", newCount);
                                            }
                                            return null;
                                        }
                                    });
                                    if(!msgList.contains(newMsg)){
                                        msgList.add(newMsg);
                                    }else{
                                        Log.i(TAG, "onEvent: We already have this msg");
                                    }
                                     Log.i(TAG, "getChatHistory: now we get" + msg.getData().toString());
                                }
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Get a unread msg");
                                break;
                            case REMOVED:
                                //Just in case
                                break;
                        }
                    }
                    gotReceivedMsg = true;
                    //Set a flag to present that there are listeners for data change
                    isListening = true;
                }
            }
        });
    }

}
