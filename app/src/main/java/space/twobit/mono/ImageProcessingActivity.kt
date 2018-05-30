package space.twobit.mono

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.LoaderManager
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import android.widget.ToggleButton
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_image_processing.*
import space.twobit.mono.helpers.ImageUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ImageProcessingActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Bitmap> {

    private var workingFileUri: Uri? = null
    private var resultBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_processing)

        // This is how the intent will get the image
        val intentReceiver = intent
        // Getting image file path with File object
        val file = File(intentReceiver.getStringExtra(INTENT_IMAGE_TO_LOAD))
        // If file's still exist, create an Uri with the file object
        if (file.exists())
            workingFileUri = Uri.fromFile(file)
        else
            finishNoExtras() // Error, no extras found.

        // Using Glide Library to load image
        Glide.with(applicationContext)
                .load(File(workingFileUri!!.path).absolutePath)
                .into(imageViewBefore)

        // Set a new listener to threshold SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Reload the loader if the user changed threshold value
                // To reapply the effect
                reloadLoader()

            }
        })
        reloadLoader()

        btn_grayscale.setOnCheckedChangeListener { _, _ ->
            reloadLoader()
        }

        imageViewAfter.setOnTouchListener { _, event ->
            hideBeforeImage(event)
        }

        btn_negative.setOnCheckedChangeListener { buttonView, isChecked -> thresholdButtonClicked(buttonView as ToggleButton, isChecked) }
        btn_save.setOnClickListener { openSave() }
        btn_share.setOnClickListener { openShareMenu() }
    }

    /**
     * Reload the loader and run it on a new thread
     */
    fun reloadLoader() {
        pb_processing.visibility = View.VISIBLE
        supportLoaderManager.restartLoader(LOADER_BOKAY_PROCESS, null, this).forceLoad()
    }

    /**
     * A method to hide/show filtered images based on user touch input
     *
     * @param motionEvent Motion event, describing the event details
     * @return boolean
     */
    private fun hideBeforeImage(motionEvent: MotionEvent): Boolean {
        // Flow control based on user action
        when (motionEvent.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN ->
                // Hide filtered image view when user touched the image
                imageViewAfter!!.visibility = View.INVISIBLE
            MotionEvent.ACTION_UP ->
                // Show filtered image view if user released the touch
                imageViewAfter!!.visibility = View.VISIBLE
        }

        // Return true, everything is fine.
        return true
    }

    fun thresholdButtonClicked(button: ToggleButton, checked: Boolean) {
//        seekBar.visibility = if (checked) View.VISIBLE else View.GONE
        reloadLoader()
    }

    /**
     * This function is to open the save file window
     */
    private fun openSave() {
        // Do nothing if resultBitmap is null (Loader still processing the image / error)
        if (resultBitmap == null)
            return

        // Create filename with prefixes IMG
        // The file will be named with datetime from the moment the file is saved
        // And the file will be stored in a new folder that specially made for it
        val filename = "IMG_" + SimpleDateFormat.getDateTimeInstance().format(Date())
        var folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        folder = File(folder.absolutePath + File.pathSeparator + getString(R.string.app_name))
        if (!folder.exists())
            if (!folder.mkdirs())
                folder = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        assert(folder != null)
        // Use the MediaStore to save images
        val path = MediaStore.Images.Media.insertImage(contentResolver, resultBitmap, folder.absolutePath + File.pathSeparator + filename, null)

        // Show a Toast so the user knows that the image has been saved
        Toast.makeText(this, if (path != null) R.string.save_success else R.string.save_error, Toast.LENGTH_SHORT).show()
    }


    /**
     * This function will allow user to share the image file when user clicked btn_share view
     */
    private fun openShareMenu() {
        if (resultBitmap == null)
            return

        val intent = Intent(Intent.ACTION_SEND)                                                        // this is to send the image to other activity
        intent.type = "image/jpeg"

        val path = MediaStore.Images.Media.insertImage(contentResolver, resultBitmap, null, null)
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path))

        // Launch the SEND intent and set the title "Share the result!"
        startActivity(Intent.createChooser(intent, "Share the result!"))
    }

    private fun finishNoExtras() {
        AlertDialog.Builder(this)
                .setTitle("Error!")
                .setMessage("There is no image to be processed, please try again later. ")
                .setPositiveButton("OK", { _, _ -> this@ImageProcessingActivity.finish() })
                .show()
    }

    /**
     * A Loader class to process the image in the background
     * Application need to process the image in the background thread
     * so it doesn't block main thread for slower devices
     */
    private class ImageProcessLoader(context: Context, val bitmap: Bitmap, val threshold: Int) : AsyncTaskLoader<Bitmap>(context) {
        internal var isNegative = true
        internal var isGrayscale = true

        override fun loadInBackground(): Bitmap? {
            return ImageUtils.process(bitmap, isGrayscale, isNegative)
        }

    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Bitmap> {

        val options = BitmapFactory.Options()
        options.inSampleSize = 8
        // Get the file that has been chosen by the user
        val bitmap = BitmapFactory.decodeFile((File(this@ImageProcessingActivity.workingFileUri!!.path)).toString(), options)

        // Create a new Loader
        val loader = ImageProcessLoader(this, bitmap, (seekBar.progress * 2.54 + 1).toInt())
        loader.isNegative = btn_negative.isChecked
        loader.isGrayscale = btn_grayscale.isChecked

        return loader
    }

    override fun onLoadFinished(loader: Loader<Bitmap>, data: Bitmap?) {
        // Show the images into imageViewAfter view after loader finished its processes
        Glide.with(applicationContext)
                .asBitmap()
                .load(data)
                .into(imageViewAfter)

        // Hide processing view
        pb_processing.visibility = View.INVISIBLE

        // Copy the image (resultBitmap will be collected by garbage collector, so we need to copy it)
        resultBitmap = data?.copy(data.config, true)
    }

    override fun onLoaderReset(loader: Loader<Bitmap>) {
        // Hide processing view
        pb_processing.visibility = View.INVISIBLE
    }

    companion object {
        const val INTENT_IMAGE_TO_LOAD = "image"
        private const val LOADER_BOKAY_PROCESS = 3
    }
}