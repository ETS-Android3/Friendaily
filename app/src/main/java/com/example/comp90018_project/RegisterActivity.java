package com.example.comp90018_project;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private Button signUpButton;
    private Button backLogin;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText confirmPassword;
    String TAG = "register";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        registerEvent();
    }

    private void registerEvent() {

        findAllViews();

        signUpButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        User user = new User();

                        String usernameStr = username.getText().toString();
                        String emailStr = email.getText().toString();
                        String passwordStr = password.getText().toString();
                        String confirmPasswordStr = confirmPassword.getText().toString();

                        if (passwordStr.equals(confirmPasswordStr)) {
                            user.setUsername(usernameStr);
                            user.setEmail(emailStr);
                            user.setPassword(passwordStr);

                            System.out.println(usernameStr);
                            System.out.println(emailStr);
                            System.out.println(passwordStr);
                            System.out.println(confirmPasswordStr);

                            DatabaseService dataService = new DatabaseService();
                            boolean signal = dataService.register(user);

                            if (signal) {
                                Log.i(TAG, "Register succeeded!");
                                Toast.makeText(RegisterActivity.this, "Register succeeded!", Toast.LENGTH_LONG).show();
                                RegisterActivity.this.finish();
                            }
                            else {
                                Log.i(TAG, "Register failed! The register information cannot be empty!");
                                Toast.makeText(RegisterActivity.this, "Register failed! The register information cannot be empty!", Toast.LENGTH_LONG).show();
                            }
                        }

                        else {
                            Log.i(TAG, "Register failed! Password and Confirm password should be same!");
                            Toast.makeText(RegisterActivity.this, "Register failed! Password and Confirm password should be same!", Toast.LENGTH_LONG).show();
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

    private void findAllViews() {
        signUpButton = findViewById(R.id.signUpButton);
        backLogin = findViewById(R.id.backLoginButton);
        username = findViewById(R.id.editRegisterUsername);
        email = findViewById(R.id.editRegisterEmail);
        password = findViewById(R.id.editRegisterPassword);
        confirmPassword = findViewById(R.id.editConfirmPassword);
    }
}
