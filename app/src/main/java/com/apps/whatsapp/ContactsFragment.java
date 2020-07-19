package com.apps.whatsapp;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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


public class ContactsFragment extends Fragment
{
    private View contactsView;
    private RecyclerView myContactList;
    private DatabaseReference contactsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {// Inflate the layout for this fragment
        contactsView= inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactList = (RecyclerView) contactsView.findViewById(R.id.recycler_contact_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        // people that are in his/her contacts

        return contactsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef,Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Contacts model)
            {
                String usersIds = getRef(position).getKey();
                usersRef.child(usersIds).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        if(snapshot.exists())
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
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("offline"))
                                {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else
                            {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if(snapshot.hasChild("image"))
                            {
                                String userImage = snapshot.child("image").getValue().toString();
                                String ProfileStatus= snapshot.child("status").getValue().toString();
                                String ProfileName = snapshot.child("name").getValue().toString();

                                holder.userName.setText(ProfileName);
                                holder.userStatus.setText(ProfileStatus);
                                Picasso.get().load(userImage).into(holder.profileImage);
                            }
                            else
                            {
                                String ProfileStatus= snapshot.child("status").getValue().toString();
                                String ProfileName = snapshot.child("name").getValue().toString();

                                holder.userName.setText(ProfileName);
                                holder.userStatus.setText(ProfileStatus);
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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usersdisplayfriendslayout,parent,false);
                // object of the contacts view holder class
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public  static  class  ContactsViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName,userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;
        private ContactsViewHolder(@NonNull View itemView)
        {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_profile_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }
}