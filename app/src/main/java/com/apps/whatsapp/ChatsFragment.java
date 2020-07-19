package com.apps.whatsapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatsFragment extends Fragment
{

    private  View privateChatsView;
    private RecyclerView chatList;
    private DatabaseReference chastRef,usersRef;
    private  String currentUserId;
    private FirebaseAuth mAuth;


    public ChatsFragment()
    {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        chastRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        chatList = privateChatsView.findViewById(R.id.recycler_chat_list_fragment);
        chatList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  privateChatsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chastRef,Contacts.class).build();

        FirebaseRecyclerAdapter<Contacts,chatsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, chatsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(@NonNull final chatsViewHolder holder, int position, @NonNull Contacts model)
            {
                final String usersId = getRef(position).getKey();
                final String[] retrImage = {"default_image"};

                usersRef.child(usersId).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                       if(snapshot.exists())
                       {
                           if(snapshot.hasChild("image"))
                           {
                               // it will check if the value of retrImage exist in snapshot it will replace with the user's profile image
                                retrImage[0] = snapshot.child("image").getValue().toString();
                               Picasso.get().load(retrImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                           }

                           final String retrName = snapshot.child("name").getValue().toString();
                           final String retrStatus = snapshot.child("status").getValue().toString();

                           holder.userName.setText(retrName);
                           holder.userStatus.setText("Last seen:  "+"\n"+ "Date"+ "Time");

                           // for showing last seen
                           if(snapshot.child("userState").hasChild("State"))
                           {
                               String State = snapshot.child("userState").child("State").getValue().toString();
                               String date = snapshot.child("userState").child("date").getValue().toString();
                               String time= snapshot.child("userState").child("time").getValue().toString();

                               // checking if user is online or not
                               if(State.equals("online"))
                               {
                                   holder.userStatus.setText("online");
                               }
                               else if(State.equals("offline"))
                               {
                                   holder.userStatus.setText("Last seen:  "+ date + " "+time);
                               }
                           }
                           else
                           {
                               holder.userStatus.setText("offline");
                           }


                           holder.itemView.setOnClickListener(new View.OnClickListener() {
                               @Override
                               public void onClick(View v)
                               {
                                   Intent intent = new Intent(getContext(),ChatActivity.class);
                                   intent.putExtra("visit_user_id",usersId);
                                   intent.putExtra("visit_user_name",retrName);
                                   intent.putExtra("visit_image", retrImage[0]);

                                   startActivity(intent);
                               }
                           });
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
            public chatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i)
            {
                View view = LayoutInflater.from(getContext()).inflate(R.layout.usersdisplayfriendslayout,parent,false);
                chatsViewHolder holder = new chatsViewHolder(view);
                return  holder;

            }
        };

        chatList.setAdapter(adapter);
        adapter.startListening();
    }

    public static  class chatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userName,userStatus;


        public chatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            profileImage = itemView.findViewById(R.id.users_profile_image);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);

        }
    }
}