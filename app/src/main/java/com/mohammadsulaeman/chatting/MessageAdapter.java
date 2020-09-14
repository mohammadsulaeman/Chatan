package com.mohammadsulaeman.chatting;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Messages> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef;

    public MessageAdapter(List<Messages> userMessageList)
    {
        this.userMessageList = userMessageList;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageSenderPicture,messageReceiverPicture;
        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText = (TextView) itemView.findViewById(R.id.sender_message_text);
            receiverMessageText = (TextView) itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage = (CircleImageView) itemView.findViewById(R.id.message_profile_image);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.message_sender_image_view);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.message_receiver_image_view);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custome_message_layout, parent, false);

        mAuth = FirebaseAuth.getInstance();


        return  new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, final int position)
    {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Messages messages = userMessageList.get(position);

        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.hasChild("image"))
                {
                    String receiverImage = dataSnapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverImage).into(holder.receiverProfileImage);


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });


        holder.receiverMessageText.setVisibility(View.GONE);
        holder.receiverProfileImage.setVisibility(View.GONE);
        holder.senderMessageText.setVisibility(View.GONE);
        holder.messageSenderPicture.setVisibility(View.GONE);
        holder.messageReceiverPicture.setVisibility(View.GONE);

        if (fromMessageType.equals("text"))
        {

            if (fromUserID.equals(messageSenderId))
            {
                holder.senderMessageText.setVisibility(View.VISIBLE);

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " +messages.getDate());
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setVisibility(View.VISIBLE);

                holder.receiverMessageText.setBackgroundResource(R.drawable.sender_message_layout);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage() + "\n \n" + messages.getTime() + " - " +messages.getDate());
            }

        }
       else if (fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get().load(messages.getMessage()).into(holder.messageSenderPicture);
            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).into(holder.messageReceiverPicture);
            }
        }
       else if (fromMessageType.equals("pdf") || (fromMessageType.equals("docx") ))
        {
            if (fromUserID.equals(messageSenderId))
            {
                holder.messageSenderPicture.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/chatting-5b641.appspot.com/o/Image%20Files%2Fdocument.png?alt=media&token=84292485-519c-4d6b-9b57-770c7ef74e99")
                        .into(holder.messageSenderPicture);

            }
            else
            {
                holder.receiverProfileImage.setVisibility(View.VISIBLE);
                holder.messageReceiverPicture.setVisibility(View.VISIBLE);

                Picasso.get()
                        .load("https://firebasestorage.googleapis.com/v0/b/chatting-5b641.appspot.com/o/Image%20Files%2Fdocument.png?alt=media&token=84292485-519c-4d6b-9b57-770c7ef74e99")
                        .into(holder.messageReceiverPicture);
            }
        }
       if (fromUserID.equals(messageSenderId))
       {
           holder.itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v)
               {
                   if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx"))
                   {
                       CharSequence option[] = new CharSequence[]
                               {
                                       "Delete for me",
                                       "Download adn view This Document",
                                       "Cancel",
                                       "Delete for Everyone"
                               };

                       AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete Message");

                       builder.setItems(option, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which)
                           {
                               if (which == 0)
                               {
                                   deletedSentMessage(position, holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                               else  if (which == 1)
                               {
                                   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                   holder.itemView.getContext().startActivity(intent);

                               }
                               else  if (which == 3)
                               {
                                   deletedMessageForEveryone(position, holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }

                           }
                       });
                       builder.show();
                   }
                   else if (userMessageList.get(position).getType().equals("text"))
                   {
                       CharSequence option[] = new CharSequence[]
                               {
                                       "Delete for me",
                                       "Cancel",
                                       "Delete for Everyone"
                               };

                       AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete Message");

                       builder.setItems(option, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               if (which == 0)
                               {
                                   deletedSentMessage(position, holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                               else if (which == 2)
                               {
                                   deletedMessageForEveryone(position, holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                           }
                       });
                       builder.show();
                   }
                  else if (userMessageList.get(position).getType().equals("image"))
                   {
                       CharSequence option[] = new CharSequence[]
                               {
                                       "Delete for me",
                                       "view This Image",
                                       "Cancel",
                                       "Delete for Everyone"
                               };

                       AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete Message");

                       builder.setItems(option, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which)
                           {
                               if (which == 0)
                               {
                                   deletedSentMessage(position, holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                               else  if (which == 1)
                               {
                                   Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                   intent.putExtra("url", userMessageList.get(position).getMessage());
                                   holder.itemView.getContext().startActivity(intent);
                               }

                               else  if (which == 3)
                               {
                                   deletedMessageForEveryone(position, holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }

                           }
                       });
                       builder.show();
                   }
               }
           });
       }
       else
       {
           holder.itemView.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v)
               {
                   if (userMessageList.get(position).getType().equals("pdf") || userMessageList.get(position).getType().equals("docx"))
                   {
                       CharSequence option[] = new CharSequence[]
                               {
                                       "Delete for me",
                                       "Download adn view This Document",
                                       "Cancel",

                               };

                       AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete Message");

                       builder.setItems(option, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which)
                           {
                               if (which== 0)
                               {
                                   deletedReceiverMessage(position,holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                               else  if (which == 1)
                               {
                                   Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessageList.get(position).getMessage()));
                                   holder.itemView.getContext().startActivity(intent);

                               }
                           }
                       });
                       builder.show();
                   }
                   else if (userMessageList.get(position).getType().equals("text"))
                   {
                       CharSequence option[] = new CharSequence[]
                               {
                                       "Delete for me",
                                       "Cancel",
                               };

                       AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete Message");

                       builder.setItems(option, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which) {
                               if (which == 0)
                               {
                                   deletedReceiverMessage(position,holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }

                           }
                       });
                       builder.show();
                   }
                   else if (userMessageList.get(position).getType().equals("image"))
                   {
                       CharSequence option[] = new CharSequence[]
                               {
                                       "Delete for me",
                                       "view This Image",
                                       "Cancel",
                               };

                       AlertDialog.Builder builder = new AlertDialog.Builder(holder.itemView.getContext());
                       builder.setTitle("Delete Message");

                       builder.setItems(option, new DialogInterface.OnClickListener() {
                           @Override
                           public void onClick(DialogInterface dialog, int which)
                           {
                               if (which == 0)
                               {
                                   deletedReceiverMessage(position,holder);
                                   Intent intent = new Intent(holder.itemView.getContext(), MainActivity.class);
                                   holder.itemView.getContext().startActivity(intent);
                               }
                               else  if (which == 1)
                               {

                                   Intent intent = new Intent(holder.itemView.getContext(), ImageViewerActivity.class);
                                   intent.putExtra("url", userMessageList.get(position).getMessage());
                                   holder.itemView.getContext().startActivity(intent);
                               }

                           }
                       });
                       builder.show();
                   }
               }
           });
       }
    }

    @Override
    public int getItemCount()
    {
        return userMessageList.size();
    }

    private void deletedSentMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messager")
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully....", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.....", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deletedReceiverMessage(final int position, final MessageViewHolder holder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messager")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(holder.itemView.getContext(), "Deleted Successfully....", Toast.LENGTH_SHORT).show();

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.....", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private void deletedMessageForEveryone(final int position, final MessageViewHolder holder)
    {
        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messager")
                .child(userMessageList.get(position).getTo())
                .child(userMessageList.get(position).getFrom())
                .child(userMessageList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful())
                {
                    rootRef.child("Messager")
                            .child(userMessageList.get(position).getFrom())
                            .child(userMessageList.get(position).getTo())
                            .child(userMessageList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                            if (task.isSuccessful())
                            {
                                Toast.makeText(holder.itemView.getContext(), "Deleted Successfully....", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
                else
                {
                    Toast.makeText(holder.itemView.getContext(), "Error Occurred.....", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}


