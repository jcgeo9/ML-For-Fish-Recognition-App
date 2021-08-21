package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.protobuf.StringValue;


import org.jetbrains.annotations.NotNull;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.TensorOperator;
import org.tensorflow.lite.support.common.TensorProcessor;
import org.tensorflow.lite.support.common.ops.DequantizeOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;

import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp;
import org.tensorflow.lite.support.image.ops.TransformToGrayscaleOp;
import org.tensorflow.lite.support.label.TensorLabel;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import static android.app.Activity.RESULT_OK;
import static android.content.ContentValues.TAG;

public class HomeFragment extends Fragment {

    StorageReference storageRef;
    FirebaseUser currentUser;
    FirebaseFirestore db;
    RelativeLayout clickToAddImage;

    private final int MY_CAMERA_PERMISSION_CODE = 100;
    private final int CAMERA_REQUEST = 1888;
    private final int PICK_IMAGE_REQUEST = 22;
    private Interpreter tfLiteBinary;
    private Interpreter tfLiteMultiClass;
    private List<String> labels;
    private int index;
    private String deleteDocument;
    private Uri imageUriGlobal;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v= inflater.inflate(R.layout.fragment_home,container,false);

        db=FirebaseFirestore.getInstance();
        currentUser= FirebaseAuth.getInstance().getCurrentUser();
        storageRef = FirebaseStorage.getInstance().getReference(currentUser.getUid());

        labels=new ArrayList<>();
        readLabelsFromFile();

        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "opencv installed");
        }else {
            Log.d(TAG, "opencv not installed");
        }

        clickToAddImage=v.findViewById(R.id.click_to_add_image);

        //listener that handles the click of the logo in order to recognize image
        //when the logo is clicked a dialog box is displayed that contains
        //two upload methods, one from camera and one from storage
        clickToAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                androidx.appcompat.app.AlertDialog builder=new androidx.appcompat.app.AlertDialog.Builder(getActivity(),R.style.CustomAlertDialog).create();
                builder.setTitle("Upload Method");

                LinearLayout linearLayout=new LinearLayout(getActivity());
                linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                linearLayout.setOrientation(LinearLayout.VERTICAL);

                TextView textViewProfileImage=new TextView(getActivity());
                textViewProfileImage.setText("Choose how you want to upload image!");
                textViewProfileImage.setTextColor(getActivity().getColor(R.color.white));
                textViewProfileImage.setPadding(20,20,20,20);
                linearLayout.addView(textViewProfileImage);

                linearLayout.setPadding(40,10,40,10);
                builder.setView(linearLayout);

                //handles storage image upload. Opens storage and gets the selected image from the user
                builder.setButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE,"Storage Image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), PICK_IMAGE_REQUEST);
                    }
                });
                //handles camera image upload. Saves the image taken by the user to his phone and gets the saved image
                builder.setButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE,"Take Image", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                        }
                        else {

                            String fileName = "Fish-Rec_"+System.currentTimeMillis() +".jpg";
                            ContentValues values = new ContentValues();
                            values.put(MediaStore.Images.Media.TITLE, fileName);
                            values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
                            imageUriGlobal = getActivity().getContentResolver().insert(
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUriGlobal);
                            startActivityForResult(intent, CAMERA_REQUEST);
                        }
                    }
                });

                builder.show();

                Button btnPositive = builder.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                Button btnNegative = builder.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE);

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
                layoutParams.weight = 10;
                btnPositive.setLayoutParams(layoutParams);
                btnNegative.setLayoutParams(layoutParams);

            }
        });

        //after the image is selected, app loads the instances of the two models, binary and
        //multi class, catching any exception
        try {
            tfLiteBinary = new Interpreter(loadBinaryModelFile());
            tfLiteMultiClass=new Interpreter(loadMultiClassModelFile());
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return v;
    }

    //used when the user selects or takes image from his phone
    //loads the image and applies the models to get their prediction
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            ContentResolver cr = getActivity().getContentResolver();
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(cr, imageUriGlobal);
                applyModels(photo,imageUriGlobal);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                applyModels(photo,imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    //function called to apply the models to the selected image
    private void applyModels(Bitmap bitmap, Uri imageUri) throws IOException {
        //variable that gets the output from the binary model
        //if model predicts there is no fish, a dialog box is displayed with a message to prompt
        //the user to try again with a new image and it does not proceed with the multi class model
        float[][] predictionBinary=doInferenceBinary(bitmap);
        if (predictionBinary[0][0]<predictionBinary[0][1]) {
            AlertDialog.Builder builder=new AlertDialog.Builder(getActivity(),R.style.CustomAlertDialog);

            builder.setMessage("No fish detected in the image. Please try again!");
            builder.setTitle("Fish Validation Alert");

            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builder.create().show();
        //if binary model predicts a fish, continues with the recognition
        }else {
            //variable that gets the output from the multi-class model as list
            float[][] predictionMultiClass=doInferenceMultiClass(bitmap);
            //gets the highest probability that the model predicts
            float max = predictionMultiClass[0][0];
            index = 0;
            for(int i=0;i<20;i++){
                if (max < predictionMultiClass[0][i]) {
                    max = predictionMultiClass[0][i];
                    index = i;
                }
            }
            //calls function to save the image and the models prediction to storage
            saveToStorage(bitmap,labels.get(index),imageUri);


            //increases the number of fish recognized in user details in database and
            //redirects to a new fragment containing the image taken, and info about the predicted fish
            db.collection("Users").document(currentUser.getUid())
                    .update("fish_rec",FieldValue.increment(1)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Bundle arguments=new Bundle();
                        arguments.putString("predictionLatin", labels.get(index));
                        arguments.putString("bitmapImageUri",imageUri.toString());
                        Fragment newFrag = new PredictionInfoFragment();
                        newFrag.setArguments(arguments);

                        FragmentManager fm = requireActivity().getSupportFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.replace(R.id.fragment_container, newFrag);
                        ft.commit();
                    }else {
                        Toast.makeText(getActivity(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    //reads the label for each fish and stores it in an array to be used when predicting and saving the model
    private void readLabelsFromFile() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(getActivity().getAssets().open("labels.txt")));

            String mLine;
            while ((mLine = reader.readLine()) != null) {
                labels.add(mLine);
            }
        } catch (IOException e) {
            Log.d("exception",e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.d("exception",e.getMessage());
                }
            }
        }
    }

    //loads the tflite instance of the binary model from the files
    private MappedByteBuffer loadBinaryModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor=getActivity().getAssets().openFd("binary_model_BEST.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    //loads the tflite instance of the multi-class model from the files
    private MappedByteBuffer loadMultiClassModelFile() throws IOException{
        AssetFileDescriptor fileDescriptor=getActivity().getAssets().openFd("multiclass_model.tflite");
        FileInputStream inputStream=new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel=inputStream.getChannel();
        long startOffset=fileDescriptor.getStartOffset();
        long declareLength=fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,declareLength);
    }

    //function for applying the binary model to the image
    public float[][] doInferenceBinary(Bitmap img) {

        //calls function to process the image according to the needs of the model
        ByteBuffer input= getByteBufferBinary(img);
        float[][] output = new float[1][2];

        //uses the model to predict
        tfLiteBinary.run(input, output);

        //returns the prediction of the model
        return output;
    }

    public float[][] doInferenceMultiClass(Bitmap img) {

        //calls function to process the image according to the needs of the model
        ByteBuffer input= getByteBufferMultiClass(img);
        float[][] output = new float[1][20];

        //uses the model to predict
        tfLiteMultiClass.run(input,output);

        //returns the prediction of the model
        return output;
    }

    //function to save the image along with the prediction of the model to the Firebase storage
    private void saveToStorage(Bitmap img,String predFishName, Uri imageUri){
        String saveLocation=String.valueOf(System.currentTimeMillis());
        StorageReference fileReference=storageRef.child(saveLocation);

        fileReference.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful()){
                            Log.d("mytag","Image uploaded");

                            task.getResult().getStorage().getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task<Uri> task) {
                                    if(task.isSuccessful()){
                                        String generatedFilePath = task.getResult().toString();
                                        Map<String, Object> predictionInfo = new HashMap<>();
                                        predictionInfo.put("predFishName", predFishName);
                                        predictionInfo.put("predImageUrl", generatedFilePath);
                                        predictionInfo.put("timestamp", FieldValue.serverTimestamp());

                                        db.collection("Users").document(currentUser.getUid())
                                                .collection("History").document().set(predictionInfo)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Log.d("mytag","Prediction added to User History");
                                                    deleteExtraItem();
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }
                });
    }


    //function called from savetostorage function to remove an extra item if the predictions history
    //exceeds 10 images
    //implemented to minimize costs of the app
    private void deleteExtraItem(){
        db.collection("Users").document(currentUser.getUid())
                .collection("History").orderBy("timestamp").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getDocuments().size()>10) {
                        deleteDocument=task.getResult().getDocuments().get(0).getId();
                        String deleteFromStorage=task.getResult().getDocuments().get(0).getString("predImageUrl");
                        StorageReference storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(deleteFromStorage);
                        storageReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Log.e("firebasestorage", "onSuccess: deleted file from storage");
                                    db.collection("Users").document(currentUser.getUid())
                                            .collection("History").document(deleteDocument).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Log.e("firestore", "onSuccess: deleted file from firestore");
                                                    }else {
                                                        Log.e("firestore", "onFailure: deleted file from firestore");
                                                    }
                                                }
                                            });
                                }else {
                                    Log.e("firebasestorage", "onFailure: did not delete file");
                                }
                            }
                        });
                    }
                }
            }
        });
    }


    //checks if user has given permissions to the app to access the camera
    //appropriate message displayed if not
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getActivity(), "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    //function to convert the image to grayscale, resize to 150x150 and store it in input variable as a byte buffer
    private ByteBuffer getByteBufferBinary(Bitmap bitmap){
        Mat imgMat= new Mat();
        Utils.bitmapToMat(bitmap, imgMat, true);
        Size size = new Size(150,150);
        Imgproc.resize(imgMat, imgMat, size, Imgproc.INTER_AREA);
        Bitmap newBitmap = Bitmap.createBitmap(imgMat.width(), imgMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgMat, newBitmap);

        int width = newBitmap.getWidth();
        int height = newBitmap.getHeight();
        ByteBuffer mImgData = ByteBuffer.allocateDirect(4 * width * height);
        mImgData.order(ByteOrder.nativeOrder());
        int[] pixels = new int[width*height];
        newBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int pixel : pixels) {
            float value = (float) Color.red(pixel)/255.0f;
            mImgData.putFloat(value);
        }
        return mImgData;
    }

    //function to resize the image to 150x150 and store it in input variable as a byte buffer
    private ByteBuffer getByteBufferMultiClass(Bitmap bitmap) {
        Mat imgMat= new Mat();
        Utils.bitmapToMat(bitmap, imgMat, true);
        Size size = new Size(150,150);
        Imgproc.resize(imgMat, imgMat, size, Imgproc.INTER_AREA);
        Bitmap newBitmap = Bitmap.createBitmap(imgMat.width(), imgMat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(imgMat, newBitmap);

        int width = newBitmap.getWidth();
        int height = newBitmap.getHeight();
        ByteBuffer mImgData = ByteBuffer.allocateDirect(4 * width * height * 3);
        mImgData.order(ByteOrder.nativeOrder());
                int[] pixels = new int[width*height];
        newBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int pixel=0;
        for (int i = 0; i < 150; ++i) {
            for (int j = 0; j < 150; ++j) {
                final int val = pixels[pixel++];
                mImgData.putFloat(((val>> 16) & 0xFF) / 255.f);
                mImgData.putFloat(((val>> 8) & 0xFF) / 255.f);
                mImgData.putFloat((val & 0xFF) / 255.f);
            }
        }
        return mImgData;

    }


}
