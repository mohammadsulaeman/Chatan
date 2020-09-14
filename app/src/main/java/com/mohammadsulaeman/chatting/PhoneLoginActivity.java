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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity
{
    private Button SendVerificationOcde, VerifiyCode;
    private EditText InputPhoneNumber,InputVerificationCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String  mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        mAuth = FirebaseAuth.getInstance();

        SendVerificationOcde = (Button) findViewById(R.id.send_ver_code_button);
        VerifiyCode = (Button) findViewById(R.id.ver_code_button);
        InputPhoneNumber = (EditText) findViewById(R.id.phone_number_input);
        InputVerificationCode = (EditText) findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        SendVerificationOcde.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {

                String phoneNumber = InputPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Phone Number is Required....", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait.....");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please enter phone number with your country code....", Toast.LENGTH_SHORT).show();

                SendVerificationOcde.setVisibility(View.VISIBLE);
                InputPhoneNumber.setVisibility(View.VISIBLE);

                VerifiyCode.setVisibility(View.INVISIBLE);
                InputVerificationCode.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {


                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code telah di kirim, tolong di periksa", Toast.LENGTH_SHORT).show();


                SendVerificationOcde.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                VerifiyCode.setVisibility(View.VISIBLE);
                InputVerificationCode.setVisibility(View.VISIBLE);
            }
        };

        VerifiyCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendVerificationOcde.setVisibility(View.INVISIBLE);
                InputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = InputVerificationCode.getText().toString();

                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code ...", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    loadingBar.setTitle("Code Verification");
                    loadingBar.setMessage("Please wait.....");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Loggied in Successfully....", Toast.LENGTH_SHORT).show();
                            SendUserToMainActivity();

                        } else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : "+message, Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}

