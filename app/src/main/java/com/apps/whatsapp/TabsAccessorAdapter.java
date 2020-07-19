package com.apps.whatsapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabsAccessorAdapter extends FragmentPagerAdapter
{

    public TabsAccessorAdapter(@NonNull FragmentManager fm)
    {
        super(fm);
    }

    public TabsAccessorAdapter(@NonNull FragmentManager fm, int behavior)
    {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
            case 2:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment ;

            case 3:
                requestFragment requestFragment = new requestFragment();
                return requestFragment ;
            default:
                return null;

        }

    }

    @Override
    public int getCount()
    {
        return 4;
    }

    // setting tittle for each fragments

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Chats";


            case 1:
                return "Groups";

            case 2:
                return "Contacts";

            case 3:
                return "Request";

            default:
                return null;

        }
    }
}