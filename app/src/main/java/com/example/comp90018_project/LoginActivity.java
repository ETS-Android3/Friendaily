package com.example.comp90018_project;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    private Button registerButton;
    private Button loginButton;
    private EditText account;
    private EditText password;
    String TAG = "login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);

        loginEvent();
    }

    private void loginEvent() {

        findAllView();

        loginButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String accountStr = account.getText().toString();
                        String passwordStr = password.getText().toString();
                        System.out.println("account:" + accountStr);
                        System.out.println("password:" + passwordStr);

                        DatabaseService dataService = new DatabaseService();
                        boolean signal = dataService.login(accountStr, passwordStr);

                        if (signal) {
                            Log.i(TAG, "Login succeeded!");
                            Toast.makeText(LoginActivity.this, "Login succeeded!", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent();
                            intent.setClass(LoginActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }
                        else {
                            Log.i(TAG, "Login failed! The Username/Email and Password cannot be empty!");
                            Toast.makeText(LoginActivity.this, "Login failed!", Toast.LENGTH_LONG).show();
                        }
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

    private void findAllView() {
        account = findViewById(R.id.editLoginAccount);
        password = findViewById(R.id.editLoginPassword);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.createAccountButton);
    }
}