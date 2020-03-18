package co.publist.features.createwish

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import co.publist.R
import co.publist.core.common.data.models.Mapper
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.DataBindingAdapters
import co.publist.core.utils.DragManageAdapter
import co.publist.core.utils.Utils.Constants.CAMERA
import co.publist.core.utils.Utils.Constants.GALLERY
import co.publist.core.utils.Utils.Constants.MINIMUM_WISH_ITEMS
import co.publist.core.utils.Utils.getDistanceBetweenViews
import co.publist.core.utils.Utils.navigateToCamera
import co.publist.core.utils.Utils.navigateToGallery
import co.publist.core.utils.Utils.resultUri
import co.publist.core.utils.Utils.startCroppingActivity
import co.publist.features.categories.CategoriesFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_create_wish.*
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
    private var editedWish: Wish? = null

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
                startCroppingActivity(this, data.data!!)
            }

        } else if (requestCode == CAMERA && resultCode == RESULT_OK) {
            startCroppingActivity(
                this,
                resultUri
            ) //Camera will automatically load image in resultUri (specified in intent)
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val resultUri = CropImage.getActivityResult(data).uri
                loadPhotoUriToImageView(resultUri)
                viewModel.wishImageUri = resultUri.toString()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (grantResults.isNotEmpty()) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0] == Manifest.permission.CAMERA
            )
                navigateToCamera(this)
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED && permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE)
                navigateToGallery(this)
            else {
                //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                // shouldShowRequestPermissionRationale will return true
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        permissions[0]
                    )
                ) {
                    Toast.makeText(
                        this,
                        getString(R.string.permission_required),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    //permission is denied (and never ask again is  checked)
                    //shouldShowRequestPermissionRationale will return false
                    Toast.makeText(
                        this,
                        getString(R.string.permission_from_settings),
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
        editedWish = intent.getParcelableExtra("editedWish") as? Wish
        categoriesFragment =
            supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        sheetBehavior = BottomSheetBehavior.from(categoriesFragmentBottomSheet)
        categoriesFragment.viewModel.isCreatingWish = true

        if (editedWish != null) {
            titleTextView.text = getString(R.string.edit_wish)
            addCategoryTextView.text = editedWish?.category!![0].name?.capitalize()
            categoriesFragment.viewModel.getCategories(editedWish?.category!![0])
            titleEditText.setText(editedWish?.title)
            titleInputLayout.hint = ""
            if(!editedWish?.wishPhotoURL.isNullOrEmpty())
                loadPhotoUrlToImageView(editedWish?.wishPhotoURL!!)
            postButton.text = getString(R.string.save)
            viewModel.populateWishData(editedWish!!)
        } else
            categoriesFragment.viewModel.getCategories()

    }

    private fun setAdapter() {
        adapter = ItemsAdapter {items ->
            viewModel.items = items
            viewModel.validateEntries()
            itemsRecyclerView.scrollToPosition(viewModel.items.size - 1)
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
                Toast.makeText(this, getString(R.string.post_wish_success), Toast.LENGTH_SHORT)
                    .show()
                finish()
            } else
                Toast.makeText(
                    this,
                    getString(R.string.minimum_wish_items).format(MINIMUM_WISH_ITEMS),
                    Toast.LENGTH_SHORT
                ).show()
        })

    }

    private fun setListeners() {
        backArrowImageViewLayout.setOnClickListener {
            onBackPressed()
        }

        activityCreateWishLayout.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                activityCreateWishLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                setUpItemsRecyclerViewMaxHeight()
                if (editedWish != null) {
                    val oldList = ArrayList(editedWish!!.items!!.entries
                        .sortedBy {it.value.orderId} // Sort map entries by order id
                        .map { it.value.name!! }) // get list of names of values(items)
                    adapter.populateOldList(oldList)
                }
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
                        categoriesFragment.viewModel.selectedCategoriesList.getOrNull(0)
                    if (category != null) {
                        val mappedCategory = Mapper.mapToCategory(category)
                        viewModel.category = Mapper.mapToCategoryWish(mappedCategory)
                        addCategoryTextView.text = category.name?.capitalize()
                    } else {
                        viewModel.category = null
                        addCategoryTextView.text =
                            getString(R.string.create_wish_categories_default)
                    }

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
            viewModel.wishImageUri = ""
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
            }
            false
        }

        titleEditText.setOnFocusChangeListener { _, hasFocus ->
            when {
                hasFocus -> {
                    titleInputLayout.hint = ""
                }
                titleEditText.text.isNullOrEmpty() -> {
                    titleInputLayout.hint = getString(R.string.title_hint)
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
                    itemTextInputLayout.hint = ""
                }
                itemEditText.text.isNullOrEmpty() -> {
                    itemTextInputLayout.hint = getString(R.string.add_item_hint)
                }
                else -> itemTextInputLayout.hint = ""
            }
        }
        itemEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                if (editable.isNullOrEmpty())
                    btnItemDone.setImageResource(R.drawable.ic_done)
                else {
                    btnItemDone.setImageResource(R.drawable.ic_done_active)

                    viewModel.validateEntries()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                btnItemDone.setImageResource(R.drawable.ic_done_active)
            }
        })

        btnItemDone.setOnClickListener {
            itemDoneOnClick()
        }
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
        pictureDialog.setTitle(getString(R.string.camera_gallery_dialog_title))
        val pictureDialogItems =
            arrayOf(getString(R.string.select_from_gallery), getString(R.string.select_from_camera))
        pictureDialog.setItems(
            pictureDialogItems
        ) { _, which ->
            when (which) {
                0 -> {
                    if (checkAndRequestPermissions(GALLERY)) {
                        navigateToGallery(this)
                    }
                }
                1 -> {
                    if (checkAndRequestPermissions(CAMERA)) {
                        navigateToCamera(this)
                    }
                }
            }
        }
        pictureDialog.show()
    }

    private fun loadPhotoUriToImageView(uri: Uri) {
        val bitmap = if (Build.VERSION.SDK_INT < 28) {
            MediaStore.Images.Media.getBitmap(
                this.contentResolver,
                uri
            )
        } else {
            val source = ImageDecoder.createSource(this.contentResolver, uri)
            ImageDecoder.decodeBitmap(source)
        }
        photoImageView.setImageBitmap(bitmap)
        imageLayout.visibility = View.VISIBLE
        addPhotoTextView.visibility = View.INVISIBLE
        listTextView.setPadding(0, 130, 0, 0)
    }

    private fun loadPhotoUrlToImageView(url: String) {
        DataBindingAdapters.loadWishImage(photoImageView, url)
        imageLayout.visibility = View.VISIBLE
        addPhotoTextView.visibility = View.INVISIBLE
        listTextView.setPadding(0, 130, 0, 0)
    }

}

