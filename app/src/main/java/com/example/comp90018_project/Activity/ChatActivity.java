package com.example.comp90018_project.Activity;

import static java.lang.Double.max;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private FirebaseFirestore mDB=FirebaseFirestore.getInstance();;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private CollectionReference messageRef = FirebaseFirestore.getInstance().collection("messages");
    //It connect with message - [Another user's ID] - [My user ID] in database
    private CollectionReference otherMsgRef;
    //It connect with message - [My user ID] - [Another user's ID] in database
    private  CollectionReference myMsgRef;
    //Connect to message - [My user ID] - [Chat] - [Another user's ID]
    private DocumentReference myChat;
    //Connect to message - [Another user's ID] - [Chat] - [My user ID]
    private DocumentReference otherChat;

    private TextView username;
    private Button send;
    private EditText content;
    private ImageView backMain;
    private LinearLayout layout;
    private ScrollView scrollView;
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
        Log.d(TAG, "oncreated is called");
        hasInitialized = false;
        Intent intent = getIntent();        
        if (message == null) {
            message = intent.getStringExtra(ProfileActivity.EXTRA_MESSAGE);
        }
        if (message == null) {
            message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
        }
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

            layout = (LinearLayout)findViewById(R.id.layout1);
            scrollView = (ScrollView) findViewById(R.id.chatView);

            content.setText(null);
            backMain = findViewById(R.id.chatBackMain);

//            otherMsgRef = messageRef.document(message).collection(userId);
//            myMsgRef = messageRef.document(userId).collection(message);



            send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "onClick: You click the send button");
                    if(!hasInitialized){
                        Toast.makeText(ChatActivity.this, "Please wait for a second!", Toast.LENGTH_LONG).show();
                    }else{
                        if(content.getText().toString().length()==0){
                            Toast.makeText(ChatActivity.this, "You can't send a empty message", Toast.LENGTH_LONG).show();
                        }else{
                            Message msg = new Message(userId, message, content.getText().toString());
                            sendMessage(msg);
                            // addMessageBox("Me: \n" + msg.getContent(), 1);
                            content.setText(null);
                        }
                    }

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
        Log.i(TAG, "onResume: test test");
        Log.i(TAG, "hasInitialized " + hasInitialized);
        if (!isListening && hasInitialized == true) {
            //resume the Listener
            Log.i(TAG, "onResume: Set a listener for receiving again");
            setChatListener();
        }
        // setChatListener();
        // findChatUser();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop listening for receiving
        if(recieveMsg != null && isListening){
            Log.i(TAG, "onPause: Remove a listener for receiving");
            recieveMsg.remove();
            isListening = false;
        }
    }


    /**
     * Find the user we want to chat with
     * This page can be used only when this user is found
     */
    private void findChatUser() {
        CollectionReference userRef = mDB.collection("users");
        Log.i(TAG, "findChatUser: find user " + message);
        userRef.document(message).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot user) {
                if(user.getData().size() != 0)
                otherMsgRef = messageRef.document(message).collection(userId);
                myMsgRef = messageRef.document(userId).collection(message);
                otherChat = messageRef.document(message).collection("Chat").document(userId);
                myChat = messageRef.document(userId).collection("Chat").document(message);
                chatUser = new User(user.getData());
                username = (TextView) findViewById(R.id.chatName);
                username.setText(chatUser.getUsername());

                getandPrintAllMessages();
                // try to get the history
                // Try to get all the history messages
                // List<Message> allHistoryMessages = getAllMessages();
                getChatHistory();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                finish();
            }
        });
    }

    /**
     * Send message to the database
     * Update last_chat_date in both users' database
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

                DocumentSnapshot otherchat = transaction.get(otherChat);
                DocumentSnapshot mychat = transaction.get(myChat);
                Log.i(TAG, "otherchat: Now we should update chat time for others");
                if (otherchat.exists()) {
                    Log.i(TAG, "otherchat: Exist");
                    Double newCount = otherchat.getDouble("unread_count") + 1.0;
                    transaction.update(otherChat, "unread_count", newCount);
                    transaction.update(otherChat, "last_chat_date", date);
                } else {
                    Log.i(TAG, "otherchat: Nonexist");
                    Map<String, Object> chat_case = new HashMap<>();
                    chat_case.put("unread_count", 1.0);
                    chat_case.put("last_chat_date",date);
                    transaction.set(otherChat, chat_case);
                }
                if (mychat.exists()) {
                    transaction.update(myChat, "last_chat_date", date);
                } else {
                    Map<String, Object> chat_case = new HashMap<>();
                    chat_case.put("unread_count", 0);
                    chat_case.put("last_chat_date",date);
                    transaction.set(myChat, chat_case);
                }
                DocumentReference newChatRef = otherMsgRef.document();
                transaction.set(newChatRef,msg.toMap());
                msg.setMsgid(newChatRef.getId());
                msgList.add(msg);
                return null;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Send failed!", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Transaction failure.", e);
            }
        }).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.w(TAG, "Transaction success.");
                addMessageBox("Me: \n" + msg.getContent(), 1);
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
                    Log.d(TAG, "The content is " + newMsg.getContent());
                    //addMessageBox(chatUser.getUsername() + ": \n" + newMsg.getContent(), 2);
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
                        if (dc.getType().equals(DocumentChange.Type.ADDED)) {
                            QueryDocumentSnapshot msg = dc.getDocument();
                            Message newMsg = new Message(msg.getData());
                            newMsg.setMsgid(msg.getId());
                            addMessageBox(chatUser.getUsername() + ": \n" + newMsg.getContent(), 2);
                            //Mark msg as read
                            mDB.runTransaction(new Transaction.Function<Void>() {
                                @Nullable
                                @Override
                                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot chat = transaction.get(myChat);
                                    if (chat.exists()) {
                                        Double newCount = max(chat.getDouble("unread_count") - 1.0,0.0);
                                        transaction.update(myChat, "unread_count", newCount);
                                    }
                                    return null;
                                }
                            });
                            if(!msgList.contains(newMsg)){
                                Log.d(TAG, "Get a new msg!!!");
                                Log.i(TAG, "getChatHistory: now we get" + msg.getData().toString());
                                msgList.add(newMsg);
                            }else{
                                Log.i(TAG,"Got a old msg");
                            }
                        }
                    }
                    gotReceivedMsg = true;
                    //Set a flag to present that there are listeners for data change
                    isListening = true;
                }
            }
        });
    }

    public void getandPrintAllMessages() {
        List<Message> allMessages = new ArrayList<>();
        otherMsgRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(@NonNull QuerySnapshot msgs) {
                for (QueryDocumentSnapshot msg: msgs){
                    Message newMsg = new Message(msg.getData());
                    newMsg.setMsgid(msg.getId());
                    Log.d(TAG, "The content is " + newMsg.getContent());
                    //addMessageBox(chatUser.getUsername() + ": \n" + newMsg.getContent(), 2);
                    allMessages.add(newMsg);
                }
                myMsgRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(@NonNull QuerySnapshot msgs) {
                        for (QueryDocumentSnapshot msg: msgs){
                            Message newMsg = new Message(msg.getData());
                            newMsg.setMsgid(msg.getId());
                            Log.d(TAG, "The content is " + newMsg.getContent());
                            //addMessageBox(chatUser.getUsername() + ": \n" + newMsg.getContent(), 2);
                            allMessages.add(newMsg);
                            Log.d(TAG, "The content is " + newMsg.getContent());
                        }

                        Log.d(TAG, "How many !!!!!!!!!!" + allMessages.size() + "!!!!!!!!!!");
                        Collections.sort(allMessages, new Comparator<Message>() {
                            @Override
                            public int compare(Message t1, Message t2) {

                                if (t1.getDate() > t2.getDate()) {
                                    return 1;
                                } else {
                                    if (t1.getDate() == t2.getDate()) {
                                        return 0;
                                    } else {
                                        return -1;
                                    }
                                }
                            }
                        });
                        for (Message m : allMessages) {
                            if (m.getSender().equals(userId)) {
                                addMessageBox("Me: \n" + m.getContent(), 1);
                            } else {
                                addMessageBox(chatUser.getUsername() + ": \n" + m.getContent(), 2);
                            }
                        }
                    }
                });
            }
        });
    }


    public void addMessageBox(String message, int type) {
        TextView textView = new TextView(ChatActivity.this);
        textView.setText(message);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,0,0,10);

//        if (type == 1) {
//            lp.setMargins(0,0,0,10);
//        } else {
//            lp.setMargins(70,0,0,10);
//        }

        textView.setLayoutParams(lp);

        // type indicating my message or other guy's message
        if (type == 1) {
            textView.setBackgroundResource(R.drawable.message_box_me);
        } else {
            textView.setBackgroundResource(R.drawable.message_box_opposite);
        }

        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }

}
