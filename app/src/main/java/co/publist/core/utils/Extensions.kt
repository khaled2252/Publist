package co.publist.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.FileProvider
import co.publist.core.utils.Extensions.Constants.GALLERY
import co.publist.core.utils.Extensions.Constants.TEMP_IMAGE
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import kotlin.math.abs

object Extensions {

    fun hideSoftKeyboard(context: Context, iBinder: IBinder) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(iBinder, 0)
    }

    fun getDistanceBetweenViews(
        firstView: View,
        secondView: View
    ): Int {
        val firstPosition = IntArray(2)
        val secondPosition = IntArray(2)
        firstView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        firstView.getLocationInWindow(firstPosition)
        secondView.getLocationInWindow(secondPosition)
        val b = firstView.measuredHeight + firstPosition[1]
        val t = secondPosition[1]
        return abs(b - t)
    }


    fun startCroppingActivity(activity: Activity, imageUri: Uri?) {
        CropImage.activity(imageUri)
            .setGuidelines(CropImageView.Guidelines.ON)
            .setAspectRatio(2, 1)
            .setOutputCompressFormat(Bitmap.CompressFormat.JPEG)
            .start(activity)
    }

    fun navigateToGallery(activity: Activity) {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        activity.startActivityForResult(galleryIntent, GALLERY)
    }

    var resultUri: Uri? = Uri.EMPTY
    fun navigateToCamera(activity: Activity) {
        val photoFile =  File(activity.cacheDir,TEMP_IMAGE) //Will create temporary file https://stackoverflow.com/a/41618796/11276817
        resultUri = FileProvider.getUriForFile(
            activity,
            activity.packageName + ".provider",
            photoFile
        )
        //Need to create a file to let camera store image in it using Extra_output, to get Uri , to Upload , without this it will send bitmap in data.extras
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, resultUri)
        activity.startActivityForResult(cameraIntent, Constants.CAMERA)
    }

    object Constants {
        const val DB_NAME = "PublistDb"
        const val SPLASH_DELAY: Long = 2000
        const val MINIMUM_SELECTED_CATEGORIES = 1
        const val MAXIMUM_SELECTED_CATEGORIES = 5
        const val MAX_VISIBLE_TODOS = 3
        const val PLATFORM_GOOGLE = "google"
        const val PLATFORM_FACEBOOK = "facebook"
        const val EMAIL_PERMISSION = "email"
        const val PROFILE_PICTURE_PERMISSION = "public_profile"
        const val USERS_COLLECTION_PATH = "users"
        const val USER_ACCOUNTS_COLLECTION_PATH = "userAccounts"
        const val MY_CATEGORIES_COLLECTION_PATH = "myCategories"
        const val MY_FAVORITES_COLLECTION_PATH = "myFavorites"
        const val MY_LISTS_COLLECTION_PATH = "myLists"
        const val CATEGORIES_COLLECTION_PATH = "categories"
        const val WISHES_COLLECTION_PATH = "wishes"
        const val PROFILE_PICTURE_URL_FIELD = "profilePictureUrl"
        const val EMAIL_FIELD = "email"
        const val NAME_FIELD = "name"
        const val DATE_FIELD = "date"
        const val CATEGORY_ID_FIELD = "categoryId"
        const val FIND_ACTION = "find"
        const val SAVE_ACTION = "save"
        const val GALLERY = 1
        const val CAMERA = 2
        const val TEMP_IMAGE = "temp_image"
    }
}
