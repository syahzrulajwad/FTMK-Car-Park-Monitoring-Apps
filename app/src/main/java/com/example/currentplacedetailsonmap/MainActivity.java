package com.example.currentplacedetailsonmap;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.appcompat.app.AppCompatActivity;

import com.example.currentplacedetailsonmap.GraphicUtils.GraphicOverlay;
import com.example.currentplacedetailsonmap.GraphicUtils.TextGraphic;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_CONTINUOUS;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_OFF;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_TAP;
import static com.wonderkiln.camerakit.CameraKit.Constants.FOCUS_TAP_WITH_MARKER;

public class MainActivity extends AppCompatActivity {


    Text longitude;
    Text latitude;
    TextView plate;
    TextView longitude1;
    TextView latitude1;
    Bitmap bitmap;
    HttpURLConnection connection;
    private String TextView;
    private android.widget.TextView Textview;

    @IntDef({FOCUS_CONTINUOUS, FOCUS_TAP, FOCUS_OFF, FOCUS_TAP_WITH_MARKER})
    public @interface Focus {
    }

    @BindView(R.id.camView) CameraView mCameraView;
    @BindView(R.id.cameraBtn) Button mCameraButton;
    @BindView(R.id.graphic_overlay) GraphicOverlay mGraphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mCameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, mCameraView.getWidth(), mCameraView.getHeight(), false);
                mCameraView.stop();
                runTextRecognition(bitmap);

            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        mCameraButton.setOnClickListener(v -> {
            mGraphicOverlay.clear();
            mCameraView.start();
            mCameraView.captureImage();

        });

        plate=(TextView)findViewById(R.id.textView);
        Button sent;
        sent = (Button) findViewById(R.id.sent);

        sent.setOnClickListener(view -> {
            if(plate.getText().toString().matches("TextView")){
                Toast.makeText(getApplicationContext(),"No text detected",Toast.LENGTH_LONG).show();
            }else{
                plate = (TextView) findViewById(R.id.textView);
                longitude1 = (TextView) findViewById(R.id.longitude);
                latitude1 = (TextView) findViewById(R.id.latitude);

                String plateNo = plate.getText().toString();
                String longitude = longitude1.getText().toString();
                String latitude = latitude1.getText().toString();

                AsyncTask.execute(() -> {
                    HttpsURLConnection connection = null;
                    int status = 0;

                    try {
                        connection = (HttpsURLConnection) new URL("https://ftmk-parking.tk/testparking2.php?plateNo="
                                + plateNo + "&latitude=" + latitude + "&longitude=" + longitude).openConnection();
                        status = connection.getResponseCode();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

//            Toast.makeText(getApplicationContext(),"Your report already sent to the system.. TQ",Toast.LENGTH_LONG).show();
                System.out.println(plateNo);
                System.out.println(longitude);
                System.out.println(latitude);


                startActivity(new Intent(MainActivity.this, loading.class));
            }});
        Intent intent=getIntent();
        String longitude=intent.getStringExtra(MapsActivity.EXTRA_TEXT);
        String latitude=intent.getStringExtra(MapsActivity.EXTRA_TEXT1);
        TextView longitude1=(TextView)findViewById(R.id.longitude);
        TextView latitude1=(TextView)findViewById(R.id.latitude);
        longitude1.setText(longitude);
        latitude1.setText(latitude);



    }

    private void runTextRecognition(Bitmap bitmap) {
        if(bitmap == null){
            Toast.makeText(getApplicationContext(),"Bitmap is null",Toast.LENGTH_LONG).show();
        }else {
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
            FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                    .getOnDeviceTextRecognizer();
            // Task failed with an exception
            detector.processImage(image)
                    .addOnSuccessListener(this::processTextRecognitionResult)
                    .addOnFailureListener(
                            Throwable::printStackTrace);
        }
    }

    private void processTextRecognitionResult(FirebaseVisionText texts) {
        List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            Toast.makeText(getApplicationContext(),"No text detected",Toast.LENGTH_LONG).show();
            //Log.d("TAG", "No text found");
            return;
        }
        else {
            for (FirebaseVisionText.TextBlock block:texts.getTextBlocks()){
                String text=block.getText();
                plate.setText(text);
            }
        }
        mGraphicOverlay.clear();
        for (int i = 0; i < blocks.size(); i++) {
            List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);

                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    public void onPause() {
        mCameraView.stop();
        super.onPause();
    }

    private void test() throws Exception{
        String text="", latitude="",longtitude="";
        HttpURLConnection connection=(HttpURLConnection)new URL(""+text).getContent();
        int status = connection.getResponseCode();
        System.out.println(status);

    }


}