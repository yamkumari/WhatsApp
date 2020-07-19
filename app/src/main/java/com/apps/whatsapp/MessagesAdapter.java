package com.apps.whatsapp;

import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    // accessing our classes
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessagesAdapter(List<Messages> userMessagesList)
    {

        this.userMessagesList= userMessagesList;
    }

    public  class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView sendMessageText,receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            sendMessageText = itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.custom_messages_layout,parent,false);

        mAuth = FirebaseAuth.getInstance();
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {

        // display the messages, the user who is online will be the sender id
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messagesObjectAdapter = userMessagesList.get(position);

        // from the class messages, we are getting the values:
        String fromUserId = messagesObjectAdapter.getFrom();
        String fromMessageType = messagesObjectAdapter.getType();

        // now retrieving the user images form the current user id(fromUserId)
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.hasChild("image"))
                {
                    String receiverImage=snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.sendMessageText.setVisibility(View.GONE);

        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if(fromMessageType.equals("text"))
        {
            if(fromUserId.equals(messageSenderId))
            {
                holder.sendMessageText.setVisibility(View.VISIBLE);

                holder.sendMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                holder.sendMessageText.setTextColor(Color.BLACK);
                holder.sendMessageText.setText(messagesObjectAdapter.getMessage()+"\n \n"+ messagesObjectAdapter.getTime()
                +"-"+messagesObjectAdapter.getDate());
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messagesObjectAdapter.getMessage()+"\n \n"+ messagesObjectAdapter.getTime()
                        +"-"+messagesObjectAdapter.getDate());
            }

        }
        else if(fromMessageType.equals("image"))
        {
            if(fromUserId.equals(messageSenderId))
            {
                // means it is online user,current user
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messagesObjectAdapter.getMessage()).into(holder.messageSenderPicture);
            }
            else
            {
                // receiver
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messagesObjectAdapter.getMessage()).into(holder.messageReceiverPicture);

            }

        }
        else // for pdf and document
        {
            if(fromUserId.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);
                holder.messageSenderPicture.setBackgroundResource(R.drawable.file);

                // download file
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // it will get that file on which the user will click , from position
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });
            }
            else
            {
                // receiver
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                holder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

                // download file
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // it will get that file on which the user will click , from position
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                        holder.itemView.getContext().startActivity(intent);

                    }
                });

            }
        }


    }
    @Override
    public int getItemCount()
    {
       return  userMessagesList.size();
    }



}
