package com.example.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class PredictionInfoFragment extends Fragment {

    private Uri imageUploadedBitmap;
    private String predictionNameLatin;


    TextView predictionTextView;
    ImageView predictionImageView;
    TextView clickForInfoTextView;

    //fragment created when a fish is recognized
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_predictioninfo, container, false);

        predictionTextView=v.findViewById(R.id.pred_fish_name);
        predictionImageView=v.findViewById(R.id.image_uploaded_by_user);
        clickForInfoTextView=v.findViewById(R.id.click_for_more_info);

        //gets the info for the fish from the previous fragment
        Bundle extras = this.getArguments();
        if(extras !=null) {
            predictionNameLatin=extras.getString("predictionLatin","");
            imageUploadedBitmap = Uri.parse(extras.getString("bitmapImageUri"));
        }

        //displays the name of the fish and the image taken to predict it
        String predText=predictionNameLatin+
                "\nEn: "+((Homepage) getActivity()).getSpeciesNamesEn().get(((Homepage) getActivity()).getSpeciesNames().indexOf(predictionNameLatin))
                +"\nΕλ: "+((Homepage) getActivity()).getSpeciesNamesGr().get(((Homepage) getActivity()).getSpeciesNames().indexOf(predictionNameLatin));
        predictionTextView.setText(predText);
        predictionImageView.setImageURI(imageUploadedBitmap);

        //button to redirect to page with more info about the fish
        //same as the one that gets called from the info adapater
        clickForInfoTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), specificFishInfo.class);

                int indexOfFish=((Homepage) getActivity()).getSpeciesNames().indexOf(predictionNameLatin);
                intent.putExtra("imageid",((Homepage) getActivity()).getSpeciesImagesUrl().get(indexOfFish));
                intent.putExtra("fishname",((Homepage) getActivity()).getSpeciesNames().get(indexOfFish));
                intent.putExtra("fishnameEn",((Homepage) getActivity()).getSpeciesNamesEn().get(indexOfFish));
                intent.putExtra("fishnameGr",((Homepage) getActivity()).getSpeciesNamesGr().get(indexOfFish));
                intent.putExtra("fishDescr",((Homepage) getActivity()).getSpeciesDescription().get(indexOfFish));
                intent.putExtra("fishDescrGreek",((Homepage) getActivity()).getSpeciesDescriptionGr().get(indexOfFish));
                getActivity().startActivity(intent);
            }
        });

        return v;
    }


}
