package com.example.locator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class user_details extends AppCompatActivity {
    EditText username,useremail,userage;
    Button createaccount;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    String userId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);
        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        userage = findViewById(R.id.userage);
        createaccount = findViewById(R.id.createaccount);
        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        createaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!username.getText().toString().isEmpty() && !useremail.getText().toString().isEmpty() && !userage.getText().toString().isEmpty()){
                    DocumentReference docref = fstore.collection("users").document(fauth.getCurrentUser().getPhoneNumber());
                    String name = username.getText().toString();
                    String email = useremail.getText().toString();
                    String  age = userage.getText().toString();
                    String phone = fauth.getCurrentUser().getPhoneNumber();
                    Map<String,Object> user = new HashMap<>();
                    user.put("name",name);
                    user.put("age",age);
                    user.put("email",email);
                    user.put("phone",phone);
                    docref.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                startActivity(new Intent(getApplicationContext(),user_profile.class));
                                finish();
                            }else{
                                Toast.makeText(getApplicationContext(),"Data is not inserted",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Toast.makeText(getApplicationContext(),"All fields are neccessary",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
