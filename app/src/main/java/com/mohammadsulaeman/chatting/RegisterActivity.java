package com.mohammadsulaeman.chatting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity
{
    private Button CreateAccountButton;
    private EditText UserEmail,UserPaasword;
    private TextView AlreadyHaveAccountLink;
    private ProgressDialog loadingBar;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        RootRef = FirebaseDatabase.getInstance().getReference();
        inisialisasi();

        AlreadyHaveAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount()
    {
        String email = UserEmail.getText().toString();
        String password = UserPaasword.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Please enter email...", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Please enter password...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating New Account");
            loadingBar.setMessage("Please wait....");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                String currentUserID = mAuth.getCurrentUser().getUid();
                                RootRef.child("Users").child(currentUserID).setValue("");

                                RootRef.child("Users").child(currentUserID).child("device_token")
                                        .setValue(deviceToken);

                                SendUSerToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account Created Succes", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error : "+message , Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }


    private void inisialisasi()
    {
        CreateAccountButton = (Button) findViewById(R.id.register_button);
        UserEmail = (EditText) findViewById(R.id.register_email);
        UserPaasword = (EditText) findViewById(R.id.register_password);
        AlreadyHaveAccountLink = (TextView) findViewById(R.id.already_have_new_account);
        loadingBar = new ProgressDialog(this);
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUSerToMainActivity()
    {
        Intent MainIntent = new Intent(RegisterActivity.this,MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(MainIntent);
        finish();
    }
}
