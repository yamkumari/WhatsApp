package com.apps.whatsapp;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class requestFragment extends Fragment
{
    private View RequestsFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference ChatRequestsRef, UsersRef, ContactsRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    public requestFragment() {// Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        RequestsFragmentView = inflater.inflate(R.layout.fragment_request, container, false);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestsRef = FirebaseDatabase.getInstance().getReference().child("Chat Request");
        ContactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        myRequestsList = (RecyclerView) RequestsFragmentView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));
        return RequestsFragmentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(ChatRequestsRef.child(currentUserID), Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts,RequetsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, RequetsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final RequetsViewHolder holder, int position, @NonNull Contacts model)
            {
                holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                // getting the id's of users who has sent me the request
                final String list_user_id = getRef(position).getKey();
                DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
                        {
                           String type = snapshot.getValue().toString();
                           if(type.equals("received"))
                           {
                               UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                                       if(snapshot.hasChild("image"))
                                       {
                                           final String requestUserImage = snapshot.child("image").getValue().toString();
                                         Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
                                       }
                                       final String requestUserName = snapshot.child("name").getValue().toString();
                                       final String requestUserStatus = snapshot.child("status").getValue().toString();
                                       holder.userName.setText(requestUserName);
                                       holder.userStatus.setText("wants to add you as a friend");

                                       holder.itemView.setOnClickListener(new View.OnClickListener()
                                       {
                                           @Override
                                           public void onClick(View v)
                                           {
                                               CharSequence options[] = new CharSequence[]
                                                       {
                                                               "Accept",
                                                               "Cancel"
                                                       };
                                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                               builder.setTitle(requestUserName+"chat Request");

                                               builder.setItems(options, new DialogInterface.OnClickListener()
                                               {
                                                   @Override
                                                   public void onClick(DialogInterface dialog, int i)
                                                   {

                                                       // getting the position of the accept and cancel
                                                       if(i==0)
                                                       {
                                                           // i == 0 is accept
                                                           ContactsRef.child(currentUserID).child(list_user_id).child("Contacts")
                                                                   .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if(task.isSuccessful())
                                                                   {
                                                                       ContactsRef.child(list_user_id).child(currentUserID).child("Contacts")
                                                                               .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task) {
                                                                               if(task.isSuccessful())
                                                                               {

                                                                                   ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                                           .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                       @Override
                                                                                       public void onComplete(@NonNull Task<Void> task) {

                                                                                           if(task.isSuccessful())
                                                                                           {
                                                                                               ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                                                       .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                   @Override
                                                                                                   public void onComplete(@NonNull Task<Void> task) {

                                                                                                       if(task.isSuccessful())
                                                                                                       {
                                                                                                           Toast.makeText(getContext(), "new contacts saved", Toast.LENGTH_SHORT).show();
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
                                                           });
                                                       }
                                                       if(i==1)
                                                           ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                   .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {

                                                                   if(task.isSuccessful())
                                                                   {
                                                                       ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                               .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task) {

                                                                               if(task.isSuccessful())
                                                                               {
                                                                                   Toast.makeText(getContext(), "new contacts not saved", Toast.LENGTH_SHORT).show();
                                                                               }
                                                                           }
                                                                       });
                                                                   }
                                                               }
                                                           });


                                                   }
                                               });


                                               builder.show();
                                           }
                                       });
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError error) {

                                   }
                               });
                           }

                           else if(type.equals("sent"))
                           {
                               Button request_sent_button = holder.itemView.findViewById(R.id.request_accept_button);
                               request_sent_button.setText("request already sent");
                               holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);

                               UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(@NonNull DataSnapshot snapshot) {
                                       if(snapshot.hasChild("image"))
                                       {
                                           final String requestUserImage = snapshot.child("image").getValue().toString();
                                           Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.userProfileImage);
                                       }
                                       final String requestUserName = snapshot.child("name").getValue().toString();
                                       final String requestUserStatus = snapshot.child("status").getValue().toString();
                                       holder.userName.setText(requestUserName);
                                       holder.userStatus.setText("you have sent a request to"+requestUserName);

                                       holder.itemView.setOnClickListener(new View.OnClickListener()
                                       {
                                           @Override
                                           public void onClick(View v)
                                           {
                                               CharSequence options[] = new CharSequence[]
                                                       {
                                                               "Cancel Chat Request"
                                                       };
                                               AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                               builder.setTitle(requestUserName+"Already sent chat Request");

                                               builder.setItems(options, new DialogInterface.OnClickListener()
                                               {
                                                   @Override
                                                   public void onClick(DialogInterface dialog, int i)
                                                   {
                                                       // getting the position of the accept and cancel
                                                       if(i==0)
                                                           ChatRequestsRef.child(currentUserID).child(list_user_id)
                                                                   .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {

                                                                   if(task.isSuccessful())
                                                                   {
                                                                       ChatRequestsRef.child(list_user_id).child(currentUserID)
                                                                               .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<Void> task) {

                                                                               if(task.isSuccessful())
                                                                               {
                                                                                   Toast.makeText(getContext(), "you have cancelled the chat request", Toast.LENGTH_SHORT).show();
                                                                               }
                                                                           }
                                                                       });
                                                                   }
                                                               }
                                                           });


                                                   }
                                               });


                                               builder.show();
                                           }
                                       });
                                   }

                                   @Override
                                   public void onCancelled(@NonNull DatabaseError error) {

                                   }
                               });
                           }

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error)
                    {

                    }
                });




            }

            @NonNull
            @Override
            public RequetsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
               View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdisplayfriendslayout,parent,false);
               RequetsViewHolder holder = new RequetsViewHolder(view);
               return  holder;


            }
        };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();


    }
    public  static class  RequetsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView userProfileImage;
        Button acceptButton, cancelButton;

        public RequetsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName  = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            userProfileImage = itemView.findViewById(R.id.users_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            cancelButton = itemView.findViewById(R.id.request_cancel_button);


        }
    }

}