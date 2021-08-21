package com.example.myapplication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;


public class AboutFragment extends Fragment implements View.OnClickListener{


    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    ImageView logoutBtn;
    TextView emailDisplay;
    TextView fullnameDisplay;
    TextView fishRecognizedDisplay;

    TextView emailChange;
    TextView fullnameChange;
    TextView passwordChange;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_about,container,false);
        mAuth = FirebaseAuth.getInstance();
        currentUser=mAuth.getCurrentUser();
        db=FirebaseFirestore.getInstance();

        emailDisplay=v.findViewById(R.id.email_display);
        fullnameDisplay=v.findViewById(R.id.fullname_display);
        fishRecognizedDisplay=v.findViewById(R.id.fish_rec_display);

        emailChange=v.findViewById(R.id.email_change);
        fullnameChange=v.findViewById(R.id.fullname_change);
        passwordChange=v.findViewById(R.id.password_change);

        logoutBtn=v.findViewById(R.id.logout_button);
        logoutBtn.setOnClickListener(this);

        //displays user information
        emailDisplay.setText(currentUser.getEmail());
        fullnameDisplay.setText(currentUser.getDisplayName());

        //gets from the databasethe number of recognitions the user have and displays it
        db.collection("Users").document(currentUser.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    fishRecognizedDisplay.setText(task.getResult().get("fish_rec").toString());
                }
            }
        });

        //button to change email, calls the function
        emailChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmail();
            }
        });

        //button to change name, calls the function
        fullnameChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeProfileDetails();
            }
        });

        //button to change password, calls the function
        passwordChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        return v;
    }

    //when log out button is pressed. firebase signs out user and redirects to the sign in activity
    @Override
    public void onClick(View v) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext(),R.style.CustomAlertDialog);
        alertDialog.setTitle("Logout");
        alertDialog.setMessage("Are you sure you want to leave?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        alertDialog.show();
    }


    //function to change email where a dialog box is displayed
    private void changeEmail(){
        androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.CustomAlertDialog);
        builder.setTitle("Change Email Address");

        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textViewEmail=new TextView(getActivity());
        textViewEmail.setText("Please enter your new email address below");
        textViewEmail.setTextColor(getActivity().getColor(R.color.white));
        textViewEmail.setPadding(20,20,20,20);
        linearLayout.addView(textViewEmail);


        EditText emailText =new EditText(getActivity());
        emailText.setHint("New Email Address");
        emailText.setHintTextColor(getActivity().getColor(R.color.black));
        emailText.setGravity(Gravity.START);
        emailText.setTextColor(getActivity().getColor(R.color.black));
        emailText.setPadding(20,20,20,20);
        emailText.setBackground(getActivity().getDrawable(R.drawable.rounded_edit_text));
        emailText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailText.setMinEms(16);

        linearLayout.addView(emailText);
        linearLayout.setPadding(40,10,40,10);

        builder.setView(linearLayout);

        //gets the info from the edit text fields, updates the email on firebase authentication
        //and updates the email in the database
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newEmail=emailText.getText().toString();
                if(newEmail.isEmpty()) {
                    Toast.makeText(getActivity(),"You have not entered an email address",Toast.LENGTH_SHORT).show();
                    return;
                }
                currentUser.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> userUpdate = new HashMap<>();
                            userUpdate.put("Email", newEmail);

                            db.collection("Users").document(currentUser.getUid()).update(userUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(getActivity(),"Fields updated successfully",Toast.LENGTH_SHORT).show();
                                        emailDisplay.setText(newEmail);
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(getActivity(),"Failed to change field",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        builder.create().show();
    }

    //function to change the password where a dialog box is displayed
    private void changePassword() {
        androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.CustomAlertDialog);
        builder.setTitle("Change Password");

        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textViewPassword=new TextView(getActivity());
        textViewPassword.setText("Please enter your new password below");
        textViewPassword.setTextColor(getActivity().getColor(R.color.white));
        textViewPassword.setPadding(20,20,20,20);
        linearLayout.addView(textViewPassword);


        EditText passwordText =new EditText(getActivity());
        passwordText.setHint("New Password");
        passwordText.setHintTextColor(getActivity().getColor(R.color.black));
        passwordText.setGravity(Gravity.START);
        passwordText.setTextColor(getActivity().getColor(R.color.black));
        passwordText.setPadding(20,20,20,20);
        passwordText.setBackground(getActivity().getDrawable(R.drawable.rounded_edit_text));
        passwordText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordText.setMinEms(16);

        linearLayout.addView(passwordText);
        linearLayout.setPadding(40,10,40,10);

        builder.setView(linearLayout);

        //gets the info from the edit text, checks if the password is valid and updates the password
        //on firebase authentication
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword=passwordText.getText().toString();
                if(newPassword.isEmpty()) {
                    Toast.makeText(getActivity(),"You have not entered a password",Toast.LENGTH_SHORT).show();
                    return;
                }
                if(newPassword.length()<6){
                    Toast.makeText(getActivity(),"Password must be at least 6 characters!",Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean hasLetters = false;
                for (char ch : newPassword.toCharArray()) {
                    if (Character.isLetter(ch)) {
                        hasLetters = true;
                        break;
                    }
                }
                if(hasLetters==false){
                    Toast.makeText(getActivity(),"Password must contain at least 1 letter!",Toast.LENGTH_SHORT).show();
                    return;
                }

                currentUser.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(),"Password updated successfully",Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(getActivity(),"Failed to change password",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        builder.create().show();
    }

    //function to change the password where a dialog box is displayed
    private void changeProfileDetails() {
        androidx.appcompat.app.AlertDialog.Builder builder=new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.CustomAlertDialog);
        builder.setTitle("Change Profile Fullname");

        LinearLayout linearLayout=new LinearLayout(getActivity());
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textViewName=new TextView(getActivity());
        textViewName.setText("Edit Fullname");
        textViewName.setTextColor(getActivity().getColor(R.color.white));
        textViewName.setPadding(20,20,20,20);
        linearLayout.addView(textViewName);


        EditText nameText =new EditText(getActivity());
        nameText.setText(currentUser.getDisplayName());
        nameText.setHint("New Name");
        nameText.setHintTextColor(getActivity().getColor(R.color.black));
        nameText.setGravity(Gravity.START);
        nameText.setTextColor(getActivity().getColor(R.color.black));
        nameText.setPadding(20,20,20,20);
        nameText.setBackground(getActivity().getDrawable(R.drawable.rounded_edit_text));
        nameText.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        nameText.setMinEms(16);

        linearLayout.addView(nameText);
        linearLayout.setPadding(40,10,40,10);

        builder.setView(linearLayout);

        //gets the info from the edit text fields, updates the display name on firebase authentication
        //and updates the fullname in the database
        builder.setPositiveButton("Change", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName=nameText.getText().toString();

                if(newName.isEmpty()) {
                    Toast.makeText(getActivity(),"You have not entered a name",Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentUser.getDisplayName().equals(newName)) {
                    Toast.makeText(getActivity(),"Nothing has changed",Toast.LENGTH_SHORT).show();
                    return;
                }

                UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder().setDisplayName(newName).build();
                Map<String, Object> userUpdate = new HashMap<>();
                userUpdate.put("Fullname", newName);

                currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            db.collection("Users").document(currentUser.getUid()).update(userUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(getActivity(),"Fields updated successfully",Toast.LENGTH_SHORT).show();
                                        fullnameDisplay.setText(newName);
                                    }
                                }
                            });
                        }else {
                            Toast.makeText(getActivity(),"Failed to change fields",Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        builder.create().show();
    }



}
