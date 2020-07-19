package com.apps.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.tv.TvContract;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

    private String messageReceiverId, messageReceiverName, messageReceiverImage,messageSenderId;
    private TextView userName,userLastSeen;
    private CircleImageView userProfileImage;
    private Toolbar chatToolbar;
    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    // to retrieve the chats in the chat activity
    private final List<Messages> mMessagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayoutManager;
    private MessagesAdapter mMessagesAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime, saveCurrentDate;
    // check the files either they are images, pdf or docx
    private String checker="", myUrl ="";
    private Uri fileUri;
    private StorageTask uploadTask;

    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderId = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        loadingBar = new ProgressDialog(this);

        messageReceiverId = getIntent().getExtras().get( "visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().getString("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().getString("visit_image").toString();

        intialiseControllers();

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userProfileImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendMessage();
            }
        });

        displayLastSeen();

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "Pdf files",
                                "Ms Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select file");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i)
                    {
                        if(i==0)
                        {
                            // for images
                            checker = "image";
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent.createChooser(galleryIntent,"Select Image"), 438);

                         /*   Intent intent = new Intent();
                            intent.setType("images/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);

                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);

                          */

                        }
                        if(i==1)
                        {
                            // for pdf
                            checker = "pdf";
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent.createChooser(galleryIntent,"Select PDF Files"), 438);


                        }
                        if(i==2)
                        {
                            // for ms word
                            checker = "docx";
                            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(galleryIntent.createChooser(galleryIntent,"Select Doc File"), 438);


                        }

                    }
                });
                builder.show();

            }
        });

        rootRef.child("messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {

                Messages messagesObject = snapshot.getValue(Messages.class);
                mMessagesList.add(messagesObject);
                mMessagesAdapter.notifyDataSetChanged();

                // so that it automatically shows the new messages, scrolling part
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {

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

    } // end of on create method



    private void intialiseControllers()
    {
        chatToolbar = findViewById(R.id.chat_toolbar_);
        setSupportActionBar(chatToolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionbarView = layoutInflater.inflate(R.layout.custom_char_bar,null);
        actionBar.setCustomView(actionbarView);

        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);
        userProfileImage = findViewById(R.id.custom_profile_image);

        sendMessageButton = findViewById(R.id.message_sent_button_chat_activity);
        sendFilesButton = findViewById(R.id.files_sent_button_chat_activity);
        messageInputText = findViewById(R.id.input_message_chat_activity);

        // to retrieve chats in the android
        mMessagesAdapter = new MessagesAdapter(mMessagesList);
        userMessagesList = findViewById(R.id.private_messages_list_of_users);
        mLinearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(mLinearLayoutManager);
        userMessagesList.setAdapter(mMessagesAdapter);

        Calendar calendar  = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd,yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }

    // storing the image in the Database
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingBar.setTitle("Sending  Image");
            loadingBar.setMessage("please wait, just sending" );
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri =data.getData();
            if(!checker.equals("image"))
            {
                // if the user has not selected image after opening gallery, then that means that will be a pdf or docx

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("DocumentFiles");

                final String messageSenderRef = "messages/"+messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef = "messages/"+messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("messages").child(messageSenderId)
                        .child(messageReceiverId).push();
                // this will get the key

                final   String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+"."+".checker");
                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",
                                    task.getResult().getMetadata().getReference().getDownloadUrl().toString());
                            messageImageBody.put("name",fileUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",messageSenderId);
                            messageImageBody.put("to",messageReceiverId);
                            messageImageBody.put("messageId",messagePushId);
                            messageImageBody.put("time",saveCurrentTime);
                            messageImageBody.put("date",saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageImageBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageImageBody);

                            rootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this, "Error"+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int)progress+"% uploading");
                    }
                });

            }
            else if(checker.equals("image"))
            {
                // if checker is image
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "messages/"+messageSenderId+"/"+messageReceiverId;
                final String messageReceiverRef = "messages/"+messageReceiverId+"/"+messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("messages").child(messageSenderId)
                        .child(messageReceiverId).push();
                // this will get the key

              final   String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId+"."+".jpg");
                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception
                    {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();


                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task <Uri> task) {

                        if(task.isSuccessful())
                        {
                            Uri downloadUri = task.getResult();
                            myUrl =downloadUri.toString();


                            Map messageImageBody = new HashMap();
                            messageImageBody.put("message",myUrl);
                            messageImageBody.put("name",fileUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",messageSenderId);
                            messageImageBody.put("to",messageReceiverId);
                            messageImageBody.put("messageId",messagePushId);
                            messageImageBody.put("time",saveCurrentTime);
                            messageImageBody.put("date",saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageImageBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageImageBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "message sent succesfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingBar.dismiss();
                                        String message = task.getException().toString();
                                        Toast.makeText(ChatActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });



            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Nothing selected ", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void displayLastSeen()
    {
        rootRef.child("Users").child(messageReceiverId).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {

                        // for showing last seen
                        if(snapshot.child("userState").hasChild("State"))
                        {
                            String state = snapshot.child("userState").child("State").getValue().toString();
                            String date = snapshot.child("userState").child("date").getValue().toString();
                            String time= snapshot.child("userState").child("time").getValue().toString();

                            // checking if user is online or not
                            if(state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if(state.equals("offline"))
                            {
                                userLastSeen.setText("Last seen:  "+ date + " "+time);
                            }
                        }
                        else
                        {
                            userLastSeen.setText("offline");
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });
    }



    // to display the chat messages
  /*  @Override
    protected void onStart()
    {
        super.onStart();
        rootRef.child("messages").child(messageSenderId).child(messageReceiverId).addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {

                Messages messagesObject = snapshot.getValue(Messages.class);
                mMessagesList.add(messagesObject);
                mMessagesAdapter.notifyDataSetChanged();

                // so that it automatically shows the new messages, scrolling part
                userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot)
            {

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


    } // end of on start

   */

    private void sendMessage()
    {
        String messageText = messageInputText.getText().toString();
        if(TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this,"please first write your message", Toast.LENGTH_SHORT).show();

        }
        else
        {
            String messageSenderRef = "messages/"+messageSenderId+"/"+messageReceiverId;
            String messageReceiverRef = "messages/"+messageReceiverId+"/"+messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("messages").child(messageSenderId)
                    .child(messageReceiverId).push();
            // this will get the key

            String messagePushId = userMessageKeyRef.getKey();

            Map messageTextBody = new HashMap();
            messageTextBody.put("message",messageText);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderId);
            messageTextBody.put("to",messageReceiverId);
            messageTextBody.put("messageId",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);

            Map messageBodyDetails = new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful())
                    {
                        Toast.makeText(ChatActivity.this, "message sent succesfully", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String message = task.getException().toString();
                        Toast.makeText(ChatActivity.this, ""+message, Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });
        }
    }
}