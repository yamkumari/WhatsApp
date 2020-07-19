package com.apps.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.icu.text.Edits;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessages;
    private String currentGroupName;
    private String currentUsername,currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef,groupNameRef,groupMessageRefKey;
    private String currentDate,currentTime;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        // getting the group name for group fragment
        currentGroupName = getIntent().getExtras().get("GroupName").toString();
        Toast.makeText(this, "Group Chat Activity", Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        intialiseFields();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessageInfoToDatabase();
                // after sending the message we want to clear that edit text
                userMessageInput.setText("");
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }



    private void intialiseFields()
    {
        mToolbar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton  = findViewById(R.id.send_button_group);
        userMessageInput = findViewById(R.id.group_message_input);
        mScrollView = findViewById(R.id.scroll_view_group_chat);
        displayTextMessages = findViewById(R.id.group_chat_text_display);

    }

    private void getUserInfo()
    {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                  if(snapshot.exists())
                  {
                      currentUsername = snapshot.child("name").getValue().toString();

                  }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void sendMessageInfoToDatabase()
    {

        String message = userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();
        // after going to the group - groupName - key (the key will be created with push method)
        if(TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "please, write a message", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar callForDate =  Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("MMM dd,YYYY");
            currentDate  = currentDateFormat.format(callForDate.getTime());
            // string current date is converted to time to store the date

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());



            // saving in the firebase
            HashMap<String,Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageRefKey = groupNameRef.child(messageKey);
            // getting the message key reference in the database

            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUsername);
            messageInfoMap.put("message",message);
            messageInfoMap.put("date",currentDate);
            messageInfoMap.put("time",currentTime);

            groupMessageRefKey.updateChildren(messageInfoMap);
        }
    }
/*
    @Override
    protected void onStart()
    {
        super.onStart();
        // when the user click the any group name it should show the previous messages
        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,  String s)
            {
                // if group name exits
                if(snapshot.exists())
                {
                    displayMessages(snapshot);
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, String s)
            {

                // if group name exits
                if(snapshot.exists())
                {
                    displayMessages(snapshot);
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    private void displayMessages(DataSnapshot snapshot)
    {

        Iterator iterator = snapshot.getChildren().iterator();
        while (iterator.hasNext())
        {
            String chatDate =(String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage =(String) ((DataSnapshot)iterator.next()).getValue();
            String chatName =(String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime=(String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName+":\n"+chatMessage+"\n"+chatDate+"  \n"+chatTime+"\n");

        }
    }

 */

    @Override
    protected void onStart()
    {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                if (dataSnapshot.exists())
                {
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void DisplayMessages(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while(iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatName + " :\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n\n");
            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            // so that user does not have to scroll down

        }
    }
}