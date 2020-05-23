package co.publist.core.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import co.publist.R
import co.publist.core.common.data.models.User
import co.publist.core.utils.DataBindingAdapters.loadProfilePicture
import co.publist.core.utils.Utils.Constants.FETCH_USER_PICTURE_CLOUD_FUNCTION
import co.publist.core.utils.Utils.Constants.GALLERY
import co.publist.core.utils.Utils.Constants.TEMP_IMAGE
import co.publist.core.utils.Utils.Constants.USER_IDS_FIELD
import co.publist.core.utils.Utils.Constants.WISH_IMAGE_FIXED_HEIGHT
import co.publist.core.utils.Utils.Constants.WISH_IMAGE_FIXED_WIDTH
import co.publist.features.login.LoginActivity
import com.google.firebase.functions.FirebaseFunctions
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.util.*
import kotlin.math.abs
import kotlin.reflect.full.memberProperties

object Utils {

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
            .setMaxCropResultSize(WISH_IMAGE_FIXED_WIDTH, WISH_IMAGE_FIXED_HEIGHT)
            .setOutputCompressQuality(40)
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
        val photoFile = File(
            activity.cacheDir,
            TEMP_IMAGE
        ) //Will create temporary file https://stackoverflow.com/a/41618796/11276817
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

    fun get90DegreesAnimation(): RotateAnimation {
        val anim = RotateAnimation(
            0f, -90f, Animation.RELATIVE_TO_SELF,
            0.5f, Animation.RELATIVE_TO_SELF, 0.5f
        )
        anim.interpolator = LinearInterpolator()
        anim.duration = 500
        anim.isFillEnabled = true
        anim.fillAfter = true
        return anim
    }

    fun getUnfavoriteAnimation(imageView: ImageView): Animation? {
        val animation = AnimationUtils.loadAnimation(imageView.context, R.anim.favorite_activate)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                imageView.setImageResource(R.drawable.ic_heart)
            }

            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}

        })
        return animation
    }

    fun loadTopUsersPictures(
        topUsersId: ArrayList<String>?,
        imageViewArrayList: ArrayList<ImageView>,
        user: User?
    ) {
        val topUsersIdCopyArray =
            topUsersId?.toMutableList() //Take a copy of the received array and do logic on it to avoid changing data in all its referrals
        //Clear extra loaded images when updating (i.e removed images)
        if (topUsersIdCopyArray.isNullOrEmpty()) {
            for (imageView in imageViewArrayList)
                imageView.setImageDrawable(null)
            return //Case where all images are removed , Undraw all then terminate
        } else if (topUsersIdCopyArray.isNotEmpty()) {
            for (emptyIndex in topUsersIdCopyArray.size.until(imageViewArrayList.size))
                imageViewArrayList[emptyIndex].setImageDrawable(null)
            //Case where some images are removed , Undraw them then load new images
        }

        //Load cached user image if is in topUsersId and load placeholders for other users till loaded
        if (user != null && topUsersIdCopyArray.contains(user.id!!)) {
            for (topUserIdIndex in topUsersIdCopyArray.size - 1 downTo 0) {
                if (topUsersIdCopyArray[topUserIdIndex] == user.id) {
                    loadProfilePicture(imageViewArrayList[topUserIdIndex], user.profilePictureUrl)
                    topUsersIdCopyArray.removeAt(topUserIdIndex)
                    imageViewArrayList.removeAt(topUserIdIndex)
                } else
                    loadProfilePicture(
                        imageViewArrayList[topUserIdIndex],
                        null
                    ) //Load placeholders for images to be loaded
            }
        } else {// case guest , load placeholders for all images
            for (index in topUsersIdCopyArray.indices) {
                loadProfilePicture(
                    imageViewArrayList[index],
                    null
                )
            }
        }

        if (topUsersIdCopyArray.isNotEmpty()) {
            //Load new Images
            FirebaseFunctions.getInstance().getHttpsCallable(FETCH_USER_PICTURE_CLOUD_FUNCTION)
                .call(hashMapOf(USER_IDS_FIELD to topUsersIdCopyArray))
                .continueWith { task ->
                    val result = task.result?.data as HashMap<String, ArrayList<String>>
                    result
                }.addOnSuccessListener { result ->
                    val pictureUrlsArrayList = result.values.elementAt(0)
                    for (pictureUrlIndex in 0 until pictureUrlsArrayList.size)
                        loadProfilePicture(
                            imageViewArrayList[pictureUrlIndex],
                            pictureUrlsArrayList[pictureUrlIndex]
                        )
                }
        }
    }

    fun showLoginPromptForGuest(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(context.getString(R.string.guest_action_prompt))
        builder.setPositiveButton(context.getString(R.string.login)) { _, _ ->
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(context, intent, null)
        }
        builder.setNegativeButton(context.getString(R.string.cancel)) { _, _ ->
        }
        builder.create().show()
    }

    @Throws(IllegalAccessException::class, ClassCastException::class)
    inline fun <reified T> Any.getField(fieldName: String): T? {
        this::class.memberProperties.forEach { kCallable ->
            if (fieldName == kCallable.name) {
                return kCallable.getter.call(this) as T?
            }
        }
        return null
    }

    fun isConnectedToNetwork(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            } else {
                connectivityManager.activeNetworkInfo
            }
        if (capabilities != null) {
            return true
        }
        return false
    }

    object Constants {
        const val DB_NAME = "PublistDb"
        const val SPLASH_DELAY: Long = 2000
        const val LOAD_MORE_DELAY: Long = 100
        const val MINIMUM_SELECTED_CATEGORIES = 1
        const val MAXIMUM_SELECTED_CATEGORIES = 9
        const val MAX_VISIBLE_WISH_ITEMS = 3
        const val MINIMUM_WISH_ITEMS = 3
        const val WISHES_NUM_PER_PAGE = 10
        const val VISIBLE_THRESHOLD = 1
        const val TOP_USERS_THRESHOLD = 10
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
        const val ITEMS_ID_SUB_COLLECTION_PATH = "itemsId"
        const val COMPLETED_USERS_IDS_COLLECTION_PATH = "completedUsersId"
        const val LIKED_USERS_IDS_COLLECTION_PATH = "viewedUsersId"
        const val USER_VIEWED_ITEMS_COLLECTION_PATH = "viewedItems"
        const val PROFILE_PICTURE_URL_FIELD = "profilePictureUrl"
        const val TOP_COMPLETED_USER_IDS_FIELD = "topCompletedUsersId"
        const val TOP_VIEWED_USER_IDS_FIELD = "topViewedUsersId"
        const val CREATOR_FIELD = "creator"
        const val WISH_ID_FIELD = "wishId"
        const val EMAIL_FIELD = "email"
        const val NAME_FIELD = "name"
        const val DATE_FIELD = "date"
        const val CATEGORY_ID_FIELD = "categoryId"
        const val COMPLETE_COUNT_FIELD = "completeCount"
        const val LIKE_COUNT_FIELD = "viewedCount"
        const val ITEMS_FIELD = "items"
        const val IS_DONE_FIELD = "done"
        const val USER_IDS_FIELD = "userIds"
        const val USER_DOC_ID_FIELD = "userDocId"
        const val WISH_DOC_ID_FIELD = "wishDocId"
        const val ID_FIELD = "id"
        const val IMAGE_PATH_FIELD = "imagePath"
        const val GALLERY = 1
        const val CAMERA = 2
        const val TEMP_IMAGE = "temp_image"
        const val PUBLIC = 0
        const val LISTS = 1
        const val FAVORITES = 2
        const val DETAILS = 3
        const val SEARCH = 4
        const val WISH_IMAGE_FIXED_WIDTH = 2400
        const val WISH_IMAGE_FIXED_HEIGHT = 1200
        const val EDIT_WISH_INTENT = "editedWish"
        const val COMING_FROM_PROFILE_INTENT = "isComingFromProfile"
        const val WISH_DETAILS_INTENT = "detailsIntent"
        const val FLAME_ICON_VIEWED_COUNT_PERCENTAGE = 0.3
        const val FLAME_ICON_COMPLETED_MINIMUM = 50
        const val FETCH_USER_PICTURE_CLOUD_FUNCTION = "fetchUserPictureUrl"
        const val SEEN_FOR_WISH_CLOUD_FUNCTION = "seenForWish"
        const val ORGANIC_SEEN_FOR_WISH_CLOUD_FUNCTION = "organicSeenForWish"
        const val ALGOLIA_APP_ID = "6RAUVU14O2"
        const val ALGOLIA_API_KEY = "c858b1b659337e1e6c9cc0cbc716e1d0"
        const val ALGOLIA_DATABASE_INDEX = "wishes"
        const val ALGOLIA_HITS_FIELD = "hits"
        const val ALGOLIA_WISH_ID_FIELD = "objectID"
        const val AUTO_COMPLETE_TEXT_VIEW_ID = "android:id/search_src_text"
        const val NULL_STRING = "null"
        const val GENERAL_TYPE = -1
        const val LOADING_MORE = -2
    }
}
