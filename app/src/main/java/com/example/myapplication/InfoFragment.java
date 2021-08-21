package com.example.myapplication;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class InfoFragment extends Fragment {

    RecyclerView recyclerView;
    InfoAdapter infoAdapter;
    RecyclerView.LayoutManager layoutManager;
    FirebaseFirestore db;
    private List<String> fishNames;
    private List<String> fishNamesGr;
    private List<String> fishNamesEn;
    private List<String> fishDescription;
    private List<String> fishDescriptionGreek;
    private List<String> fishImages;


    //similar to history fragment but in this fragment a list of all fish is provided
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_info,container,false);

        db=FirebaseFirestore.getInstance();
        recyclerView= v.findViewById(R.id.info_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        fishNames=((Homepage) getActivity()).getSpeciesNames();
        fishNamesGr=((Homepage) getActivity()).getSpeciesNamesGr();
        fishNamesEn=((Homepage) getActivity()).getSpeciesNamesEn();
        fishDescription=((Homepage) getActivity()).getSpeciesDescription();
        fishDescriptionGreek=((Homepage) getActivity()).getSpeciesDescriptionGr();
        fishImages=((Homepage) getActivity()).getSpeciesImagesUrl();

        //creates the adapter and shows the list
        infoAdapter=new InfoAdapter(getActivity(),fishNames,fishNamesEn,fishNamesGr,fishImages,fishDescription, fishDescriptionGreek);
        recyclerView.setAdapter(infoAdapter);

        //search text to update the list according to what the user types
        EditText editText=v.findViewById(R.id.searchEditText);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());

            }
        });
        return v;
    }

    //the filtered lists of fish info after the user types in search text
    private void filter(String text) {
        List<String> filteredListNames = new ArrayList<>();
        List<String> filteredListNamesGr = new ArrayList<>();
        List<String> filteredListNamesEn = new ArrayList<>();
        List<String> filteredListDescr = new ArrayList<>();
        List<String> filteredListDescrGreek = new ArrayList<>();
        List<String> filteredListImgs = new ArrayList<>();

        for (int i=0; i<fishNames.size();i++){
            if (fishNames.get(i).toLowerCase().contains(text.toLowerCase())){
                filteredListNamesGr.add(fishNamesGr.get(i));
                filteredListNamesEn.add(fishNamesEn.get(i));
                filteredListNames.add(fishNames.get(i));
                filteredListImgs.add(fishImages.get(i));
                filteredListDescr.add(fishDescription.get(i));
                filteredListDescrGreek.add(fishDescriptionGreek.get(i));
            }
        }

        //calls the adapter function to update the list
        infoAdapter.updateAdapter(filteredListNames,filteredListNamesEn,filteredListNamesGr,filteredListImgs,filteredListDescr,filteredListDescrGreek);

    }
}
