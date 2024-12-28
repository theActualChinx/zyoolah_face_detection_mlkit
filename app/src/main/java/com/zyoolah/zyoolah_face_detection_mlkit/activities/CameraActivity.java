package com.zyoolah.zyoolah_face_detection_mlkit.activities;

import android.graphics.ImageFormat;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.otaliastudios.cameraview.CameraView;
import com.zyoolah.zyoolah_face_detection_mlkit.R;
import com.zyoolah.zyoolah_face_detection_mlkit.services.FrameProcessorService;


public class CameraActivity extends AppCompatActivity {

    private CameraView camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Bootstrap the activity
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_camera);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find the cameraview
        camera = findViewById(R.id.camera);

        // Configure the cameraview
        camera.setLifecycleOwner(this);
        // Keep the screen on while the camera is running
        camera.setKeepScreenOn(true);
        // Set the frame processing format to NV21 - Default android camera output
        camera.setFrameProcessingFormat(ImageFormat.NV21);


        // Find the Imageview we ar going to use to display the face bounding box
        ImageView rectView = findViewById(R.id.rect_view);

        // listen for camera frames as they are rendered on the screen - Process them in the FrameProcessor service
        camera.addFrameProcessor(new FrameProcessorService(this, rectView));

    }

}