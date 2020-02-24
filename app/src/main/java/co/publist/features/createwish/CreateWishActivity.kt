package co.publist.features.createwish

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.getDistanceBetweenViews
import co.publist.features.categories.CategoriesFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_create_wish.*
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


class CreateWishActivity : BaseActivity<CreateWishViewModel>() {
    @Inject
    lateinit var viewModel: CreateWishViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var adapter: ItemsAdapter
    private lateinit var categoriesFragment: CategoriesFragment
    private lateinit var sheetBehavior: BottomSheetBehavior<*>
    //File Path , Uri From Camera
    private lateinit var imageFilePath: String
    private lateinit var photoUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wish)
        onCreated()
        setAdapter()
        setObservers()
        setListeners()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                viewModel.wishImageUri = contentURI.toString()
                try {
                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source: ImageDecoder.Source =
                            ImageDecoder.createSource(this.contentResolver, contentURI!!)
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                    }

                    loadPhotoToImageView(bitmap)

                } catch (e: IOException) {
                    Timber.e(e)
                }

            }

        } else if (requestCode == CAMERA && resultCode == RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(imageFilePath)
            loadPhotoToImageView(bitmap)
            viewModel.wishImageUri = photoUri.toString()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0] == Manifest.permission.CAMERA
            )
                navigateToCamera()
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE)
                navigateToGallery()
            else {
                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                // shouldShowRequestPermissionRationale will return true
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permissions[0]
                    )
                ) {
                    Toast.makeText(this, "Permission is required to proceed", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    //permission is denied (and never ask again is  checked)
                    //shouldShowRequestPermissionRationale will return false
                    Toast.makeText(
                        this,
                        "Enable permissions from settings to proceed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun checkAndRequestPermissions(permissionType: Int): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val readPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        var permissionNeeded = ""

        if (permissionType == CAMERA) {
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded = Manifest.permission.CAMERA
            }
        } else if (permissionType == GALLERY) {
            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                permissionNeeded = Manifest.permission.READ_EXTERNAL_STORAGE
            }
        }

        if (permissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, arrayOf(permissionNeeded), 1)
            return false
        }
        return true
    }

    private fun onCreated() {
        categoriesFragment =
            supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        sheetBehavior = BottomSheetBehavior.from(categoriesFragmentBottomSheet)
        categoriesFragment.viewModel.isCreatingWish = true
        categoriesFragment.viewModel.getSelectedCategories()
    }

    private fun setAdapter() {
        adapter = ItemsAdapter {
            viewModel.items = adapter.getlist()
            viewModel.validateEntries()
        }
        itemsRecyclerView.adapter = adapter

        // Setup ItemTouchHelper
        val callback = DragManageAdapter(
            adapter,
            UP or DOWN, START or END
        )
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(itemsRecyclerView)
    }

    private fun setObservers() {
        viewModel.validationLiveData.observe(this, Observer { isValid ->
            postButton.isEnabled = isValid
        })

        viewModel.addingWishLiveData.observe(this, Observer { isCreated ->
            if (isCreated) {
                Toast.makeText(this, "Posted Successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else
                Toast.makeText(this, "You have to make at least 3 items", Toast.LENGTH_SHORT).show()
        })

        viewModel.categoryLiveData.observe(this, Observer { categoryName ->
            addCategoryTextView.text = categoryName
        })
    }

    private fun setListeners() {

        activityCreateWishLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                activityCreateWishLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setUpItemsRecyclerViewMaxHeight()
            }

        })
        postButton.setOnClickListener {
            viewModel.postWish()
        }

        categoryDoneButton.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        blurredBgView.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                blurredBgView.visibility = View.VISIBLE
                //Change alpha on sliding
                blurredBgView.alpha = slideOffset
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    blurredBgView.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    val category =
                        categoriesFragment.viewModel.selectedCategoriesList.getOrElse(0) { "" }
                    if (category.isNotEmpty()) {
                        viewModel.categoryId = category
                        viewModel.getCategoryObject()
                    } else
                        addCategoryTextView.text = "Choose Categories"

                    viewModel.validateEntries()
                }

            }
        })
        addCategoryTextView.setOnClickListener {
            if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
            }
        }

        deletePhotoImageView.setOnClickListener {
            imageLayout.visibility = View.GONE
            addPhotoTextView.visibility = View.VISIBLE
            listTextView.setPadding(0, 0, 0, 0)
        }

        addPhotoTextView.setOnClickListener {
            showCameraGalleryDialog()
        }

        itemEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                itemDoneOnClick()
                hideKeyboard()
                itemEditText.requestFocus()
            }
            false
        }

        titleEditText.setOnFocusChangeListener { _, hasFocus ->
            when {
                hasFocus -> {
                    titleInputLayout.hint = ""
                }
                titleEditText.text.isNullOrEmpty() -> {
                    titleInputLayout.hint = "Type something"
                }
                else -> titleInputLayout.hint = ""
            }

        }

        titleEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(text: Editable?) {
                viewModel.title = text.toString()
                viewModel.validateEntries()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        itemEditText.setOnFocusChangeListener { _, hasFocus ->
            when {
                hasFocus -> {
                    itemInputLayout.hint = ""
                }
                itemEditText.text.isNullOrEmpty() -> {
                    itemInputLayout.hint = "Add Text"
                }
                else -> itemInputLayout.hint = ""
            }
        }
        itemEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable.isNullOrEmpty())
                    itemEditText.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_done,
                        0
                    )
                else {
                    itemEditText.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_done_active,
                        0
                    )
                    viewModel.validateEntries()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                itemEditText.setCompoundDrawablesWithIntrinsicBounds(
                    0,
                    0,
                    R.drawable.ic_done_active,
                    0
                )

            }
        })

        itemEditText.setOnTouchListener(OnTouchListener { _, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= itemEditText.right - itemEditText.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    itemDoneOnClick()
                    return@OnTouchListener false
                }
            }
            false
        })
    }

    private fun setUpItemsRecyclerViewMaxHeight() {
        val distance = getDistanceBetweenViews(postButton, itemEditText)
        val params = itemsRecyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintMaxHeight =
            distance - (2 * itemEditText.measuredHeight + postButton.measuredHeight)
        itemsRecyclerView.layoutParams = params
    }

    private fun itemDoneOnClick() {
        if (itemEditText.text!!.isNotEmpty()) {
            adapter.addItem(itemEditText.text.toString())
            itemEditText.text = null
        }
    }

    private fun showCameraGalleryDialog() {
        val pictureDialog =
            AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_NoActionBar)
        pictureDialog.setTitle("Add a Photo")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> {
                    if (checkAndRequestPermissions(GALLERY)) {
                        navigateToGallery()
                    }
                }
                1 -> {
                    if (checkAndRequestPermissions(CAMERA)) {
                        navigateToCamera()
                    }
                }
            }
        }
        pictureDialog.show()
    }

    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpeg",   /* suffix */
            storageDir      /* directory */
        )
        imageFilePath = image.absolutePath
        return image
    }

    private fun navigateToCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        photoUri = getUriForFile(this, applicationContext.packageName + ".provider", photoFile)
        //Need to create a file to let camera store image in it using Extra_output, to get Uri , to Upload , without this it will send bitmap in data.extras
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
        startActivityForResult(cameraIntent, CAMERA)
    }

    private fun navigateToGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun loadPhotoToImageView(bitmap: Bitmap?) {
        photoImageView.setImageBitmap(bitmap)
        imageLayout.visibility = View.VISIBLE
        addPhotoTextView.visibility = View.INVISIBLE
        listTextView.setPadding(0, 130, 0, 0)
    }
    
    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
    }
}

