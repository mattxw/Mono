package space.twobit.mono

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image_selection.*
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import java.io.File
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ImageSelectionActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_OPEN_GALLERY = 0
        private const val REQUEST_CODE_OPEN_CAMERA = 1
    }


    // Current file path for the picture
    private var currentFilePath: String? = null

//    val buttonOpenGallery = R.id.buttonOpenGallery;
//    val buttonOpenCamera = R.id.buttonLaunchCamera;
//    val buttonProcess = R.id.buttonProcess;
//    val imageViewPreview : ImageView = R.id.imageViewPreview;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_selection)


        Log.d("Eradicated", "onCreate : Loaded content and Glide")

        // Using Glide Library to load image
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.placeholder)
                .into(imageViewPreview)

        /**
         * This function will open user's gallery if the user choose to pick the image from gallery
         */
        buttonOpenGallery.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, Uri.parse("content://media/internal/images/media"))
            startActivityForResult(intent, REQUEST_CODE_OPEN_GALLERY)
        }

        /**
         * This function will open user's camera to take the image if user choose to pick the image via camera
         * Then the image will be stored with name and path that had set
         */
        buttonLaunchCamera.setOnClickListener {
            // Create the intent
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Try to resolve the intent
            if (intent.resolveActivity(packageManager) != null) {
                try {
                    // Create filename with prefixes IMG
                    val filename = "IMG_" + SimpleDateFormat.getDateTimeInstance().format(Date())
                    // Create a temporary file based on it
                    val file = File.createTempFile(filename, ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES))
                    // Delete on app exit to save user's space
                    file.deleteOnExit()
                    // Get absolute path and set it to currentFilePath
                    currentFilePath = file.absolutePath

                    val photoURI = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)

                    val resInfoList = applicationContext.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        applicationContext.grantUriPermission(packageName, photoURI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(intent, REQUEST_CODE_OPEN_CAMERA)
                } catch (e: IOException) {                                        // if failed or there is something error
                    e.printStackTrace()
                }
            }
        }

        buttonProcess.setOnClickListener {
            /**
             * This function fill be triggered when the Process button is pressed and run ImageProcessingActivity
             */
            if (null != currentFilePath) {
                val intent = Intent(this, ImageProcessingActivity::class.java)
                intent.putExtra(ImageProcessingActivity.INTENT_IMAGE_TO_LOAD, currentFilePath)
                startActivity(intent)
            } else {
                Toast.makeText(this, R.string.no_image_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val mLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            super.onManagerConnected(status)
            when (status) {
                LoaderCallbackInterface.SUCCESS -> {
                    Log.i("OpenCV", "OpenCV loaded successfully")
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d("Eradicated", "onCreate : onResume")

        // If OpenCV couldn't be loaded, output a debug log and load by async
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback)
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Check if request is from gallery or camera with result OK
        if ((requestCode == REQUEST_CODE_OPEN_GALLERY || requestCode == REQUEST_CODE_OPEN_CAMERA) && resultCode == RESULT_OK) {

            if (requestCode == REQUEST_CODE_OPEN_GALLERY) {

                // We shouldn't process this any further if she or he didn't give me anything
                if (data == null || data.data == null)
                    return

                val selectedImage = data.data
                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

                // Cursor object was made for retrieve some data from database
                val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)!!
                cursor.moveToFirst()

                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                currentFilePath = cursor.getString(columnIndex)
                cursor.close() // Always close unused cursor
            }

            if (currentFilePath == null)
                return

            // Create a new Bitmap object from the current file
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 2;
//            currentBitmap = BitmapFactory.decodeFile(currentFilePath, options);
//
//            int orientation = 0;
//
//            try {
//                // this a class for reading and writing Exchangeable image file tags in jpeg or raw image file
//                ExifInterface imgParams = new ExifInterface(currentFilePath);
//                orientation =
//                        imgParams.getAttributeInt(
//                                ExifInterface.TAG_ORIENTATION,
//                                ExifInterface.ORIENTATION_UNDEFINED);
//            } catch (IOException e) {
//                // if process is failed or there is something error
//                e.printStackTrace();
//            }
//
//            // Object for rotating the bitmap image
//            Matrix rotate90 = new Matrix();
//            rotate90.postRotate(orientation);
//
//            // Do the "rotating" process and save it to originalBitmap
//            Bitmap originalBitmap = ImageUtils.rotateBitmap(currentBitmap, orientation);

//            Bitmap tempBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
//            Mat originalMat = new Mat(tempBitmap.getHeight(), tempBitmap.getWidth(), CvType.CV_8U);
//            Utils.bitmapToMat(tempBitmap, originalMat);
//            Bitmap currentBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, false);

            Glide.with(this)                                                                        // Using Glide Library to load image
                    .load(currentFilePath)
                    .into(imageViewPreview)
        }
    }

}
