package com.example.imagetotext

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.activity_main.*
import java.util.logging.Logger

class MainActivity : AppCompatActivity() {

    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 101
    private lateinit var mCameraSource: CameraSource
    private val tag: String? = "MainActivity"
    private lateinit var textRecognizer: TextRecognizer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestForPermission()


// Code for Full screen view:

        val overlay: View = findViewById(R.id.main)
        overlay.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_FULLSCREEN)

//  Create text Recognizer

        textRecognizer = TextRecognizer.Builder(this).build()

        if (!textRecognizer.isOperational) {

            Toast.makeText(
                this,
                "Dependencies are not loaded yet...please try after few moment!!",
                Toast.LENGTH_LONG
            ).show()

            Log.e(tag, "Dependencies are downloading....getting started in a few moments ")

            return
        }

//  Init camera source to use high resolution and auto focus

        mCameraSource = CameraSource.Builder(applicationContext, textRecognizer)

            .setFacing(CameraSource.CAMERA_FACING_BACK)

            .setRequestedPreviewSize(1280, 1024)

            .setAutoFocusEnabled(true)

            .setRequestedFps(2.0f)

            .build()
        surface_camera_preview.holder.addCallback(object : SurfaceHolder.Callback {

            override fun surfaceDestroyed(holder: SurfaceHolder?) {
                mCameraSource.stop()
            }

            @SuppressLint("MissingPermission")
            override fun surfaceCreated(holder: SurfaceHolder?) {
                try {

                    if (isCameraPermissionGranted()) {

                        mCameraSource.start(surface_camera_preview.holder)

                    } else {

                        requestForPermission()

                    }

                } catch (e: Exception) {

                    toast("Error:  ${e.message}")
                }
            }


            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {

            }

        })
// stetting Up detector Function:
        textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {

            override fun release() {}



            override fun receiveDetections(detections: Detector.Detections<TextBlock>) {

                val items = detections.detectedItems



                if (items.size() <= 0) {

                    return

                }



                tv_result.post {

                    val stringBuilder = StringBuilder()

                    for (i in 0 until items.size()) {

                        val item = items.valueAt(i)

                        stringBuilder.append(item.value)

                        stringBuilder.append("\n")

                    }

                    tv_result.text = stringBuilder.toString()

                }

            }

        })


    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }


    private fun requestForPermission() {
// Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this@MainActivity,
                Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                    Manifest.permission.CAMERA)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this@MainActivity,
                    arrayOf(Manifest.permission.CAMERA),
                    MY_PERMISSIONS_REQUEST_CAMERA)

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted

        }    }

    // function for toast:
    fun toast(text: String){
        Toast.makeText(this,text,Toast.LENGTH_SHORT).show()
    }

    //For handling Permission:
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CAMERA -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    requestForPermission()
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }
}

