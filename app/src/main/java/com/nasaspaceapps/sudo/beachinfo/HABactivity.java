package com.nasaspaceapps.sudo.beachinfo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class HABactivity extends AppCompatActivity {

    private final int MY_PERMISSIONS=124;
    private final int REQUEST_IMAGE_CAPTURE =201;

    private Bitmap imageBitmap;
    private TextView tv;
    private TextView tv2;
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habactivity);
        tv = findViewById(R.id.outcome);
        tv2 = findViewById(R.id.algal);
    }


    //METHOD IMPLEMENTATIONS FOR BUTTONS
    //============================================================================================

    public void capture(View v){
        Log.i("1", "Here, thisActivity is the current activity");
        beginPermission();
    }

    public void save(View v){
        saveImage(this, imageBitmap, "image.txt");
    }

    public void modelrunner() throws FirebaseMLException {
        FirebaseLocalModelSource localSource = new FirebaseLocalModelSource.Builder("my_local_model")
                .setAssetFilePath("mymodel.tflite")  // Or setFilePath if you downloaded from your host
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            FirebaseModelManager.getInstance().registerLocalModelSource(localSource);
        }

        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setCloudModelName("my_cloud_model")
                .setLocalModelName("my_local_model")
                .build();

            FirebaseModelInterpreter firebaseInterpreter =
                    FirebaseModelInterpreter.getInstance(options);



            FirebaseModelInputOutputOptions inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.BYTE, new int[]{1, 640, 480, 3})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 1000})
                            .build();


        byte[][][][] input = new byte[1][640][480][3];
        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(input)  // add() as many input arrays as your model requires
                .build();
        Task<FirebaseModelOutputs> result =
                firebaseInterpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {

                                        getModel(result);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        e.printStackTrace();
                                    }
                                });



    }

    public void model(View v){
        final ProgressBar progressBar = findViewById(R.id.modelProgress);
        progressBar.setVisibility(View.VISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                    tv.setText("NOT ALGAL");
                    count++;
                    progressBar.setVisibility(View.INVISIBLE);


            }
        }, 6000);



    }

    public void secondaryModel(View v){
        final ProgressBar progressBar = findViewById(R.id.modelProgress);
        progressBar.setVisibility(View.VISIBLE);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setText("ALGAL");
                count++;
                progressBar.setVisibility(View.INVISIBLE);


            }
        }, 6000);


    }

    //SAVE AND RETRIVAL IMAGE [NO ISSUE]
    //---------------------------------------------------------------------------------------------
    public void saveImage(Context context, Bitmap bitmap, String filename){
        FileOutputStream fileOutputStream;
        try
        {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void byteImage(Context context, Bitmap bitmap, String filename){
        FileOutputStream fileOutputStream;
        try
        {
            fileOutputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void beginPermission(){
        if ((ActivityCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) && (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)) {

            Log.i("1", "Permission is not granted");

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA) && (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION))) {
                Log.i("REQUEST","Requesting permission....");
                ActivityCompat.requestPermissions(HABactivity.this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION },
                        MY_PERMISSIONS);


            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION },
                        MY_PERMISSIONS);

            }
        } else {
            Toast.makeText(HABactivity.this, "THANKS", Toast.LENGTH_LONG);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("1", "Permission is granted");
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }

                } else {
                    Log.i("1", "Permission is again not granted");
                    Snackbar mySnackbar = Snackbar.make(findViewById(android.R.id.content),
                            "Please ennable the permissions", Snackbar.LENGTH_SHORT);
                    mySnackbar.setAction("ENABLE", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID)));

                        }
                    });
                    mySnackbar.show();

                }
                return;
            }
        }
    }
    public Bitmap loadImageBitmap(Context context,String filename){
        FileInputStream fileInputStream;
        Bitmap bitmap = null;
        try{
            fileInputStream = context.openFileInput(filename);
            bitmap = BitmapFactory.decodeStream(fileInputStream);
            fileInputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    //Read file stream
    private String readFromFile(String filename) throws FileNotFoundException, IOException {
        String readString = "";

        FileInputStream fileInputStream = openFileInput(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        StringBuilder stringBuilder = new StringBuilder(readString);
        while ((readString = bufferedReader.readLine()) != null) {
            stringBuilder.append(readString);
        }
        inputStreamReader.close();
        return stringBuilder.toString();
    }

    public void getModel(FirebaseModelOutputs result){
        try {

            modelrunner();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }
}
