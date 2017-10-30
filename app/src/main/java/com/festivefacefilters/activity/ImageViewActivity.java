package com.festivefacefilters.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.festivefacefilters.ObjectMover;
import com.festivefacefilters.R;
import com.festivefacefilters.SafeFaceDetector;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;

import static com.festivefacefilters.activity.CameraActivity.currFilter;

public class ImageViewActivity extends AppCompatActivity {
    private static final String TAG = "ImageView";

    private ImageView imageView;
    Bitmap bitmap;
    ProgressBar mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        byte[] bytes = (byte[])ObjectMover.get("BITMAP");
        imageView=(ImageView) findViewById(R.id.imageView);
        mProgress=(ProgressBar)findViewById(R.id.progressBar);
        Log.d("PRI", "Create Image");

        mProgress.setVisibility(View.VISIBLE);
        final Bitmap b= BitmapFactory.decodeByteArray(bytes,0,bytes.length);

        final FaceDetector detector = new FaceDetector.Builder(getApplicationContext())
                .setTrackingEnabled(false)
                .setMode(FaceDetector.FAST_MODE)
                .setLandmarkType(FaceDetector.NO_LANDMARKS)
                .setClassificationType(FaceDetector.NO_CLASSIFICATIONS)
                .build();

        Runnable r=new Runnable() {
            @Override
            public void run() {

                Frame frame=new Frame.Builder().setBitmap(b).build();
                Detector<Face> safeDetector = new SafeFaceDetector(detector);
                Log.d("PRI", "Detector started");

                SparseArray<Face> faceSparseArray=detector.detect(frame);
                Log.d("PRI", "Detector complete");

                mProgress.setVisibility(View.GONE);
                Log.d("PRI", String.valueOf(faceSparseArray.size()));
                if (!safeDetector.isOperational()) {
                    // Note: The first time that an app using face API is installed on a device, GMS will
                    // download a native library to the device in order to do detection.  Usually this
                    // completes before the app is run for the first time.  But if that download has not yet
                    // completed, then the above call will not detect any faces.
                    //
                    // isOperational() can be used to check if the required native library is currently
                    // available.  The detector will automatically become operational once the library
                    // download completes on device.
                    Log.w(TAG, "Face detector dependencies are not yet available.");

                    // Check for low storage.  If there is low storage, the native library will not be
                    // downloaded, so detection will not become operational.
                    IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
                    boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

                    if (hasLowStorage) {
                        //Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                        Log.w(TAG, getString(R.string.low_storage_error));
                    }
                }
                else {
                    createImage(b, faceSparseArray);
                }
                detector.release();

            }
        };
//        r.run();
        new Handler().postDelayed(r,500);




    }
    private void createImage(Bitmap b, SparseArray<Face> faceSparseArray) {
        Log.d("PRI", "Create Image Started");

        Bitmap bmOverlay = Bitmap.createBitmap(b.getWidth(), b.getHeight(), b.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(b, new Matrix(), null);
        Log.d("PRI", String.valueOf(faceSparseArray.size()));
        for(int i=0;i<faceSparseArray.size();i++) {
            Face face=faceSparseArray.valueAt(i);
            Log.d("PRI1", String.valueOf(face));

            Bitmap b2 = BitmapFactory.decodeResource(getResources(), CameraActivity.draw.get(currFilter));
            Bitmap b3=Bitmap.createScaledBitmap(b2, (int) face.getWidth(),
                    (int) ((b2.getHeight() * face.getWidth()) / b2.getWidth()), false);

            float x = face.getPosition().x + face.getWidth() / 2;
            float y = face.getPosition().y + face.getHeight() / 2;

            float xOffset = face.getWidth() / 2.0f;
            float yOffset = face.getHeight() / 2.0f;


            canvas.drawBitmap(b3,x-xOffset,y-yOffset,new Paint());
            Log.d("PRI", "Create Image Complete");

        }
        imageView.setImageBitmap(bmOverlay);
        try {
            // Use the compress method on the Bitmap object to write image to
            // the OutputStream
            File f=new File(Environment.getExternalStorageDirectory()+"/Haloween.png");
            if(!f.canWrite())
                f.setWritable(true);
            FileOutputStream fos = new FileOutputStream(f);

            // Writing the bitmap to the output stream
            bmOverlay.compress(Bitmap.CompressFormat.PNG, 2, fos);
            fos.close();
            Log.e("saveToInternalStorage()", "DONE");

        } catch (Exception e) {
            Log.e("saveToInternalStorage()", e.getMessage());
        }


    }
}
