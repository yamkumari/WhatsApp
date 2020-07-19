package com.apps.whatsapp;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{

   private String receiverUserId,current_state, sender_user_id;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineRequestMessageButton;

    private DatabaseReference userRef,chatReqRef,contactsRef,notificationRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
// wait just dont make anything check if you can start from noting // comment all
        //try again


     //   Toast.makeText(this, "User id is"+receiverUserId, Toast.LENGTH_SHORT).show();

        intialiseFields();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatReqRef= FirebaseDatabase.getInstance().getReference().child("Chat Request");
        contactsRef= FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef= FirebaseDatabase.getInstance().getReference().child("Notifications");
        retrieveUserInfo();
    }

    private void intialiseFields()
    {
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_profile_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineRequestMessageButton = findViewById(R.id.decline_message_request_button);

        current_state = "new";
        mAuth = FirebaseAuth.getInstance();
        sender_user_id = mAuth.getCurrentUser().getUid();
        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();


        sendMessageRequestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
    }
    private void retrieveUserInfo()
    {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                if((snapshot.exists())&& (snapshot.hasChild("image")))
                {
                    String userImage = snapshot.child("image").getValue().toString();
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    // to send message to the particular use
                    manageChatRequest();
                }
                else
                {
                    // if profile image is not set
                    String userName = snapshot.child("name").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequest();

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

    }

    private void manageChatRequest()
    {
        chatReqRef.child(sender_user_id).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

                if (snapshot.hasChild(receiverUserId))
                {
                   // String request_type = dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();
                    String request_type =snapshot.child(receiverUserId).child("request_type").getValue().toString();

                    if(request_type.equals("sent"))
                    {

                        current_state = "request_sent";
                        sendMessageRequestButton.setText("Cancel chat request");
                    }
                    // for receiver to accept the chat request
                    else  if(request_type.equals("received"))
                    {
                        current_state = "request_received";
                        sendMessageRequestButton.setText("Accept the request message");
                        declineRequestMessageButton.setVisibility(View.VISIBLE);
                        declineRequestMessageButton.setEnabled(true);

                        declineRequestMessageButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelChatRequest();
                            }
                        });

                    }



                }
                else
                {
                    contactsRef.child(sender_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.hasChild(receiverUserId))
                            {
                                current_state = "friends";
                                sendMessageRequestButton.setText("remove this contact");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });

        if(!sender_user_id.equals(receiverUserId))
        {
            // if id's are not equal, send message
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // means the button is not clickable
                    sendMessageRequestButton.setEnabled(false);
                    if(current_state.equals("new"))
                    {
                        sendChatRequest();
                    }
                    if(current_state.equals("request_sent"))
                    {
                        // now the user has the right to cancel that message request that has been sent
                        cancelChatRequest();

                    }
                    if(current_state.equals("request_received"))
                    {
                        // now the user has the right to cancel that message request that has been sent
                        acceptChatRequest();

                    }
                    if(current_state.equals("friends"))
                    {
                        // now the user has the right to cancel that message request that has been sent
                        removeSpecificContact();

                    }



                }
            });

        }
        else
        {
            // if that is my id then i will invisible the send request message
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void removeSpecificContact()
    {
       contactsRef.child(sender_user_id).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    contactsRef.child(receiverUserId).child(sender_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {

                            if(task.isSuccessful())
                            {
                                sendMessageRequestButton.setEnabled(true);
                                current_state ="new";
                                // now both are new to each other
                                sendMessageRequestButton.setText("Send Chat Request ");
                                declineRequestMessageButton.setVisibility(View.INVISIBLE);
                                declineRequestMessageButton.setEnabled(false);
                            }

                        }
                    });

                }

            }
        });

    }

    private void acceptChatRequest()
    {

        contactsRef.child(sender_user_id).child(receiverUserId).child("contacts list").setValue("saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful())
                {
                    contactsRef.child(receiverUserId).child(sender_user_id).child("contacts list").setValue("saved").
                            addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if(task.isSuccessful())
                            {
                                // this will remove the sender request
                                chatReqRef.child(sender_user_id).child(receiverUserId).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                // this will remove the sender request
                                                chatReqRef.child(receiverUserId).child(sender_user_id).removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {

                                                                sendMessageRequestButton.setEnabled(true);
                                                                current_state ="friends";
                                                                sendMessageRequestButton.setText("remove this contact");

                                                                declineRequestMessageButton.setVisibility(View.INVISIBLE);
                                                                declineRequestMessageButton.setEnabled(false);

                                                            }
                                                        });

                                            }
                                        });

                            }
                        }
                    });
                }
            }
        });
    }

    private void cancelChatRequest()
    {
        chatReqRef.child(sender_user_id).child(receiverUserId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                        chatReqRef.child(receiverUserId).child(sender_user_id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                        {
                            @Override
                            public void onComplete(@NonNull Task<Void> task)
                            {

                                if(task.isSuccessful())
                                {
                                    sendMessageRequestButton.setEnabled(true);
                                    current_state ="new";
                                    // now both are new to each other
                                    sendMessageRequestButton.setText("Send Chat Request ");
                                    declineRequestMessageButton.setVisibility(View.INVISIBLE);
                                    declineRequestMessageButton.setEnabled(false);
                                }

                            }
                        });

                }

            }
        });
    } // end of cancel chat request method

    private void sendChatRequest()
    {
        // for sender the message will be store in the sent
        chatReqRef.child(sender_user_id).child(receiverUserId).child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if(task.isSuccessful())
                {
                    chatReqRef.child(receiverUserId).child(sender_user_id).child("request_type").setValue("received")
                            .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        // for receiver the message will be store in the receiver

                                        // notification for friend request
                                        HashMap<String, String> chatNotificationMap = new HashMap<>();
                                        chatNotificationMap.put("from", sender_user_id);
                                        chatNotificationMap.put("type", "request");

                                        // use push because we have to create a key value
                                        notificationRef.child(receiverUserId).push().setValue(chatNotificationMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        if(task.isSuccessful())
                                                        {
                                                            sendMessageRequestButton.setEnabled(true);
                                                            current_state = "request_sent";
                                                            // now the user has the choice to cancel
                                                            sendMessageRequestButton.setText("Cancel chat request");

                                                        }
                                                    }
                                                });


                                    }

                            }
                        });
                    }
                }


        });

    }
}