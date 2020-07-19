package com.apps.whatsapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment<ArraryList> extends Fragment
{


    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();

    private DatabaseReference groupRef;


    public GroupsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        // getting the group reference
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        // initialise fields
        InitialiseFields();

        // for clicking the groups and passing to chat activity
        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // when a group name is clicked then it will store the group name in the parentGroupName
                String parentGroupName = parent.getItemAtPosition(position).toString();

                Intent groupChatIntent = new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("GroupName",parentGroupName);
                startActivity(groupChatIntent);
                // the intent will pass the group name to the chats fragment


            }
        });
        // retrieving and group
        retrieveAndDisplayGroups();
        return groupFragmentView;


    }

    private void retrieveAndDisplayGroups()
    {
        groupRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                // It is an unordered collection of objects in which duplicate values cannot be stored.
                Set<String> set = new HashSet<>();
                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext())
                {
                    // prevent the duplication of values
                    //get key will get the name of the groups
                    set.add(((DataSnapshot)iterator.next()).getKey());

                }
                list_of_groups.clear();
                list_of_groups.addAll(set);
                mArrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }

    private void InitialiseFields()
    {
        list_view = (ListView) groupFragmentView.findViewById(R.id.list_view_group);
        mArrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);
        list_view.setAdapter(mArrayAdapter);



    }
}