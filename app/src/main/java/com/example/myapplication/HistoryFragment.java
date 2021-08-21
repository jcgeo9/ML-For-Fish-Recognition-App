package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class HistoryFragment extends Fragment {

    FirebaseFirestore db;
    FirebaseUser currentUser;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    HistoryAdapter historyAdapter;

    private List<String> historyFishNameLatin;
    private List<String> historyFishNameEn;
    private List<String> historyFishNameGr;
    private List<String> historyFishDescrEn;
    private List<String> historyFishDescrGr;
    private List<String> historyFishImageUrl;
    private List<String> historyFishTimestamp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fragment_history,container,false);

        recyclerView= v.findViewById(R.id.info_recycler_view_history);
        recyclerView.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        historyFishNameLatin=new ArrayList<>();
        historyFishNameEn=new ArrayList<>();
        historyFishNameGr=new ArrayList<>();
        historyFishDescrEn=new ArrayList<>();
        historyFishDescrGr=new ArrayList<>();
        historyFishImageUrl=new ArrayList<>();
        historyFishTimestamp=new ArrayList<>();

        db=FirebaseFirestore.getInstance();
        currentUser= FirebaseAuth.getInstance().getCurrentUser();

        //gets the previous recognitions made from the user using the app and displays it in a list
        //that contains the prediction name, time and can be clicked to access more info about hte fish
        //this is done using an adapter named historyAdapter
        db.collection("Users").document(currentUser.getUid()).collection("History")
                .orderBy("timestamp", Query.Direction.DESCENDING).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(DocumentSnapshot doc: task.getResult().getDocuments()) {
                        int indexOfFish=((Homepage) getActivity()).getSpeciesNames().indexOf(doc.getString("predFishName"));
                        historyFishNameLatin.add(doc.getString("predFishName"));
                        historyFishTimestamp.add(doc.getTimestamp("timestamp").toDate().toString());
                        historyFishNameEn.add(((Homepage) getActivity()).getSpeciesNamesEn().get(indexOfFish));
                        historyFishNameGr.add(((Homepage) getActivity()).getSpeciesNamesGr().get(indexOfFish));
                        historyFishDescrEn.add(((Homepage) getActivity()).getSpeciesDescription().get(indexOfFish));
                        historyFishDescrGr.add(((Homepage) getActivity()).getSpeciesDescriptionGr().get(indexOfFish));
                        historyFishImageUrl.add(doc.getString("predImageUrl"));
                    }
                    historyAdapter=new HistoryAdapter(getActivity(),historyFishNameLatin,historyFishNameEn,
                            historyFishNameGr,historyFishImageUrl,historyFishDescrEn, historyFishDescrGr,historyFishTimestamp);
                    recyclerView.setAdapter(historyAdapter);
                }
            }
        });



        return v;

    }
}

