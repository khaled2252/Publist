package co.publist.features.createwish

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MotionEvent
import android.view.View.OnTouchListener
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import kotlinx.android.synthetic.main.activity_create_wish.*
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
    private val GALLERY = 1
    private val CAMERA = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_wish)
        setAdapter()
        setListeners()
    }

    private fun showPictureDialog() {
        val pictureDialog = AlertDialog.Builder(this)
        pictureDialog.setTitle("Select Action")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> choosePhotoFromGallery()
                1 -> takePhotoFromCamera()
            }
        }
        pictureDialog.show()
    }

    private fun choosePhotoFromGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        startActivityForResult(galleryIntent, GALLERY)
    }

    private fun takePhotoFromCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GALLERY) {
            if (data != null) {
                val contentURI = data.data
                try {
                    val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                  photoImageView.setImageBitmap(bitmap)

                } catch (e: IOException) {
                    e.printStackTrace()
//                    Toast.makeText(this@MainActivity, "Failed!", Toast.LENGTH_SHORT).show()
                }

            }

        } else if (requestCode == CAMERA) {
            val bitmap = data!!.extras!!.get("data") as Bitmap
                photoImageView.setImageBitmap(bitmap)
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

    private fun setListeners() {
        addPhotoTextView.setOnClickListener {
            if (checkAndRequestPermissions()) {
                showPictureDialog()
            }
            titleEditText.setOnFocusChangeListener { _, hasFocus ->
                when {
                    hasFocus -> {
                        titleInputLayout.hint = ""
                    }
                    titleEditText.text.isNullOrEmpty() -> {
                        titleInputLayout.hint = "Type something..."
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
    }

    private fun checkAndRequestPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val readPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)

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

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.CAMERA] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.READ_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                 // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.CAMERA] == PackageManager.PERMISSION_GRANTED
                        && perms[Manifest.permission.READ_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                        ) {
                        Log.d("tag", "sms & location services permission granted")
                        // process the normal flow
                        showPictureDialog()
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d("tag", "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
                            || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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

//    }

    //    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
//        AlertDialog.Builder(this)
//            .setMessage(message)
//            .setPositiveButton("OK", okListener)
//            .setNegativeButton("Cancel", okListener)
//            .create()
//            .show()
//    }
//
//    private fun explain(msg: String) {
//        val dialog = android.support.v7.app.AlertDialog.Builder(this)
//        dialog.setMessage(msg)
//            .setPositiveButton("Yes") { paramDialogInterface, paramInt ->
//                //  permissionsclass.requestPermission(type,code);
//                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.example.parsaniahardik.kotlin_marshmallowpermission")))
//            }
//            .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> finish() }
//        dialog.show()

}

