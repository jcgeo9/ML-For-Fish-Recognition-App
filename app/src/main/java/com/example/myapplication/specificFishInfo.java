package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.util.Arrays;
import java.util.List;

public class specificFishInfo extends AppCompatActivity {

    TextView textView;
    TextView textViewDescr;
    ImageView imgView;
    Button translateToGreek;

    //activity that displays information about a specific species of fish
    //can be accessed from info fragment or when a fish is predicted and the user wants more info
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.specific_item_info);

        textView=  findViewById(R.id.fish_name_txtView);
        textViewDescr= findViewById(R.id.fish_description_xtView);
        imgView= findViewById(R.id.fish_img_ImgView);
        translateToGreek=findViewById(R.id.translation_button);

        Bundle extras=getIntent().getExtras();
        String fishName=extras.getString("fishname");
        String fishDescr=extras.getString("fishDescr");
        String fishDescrGreek=extras.getString("fishDescrGreek");
        String fishImageUrl=extras.getString("imageid");

        //displays fish name, description and image
        textView.setText(fishName);
        textViewDescr.setText(fishDescr);
        textViewDescr.setMovementMethod(new ScrollingMovementMethod());
        Glide.with(this).load(fishImageUrl).into(imgView);

        //if user wants to translate to greek or english, clicks on translate button
        //changes the description according to in what language was the previous
        translateToGreek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(textViewDescr.getText().toString().equals(fishDescr)){
                    translateToGreek.setText("Translate to English");
                    textViewDescr.setText(fishDescrGreek);
                }else {
                    translateToGreek.setText("Translate to Greek");
                    textViewDescr.setText(fishDescr);
                }
            }
        });

    }
}
