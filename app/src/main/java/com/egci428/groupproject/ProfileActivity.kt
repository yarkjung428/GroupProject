package com.egci428.groupproject

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import kotlinx.android.synthetic.main.activity_profile.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

class ProfileActivity : AppCompatActivity() {

    val REQUEST_IMAGE_CAPTURE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //Disable the button if the user has no camera
        if (!hasCamera()) {
            photoButton.setEnabled(false)
        }
        val resimUri = Uri.parse("data/user/0/com.egci428.groupproject/app_images/profile.jpg")
        val imgFile = File(resimUri.path)
        if (imgFile.exists()) {
            photoimageView.setImageURI(resimUri)
        }

        actionbar_back.setOnClickListener(){ this.finish() }
    }

    //Check whether the user uses the camera
    private fun hasCamera(): Boolean {
        return packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    //Launch the camera
    fun launchCamera(view: View) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //Take a picture and pass the result to onActivityResult
        startActivityForResult(intent, 1)
    }

    //If you want to return the image taken
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            //Get the photo
            val extras = data!!.extras
            val photo = extras!!.get("data") as Bitmap
            photoimageView.setImageBitmap(photo)
            val uri: Uri = saveImageToInternalStorage(photo)
        }
    }

    // Method to save an image to internal storage
    private fun saveImageToInternalStorage(bitmap:Bitmap): Uri {
        // Get the context wrapper instance
        val wrapper = ContextWrapper(applicationContext)
        // Initializing a new file
        // The bellow line return a directory in internal storage
        var file = wrapper.getDir("images", Context.MODE_PRIVATE)
        // Create a file to save the image
        file = File(file, "profile.jpg")

        try {
            // Get the file output stream
            val stream: OutputStream = FileOutputStream(file)
            // Compress bitmap
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            // Flush the stream
            stream.flush()
            // Close stream
            stream.close()
        } catch (e: IOException){ // Catch the exception
            e.printStackTrace()
        }
        // Return the saved image uri
        return Uri.parse(file.absolutePath)
    }
}