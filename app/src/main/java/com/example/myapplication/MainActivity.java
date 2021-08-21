package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView register,login,forgot;
    private EditText editTextEmail,editTextPassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onBackPressed() {
        //on back pressed opens menu on phone
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    protected void onStart() {
        //on start of activity, checks if user is signed in.
        //if signed in then redirects to home page of the app
        currentUser=FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser!=null) {
            Intent intent = new Intent(MainActivity.this, Homepage.class);
            intent.putExtra("signed_in", "already");
            startActivity(intent);
        }
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        login=(Button) findViewById(R.id.sign_in_button);
        login.setOnClickListener(this);

        register= (TextView) findViewById(R.id.register);
        register.setOnClickListener(this);

        forgot= (TextView) findViewById(R.id.forgot_password);
        forgot.setOnClickListener(this);

        editTextEmail=(EditText) findViewById(R.id.email);
        editTextPassword=(EditText) findViewById(R.id.password);
    }

    @Override
    public void onClick(View v) {
        //buttons the user clicked
        switch (v.getId()){
            //if register text pressed, navigates to register page
            case R.id.register:
                startActivity(new Intent(this,Register.class));
                break;
            //if sign in button, calls function to sign the user in
            case R.id.sign_in_button:
                signInUser();
                break;
            //if forgot password pressed, displays a dialog to recover
            case R.id.forgot_password:
                showRecoverPasswordDialog();
                break;
        }
    }

    //function to sign in user
    //gets the email and password from the text fields and uses firebase to authenticate
    //the user based on his registration
    private void signInUser() {
        String email=editTextEmail.getText().toString();
        String password=editTextPassword.getText().toString();

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

        //if authentication is successful, user is redirected to the home page of the app
        //else it displays a pop up message with the appropriate instruction
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent intent = new Intent(MainActivity.this, Homepage.class);
                            intent.putExtra("signed_in", "now");
                            startActivity(intent);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed!", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    //function to display a dialog box to recover the password of the user
    private void showRecoverPasswordDialog() {
        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.CustomAlertDialog);
        builder.setTitle("Recover Password");

        LinearLayout linearLayout=new LinearLayout(this);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        EditText emailText =new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 40, 0, 0);
        emailText.setLayoutParams(lp);
        emailText.setHint("Email");
        emailText.setGravity(Gravity.START);
        emailText.setTextColor(getColor(R.color.black));
        emailText.setPadding(20,20,20,20);
        emailText.setBackground(getDrawable(R.drawable.rounded_edit_text));
        emailText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailText.setHint("EMAIL ADDRESS");
        emailText.setHintTextColor(getColor(R.color.silver));
        emailText.setMinEms(16);

        linearLayout.addView(emailText);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //gets the email from the text field of the dialog and using firebase api it sends
        //an email to the given address for password recovery
        //appropriate pop up message appears either it succeeds or not
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String emailRec=emailText.getText().toString().trim();
                if(emailRec.isEmpty()) {
                    Toast.makeText(MainActivity.this,"Email is required",Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.sendPasswordResetEmail(emailRec).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this,"Email sent",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(MainActivity.this,"Failed to send email",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });


            }
        });

        builder.create().show();
    }

}