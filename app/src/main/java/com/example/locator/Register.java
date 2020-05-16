package com.example.locator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    EditText phonenum,otp;
    TextView state;
    ProgressBar progressbar;
    Button nextbtn,resendbtn;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken token;
    Boolean verificationInProgress = false;
    private LocationManager locationManager;
    ProgressBar verifyprogress;
    TextView validating;
    public static String phonenumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getPermission();
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        phonenum = findViewById(R.id.phonenum);
        otp = findViewById(R.id.otp);
        resendbtn = findViewById(R.id.resendbtn);
        state = findViewById(R.id.state);
        progressbar = findViewById(R.id.progressbar);
        nextbtn = findViewById(R.id.nextbtn);
        verifyprogress = findViewById(R.id.verifyprrogres);
        validating = findViewById(R.id.vadidating);
        nextbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!verificationInProgress){
                    if(!phonenum.getText().toString().isEmpty() && phonenum.getText().toString().length() == 10){
                    phonenumber = "+"+"91"+phonenum.getText().toString();
                    state.setText("sending otp...");
                    state.setVisibility(View.VISIBLE);
                    progressbar.setVisibility(View.VISIBLE);
                    requestOTP(phonenumber);
                    }else{
                        phonenum.setError("Phone Number Is Not Valid");
                        }
                }else{
                    String userotp = otp.getText().toString();
                    if(!userotp.isEmpty() && userotp.length()==6){
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId,userotp);
                        verifyAuth(credential);
                    }else {
                        otp.setError("Invalid OTP");
                    }
                }
            }
        });
        resendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Sending OTP again",Toast.LENGTH_SHORT).show();
                progressbar.setVisibility(View.VISIBLE);
                state.setVisibility(View.VISIBLE);
                requestOTP(phonenumber);
            }
        });
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            Toast.makeText(this, "GPS is Enabled in your device", Toast.LENGTH_SHORT).show();
        }else{
            // GPS not enabled
            Toast.makeText(this, "GPS is not enabled in your device", Toast.LENGTH_SHORT).show();
            showGPSDisabledAlert();
        }
    }


    private void checkUserProfile() {
        DocumentReference docref = fstore.collection("users").document(fauth.getCurrentUser().getPhoneNumber());
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    Intent i = new Intent(getApplicationContext(),user_profile.class);
                    startActivity(i);
                }else{
                    Intent i = new Intent(getApplicationContext(),user_details.class);
                    startActivity(i);
                }
            }
        });
    }

    public void verifyAuth(PhoneAuthCredential credential){
        fauth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    checkUserProfile();
                }else{
                    Toast.makeText(getApplicationContext(),"Authentication Failed",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void requestOTP(final String phonenumber){
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phonenumber, 90l, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progressbar.setVisibility(View.GONE);
                state.setVisibility(View.GONE);
                otp.setVisibility(View.VISIBLE);
                verificationId = s;
                token = forceResendingToken;
                verificationInProgress = true;
                nextbtn.setText("Verify");
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(@NonNull String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                Toast.makeText(Register.this, "OTP Timeout, Please Re-generate the OTP Again.", Toast.LENGTH_SHORT).show();
                resendbtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                verifyprogress.setVisibility(View.VISIBLE);
                validating.setVisibility(View.VISIBLE);
                verifyAuth(phoneAuthCredential);

            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(getApplicationContext(),"cannot create account"+e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  void getPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PackageManager.PERMISSION_GRANTED);
        }
    }

    private void showGPSDisabledAlert() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("GPS Service is disabled .\nWould you like to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        })
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .create().show();
    }

}
