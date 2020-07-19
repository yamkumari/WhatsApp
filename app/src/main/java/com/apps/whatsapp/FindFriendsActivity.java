package com.apps.whatsapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FindFriendsActivity extends AppCompatActivity implements RecyclerClick
{

    private Toolbar mToolbar;
    private RecyclerView recylerListFriends;
    private DatabaseReference userRef;

    public FriendsAdatper adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends2);

        recylerListFriends = findViewById(R.id.find_friends_recycer_list);
        recylerListFriends.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.find_friends_toolbar);
       setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");


        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userRef,Contacts.class).build();
        adapter = new FriendsAdatper(options, FindFriendsActivity.this, this);
        recylerListFriends.setAdapter(adapter);



    }



    @Override
    protected void onStart()
    {
        super.onStart();


        adapter.startListening(); // adapter.start listening here only
    }


    @Override
    public void OnClick(String your_data) {
        // do whatever with your click here
        // you can check with a toast
        // i want to pass the string to the profiel activity
        // make an intent here and


        Intent profileIntent = new Intent(FindFriendsActivity.this, ProfileActivity.class);
        profileIntent.putExtra("visit_user_id", your_data);
        startActivity(profileIntent);
       // Toast.makeText(this, "Profile" + your_data, Toast.LENGTH_SHORT).show();

        // thats all

    }
}