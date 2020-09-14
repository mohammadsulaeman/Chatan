package com.mohammadsulaeman.chatting;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{
    private String messageReceivedId, messageReceicedname, messageReceivedImage, messageSenderID;
    private TextView userName,userLastSeen;
    private CircleImageView userImage;

    private Toolbar ChatToolBar;
    private ImageButton SendMessageButton, SendFilesButton;
    private EditText Inputmessage;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private String saveCurrentTime, saveCurrentDate;

    private String checker = "" , myUrl="";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingbar;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();

        RootRef = FirebaseDatabase.getInstance().getReference();
        messageReceivedId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceicedname = getIntent().getExtras().get("visit_user_name").toString();
        messageReceivedImage= getIntent().getExtras().get("visit_image").toString();


       inisialisasi();

       userName.setText(messageReceicedname);
        Picasso.get().load(messageReceivedImage).into(userImage);

        SendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendMessage();
            }
        });

        DisplayLastSeen();

        SendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CharSequence options[] = new CharSequence[]
                        {
                          "Images",
                          "PDF File",
                          "Ms Word Files",
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        if(which == 0)
                        {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if(which == 1)
                        {
                            checker = "pdf";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        }
                        if(which == 2)
                        {
                            checker = "docx";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Word File"), 438);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void inisialisasi()
    {
        ChatToolBar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(ChatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_layout, null);
        actionBar.setCustomView(actionBarView);

        userImage  = (CircleImageView) findViewById(R.id.custom_profile_image);
        userName  = (TextView) findViewById(R.id.custome_profile_name);
        userLastSeen = (TextView) findViewById(R.id.custome_user_last_seen);

        loadingbar = new ProgressDialog(this);

        SendMessageButton = findViewById(R.id.send_message_btn);
        SendFilesButton = findViewById(R.id.send_files_btn);
        Inputmessage = (EditText) findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList = (RecyclerView) findViewById(R.id.private_message_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);


        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            loadingbar.setTitle("Sending  Image");
            loadingbar.setMessage("Please wait , we are sending that file.....");
            loadingbar.setCanceledOnTouchOutside(false);
            loadingbar.show();

            fileUri = data.getData();

            if (!checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderref = "Messages/"+ messageSenderID + "/" + messageReceivedId;
                final String messageReceiverref = "Messages/"+ messageReceivedId+ "/" + messageSenderID;


                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceivedId).push();

                final String messagePush = userMessageKeyRef.getKey();

                final StorageReference filePath =storageReference.child(messagePush + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", task.getResult().getDownloadUrl().toString());
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageSenderID);
                            messageTextBody.put("messageID", messagePush);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderref + "/" + messagePush, messageTextBody);
                            messageBodyDetails.put(messageReceiverref + "/" + messagePush, messageTextBody);


                            RootRef.updateChildren(messageBodyDetails);
                            loadingbar.dismiss();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        loadingbar.dismiss();
                        Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        double p = (100.0*taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingbar.setMessage((int) p + " % Uploading....");
                    }
                });
            }
            else  if (checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderref = "Messages/"+ messageSenderID + "/" + messageReceivedId;
                final String messageReceiverref = "Messages/"+ messageReceivedId+ "/" + messageSenderID;


                DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                        .child(messageSenderID).child(messageReceivedId).push();

                final String messagePush = userMessageKeyRef.getKey();

                final StorageReference filePath =storageReference.child(messagePush + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful())
                        {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful())
                        {
                            Uri donawloadUrl = task.getResult();
                            myUrl = donawloadUrl.toString();

                            Map messageTextBody = new HashMap();
                            messageTextBody.put("message", myUrl);
                            messageTextBody.put("name", fileUri.getLastPathSegment());
                            messageTextBody.put("type", checker);
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("to", messageSenderID);
                            messageTextBody.put("messageID", messagePush);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("date", saveCurrentDate);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderref + "/" + messagePush, messageTextBody);
                            messageBodyDetails.put(messageReceiverref + "/" + messagePush, messageTextBody);

                            RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {

                                    if (task.isSuccessful())
                                    {
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingbar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    Inputmessage.setText("");
                                }
                            });
                        }
                    }
                });
            }
            else
            {
                loadingbar.dismiss();
                Toast.makeText(this, "Nothing Selected, Error.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void DisplayLastSeen()
    {
        RootRef.child("Users").child(messageReceivedId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.child("userState").hasChild("state"))
                        {
                            String state = dataSnapshot.child("userState").child("state").getValue().toString();
                            String date = dataSnapshot.child("userState").child("date").getValue().toString();
                            String time = dataSnapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online"))
                            {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline"))
                            {
                                userLastSeen.setText("Last Seen ; " + date + "  " + time);
                            }
                        }
                        else
                        {
                            userLastSeen.setText("Offline");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        RootRef.child("Messages").child(messageSenderID).child(messageReceivedId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Messages messages = dataSnapshot.getValue(Messages.class);

                        messagesList.add(messages);

                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot)
                    {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s)
                    {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
    }

    private void SendMessage()
    {
       String messageText = Inputmessage.getText().toString();

       if (TextUtils.isEmpty(messageText))
       {
           Toast.makeText(this, "write message first", Toast.LENGTH_SHORT).show();
       }
       else
       {
           String messageSenderref = "Messages/"+ messageSenderID + "/" + messageReceivedId;
           String messageReceiverref = "Messages/"+ messageReceivedId+ "/" + messageSenderID;


           DatabaseReference userMessageKeyRef = RootRef.child("Messages")
                   .child(messageSenderID).child(messageReceivedId).push();

           String messagePush = userMessageKeyRef.getKey();

           Map messageTextBody = new HashMap();
           messageTextBody.put("message", messageText);
           messageTextBody.put("type", "text");
           messageTextBody.put("from", messageSenderID);
           messageTextBody.put("to", messageSenderID);
           messageTextBody.put("messageID", messagePush);
           messageTextBody.put("time",saveCurrentTime);
           messageTextBody.put("date", saveCurrentDate);

           Map messageBodyDetails = new HashMap();
           messageBodyDetails.put(messageSenderref + "/" + messagePush, messageTextBody);
           messageBodyDetails.put(messageReceiverref + "/" + messagePush, messageTextBody);

           RootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
               @Override
               public void onComplete(@NonNull Task task) {

                   if (task.isSuccessful())
                   {
                       Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
                   }
                   else
                   {
                       Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                   }
                   Inputmessage.setText("");
               }
           });
       }
    }
}
