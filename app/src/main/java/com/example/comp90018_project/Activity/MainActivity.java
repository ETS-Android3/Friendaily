package com.example.comp90018_project.Activity;

import androidx.appcompat.app.AppCompatActivity;

import com.example.comp90018_project.R;
import com.example.comp90018_project.adapter.FriendAdapter;
import com.example.comp90018_project.adapter.MomentAdapter;
import com.example.comp90018_project.adapter.ProfileAdapter;
import com.example.comp90018_project.model.User;
import com.example.comp90018_project.Util.LoadImageView;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.internal.NavigationMenuItemView;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import static com.example.comp90018_project.Activity.LoginActivity.USERID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "main";
    private FirebaseFirestore mDB;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    // UI Object
    private TextView txt_topbar;
    private ImageView channel;
    private ImageView message;
    private ImageView setting;
    private FrameLayout ly_content;
    private ImageView find_new_friend;

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
    private ListView MomentListview;
    private Button edit_user;
    private Button postMomentButton;
    private User myInfo;
    private User user;


    public static final String EXTRA_MESSAGE = "com.example.comp90018_project.MAIN_MESSAGE";


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
            setContentView(R.layout.activity_main);
            momentView();
            if (getSupportActionBar() != null) {
                getSupportActionBar().hide();
            }

            fragmentManager = getSupportFragmentManager();
            bindViews();
            findNewFriendView();
            postMomentView();
            // channel.performClick()

            initView();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void findNewFriendView() {
        find_new_friend = (ImageView)findViewById(R.id.addFriendImage);
        find_new_friend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FindNewFriendActivity.class);
                startActivity(intent);
            }
        });
    }

    private void postMomentView() {
        postMomentButton = (Button) findViewById(R.id.postMomentButton);
        postMomentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PostMomentActivity.class);
                startActivity(intent);
            }
        });
    }

    private void bindViews() {
        txt_topbar = (TextView)findViewById(R.id.txt_explore);
        channel = (ImageView)findViewById(R.id.channel);
        message = (ImageView) findViewById(R.id.message);
        setting = (ImageView)findViewById(R.id.setting);
//        ly_content = (FrameLayout) findViewById(R.id.ly_content);

        channel.setOnClickListener(this);
        message.setOnClickListener(this);
//        message.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(MainActivity.this, PostMomentActivity.class);
//                startActivity(intent);
//            }
//        });
        setting.setOnClickListener(this);
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
        edit_user = headerView.findViewById(R.id.profileButton);

        navigationView.setBackgroundColor(Color.parseColor("#3c3c3c"));
        navigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")));
//        navigationView.setItemTextColor(ColorStateList.valueOf(Color.parseColor("#F88A99")));

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

        edit_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        // navigation
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item1:
                        Intent intent = new Intent(MainActivity.this, MyCollectionActivity.class);
                        startActivity(intent);
                        break;
                    case R.id.menu_item2:
                        Intent intent2 = new Intent(MainActivity.this, MyLikeActivity.class);
                        startActivity(intent2);
                        break;
                    case R.id.menu_item3:
                        Intent intent3 = new Intent(MainActivity.this, MyMomentActivity.class);
                        startActivity(intent3);
                        break;
                    case R.id.menu_item4:
                        Intent intent4 = new Intent(MainActivity.this, ShowFriendActivity.class);
                        startActivity(intent4);
                        break;
                    case R.id.menu_item5:
                        Intent intent5 = new Intent(MainActivity.this, PostMomentActivity.class);
                        startActivity(intent5);
                        break;
                    case R.id.menu_item6:
                        Intent intent6 = new Intent(MainActivity.this, GeoQueryActivity.class);
                        startActivity(intent6);
                        break;
                    case R.id.menu_item7:
                        //Messages
                        Intent intent7 = new Intent(MainActivity.this, MessageActivity.class);
                        startActivity(intent7);
                        break;
                    case R.id.menu_item8:
                        // Log Out

//                        Map<String, Object> isOffline = new HashMap<String,Object>();
//                        isOffline.put("status",false);
//                        isOffline.put("last_status_changed",System.currentTimeMillis());
//                        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("status/"+currentUser.getUid());
//                        statusRef.updateChildren(isOffline);
//                        DatabaseReference localRef = FirebaseDatabase.getInstance().getReference("usersAvailable");
//                        localRef.child(mAuth.getCurrentUser().getUid()).removeValue();
//                        mAuth.signOut();
//
//                        Intent intent8 = new Intent();
//                        intent8.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent8.setClass(MainActivity.this, LoginActivity.class);
//                        startActivity(intent8);


                        logout();
                }
                return true;
            }
        });
    }

    private void momentView() {
        MomentListview = findViewById(R.id.momentsList);
        MomentListview.setAdapter(null);
        String UserID = currentUser.getUid();
        Log.d(TAG, UserID);
        // Get moment query and set adapter
        //CollectionReference momentRef = mDB.collection("moments");
        DocumentReference docRef = mDB.collection("moments").document(USERID);
        //Query query = momentRef.whereEqualTo("uid", USERID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    Log.d(TAG, "===============================TRY TO GET MOMENTS LIST ========================================");

                    Log.d(TAG, "===============================" +  task.getResult().getData().size() + "========================================");
                    ArrayList<Map<String, Object>> moments_list = (ArrayList<Map<String, Object>>) task.getResult().getData().get("all_friends_moments");
                    if (moments_list != null) {
                        Log.d(TAG, "moments list get");
                        List<Map<String, Object>> momentfound_list = new ArrayList<Map<String, Object>>();
                        for (int i=moments_list.size() - 1; i >= 0; i--) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            Map<String, Object> moment_map = moments_list.get(i);
                            String avatar_url = (String) moment_map.get("user_avatar_url");
                            map.put("avatar", avatar_url);
                            map.put("name", moment_map.get("username"));
                            map.put("content", moment_map.get("content"));
                            map.put("image", moment_map.get("image_download_url"));
                            map.put("timestamp", moment_map.get("date"));
                            momentfound_list.add(map);
                        }
                        MomentAdapter adapter = new MomentAdapter(MainActivity.this);
                        adapter.setMomentList(momentfound_list);
                        MomentListview.setAdapter(adapter);
                        MomentListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                LoadImageView avatar = view.findViewById(R.id.img_avatar);
                                avatar.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String selectUser = (String) moments_list.get(moments_list.size() - position - 1).get("uid");
                                        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                                        intent.putExtra(EXTRA_MESSAGE, selectUser);
                                        startActivity(intent);
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });
    }

    private void friendView() {
        MomentListview.setAdapter(null);
        String UserID = currentUser.getUid();
        CollectionReference friendRef = mDB.collection("users");
        Query query = friendRef.whereEqualTo("uid", UserID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    User user = new User(task.getResult().getDocuments().get(0).getData());
                    ArrayList<Map<String,Object>> friendList = (ArrayList<Map<String,Object>>) user.getaddedFriends();
                    if (friendList != null) {
                        Log.d(TAG, "friend list" + friendList);
                        List<Map<String, Object>> userfound_list = new ArrayList<Map<String, Object>>();
                        for (int i = 0; i < friendList.size(); i++) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            String avatar_url = (String) friendList.get(i).get("avatar_url");
                            if (avatar_url == null) {
                                map.put("avatar", R.drawable.default_user_avatar);
                            }
                            else {
                                map.put("avatar", friendList.get(i).get("avatar_url"));
                            }
                            map.put("name", friendList.get(i).get("username"));
                            userfound_list.add(map);
                        }
                        FriendAdapter adapter = new FriendAdapter(MainActivity.this);
                        adapter.setFriendList(userfound_list);
                        MomentListview.setAdapter(adapter);
                        MomentListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                String selectUser = (String) friendList.get(position).get("uid");
                                Log.d(TAG, "selected:" + selectUser);
                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                intent.putExtra(EXTRA_MESSAGE, selectUser);
                                startActivity(intent);
                            }
                        });
                    }
                }
            }
        });
    }

    private void profileView() {
        MomentListview.setAdapter(null);
        String UserID = currentUser.getUid();
        CollectionReference friendRef = mDB.collection("users");
        Query query = friendRef.whereEqualTo("uid", UserID);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                } else {
                    User user = new User(task.getResult().getDocuments().get(0).getData());
                    List<Map<String, Object>> userProfile = new ArrayList<Map<String, Object>>();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("avatar", user.getAvatarUrl());
                    map.put("username", user.getUsername());
                    map.put("email", user.getEmail());
                    map.put("uid", user.getUid());
                    map.put("bio", user.getBio());
                    userProfile.add(map);
                    ProfileAdapter adapter = new ProfileAdapter(MainActivity.this);
                    adapter.setProfileList(userProfile);
                    MomentListview.setAdapter(adapter);
                }
            }
        });
    }

    private void setNotSelected() {
        channel.setSelected(false);
        message.setSelected(false);
        setting.setSelected(false);
    }

    @Override
    public void onClick(View view) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

//        if(channelF != null) fragmentTransaction.hide(channelF);
//        if(announceF != null) fragmentTransaction.hide(announceF);
//        if(settingF != null) fragmentTransaction.hide(settingF);
        //todo: hideallfragment ??
        switch (view.getId()) {
            case R.id.channel:
                postMomentButton.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "This is text channel", Toast.LENGTH_SHORT).show();
                momentView();
//                setNotSelected();
//                txt_topbar.setText(R.string.tab_menu_normal);
//                channel.setSelected(true);
//                if (channelF == null) {
//                    channelF = new ChannelFragment();
//                    fragmentTransaction.add(R.id.ly_content, channelF);
//                } else {
//                    fragmentTransaction.show(channelF);
//                }
//                Intent intent = new Intent(MainActivity.this, ViewMomentActivity.class);
//                startActivity(intent);
                break;
            case R.id.message:
                postMomentButton.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "This is text message", Toast.LENGTH_SHORT).show();
                friendView();
//                setNotSelected();
//                txt_topbar.setText(R.string.tab_menu_message);
//                message.setSelected(true);
//                if (announceF == null) {
//                    announceF = new AnnounceFragment();
//                    fragmentTransaction.add(R.id.ly_content, announceF);
//                } else {
//                    fragmentTransaction.show(announceF);
//                }
                break;
            case R.id.setting:
                postMomentButton.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "This is text setting", Toast.LENGTH_SHORT).show();
                profileView();
//                setNotSelected();
//                txt_topbar.setText(R.string.tab_menu_setting);
//                setting.setSelected(true);
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

    private void logout() {
        Map<String, Object> isOffline = new HashMap<String,Object>();
        isOffline.put("status",false);
        isOffline.put("last_status_changed",System.currentTimeMillis());
        DatabaseReference statusRef = FirebaseDatabase.getInstance().getReference("status/"+currentUser.getUid());
        statusRef.updateChildren(isOffline);
        DatabaseReference localRef = FirebaseDatabase.getInstance().getReference("usersAvailable");
        localRef.child(mAuth.getCurrentUser().getUid()).removeValue();
        mAuth.signOut();
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    public void clickLogout(View view) {
        logout();
    }

    public void clickEditProfile(View view) {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, HomeActivity.class);
        startActivity(intent);
    }

}