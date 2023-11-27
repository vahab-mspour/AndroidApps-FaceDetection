package com.example.faceidentification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //UI views
    private ImageView originalImageIv;
    private ImageView croppedImageIv;
    private Button detectFaceBtn;

    //TAG for debugging
    private static final String TAG = "FACE_DETECT_TAG";

    //This fator is used to make the detection image smaller, to make th process faster
    private static final int SCALING_FACTOR = 10;
    private FaceDetector detector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inint UI views
        originalImageIv = findViewById(R.id.originalImageIv);
        croppedImageIv  = findViewById(R.id.croppedImageIv);
        detectFaceBtn   = findViewById(R.id.detectFaceBtn);

        FaceDetectorOptions realTimeFdo =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .build();

        //init FaceDetector obj
        detector = FaceDetection.getClient(realTimeFdo);

        //handle onclick , start detecting face from origin face
        detectFaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Her we show all ways to get bitmap from all resources like drawable, uri, image etc use the one that arrive,
                * im using drawable, so will comment others*/

                //Bitmap for drawable
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.ilhankaya1001);
                /*
                //Bitmap from Uri, in case to detect from gallery
                Uri imageUri = null;

                try {
                    Bitmap bitmap1 = MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                }catch (IOException e){
                    e.printStackTrace();
                }

                //Bitmap fro ImageView, in case the image is in ImageView may got from URL/web
                BitmapDrawable bitmapDrawable = (BitmapDrawable) originalImageIv.getDrawable();
                Bitmap bitmap1 = bitmapDrawable.getBitmap();
                */
                analyzePhoto(bitmap);
            }
        } );

    }

    private void analyzePhoto(Bitmap bitmap){
        Log.d(TAG,"analyzePhoto: ");
        Bitmap smallerBitmap = Bitmap.createScaledBitmap(bitmap,
                bitmap.getWidth()/SCALING_FACTOR,
                bitmap.getHeight()/SCALING_FACTOR,false);

        //get inputimage using bitmap, you may use fromUri method
        InputImage inputImage = InputImage.fromBitmap(smallerBitmap,0);

        //start detecting process
        detector.process(inputImage).addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {
                //there can be multiple detected from on image, manage them using loop from List<Face> faces
                Log.d(TAG,"onSuccess None of faces detected: "+faces.size());
                for (Face face: faces){
                    //get detected face as rectangle
                    Rect rect  = face.getBoundingBox();
                    rect.set(rect.left*SCALING_FACTOR,
                            rect.top*(SCALING_FACTOR-1),
                            rect.right*SCALING_FACTOR,
                            (rect.bottom*SCALING_FACTOR)+90
                    );
                }
                cropDetectFaces(bitmap, faces);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Detection failed
                Log.e(TAG, "onFailure: ",e);
                Toast.makeText(MainActivity.this, "Detection failed due to "+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void cropDetectFaces(Bitmap bitmap, List<Face> faces) {
        Log.d(TAG,"cropDetectFaces: ");
        //face was detected, Now we will crop the face part of image
        // there can be multiple faces, the we can use loop to manage each,
        // But, here we select the first one from the list..
        Rect rect = faces.get(0).getBoundingBox();//0th image of the list- OW. use loop over them
        int x = Math.max(rect.left,0);
        int y =Math.max(rect.top,0);
        int with = rect.width();
        int height = rect.height();

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap,
                x,
                y,
                (x+with > bitmap.getWidth())? bitmap.getWidth() - x : with,
                (y + height > bitmap.getHeight())? bitmap.getHeight() - y : height
        );

        //set the cropped bitmap to image view
        croppedImageIv.setImageBitmap(croppedBitmap);




    }
}