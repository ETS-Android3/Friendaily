package com.example.comp90018_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private Button signUpButton;
    private Button backLogin;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    private String emailReg ="^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseFirestore mDB;
    String TAG = "register";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDB = FirebaseFirestore.getInstance();
        registerEvent();
    }

    private void registerEvent() {

        findAllViews();

        signUpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        String usernameStr = username.getText().toString();
                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();
                        String confirmPasswordStr = confirmPassword.getText().toString();

                        //Check the format of input first, then check whether this username and email address can be used
                        boolean isCorrect = checkFormat(usernameStr,emailStr,passwordStr,confirmPasswordStr);
                        if (isCorrect) {
                            User user = new User();
                            user.setUsername(usernameStr);
                            user.setEmail(emailStr);
                            user.setPassword(Md5Util.md5(passwordStr));
                            checkUsername(user);
                        }
                    }
                }
        );

        backLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    private boolean checkFormat(String username, String email, String password, String confirmPass){
        if(username.length()==0||email.length()==0||password.length()==0||confirmPass.length()==0){
            Log.i(TAG, "Register failed! Must complete all the forms!");
            Toast.makeText(RegisterActivity.this, "Register failed! Must complete all the forms!", Toast.LENGTH_LONG).show();
            return false;
        }
        if(!password.equals(confirmPass)){
            Log.i(TAG, "Register failed! Password and Confirm password should be same!");
            Toast.makeText(RegisterActivity.this, "Register failed! Password and Confirm password should be same!", Toast.LENGTH_LONG).show();
            return false;
        }
        //Check whether it is correct email format
        if(!Pattern.matches(emailReg,email)){
            Log.i(TAG, "Register failed! Incorrect email format!");
            Toast.makeText(RegisterActivity.this, "Register failed! Incorrect email format!", Toast.LENGTH_LONG).show();
            return false;
        }
        if(password.length()<6){
            Log.i(TAG, "Register failed! Password should at least contain 6 symbols");
            Toast.makeText(RegisterActivity.this, "Register failed! Password should at least contain 6 symbols!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void checkUsername(User user){
        //Check whether this username has been used
        CollectionReference userRef = mDB.collection("users");
        Query query = userRef.whereEqualTo("username",user.getUsername());
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){
                    if(task.getResult().size()==0){
                        //If this username has not been used, then register
                        register(user);
                    }
                    else{
                        Log.i(TAG, "Register failed! This username has been used!");
                        Toast.makeText(RegisterActivity.this, "Register failed! This username has been used!", Toast.LENGTH_LONG).show();
                    }
                }else{
                    Log.i(TAG, "Register failed! Something wrong with checking username");
                    Toast.makeText(RegisterActivity.this, "Register failed! Something wrong with checking username", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //Create a new account in database
    private void register(User user){
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, store the information of user in database
                            user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            //Store the pair of username and email for login using real-time database
//                            mDatabase.child("users").child(user.getUid()).setValue(user);
//                            mDatabase.child("login").child(user.getUsername()).setValue(user.getEmail());
//                            RegisterActivity.this.finish();
                            addNewUser(user);
                        } else {
                            // If register fails, display a message to the user.
                            Log.i(TAG, "Register failed!");
                            Toast.makeText(RegisterActivity.this, "Register failed! Please check your network or this email has existed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
    // If there is a problem about missing google play service, please use Nougat version emulator
    // Store user information in firebase cloud
    private void addNewUser(User user){
        Map<String,String> newuser = new HashMap<>();
        newuser.put("uid",user.getUid());
        newuser.put("username",user.getUsername());
        newuser.put("email", user.getEmail());
        newuser.put("password",user.getPassword());
        mDB.collection("users").document(user.getUid()).set(newuser).addOnSuccessListener(new OnSuccessListener<Void>(){
            @Override
            public void onSuccess(Void unused) {
                Log.i(TAG, "Store user successful!");
                Intent intent = new Intent();
                intent.setClass(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i(TAG, "Register failed!");
                Toast.makeText(RegisterActivity.this, "Register failed! There is something wrong with database, please try again later", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void findAllViews() {
        signUpButton = findViewById(R.id.signUpButton);
        backLogin = findViewById(R.id.backLoginButton);
        username = findViewById(R.id.editRegisterUsername);
        email = findViewById(R.id.editRegisterEmail);
        password = findViewById(R.id.editRegisterPassword);
        confirmPassword = findViewById(R.id.editConfirmPassword);
    }
}
