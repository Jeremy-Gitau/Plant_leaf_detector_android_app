package com.example.plant_leaf;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.plant_leaf.ml.LeafDetector;
import com.example.plant_leaf.ml.PlantLeaf;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class HomePage extends AppCompatActivity {
    FloatingActionButton camera, folder, add;
    Animation open,close,forward,backword;
    boolean isOpen = false;
    TextView textView,txtView;
    ImageView imageView4;

    int imageSize = 256;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        camera =(FloatingActionButton) findViewById(R.id.camera);
        folder =(FloatingActionButton) findViewById(R.id.folder);
        add = (FloatingActionButton) findViewById(R.id.add);

        open = AnimationUtils.loadAnimation(this, R.anim.fab_open);
        close = AnimationUtils.loadAnimation(this, R.anim.fab_close);

        forward = AnimationUtils.loadAnimation(this, R.anim.rotate_forward);
        backword = AnimationUtils.loadAnimation(this, R.anim.rotate_backword);

        textView = findViewById(R.id.textView);
        txtView = findViewById(R.id.txtView);

        imageView4 = findViewById(R.id.imageView4);

        //floating action
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                animateAdd();

            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,3);
                }
                else{
                    requestPermissions(new String[]{Manifest.permission.CAMERA},100);
                }
            }
        });

        folder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent,1);
            }
        });
    }

    public void modelPredict(Bitmap image){

        try {
            LeafDetector model = LeafDetector.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());


            int[] integerValues =  new int[imageSize*imageSize];
            image.getPixels(integerValues,0,image.getWidth(), 0,0,image.getWidth(),image.getHeight());
            int pixel = 0;

            for(int i = 0; i<imageSize; i++){
                for(int j = 0; j<imageSize; j++){
                    int val = integerValues[pixel++]; //rgb
                    byteBuffer.putFloat(((val >> 16 ) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat(((val >> 8 ) & 0xFF) * (1.f/1));
                    byteBuffer.putFloat((val & 0xFF) * (1.f/1));

                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            LeafDetector.Outputs outputs11 = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs11.getOutputFeature0AsTensorBuffer();

            float[] conf = outputFeature0.getFloatArray();
            for (int k = 0; k<conf.length; k++){
                if (conf[k] < 0.5){
                    try {
                        PlantLeaf model2 = PlantLeaf.newInstance(getApplicationContext());

                        // Creates inputs for reference.
//                        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 256, 256, 3}, DataType.FLOAT32);
//                        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
//                        byteBuffer.order(ByteOrder.nativeOrder());



                        int[] intValues =  new int[imageSize*imageSize];
                        image.getPixels(intValues,0,image.getWidth(), 0,0,image.getWidth(),image.getHeight());
                        int pixels = 0;

                        for(int i = 0; i<imageSize; i++){
                            for(int j = 0; j<imageSize; j++){
                                int val = intValues[pixels++]; //rgb
                                byteBuffer.putFloat(((val >> 16 ) & 0xFF) * (1.f/1));
                                byteBuffer.putFloat(((val >> 8 ) & 0xFF) * (1.f/1));
                                byteBuffer.putFloat((val & 0xFF) * (1.f/1));

                            }
                        }
                        inputFeature0.loadBuffer(byteBuffer);

                        // Runs model inference and gets result.
                        PlantLeaf.Outputs outputs = model2.process(inputFeature0);
                        TensorBuffer outputFeature00 = outputs.getOutputFeature0AsTensorBuffer();

                        float[] confidences = outputFeature00.getFloatArray();
                        int maxPos = 0;
                        float maxConfidence = 0;
                        for (int i = 0 ; i< confidences.length; i++){
                            if (confidences[i] > maxConfidence){
                                maxConfidence = confidences[i];
                                maxPos = i;

                            }
                        }
                        String[] classes = {"Apple Apple scab", "Apple Black rot","Apple Cedar apple rust","Apple healthy","Blueberry healthy", "Cherry (including sour) Powdery mildew","Cherry (including sour) healthy","Corn_(maize) Cercospora leaf spot Gray leaf spot",
                                "Corn (maize) Common rust", "Corn (maize) Northern Leaf Blight", "Corn (maize) healthy","Grape Black rot", "Grape Esca (Black Measles)",
                                "Grape Leaf blight (Isariopsis Leaf Spot)","Grape healthy","orange Haunglongbing (Citrus greening)","Peach Bacterial spot",
                                "Peach healthy", "Pepper,bell Bacterial spot", "Pepper, bell healthy", "Potato Early blight", "Potato Late blight", "Potato healthy",
                                "Raspberry healthy","Soybean healthy","Squash Powdery mildew", "Strawberry Leaf scorch","Strawberry healthy","Tomato Bacterial spot",
                                "Tomato Early blight","Tomato Late blight","Tomato Leaf Mold","Tomato Septoria leaf spot", "Tomato Spider mites Two spotted spider mite",
                                "Tomato Target Spot","Tomato Tomato Yellow Leaf Curl Virus", "Tomato Tomato mosaic virus","Tomato healthy"};

                        textView.setText(classes[maxPos]);
                        txtView.setText(String.format("Accuracy: %s", outputFeature00.getFloatArray()[maxPos]));
                        // Releases model resources if no longer used.
                        model.close();
                    } catch (IOException e) {
                        // TODO Handle the exception
                    }
                }
                else{
                    textView.setText("ERROR!! Image is not a leaf!!");
                    txtView.setText("PLease Upload a leaf image");
                }
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == 3) {
                Bitmap image = (Bitmap) data.getExtras().get("data");
                int dimension = Math.min(image.getWidth(), image.getHeight());
                image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                imageView4.setImageBitmap(image);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                modelPredict(image);
            }
            else{
                Uri dat = data.getData();
                Bitmap image = null;
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                }
                catch(IOException e){
                    e.printStackTrace();
                }
                imageView4.setImageBitmap(image);
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                modelPredict(image);
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private void animateAdd(){
        if (isOpen){
            add.startAnimation(forward);
            camera.startAnimation(close);
            folder.startAnimation(close);
            camera.setClickable(false);
            folder.setClickable(false);
            isOpen=false;
        }
        else{
            add.startAnimation(backword);
            camera.startAnimation(open);
            folder.startAnimation(open);
            camera.setClickable(true);
            folder.setClickable(true);
            isOpen = true;
        }
    }
}