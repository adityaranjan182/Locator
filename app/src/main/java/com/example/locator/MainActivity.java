package com.example.locator;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity {
    private Button register;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    TextView loading;
    ProgressBar progressBar1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        register = findViewById(R.id.register);
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        loading = findViewById(R.id.loading);
        progressBar1 = findViewById(R.id.progressBar1);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),Register.class);
                startActivity(i);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        if(fauth.getCurrentUser()!=null){
            checkUserProfile();
            progressBar1.setVisibility(View.VISIBLE);
            loading.setVisibility(View.VISIBLE);
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
}

