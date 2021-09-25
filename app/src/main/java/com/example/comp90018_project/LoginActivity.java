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

public class LoginActivity extends AppCompatActivity {

    private Button registerButton;
    private Button loginButton;
    private EditText email;
    private EditText password;
    private FirebaseAuth mAuth;
    String TAG = "login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.log_in);
        loginEvent();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Check if user is signed in when we start login activity
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        if (currentUser != null){
//            reload();
//        }
    }

    private void loginEvent() {

        findAllView();

        loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();
                        System.out.println("account:" + emailStr);
                        System.out.println("password:" + passwordStr);
                        signIn(emailStr,passwordStr);
                    }
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
    private void findAllView() {
        email = findViewById(R.id.editLoginAccount);
        password = findViewById(R.id.editLoginPassword);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.createAccountButton);
    }
}