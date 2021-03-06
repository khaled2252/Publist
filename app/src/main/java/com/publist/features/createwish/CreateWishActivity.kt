package com.publist.features.createwish

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
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.analytics.FirebaseAnalytics
import com.publist.R
import com.publist.core.common.data.models.Mapper
import com.publist.core.common.data.models.wish.WishAdapterItem
import com.publist.core.platform.BaseActivity
import com.publist.core.platform.ViewModelFactory
import com.publist.core.utils.DataBindingAdapters
import com.publist.core.utils.DragManageAdapter
import com.publist.core.utils.Utils.Constants.CAMERA
import com.publist.core.utils.Utils.Constants.EDIT_WISH_INTENT
import com.publist.core.utils.Utils.Constants.GALLERY
import com.publist.core.utils.Utils.Constants.MINIMUM_WISH_ITEMS
import com.publist.core.utils.Utils.Constants.PUBLISH_NEW_WISH
import com.publist.core.utils.Utils.Constants.WISH_ID
import com.publist.core.utils.Utils.getDistanceBetweenViews
import com.publist.core.utils.Utils.getField
import com.publist.core.utils.Utils.isConnectedToNetwork
import com.publist.core.utils.Utils.navigateToCamera
import com.publist.core.utils.Utils.navigateToGallery
import com.publist.core.utils.Utils.resultUri
import com.publist.core.utils.Utils.startCroppingActivity
import com.publist.features.categories.CategoriesFragment
import com.publist.features.home.HomeActivity
import com.publist.features.mylists.MyListsFragment
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_create_wish.*
import kotlinx.android.synthetic.main.back_button_layout.*
import java.util.*
import javax.inject.Inject


class CreateWishActivity : BaseActivity<CreateWishViewModel>() {
    @Inject
    lateinit var viewModel: CreateWishViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var mFirebaseAnalytics: FirebaseAnalytics

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var wishCreateWishItemsAdapter: CreateWishItemsAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper
    private lateinit var categoriesFragment: CategoriesFragment
    private lateinit var sheetBehavior: BottomSheetBehavior<*>
    private var editedWish: WishAdapterItem? = null

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
        editedWish = intent.getParcelableExtra(EDIT_WISH_INTENT) as? WishAdapterItem
        categoriesFragment =
            supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        sheetBehavior = BottomSheetBehavior.from(categoriesFragmentBottomSheet)
        categoriesFragment.viewModel.isCreatingWish = true

        if (editedWish != null) {
            titleTextView.text = getString(R.string.edit_wish)
            addCategoryTextView.text = ""
            categoryChip.visibility = View.VISIBLE
            categoryChip.text = editedWish?.category!![0].name?.capitalize()
            categoriesFragment.viewModel.getCategories(editedWish?.category!![0])
            titleHintTextView.visibility = View.GONE
            titleEditText.setText(editedWish?.title)

            if (editedWish!!.items!!.isNotEmpty())
                editWishItemsLayout.visibility = View.VISIBLE

            if (!editedWish?.wishPhotoURL.isNullOrEmpty()) {
                DataBindingAdapters.loadWishImage(photoImageView, editedWish?.wishPhotoURL!!)
                updateImageLayout()
            }

            postButton.text = getString(R.string.save)
            viewModel.populateEditedWishData(editedWish!!)
        } else
            categoriesFragment.viewModel.getCategories()

    }

    private fun setAdapter() {
        wishCreateWishItemsAdapter = CreateWishItemsAdapter { items ->
            if (items.size > 0)
                editWishItemsLayout.visibility = View.VISIBLE
            else
                editWishItemsLayout.visibility = View.GONE
            viewModel.updateWishItems(items)
            viewModel.validateEntries()
            itemsRecyclerView.scrollToPosition(items.size - 1)
        }
        wishCreateWishItemsAdapter.setHasStableIds(true)
        itemsRecyclerView.adapter = wishCreateWishItemsAdapter

        // Setup ItemTouchHelper
        val callback = DragManageAdapter(
            wishCreateWishItemsAdapter,
            UP or DOWN, START or END
        )
        itemTouchHelper = ItemTouchHelper(callback)

    }

    private fun setObservers() {
        viewModel.validationLiveData.observe(this, Observer { isValid ->
            postButton.isEnabled = isValid
        })

        viewModel.addingWishLiveData.observe(this, Observer { createdWishId ->
            if (createdWishId != null) {
                Toast.makeText(this, getString(R.string.post_wish_success), Toast.LENGTH_SHORT)
                    .show()
                HomeActivity.Data.isChanged = true
                MyListsFragment.Data.isChanged = true
                finish()

                mFirebaseAnalytics.logEvent(
                    PUBLISH_NEW_WISH,
                    bundleOf(Pair(WISH_ID, createdWishId))
                )
            } else
                Toast.makeText(
                    this,
                    getString(R.string.minimum_wish_items).format(MINIMUM_WISH_ITEMS),
                    Toast.LENGTH_SHORT
                ).show()
        })

        viewModel.editedWishLiveData.observe(this, Observer {
            Toast.makeText(this, getString(R.string.edit_wish_success), Toast.LENGTH_SHORT)
                .show()
            HomeActivity.Data.isChanged = true
            MyListsFragment.Data.isChanged = true
            finish()
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
                        .sortedBy { it.value.orderId } // Sort map entries by order id
                    )
                    wishCreateWishItemsAdapter.populateOldList(oldList)
                }
            }

        })

        postButton.setOnClickListener {
            if (!isConnectedToNetwork(this))
                viewModel.noInternetConnection.postValue(true)
            else
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
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    blurredBgView.visibility = View.GONE
                    val category =
                        categoriesFragment.viewModel.selectedCategoriesList.getOrNull(0)
                    if (category != null) {
                        val mappedCategory = Mapper.mapToCategory(category)
                        viewModel.category = Mapper.mapToCategoryWish(mappedCategory)
                        addCategoryTextView.text = ""
                        categoryChip.visibility = View.VISIBLE
                        val currentDeviceLanguage = Locale.getDefault().language
                        categoryChip.text =
                            category.localizations?.getField<String>(currentDeviceLanguage)
                                ?.capitalize()
                    } else {
                        viewModel.category = null
                        categoryChip.visibility = View.GONE
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

        categoryChip.setOnCloseIconClickListener {
            categoriesFragment.viewModel.removeWishCategory()
            viewModel.category = null
            viewModel.validateEntries()
            categoryChip.visibility = View.GONE
            addCategoryTextView.text =
                getString(R.string.create_wish_categories_default)
        }

        deletePhotoImageView.setOnClickListener {
            if (editedWish != null)
                viewModel.deletedOldPhoto = true

            viewModel.wishImageUri = ""
            deletePhotoImageView.setImageResource(R.drawable.ic_attachment)

            addPhotoTextView.visibility = View.VISIBLE
            photoImageView.visibility = View.INVISIBLE

            deletePhotoImageView.isEnabled = false
            addPhotoLayout.isEnabled = true
        }

        addPhotoLayout.setOnClickListener {
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
                    titleHintTextView.visibility = View.GONE
                }
                titleEditText.text.isNullOrEmpty() -> {
                    titleHintTextView.visibility = View.VISIBLE
                }
                else -> titleHintTextView.visibility = View.GONE
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

        editWishItemsLayout.setOnClickListener {
            if (!wishCreateWishItemsAdapter.isInEditingMode) {
                // Enable editing mode
                editWishItemsImageView.setColorFilter(
                    ContextCompat.getColor(
                        this,
                        R.color.sunsetOrange
                    )
                )
                itemTouchHelper.attachToRecyclerView(itemsRecyclerView)
                viewModel.isInEditingWishItemsMode = true
            } else {
                // Disable editing mode
                editWishItemsImageView.setColorFilter(0)
                itemTouchHelper.attachToRecyclerView(null)
                viewModel.isInEditingWishItemsMode = false
            }

            wishCreateWishItemsAdapter.toggleEditingMode()
            viewModel.validateEntries()
        }
    }

    private fun setUpItemsRecyclerViewMaxHeight() {
        val distance = getDistanceBetweenViews(postButton, itemEditText)
        val params = itemsRecyclerView.layoutParams as ConstraintLayout.LayoutParams
        params.matchConstraintMaxHeight =
            distance - (2 * itemEditText.measuredHeight + (0.5 * postButton.measuredHeight).toInt())
        itemsRecyclerView.layoutParams = params
    }

    private fun itemDoneOnClick() {
        if (itemEditText.text!!.isNotEmpty()) {
            wishCreateWishItemsAdapter.addItem(itemEditText.text.toString())
            itemEditText.text = null
        }
    }

    private fun showCameraGalleryDialog() {
        val pictureDialog =
            AlertDialog.Builder(this, R.style.AlertDialogCustom)
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
        updateImageLayout()
    }

    private fun updateImageLayout() {
        deletePhotoImageView.setImageResource(R.drawable.ic_cross)

        photoImageView.visibility = View.VISIBLE
        addPhotoTextView.visibility = View.INVISIBLE

        deletePhotoImageView.isEnabled = true
        addPhotoLayout.isEnabled = false
    }

}

