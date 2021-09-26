package com.example.comp90018_project;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {

    private Button logoutButton;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_page);
        mAuth = FirebaseAuth.getInstance();
        findAllView();
    }

    @Override
    public void onStart() {
        super.onStart();
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Log out
                mAuth.signOut();
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, LoginActivity.class);
                startActivity(intent);

            }
        });
    }

    private void findAllView() {
        logoutButton = findViewById(R.id.logoutButton);
    }
}
