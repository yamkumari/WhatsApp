package com.apps.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

public class MainActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;

    // if user is signed in
    FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    // accessing the TabAccessor Adapter java class
    private TabsAccessorAdapter myTabsAccessorAdapter;

    // getting the uid
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = findViewById(R.id.main_page_toolbar);
        mToolbar.setTitle("WhatsApp");
        setSupportActionBar(mToolbar); // replacing the tool bar

        // for fragments
        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);


        mAuth = FirebaseAuth.getInstance();
        rootRef = FirebaseDatabase.getInstance().getReference();


    } // end of on create

    // if user is null send to the login activity
    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            updateUserStatus("online");
            verifyUserExistence();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null)
        {
           updateUserStatus("offline");
        }
    }
    // on stop if the app crashes


    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null)
        {
            updateUserStatus("offline");
        }
    }

    private void verifyUserExistence()
    {
        // getting the current user id
        String currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if(snapshot.child("name").exists())
                {
                    Toast.makeText(MainActivity.this, "Welcome to the user", Toast.LENGTH_SHORT).show();
                }else
                {
                    sendUserToSettingActivity2();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }


    // adding options to the main activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {

       super.onCreateOptionsMenu(menu);
       // for retrieving the options in android
        
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return  true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        // by this method we will get the position of the options

         super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_options)
        {
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId()==R.id.main_find_friends_options)
        {
            sendUserToFindFriendsActivity();

        }
        if(item.getItemId()==R.id.main_setting_options)
        {
            sendUserToSettingActivity2();

        }
        if(item.getItemId()==R.id.main_create_group_options)
        {
          requestNewGroup();

        }
        return true;
    }



    private void requestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.AlertDialogue);
        builder.setTitle("Enter Group Name:");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("Group name");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName))
                {
                    Toast.makeText(MainActivity.this, "please write group name", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    getNewGroup(groupName);
                }

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();

            }
        });
        builder.show();


    }
// new group name
    private void getNewGroup(String groupName)
    {
        rootRef.child("Groups").child(groupName).setValue("").addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {

                if(task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Group is created", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendUserToSettingActivity2()
    {
        Intent intent = new Intent(MainActivity.this, SettingActivity2.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
       // finish();
    }
    private void sendUserToLoginActivity()
    {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void sendUserToFindFriendsActivity()
    {
        Intent intent = new Intent(MainActivity.this, FindFriendsActivity.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // finish();
    }

    private void updateUserStatus(String state)
    {
        String saveCurrentTime, saveCurrentDate;

        Calendar calendar  = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object>  onlineStateMap  = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("State",state);

        currentUserId = mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserId).child("userState").updateChildren(onlineStateMap);

        // users- current user id - userstate- (date,time,online,)
    }
}