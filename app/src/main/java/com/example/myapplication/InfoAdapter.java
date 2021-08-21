package com.example.myapplication;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.ViewHolder> {
    List<String> fishNames;
    List<String> fishNamesEn;
    List<String> fishNamesGr;
    List<String> fishImages;
    List<String> fishDescription;
    List<String> fishDescriptionGreek;
    Context mContext;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rowFishName;
        TextView rowFishNameEn;
        TextView rowFishNameGr;
        ImageView rowFishImage;
        RelativeLayout fishNamesInAll;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            fishNamesInAll=itemView.findViewById(R.id.fishNamesInLanguages);
            rowFishImage= itemView.findViewById(R.id.info_item_image);
            rowFishName= itemView.findViewById(R.id.info_item_text);
            rowFishNameEn= itemView.findViewById(R.id.info_item_textEn);
            rowFishNameGr= itemView.findViewById(R.id.info_item_textGr);
        }
    }

    //constructor for the adapter where it gets the parameters given from the fragment
    public InfoAdapter(Context mContext, List<String> fishNames, List<String> fishNamesEn, List<String> fishNamesGr, List<String> fishImages,List<String> fishDescription, List<String> fishDescriptionGreek){
        this.mContext=mContext;
        this.fishImages=fishImages;
        this.fishNames=fishNames;
        this.fishNamesEn=fishNamesEn;
        this.fishNamesGr=fishNamesGr;
        this.fishDescription=fishDescription;
        this.fishDescriptionGreek=fishDescriptionGreek;
    }

    @NonNull
    @Override
    public InfoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater=LayoutInflater.from(parent.getContext());
        View view=inflater.inflate(R.layout.info_item,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    //for every info item, it displays the fish image and name
    @Override
    public void onBindViewHolder(@NonNull InfoAdapter.ViewHolder holder, int position) {
        holder.rowFishName.setText(fishNames.get(position));
        holder.rowFishNameEn.setText("En: "+fishNamesEn.get(position).toString());
        holder.rowFishNameGr.setText("Ελ: "+fishNamesGr.get(position));
        Glide.with(mContext).load(fishImages.get(position)).into(holder.rowFishImage);

        //if user clicks on the fish image, it is enlarged to fit in the screen as an alert dialog
        holder.rowFishImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(mContext,R.style.CustomAlertDialog);
                builder.setTitle(fishNames.get(position));
                LinearLayout linearLayout=new LinearLayout(mContext);
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);

                ImageView fishImageView =new ImageView(mContext);
                Glide.with(mContext).load(fishImages.get(position)).into(fishImageView);


                linearLayout.addView(fishImageView);
                linearLayout.setPadding(10,10,10,10);

                builder.setView(linearLayout);
                builder.create().show();
            }
        });

        //if the user clicks on the name of the fish, it is redirected to a page containing
        //more info about the fish
        holder.fishNamesInAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, specificFishInfo.class);
                intent.putExtra("imageid",fishImages.get(position));
                intent.putExtra("fishname",fishNames.get(position));
                intent.putExtra("fishnameEn",fishNamesEn.get(position));
                intent.putExtra("fishnameGr",fishNamesGr.get(position));
                intent.putExtra("fishDescr",fishDescription.get(position));
                intent.putExtra("fishDescrGreek",fishDescriptionGreek.get(position));
                mContext.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return fishNames.size();
    }

    //function that gets called when user search for specific fish. the new list is given to the adapter
    void updateAdapter(List<String> fishNamesUpd,List<String> fishNamesEnUpd,List<String> fishNamesGrUpd, List<String> fishImagesUpd,List<String> fishDescrUpd,List<String> fishDescrGreekUpd ){
        this.fishNames=fishNamesUpd;
        this.fishNamesEn=fishNamesEnUpd;
        this.fishNamesGr=fishNamesGrUpd;
        this.fishImages=fishImagesUpd;
        this.fishDescription=fishDescrUpd;
        this.fishDescriptionGreek=fishDescrGreekUpd;

        notifyDataSetChanged();
    }


}
