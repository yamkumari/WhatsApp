package com.apps.whatsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity
{
    private Button sendVerificationCode, verifyButton;
    private EditText inputPhoneNumber, inputVerificationCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks Callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        intialiseField();
    }

    private void intialiseField()
    {
        sendVerificationCode = findViewById(R.id.send_verification_code_button);
        verifyButton = findViewById(R.id.verify_button);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerificationCode = findViewById(R.id.verification_code_input);
        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationCode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = inputPhoneNumber.getText().toString();
                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();

                }
                else
                    {
                        loadingBar.setTitle("Phone Verification");
                        loadingBar.setMessage("please, wait while we are authenticating number");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();


                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                           PhoneLoginActivity.this,               // Activity (for callback binding)
                            Callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        verifyButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

             String verificationCode = inputVerificationCode.getText().toString();
             if(TextUtils.isEmpty(verificationCode))
             {
                 Toast.makeText(PhoneLoginActivity.this, "please enter the verification code", Toast.LENGTH_SHORT).show();
             }
             else
             {

                 loadingBar.setTitle("Code Verification");
                 loadingBar.setMessage("please, wait while we are authenticating validation coder");
                 loadingBar.setCanceledOnTouchOutside(false);
                 loadingBar.show();
                 // parameters are if and verification code which the user will enter
                 PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                 signInWithPhoneAuthCredential(credential);

             }
            }
        });

        Callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential)
            {
                // after the verification of code
                signInWithPhoneAuthCredential(credential);

            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid ... Please enter correct phone number" +
                        "with your country code", Toast.LENGTH_SHORT).show();
                sendVerificationCode.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                verifyButton.setVisibility(View.INVISIBLE);
                inputVerificationCode.setVisibility(View.INVISIBLE);

            }
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token)
            {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                // Save verification ID and resending token so we can use them later
                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "verification code is sent, please check and verify",
                        Toast.LENGTH_SHORT).show();

                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                verifyButton.setVisibility(View.VISIBLE);
                inputVerificationCode.setVisibility(View.VISIBLE);
            }

        };
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // user has provided the correct code
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulations successfully logged in", Toast.LENGTH_SHORT).show();
                            senduserToMainActivity();
                        }
                        else
                            {
                                   String message = task.getException().toString();
                                Toast.makeText(PhoneLoginActivity.this, "Error"+message, Toast.LENGTH_SHORT).show();

                            }

                    }
                });
    }

    private void senduserToMainActivity()
    {
        Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}