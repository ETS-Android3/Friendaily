package com.example.comp90018_project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private Button registerButton;
    private Button loginButton;
    private EditText account;
    private EditText password;
    private FirebaseAuth mAuth;
    private FirebaseFirestore mDB;
    private String emailReg ="^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
    String TAG = "login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDB = FirebaseFirestore.getInstance();
        setContentView(R.layout.log_in);
        loginEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Check if user is signed in when we start login activity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            reload();
        }
    }

    private void loginEvent() {

        findAllView();

        loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String accountStr = account.getText().toString();
                        String passwordStr = password.getText().toString();
                        if(!isEmpty(accountStr,passwordStr)){
                            System.out.println("account:" + accountStr);
                            System.out.println("password:" + passwordStr);
                            //If user give email, then sign in use given input
                            if(Pattern.matches(emailReg,accountStr)){
                                String emailStr = accountStr;
                                signIn(emailStr,passwordStr);
                            }else{
                                //Else we should find out the email address
                                CollectionReference userRef = mDB.collection("users");
                                Query query = userRef.whereEqualTo("username",accountStr);
                                query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            if (task.getResult().size() == 0) {
                                                //If this username isn't stored in database
                                                Log.i(TAG, "Sign in failed! This account don't exist");
                                                Toast.makeText(LoginActivity.this, "Register failed! This username has been used!", Toast.LENGTH_LONG).show();
                                            } else {
                                                //Get the corresponding email address
                                                String emailStr = (String) task.getResult().getDocuments().get(0).getData().get("email");
                                                signIn(emailStr,passwordStr);
                                            }
                                        }
                                    }
                                });
                            }
                        }else{
                            Log.i(TAG, "Login failed!");
                            Toast.makeText(LoginActivity.this, "Login failed! The account or password cannot be empty!", Toast.LENGTH_LONG).show();
                        }

                    };
                }
        );

        registerButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setClass(LoginActivity.this, RegisterActivity.class);
                        startActivity(intent);
                    }
                }
        );
    }

    private void signIn(String email, String password){
        //Using account and passowrd get before to login

        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //if signin successfully, go to the login page
                    Log.i(TAG, "Login succeeded!");
                    Toast.makeText(LoginActivity.this, "Login succeeded!", Toast.LENGTH_LONG).show();
                    reload();
                }else{
                    Log.i(TAG, "Login failed!");
                    Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //if user has log in, return to the homepage
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(LoginActivity.this, HomeActivity.class);
        startActivity(intent);
    }

    //Check input before sign in
    private Boolean isEmpty(String email, String password){
        if(email.length()==0||password.length()==0){
            return true;
        }else return false;
    }

    private void findAllView() {
        account = findViewById(R.id.editLoginAccount);
        password = findViewById(R.id.editLoginPassword);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.createAccountButton);
    }
}