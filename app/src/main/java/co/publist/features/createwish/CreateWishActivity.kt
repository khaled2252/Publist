package co.publist.features.createwish

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import kotlinx.android.synthetic.main.activity_create_wish.*
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


class CreateWishActivity : BaseActivity<CreateWishViewModel>() {
    @Inject
    lateinit var viewModel: CreateWishViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var adapter: ItemsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wish)
        setAdapter()
        setObservers()
        setListeners()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
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

        } else if (requestCode == CAMERA) {
            if (data != null) {
                val bitmap = data.extras!!.get("data") as Bitmap
                loadPhotoToImageView(bitmap)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                    ) {
                        // process the normal flow
                        showCameraGalleryDialog()
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("tag", "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.CAMERA
                            )
                            || ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )
                        ) {
//                            showDialogOK("Service Permissions are required for this app",
//                                DialogInterface.OnClickListener { dialog, which ->
//                                    when (which) {
//                                        DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
//                                        DialogInterface.BUTTON_NEGATIVE ->
//                                            // proceed with logic by disabling the related features or quit the app.
//                                            finish()
//                                    }
//                                })
                        } else {
//                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

    private fun setAdapter() {
        adapter = ItemsAdapter()
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

        viewModel.validationLiveData.observe(this, Observer {valid ->
            if(valid)
                postButton.isEnabled = true
        })

    }

    private fun setListeners() {

        addCategoryTextView.setOnClickListener {

        }

        postButton.setOnClickListener {
            viewModel.addWish()
        }

        deletePhotoImageView.setOnClickListener {
            it.visibility = View.GONE
            photoImageView.visibility = View.GONE
            addPhotoTextView.visibility = View.VISIBLE
        }

        addPhotoTextView.setOnClickListener {
            if (checkAndRequestPermissions())
                showCameraGalleryDialog()
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
                else
                    itemEditText.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_done_active,
                        0
                    )
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

        itemEditText.setOnTouchListener(OnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= itemEditText.right - itemEditText.compoundDrawables[DRAWABLE_RIGHT].bounds.width()
                ) {
                    if (itemEditText.text!!.isNotEmpty()) {
                        adapter.addItem(itemEditText.text.toString())
                        itemEditText.text = null
                        itemEditText.clearFocus()
                        this.hideKeyboard() //todo not working
                    }
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun showCameraGalleryDialog() {
        val pictureDialog = AlertDialog.Builder(this,android.R.style.Theme_Material_Light_Dialog_NoActionBar)
        pictureDialog.setTitle("Add a Photo")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> {
                    val galleryIntent = Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    )
                    startActivityForResult(galleryIntent, GALLERY)
                }
                1 -> {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA)
                }
            }
        }
        pictureDialog.show()
    }

    private fun loadPhotoToImageView(bitmap: Bitmap?) {
        photoImageView.visibility = View.VISIBLE
        photoImageView.setImageBitmap(bitmap)
        deletePhotoImageView.visibility = View.VISIBLE
        addPhotoTextView.visibility = View.INVISIBLE
    }

    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val readPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

        val listPermissionsNeeded = ArrayList<String>()

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA)
        }

        if (readPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 1)
            return false
        }
        return true
    }

    companion object {
        private const val GALLERY = 1
        private const val CAMERA = 2
    }
}

