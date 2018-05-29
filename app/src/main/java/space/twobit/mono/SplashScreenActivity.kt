package space.twobit.mono

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log

class SplashscreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("Eradicated", "Hello.")

        // Calling checkOnPermission Function
        checkOnPermission()
    }

    /**
     * Function to check if the Permission to use camera to read and write the storage
     */
    private fun checkOnPermission() {
        val permissionList = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)

        var denied = false
        for (s in permissionList) {
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED)
                denied = true
        }

        if (!denied)
            startApp()
        else
            ActivityCompat.requestPermissions(
                    this,
                    permissionList,
                    0)

    }

    /**
     * This function will be executed when the permission is granted
     * and start the Image Selection process to take an image input.
     */
    private fun startApp() {
        // Splash screen the right way
        // https://www.bignerdranch.com/blog/splash-screens-the-right-way/
        val intent = Intent(this, ImageSelectionActivity::class.java)
        Log.d("Eradicated", "onCreate : Starting Image Selection")
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        var granted = true
        for (i in permissions.indices) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                finish()
                granted = false
            }
        }

        if (granted)
        // if the permission is granted then we can start the app
            startApp() // this function will start the app
    }
}