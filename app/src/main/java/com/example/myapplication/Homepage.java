package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class Homepage extends AppCompatActivity {

    private BottomNavigationView bottomNav;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private String selectedFragment="Home";
    private String previousFragment="";
    private boolean firstBack=true;
    private List<String> fishNames;
    private List<String> fishNamesEn;
    private List<String> fishNamesGr;
    private List<String> fishImagesUrl;
    private List<String> fishDescriptionGreek;
    private List<String> fishDescriptionEn;


    @Override
    public void onBackPressed() {
        //on back pressed checks if the fragment is the prediction fragment i.e. after the image has
        //been taken and model predicted species
        boolean predictionFragment=false;
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof PredictionInfoFragment) {
            predictionFragment=true;
        }

        //if the fragment is the home fragment, redirects user to menu of his/her phone
        //else redirects to home fragment
        if (bottomNav.getSelectedItemId()==R.id.nav_home && !predictionFragment){
            finish();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
        }else {
            bottomNav.setSelectedItemId(R.id.nav_home);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_homepage);

        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        db=FirebaseFirestore.getInstance();

        //checks whether user just signed in now, or he/she was already signed in from previous
        //session and calls the welcome user function
        String signed_in_data = getIntent().getExtras().getString("signed_in");
        if (signed_in_data.equals("now")){
            welcomeUser();
        }


        bottomNav=findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new HomeFragment()).commit();

        fishNames= new ArrayList<>();
        fishNamesEn= new ArrayList<>();
        fishNamesGr= new ArrayList<>();
        fishImagesUrl= new ArrayList<>();
        fishDescriptionGreek= new ArrayList<>();
        fishDescriptionEn= new ArrayList<>();
        getSpeciesData();
    }

    //function to greet the user with his name when he signs in. The name is read from the database
    //details of the user
    //if user is already signed in from previous sessions no message is displayed
    private void welcomeUser() {
        DocumentReference docRef = db.collection("Users").document(currentUser.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {

                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String userFullName= document.getString("Fullname");
                        if (currentUser.getDisplayName()==null){
                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(userFullName).build();
                            currentUser.updateProfile(profileUpdate).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Log.d("mytag", "User Profile Updated");
                                    }
                                }
                            });
                        }
                        String welcomeMsg= "Welcome back "+ userFullName+" !";
                        Toast.makeText(Homepage.this,welcomeMsg,Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(null, "No such document");
                    }
                } else {
                    Log.d(null, "get failed with ", task.getException());
                }
            }
        });
    }

    //to handle the menu items
    //navListener handles the clicks from the user on the menu items and redirects accordingly
    private BottomNavigationView.OnNavigationItemSelectedListener navListener=
            item -> {
                previousFragment=selectedFragment;

                switch (item.getItemId()){
                    case R.id.nav_home:
                        selectedFragment="Home";
                        replaceFragment(new HomeFragment());

                        break;
                    case R.id.nav_history:
                        selectedFragment="History";
                        replaceFragment(new HistoryFragment());

                        break;
                    case R.id.nav_info:
                        selectedFragment="Info";
                        replaceFragment(new InfoFragment());

                        break;
                    case R.id.nav_about:
                        selectedFragment="About";
                        replaceFragment(new AboutFragment());

                        break;
                }
                return true;
            };

    //function called to switch the fragments (based on the menu item clicks)
    private void replaceFragment(Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                fragment).commit();
    }

    //everything about the species is stored in the database as a different collection from the users
    //function to read all details of species when the user opens the app
    private void getSpeciesData() {
        db.collection("Species").orderBy("nameLatin")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                fishNames.add(document.getString("nameLatin"));
                                fishNamesGr.add(document.getString("nameGr"));
                                fishNamesEn.add(document.getString("nameEn"));
                                fishDescriptionEn.add(document.getString("descrEn"));
                                fishDescriptionGreek.add(document.getString("descrGr"));
                                fishImagesUrl.add(document.getString("downloadUrl"));
                            }
                        } else {
                            Log.d("mytag", "Error getting documents: ", task.getException());
                            Toast.makeText(Homepage.this, "Error getting species info",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //these functions are called from the fragments to get fish details according to their needs
    public List<String> getSpeciesNames() {
        return fishNames;
    }
    public List<String> getSpeciesNamesGr() {
        return fishNamesGr;
    }
    public List<String> getSpeciesNamesEn() {
        return fishNamesEn;
    }
    public List<String> getSpeciesDescription() {
        return fishDescriptionEn;
    }
    public List<String> getSpeciesDescriptionGr() {
        return fishDescriptionGreek;
    }
    public List<String> getSpeciesImagesUrl() {
        return fishImagesUrl;
    }

}