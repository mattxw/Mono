package space.twobit.mono.helpers

import android.graphics.Bitmap
import android.graphics.Matrix

import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object ImageUtils {

    /**
     * This function is to rotate the bitmap image on 90 angle
     *
     * @param source The wrong-rotated bitmap object
     * @param angle  Angle
     * @return Rotated bitmap
     */
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    // This is where the Bitmap image will be transform into matrix object
    // And the matrix object will be used for processing the adaptive gaussian processing
    fun outline(bitmap: Bitmap, threshold: Int, isGaussian: Boolean): Bitmap {
        val imageMat = Mat()
        Utils.bitmapToMat(bitmap, imageMat)

        // Process smaller image
        Imgproc.resize(imageMat, imageMat, Size(1024.0, (imageMat.height() * 1024 / imageMat.width()).toDouble()))

        //        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
        //        Imgproc.GaussianBlur(imageMat, imageMat, new Size(45, 45), 0);
        //        Imgproc.adaptiveThreshold(imageMat, imageMat, 50, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 25);

        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY)
        if (isGaussian)
            Imgproc.GaussianBlur(imageMat, imageMat, Size(3.0, 3.0), 0.0)

        // The result actually is slightly better with threshold
        // You can see the comparison example here https://docs.opencv.org/3.3.1/d7/d4d/tutorial_py_thresholding.html
        // Imgproc.adaptiveThreshold(imageMat, imageMat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 4);
//        if (threshold > 0)
//            Imgproc.threshold(imageMat, imageMat, 0.0, threshold.toDouble(), Imgproc.THRESH_OTSU)
        if (threshold > 0)
            Imgproc.adaptiveThreshold(imageMat, imageMat, threshold.toDouble(), Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 11, 2.0)

        val outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(imageMat, outputBitmap)

        // FREE imageMat!!! Release the memory
        imageMat.release()
        bitmap.recycle()
        System.gc()

        return outputBitmap
    }

    fun grayscale(bitmap: Bitmap, threshold: Int): Bitmap {
        val imageMat = Mat()
        Utils.bitmapToMat(bitmap, imageMat)

        // Process smaller image
        Imgproc.resize(imageMat, imageMat, Size(1024.0, (imageMat.height() * 1024 / imageMat.width()).toDouble()))

        //        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);
        //        Imgproc.GaussianBlur(imageMat, imageMat, new Size(45, 45), 0);
        //        Imgproc.adaptiveThreshold(imageMat, imageMat, 50, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 25);

        Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGB2GRAY)

        val outputBitmap = Bitmap.createBitmap(imageMat.cols(), imageMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(imageMat, outputBitmap)

        // FREE imageMat!!! Release the memory
        imageMat.release()
        bitmap.recycle()
        System.gc()

        return outputBitmap
    }
}
