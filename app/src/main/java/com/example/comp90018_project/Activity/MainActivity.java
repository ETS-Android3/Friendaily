package com.example.comp90018_project.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.R;
import com.example.comp90018_project.model.User;
import com.example.comp90018_project.Util.LoadImageView;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.comp90018_project.Activity.LoginActivity.USERID;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "main";
    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // UI Object
    private TextView txt_topbar;
    private TextView txt_channel;
    private TextView txt_message;
    private TextView txt_setting;
    private FrameLayout ly_content;
    private ImageButton find_new_friend_btn;

    // todo:Fragment Object
    private FragmentManager fragmentManager;
//    private ChannelFragment channelF;
//    private AnnounceFragment announceF;
//    private FriendFragment settingF;

    // menu bar
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MenuItem my_setting;
    ImageView menu_btn;

    // deal with user case
    private LoadImageView user_avatar;
    private TextView user_name;
    private TextView user_email;
    private TextView user_bio;
    private User myInfo;
    private User user;


    Handler handler = new Handler(Looper.getMainLooper()) {
        /**
         * Display user name, avatar and bio
         * @param message
         */
        @Override
        public void handleMessage(Message message) {
            myInfo = (User) message.obj;
            //todo: get the avatar url from firestore, if empty, set default avatar
            user_name.setText(myInfo.getUsername());
            user_email.setText(myInfo.getEmail());
            String avatar_url = (String)myInfo.getAvatarUrl();
            if (avatar_url == null) {
                user_avatar.setImageResource(R.drawable.default_user_avatar);
                Log.d(TAG, "avatar_url null ***********************");
            } else {
                //Log.d(TAG, "avatar_url existed ***********************");
                user_avatar.loadImageFromURL(avatar_url);
            }

            String currentBio = myInfo.getBio();
            if (currentBio == null) {
                user_bio.setText(R.string.empty_bio);
                Log.d(TAG, "bio is null [[[[[[[[[[[[[[[[[[[[[[[[[[");
            } else {
                user_bio.setText(currentBio);
                Log.d(TAG, "bio is not  empty [[[[[[[[[[[[[[[[[[[[[[[[[[");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDB = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        //Check whether this user has login; if not, return to login page
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null){
            reload();
        }
        else{
            USERID = currentUser.getUid();
            Log.i(TAG, "test");
            setContentView(R.layout.activity_main);
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            fragmentManager = getSupportFragmentManager();
            bindViews();
            findNewFriendView();
            // txt_channel.performClick()

            initView();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void findNewFriendView() {
        find_new_friend_btn = (ImageButton)findViewById(R.id.addFriendButton);
        find_new_friend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FindNewFriendActivity.class);
                startActivity(intent);
            }
        });
    }

    private void bindViews() {
        txt_topbar = (TextView)findViewById(R.id.txt_topbar);
        txt_channel = (TextView)findViewById(R.id.txt_channel);
        txt_message = (TextView)findViewById(R.id.txt_message);
        txt_setting = (TextView)findViewById(R.id.txt_setting);
        ly_content = (FrameLayout) findViewById(R.id.ly_content);

        txt_channel.setOnClickListener(this);
        txt_message.setOnClickListener(this);
        txt_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PostMomentActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initView() {
        drawerLayout = findViewById(R.id.activity_na);
        navigationView = findViewById(R.id.nav);
        menu_btn = findViewById(R.id.iv_menu);

        View headerView = navigationView.getHeaderView(0);

        //todo: get from firestore and set value for user_image, user_name and user_sign, using handler
        user_avatar = headerView.findViewById(R.id.iv_menu_user);
        user_name = headerView.findViewById(R.id.tv_menu_user);
        user_email = headerView.findViewById(R.id.tv_menu_useremail);
        user_bio = headerView.findViewById(R.id.tv_menu_usersign);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "!!!!!!!!!!!!!!!!!!!!!" + USERID + "!!!!!!!!!!!!!!!!!!!!!!!!");
                // get from firestore
                DocumentReference docRef = mDB.collection("users").document(USERID);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            String username = (String) document.getData().get("username");
                            String email = (String) document.getData().get("email");
                            String password = (String) document.getData().get("password");
                            String bio = (String) document.getData().get("bio");
                            String avatarUrl = (String) document.getData().get("avatar_url");
                            if (avatarUrl == null) {
                                Log.d(TAG, "!!!!!!!!!!!!!!!!AVATAR URL NULL!!!!!!!");
                            }
                            myInfo = new User(USERID, email, username, password, bio, avatarUrl);
                            handler.sendMessage(handler.obtainMessage(1, myInfo));
                        }
                    }
                });
            }
        }).start();


        // toggle the menu button
        menu_btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if (drawerLayout.isDrawerOpen(navigationView)) {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    findUser();
                    drawerLayout.openDrawer(navigationView);
                    NavigationMenuItemView message = findViewById(R.id.menu_item7);
                    if (user != null && user.getpendingFriends() != null && user.getpendingFriends().size() > 0) {
                        message.setIcon(getDrawable(R.drawable.new_message));
                        Log.i(TAG, "New friend request!");
                    }
                    else {
                        message.setIcon(getDrawable(R.drawable.message));
                        Log.i(TAG, "No new friend request!");
                    }
                }
            }
        });

        // navigation
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item1:
//                        Intent intent = new Intent(MainActivity.this, FindNewFriendActivity.class);
//                        startActivity(intent);
                        break;
                    case R.id.menu_item2:
//                        Intent intent2 = new Intent(MainActivity.this, MyLikeActivity.class);
//                        startActivity(intent2);
                        break;
                    case R.id.menu_item3:
//                        Intent intent3 = new Intent(MainActivity.this, CommentActivity.class);
//                        startActivity(intent3);
                        break;
                    case R.id.menu_item4:
                        Intent intent4 = new Intent(MainActivity.this, ShowFriendActivity.class);
                        startActivity(intent4);
                        break;
                    case R.id.menu_item5:
//                        Intent intent5 = new Intent(MainActivity.this, MyMomentActivity.class);
//                        startActivity(intent5);
                        break;
                    case R.id.menu_item6:
                        //Setting avatar and bio
                        Intent intent6 = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent6);
                        break;
                    case R.id.menu_item7:
                        //Messages
                        Intent intent7 = new Intent(MainActivity.this, MessageActivity.class);
                        startActivity(intent7);
                        break;
                    case R.id.menu_item8:
                        // Log Out
                        Map<String, Object> isOffline = new HashMap<String,Object>();
                        isOffline.put("status",false);
                        isOffline.put("last_status_changed",System.currentTimeMillis());
                        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("status/"+currentUser.getUid());
                        statusRef.updateChildren(isOffline);
                        mAuth.signOut();
                        Intent intent = new Intent();
                        intent.setClass(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                }
                return true;
            }
        });
    }

    private void setNotSelected() {
        txt_channel.setSelected(false);
        txt_message.setSelected(false);
        txt_setting.setSelected(false);
    }

    @Override
    public void onClick(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

//        if(channelF != null) fragmentTransaction.hide(channelF);
//        if(announceF != null) fragmentTransaction.hide(announceF);
//        if(settingF != null) fragmentTransaction.hide(settingF);
        //todo: hideallfragment ??
        switch (view.getId()) {
            case R.id.txt_channel:
                Toast.makeText(MainActivity.this, "This is text channel", Toast.LENGTH_SHORT).show();
//                setNotSelected();
//                txt_topbar.setText(R.string.tab_menu_normal);
//                txt_channel.setSelected(true);
//                if (channelF == null) {
//                    channelF = new ChannelFragment();
//                    fragmentTransaction.add(R.id.ly_content, channelF);
//                } else {
//                    fragmentTransaction.show(channelF);
//                }
                break;
            case R.id.txt_message:
                Toast.makeText(MainActivity.this, "This is text message", Toast.LENGTH_SHORT).show();
//                setNotSelected();
//                txt_topbar.setText(R.string.tab_menu_message);
//                txt_message.setSelected(true);
//                if (announceF == null) {
//                    announceF = new AnnounceFragment();
//                    fragmentTransaction.add(R.id.ly_content, announceF);
//                } else {
//                    fragmentTransaction.show(announceF);
//                }
                break;
            case R.id.txt_setting:
                Toast.makeText(MainActivity.this, "This is text setting", Toast.LENGTH_SHORT).show();
//                setNotSelected();
//                txt_topbar.setText(R.string.tab_menu_setting);
//                txt_setting.setSelected(true);
//                if (settingF == null) {
//                    settingF = new FriendFragment();
//                    fragmentTransaction.add(R.id.ly_content, settingF);
//                } else {
//                    fragmentTransaction.show(settingF);
//                }
                break;
        }
        fragmentTransaction.commitAllowingStateLoss();
    }
    //if user don't log in, return to login page
    private void reload(){
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);
    }

    private void findUser() {
        CollectionReference userRef = mDB.collection("users");
        Query query = userRef.whereEqualTo("uid", USERID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                user = new User(task.getResult().getDocuments().get(0).getData());
            }
        });
    }

}