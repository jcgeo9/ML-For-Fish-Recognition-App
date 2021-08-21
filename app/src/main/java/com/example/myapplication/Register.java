package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private TextView registerUser;
    private CircularImageView appTitleReg;
    private EditText editTextFullName, editTextEmail, editTextValidateEmail, editTextPassword, editTextValidatePassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        appTitleReg=findViewById(R.id.logoImageView);
        appTitleReg.setOnClickListener(this);

        registerUser=(Button) findViewById(R.id.register_button);
        registerUser.setOnClickListener(this);

        editTextFullName=(EditText) findViewById(R.id.full_name);
        editTextEmail=(EditText) findViewById(R.id.email_reg);
        editTextValidateEmail=(EditText) findViewById(R.id.validate_email_reg);
        editTextPassword=(EditText) findViewById(R.id.password_reg);
        editTextValidatePassword=(EditText) findViewById(R.id.validate_password_reg);

        db = FirebaseFirestore.getInstance();
    }

    @Override
    public void onClick(View v) {
        //buttons the user clicked
        switch (v.getId()) {
            //if logo is pressed, user is redirected to login page
            case R.id.logoImageView:
                startActivity(new Intent(this, MainActivity.class));
                break;
            //if register button is pressed, calls function to register the user
            case R.id.register_button:
                registerUser();
                break;
        }

    }

    //function to register the user
    //checks if all fields are valid and if not, appropriate messages are displayed
    private void registerUser() {
        String email=editTextEmail.getText().toString();
        String valEmail=editTextValidateEmail.getText().toString();
        String fullname=editTextFullName.getText().toString();
        String password=editTextPassword.getText().toString();
        String valPassword=editTextValidatePassword.getText().toString();

        if(fullname.isEmpty()){
            editTextFullName.setError("Full name is required!");
            editTextFullName.requestFocus();
            return;
        }
        if(email.isEmpty()){
            editTextEmail.setError("Email is required!");
            editTextEmail.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please provide valid email!");
            editTextEmail.requestFocus();
            return;
        }

        if(password.isEmpty()){
            editTextPassword.setError("Password is required!");
            editTextPassword.requestFocus();
            return;
        }

        if(!email.equals(valEmail)){
            editTextValidateEmail.setError("Email validation failed!");
            editTextValidateEmail.requestFocus();
            return;
        }

        if(!password.equals(valPassword)){
            editTextValidatePassword.setError("Password validation failed!");
            editTextValidatePassword.requestFocus();
            return;
        }

        if(password.length()<6){
            editTextPassword.setError("Min password length should be 6 characters!");
            editTextPassword.requestFocus();
            return;
        }

        //creates an account on Firebase authentication and adds the details (except the password)
        //to the Firebase Firestore (database)
        //messages are provided accordingly and if the procedure is successful user is redirected
        //to the sign in page
        mAuth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful()){

                        Map<String, Object> user = new HashMap<>();
                        user.put("Fullname", fullname);
                        user.put("Email", email);

                        db.collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(Register.this,"Successful Registration!",Toast.LENGTH_LONG).show();
                                        FirebaseAuth.getInstance().signOut();
                                        startActivity(new Intent(Register.this, MainActivity.class));
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Register.this,"Failed to register. Try again!",Toast.LENGTH_LONG).show();
                                    }
                                });
                    }else{
                        Toast.makeText(Register.this,"Failed to register.",Toast.LENGTH_LONG).show();
                    }
                }
            });

    }
}