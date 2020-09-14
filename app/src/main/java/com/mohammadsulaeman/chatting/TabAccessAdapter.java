package com.mohammadsulaeman.chatting;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TabAccessAdapter extends FragmentPagerAdapter {

    public TabAccessAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i)
    {
        switch (i)
        {
            case 0:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 1:
                ContactsFragment contactsFragment = new ContactsFragment();
                return contactsFragment;

            case 2:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;

            case 3:
                RequestFragment requestFragment = new RequestFragment();
                return requestFragment;

            default:

                return null;

        }
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "Chats";

            case 1:
                return  "Contact";

            case 2:
                return "Group";

            case 3:
                return "Requests";

            default:
                return null;
        }

    }
}
