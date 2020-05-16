package com.example.locator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class user_profile extends AppCompatActivity {
    TextView username,useremail,userage,userphone;
    Button locationtrail,logout1,menu;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    boolean click = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        username = findViewById(R.id.username);
        userage = findViewById(R.id.userage);
        useremail = findViewById(R.id.useremail);
        userphone = findViewById(R.id.userphone);
        locationtrail = findViewById(R.id.locationtrail);
        logout1 = findViewById(R.id.logout1);
        menu = findViewById(R.id.menu);
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        final DocumentReference docref = fstore.collection("users").document(fauth.getCurrentUser().getPhoneNumber());
        docref.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if(documentSnapshot.exists()){
                    username.setText(documentSnapshot.getString("name"));
                    userage.setText(documentSnapshot.getString("age"));
                    useremail.setText(documentSnapshot.getString("email"));
                    userphone.setText(documentSnapshot.getString("phone"));
                }
            }
        });
        locationtrail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
            }
        });
    }
    public  void menu_click(View view){
        if(!click){
            logout1.setVisibility(View.VISIBLE);
            click = true;
        }else{
            logout1.setVisibility(View.INVISIBLE);
            click = false;
        }
    }
    public void logout_user(View view){
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), Register.class));
        finish();
    }
}
