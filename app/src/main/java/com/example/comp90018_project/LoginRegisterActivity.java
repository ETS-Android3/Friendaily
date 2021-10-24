package com.example.comp90018_project;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class LoginRegisterActivity extends AppCompatActivity {
    private Button login, register;
    private String TAG = "login/rigester";
    private FirebaseAuth mAuth;
    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loginregister);

        login = (Button)findViewById(R.id.btn_login);
        register = (Button)findViewById(R.id.btn_register);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginRegisterActivity.this,RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() != null){
            setPresence();
            Intent intent = new Intent(LoginRegisterActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }
    }

    /**
     * Build event listener for presence system
     */
    private void setPresence(){
        // Check the connection state of this user in realtime database
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid = currentUser.getUid();
        DatabaseReference connectRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("status/"+uid);

        //Add a event listener on user's status in real time database
        //Once status in real time database changed, sync it with firestore database
        connectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
//                    statusRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            final Boolean[] isOffline = {false};
//                            statusRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
//                                @Override
//                                public void onComplete(@NonNull Task<DataSnapshot> task) {
//                                    if (task.isSuccessful()) {
//                                        if (task.getResult() != null) {
//                                            Map<String, Object> status = (Map<String, Object>) task.getResult().getValue();
//                                            DocumentReference userRef = mDB.collection("users").document(uid);
//                                            userRef.update(status);
//                                            if (!(Boolean)status.get("status")) isOffline[0] = true;
//                                            Log.i(TAG, "This user change status in firestore");
//                                        }
//                                    }
//                                }
//                            });
//                            if (isOffline[0]) statusRef.removeEventListener(this);
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//                            Log.i(TAG,"Since this user has log off, this event listener has been removed");
//
//                        }
//                    });

                    //Change user's status in real time database
                    Log.i(TAG, " this user is online in realtime database");
                    Map<String, Object> isOnline = new HashMap<String,Object>();
                    isOnline.put("status",true);
                    isOnline.put("last_status_changed",System.currentTimeMillis());
                    statusRef.updateChildren(isOnline);
                    Map<String, Object> isOffline = new HashMap<String,Object>();
                    isOffline.put("status",false);
                    isOffline.put("last_status_changed",System.currentTimeMillis());
                    statusRef.onDisconnect().updateChildren(isOffline);

                }else {
                    Log.i(TAG, "Sorry this user is offline in realtime database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.i(TAG, "Errors occur in realtime database");
            }
        });

    }
}
