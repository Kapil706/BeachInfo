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
import android.os.SystemClock;
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
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

public class HABactivity extends AppCompatActivity {

    private final int MY_PERMISSIONS=124;
    private final int REQUEST_IMAGE_CAPTURE =201;
    private final int DIM_IMG_SIZE_X = 224;
    private final int DIM_IMG_SIZE_Y = 224;
    private final int DIM_BATCH_SIZE = 1;
    private final int DIM_PIXEL_SIZE  = 3;

    //private ByteBuffer imgData =null; Defined in convert to byte array method
    private int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];
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
        //This might not be required
        saveImage(this, imageBitmap, "image.txt");

    }

    public void model(View v){
        try {
            modelrunner();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
        }
    }



    //The magic happens here
    public void modelrunner() throws FirebaseMLException {
        FirebaseLocalModelSource localSource = new FirebaseLocalModelSource.Builder("hab")
                .setAssetFilePath("hab.tflite")  // Or setFilePath if you downloaded from your host
                .build();

        FirebaseModelManager.getInstance().registerLocalModelSource(localSource);


        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setLocalModelName("hab")
                .build();

        FirebaseModelInterpreter firebaseInterpreter =
                FirebaseModelInterpreter.getInstance(options);


        FirebaseModelInputOutputOptions inputOutputOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                        .build();


        //byte[][][][] input = convertBitmapToByteBuffer(loadImageBitmap(this, "image.txt"));//new byte[1][640][480][3];
        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(convertBitmapToByteBuffer(imageBitmap))  // add() as many input arrays as your model requires
                .build();
        Task<FirebaseModelOutputs> result =
                firebaseInterpreter.run(inputs, inputOutputOptions)
                        .addOnSuccessListener(
                                new OnSuccessListener<FirebaseModelOutputs>() {
                                    @Override
                                    public void onSuccess(FirebaseModelOutputs result) {

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

    //This function is redundant
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

    /** Writes Image data into a {@code ByteBuffer}. */
    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        /**
        if (imgData == null) {
            Log.e("ERROR", "IMAGE DATA IS NULL");
            return null;
        }
         **/
        ByteBuffer imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE*4);
        imgData.order(ByteOrder.nativeOrder());
//        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, DIM_IMG_SIZE_X, DIM_IMG_SIZE_Y,
//                true);
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
       // long startTime = SystemClock.uptimeMillis();
        for (int i = 0; i < DIM_IMG_SIZE_X; ++i) {
            for (int j = 0; j < DIM_IMG_SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                imgData.putFloat((val >> 16) & 0xFF);
                imgData.putFloat(((val >> 8) & 0xFF));
                imgData.putFloat((val & 0xFF));
            }
            return imgData;
        }

        return imgData;


//        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * inputSize * inputSize * PIXEL_SIZE);
//        byteBuffer.order(ByteOrder.nativeOrder());
//        int[] intValues = new int[inputSize * inputSize];
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//        int pixel = 0;
//        for (int i = 0; i < inputSize; ++i) {
//            for (int j = 0; j < inputSize; ++j) {
//                final int val = intValues[pixel++];
//                byteBuffer.put((byte) ((val >> 16) & 0xFF));
//                byteBuffer.put((byte) ((val >> 8) & 0xFF));
//                byteBuffer.put((byte) (val & 0xFF));
//            }
//        }
//        return byteBuffer;

    }

    /**
    private void doRecognize(Bitmap image) {
        // Allocate space for the inference results
        float[][] confidencePerLabel = new float[1][mLabels.size()];
        // Allocate buffer for image pixels.
        int[] intValues = new int[300 * 300];
        ByteBuffer imgData = ByteBuffer.allocateDirect(1*300*300*3* 4);
        imgData.order(ByteOrder.nativeOrder());

        // Read image data into buffer formatted for the TensorFlow model
        TensorFlowHelper.convertBitmapToByteBuffer_float32(image, intValues, imgData);

        // Run inference on the network with the image bytes in imgData as input,
        // storing results on the confidencePerLabel array.
        mTensorFlowLite.run(imgData, confidencePerLabel);

        // Get the results with the highest confidence and map them to their labels
        Collection<Recognition> results =
                TensorFlowHelper.getBestResults(confidencePerLabel, mLabels);
        // Report the results with the highest confidence
        onPhotoRecognitionReady(results);
    }
     **/



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

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
