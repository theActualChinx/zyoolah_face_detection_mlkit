package com.zyoolah.zyoolah_face_detection_mlkit.services;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.otaliastudios.cameraview.frame.Frame;
import com.otaliastudios.cameraview.frame.FrameProcessor;

import java.util.ArrayList;
import java.util.List;

public class FrameProcessorService implements FrameProcessor {

    private final Context ctx;
    private final ImageView rectView;
    private final Paint paint;

    // constants
    private final int color = 0xFFFFFFFF;
    private final boolean LARGEST_FACE_ONLY = true;

    // Change these options to suit your use case
    FaceDetectorOptions faceOptions = new FaceDetectorOptions.Builder()
            // Self explanatory
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            // Will include facial landmarks for all faces found in the frame - eyes, ears, nose, etc
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            // Will include facial classification for all faces found in the frame - smiling, eyes open, etc
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            // Will include facial contours for the most prominent face found in the frame - jawline, cheekbones, etc
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            // You can also add your own executor for the detection process using .setExecutor(Executor executor)
            // And enable face tracking using .enableTracking()
            .build();

    // Create a face detector
    FaceDetector detector = FaceDetection.getClient(faceOptions);

    public FrameProcessorService(Context ctx, ImageView rectView) {
        this.ctx = ctx;
        this.rectView = rectView;

        // Create a paint object to draw the bounding boxes
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(5);
    }

    @Override
    public void process(@NonNull Frame frame) {

        Log.i("Z-Frame", "Frame captured : " + frame.getSize().getWidth() + " x " + frame.getSize().getHeight());
        // We need to create a new InputImage object from the frame data for processing
        // NV21 is the default format for the CameraView (YUV_420_888) - Its also set in the camera configuration
        InputImage image = InputImage.fromByteArray(frame.getData(), frame.getSize().getWidth(), frame.getSize().getHeight(), frame.getRotationToView(), InputImage.IMAGE_FORMAT_NV21);

        // Processing will hold the frame for longer than the camera is happy with and thus we need to temporarily store the width, height and rotation as we will need these later
        int width = frame.getSize().getWidth();
        int height = frame.getSize().getHeight();
        int rotation = frame.getRotationToView();

        // Keep the aspect ratio
        rectView.setAdjustViewBounds(true);
        // Scale to fit center as this matches the CameraView's default behavior when displaying the camera preview using wrap_content
        rectView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        // Process the captured image
        detector.process(image).addOnSuccessListener(faces -> {

            Log.i("Z-Frame", "Faces detected : " + faces.size());

            // Create a collection to hold all the detected faces
            List<Rect> faceRects = new ArrayList<>();

            // If we are only interested in the largest face, we will create a variable to hold the largest face
            Face largestFace = null;
            // Loop through the faces detected in the frame
            for (Face face : faces) {
                // Get the bounding box of the face
                Rect boundingBox = face.getBoundingBox();

                // If we are only interested in the largest face, we will compute the area of the bounding box first and compare it with the previous largest face
                // else we just add the bounding box to the collection of faces
                if (LARGEST_FACE_ONLY) {
                    if (largestFace == null || boundingBox.width() * boundingBox.height() > largestFace.getBoundingBox().width() * largestFace.getBoundingBox().height()) {
                        largestFace = face;
                    }
                } else {
                    faceRects.add(boundingBox);
                }
            }
            // If we are only interested in the largest face, we will add the bounding box of the largest face to the collection of faces
            if (largestFace != null) {
                faceRects.add(largestFace.getBoundingBox());
            }

            // Create a new bitmap to draw the bounding boxes on
            // ARGB_8888 because it contains an alpha channel (for transparency) and it also renders fairly higher quality images
            // Because the camera (normally) is at an angle of 270deg, we will swap the width and height - this is because the camera is in landscape mode
            Bitmap bitmap = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

            // Create a new canvas to draw the bounding boxes on - attaching it to the bitmap
            Canvas canvas = new Canvas(bitmap);

            if (!faceRects.isEmpty()) {
                Log.i("Z-Frame", "Drawing " + faceRects.size() + " faces on the canvas");
                for (Rect faceRect : faceRects) {

                    // If you want to draw the facial landmarks, classification or contours, you can do so here
                    doNothing();

                    ((Activity) ctx).runOnUiThread(() -> {
                        // Draw the bounding box on the canvas
                        canvas.drawRect(faceRect, paint);
                        // Set the bitmap to the imageview
                        rectView.setImageBitmap(bitmap);
                    });
                }
            } else {
                Log.i("Z-Frame", "No faces detected");
                ((Activity) ctx).runOnUiThread(() -> {
                    // If no faces are detected, we will clear the imageview
                    rectView.setImageBitmap(null);
                });
            }

        }).addOnFailureListener(e -> {
            // Handle the error - print the stacktrace / whatever
        });

    }

    private void doNothing() {}


}
