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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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
    String TAG = "register";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(); ;
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
                        boolean signal = checkFormat(usernameStr,emailStr,passwordStr,confirmPasswordStr);
                        if (signal) {
                            User user = new User();
                            user.setUsername(usernameStr);
                            user.setEmail(emailStr);
                            user.setPassword(passwordStr);
                            register(user);
                        }
                    }
                }
        );

        backLogin.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        RegisterActivity.this.finish();
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

    //Create a new account in database
    private void register(User user){
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, store the information of user in database
                            Log.i(TAG, "Begin to store!");
                            Log.i(TAG, "to String reference"+mDatabase.getRoot().toString());
                            user.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            mDatabase.child("users").child(user.getUid()).setValue(user);
                            Log.i(TAG, "end store!");
                            RegisterActivity.this.finish();
                        } else {
                            // If register fails, display a message to the user.
                            Log.i(TAG, "Register failed!");
                            Toast.makeText(RegisterActivity.this, "Register failed! Please check your network or this email has existed", Toast.LENGTH_LONG).show();
                        }
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
