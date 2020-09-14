package com.mohammadsulaeman.chatting;


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


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends Fragment
{

    private View PrivateChatsView;
    private RecyclerView chatsLists;
    private DatabaseReference ChatsRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ChatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        PrivateChatsView =  inflater.inflate(R.layout.fragment_chats, container, false);
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        ChatsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        chatsLists = (RecyclerView) PrivateChatsView.findViewById(R.id.chats_list);
        chatsLists.setLayoutManager(new LinearLayoutManager(getContext()));

        return PrivateChatsView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ChatsViewHolder > adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatsViewHolder holder, int i, @NonNull Contacts contacts)
                    {
                        final String userIDs = getRef(i).getKey();
                        final String[] retImage = {"default_image"};

                        UsersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                if (dataSnapshot.exists())
                                {
                                    if (dataSnapshot.hasChild("image"))
                                    {
                                         retImage[0] = dataSnapshot.child("image").getValue().toString();
                                        Picasso.get().load(retImage[0]).into(holder.profileImage);
                                    }

                                    final String retName = dataSnapshot.child("name").getValue().toString();
                                    final String retStatus = dataSnapshot.child("status").getValue().toString();

                                    holder.userName.setText(retName);


                                    if (dataSnapshot.child("userState").hasChild("state"))
                                    {
                                        String state = dataSnapshot.child("userState").child("state").getValue().toString();
                                        String date = dataSnapshot.child("userState").child("date").getValue().toString();
                                        String time = dataSnapshot.child("userState").child("time").getValue().toString();

                                        if (state.equals("online"))
                                        {
                                            holder.userStatus.setText("online");
                                        }
                                         else if (state.equals("offline"))
                                        {
                                            holder.userStatus.setText("Last Seen ; " + date + "  " + time);
                                        }
                                    }
                                    else
                                    {
                                        holder.userStatus.setText("Offline");
                                    }



                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v)
                                        {
                                            Intent ChatIntent = new Intent(getContext(), ChatActivity.class);
                                            ChatIntent.putExtra("visit_user_id", userIDs);
                                            ChatIntent.putExtra("visit_user_name", retName);
                                            ChatIntent.putExtra("visit_image", retImage[0]);
                                            startActivity(ChatIntent);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
                    {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                       return new ChatsViewHolder(view);
                    }
                };

        chatsLists.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatsViewHolder extends  RecyclerView.ViewHolder
    {
        TextView userName, userStatus;
        CircleImageView profileImage;

        public ChatsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.users_set_nama);
            userStatus = itemView.findViewById(R.id.users_set_status);
            profileImage = itemView.findViewById(R.id.users_profile_image);
        }
    }
}
