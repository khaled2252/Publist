package com.publist.features.editprofile


import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.publist.R
import com.publist.core.platform.BaseActivity
import com.publist.core.platform.ViewModelFactory
import com.publist.core.utils.DataBindingAdapters.loadProfilePicture
import com.publist.core.utils.Utils.Constants.COMING_FROM_PROFILE_INTENT
import com.publist.core.utils.Utils.Constants.MINIMUM_SELECTED_CATEGORIES
import com.publist.databinding.ActivityEditProfileBinding
import com.publist.features.categories.CategoriesFragment
import com.publist.features.home.HomeActivity
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.back_button_layout.*
import javax.inject.Inject


class EditProfileActivity : BaseActivity<EditProfileViewModel>() {

    @Inject
    lateinit var viewModel: EditProfileViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var categoriesFragment: CategoriesFragment

    private var isComingFromProfile: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityEditProfileBinding>(
            this,
            R.layout.activity_edit_profile
        ).executePendingBindings()
        isComingFromProfile = this.intent.getBooleanExtra(COMING_FROM_PROFILE_INTENT, false)
        if (!isComingFromProfile!!)
            backArrowImageViewLayout.visibility = View.GONE

        onCreated()
        setObservers()
        setListeners()
    }

    private fun onCreated() {
        categoriesFragment =
            supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        categoriesFragment.viewModel.getCategories()
        viewModel.onCreated()
    }

    private fun setObservers() {
        viewModel.userLiveData.observe(this, Observer { user ->
            nameTextView.text = user.name
            loadProfilePicture(profilePictureImageView, user.profilePictureUrl)
        })

        categoriesFragment.viewModel.actionButtonLiveData.observe(this, Observer { viable ->
            if (!viable) {
                val toast =
                    Toast.makeText(
                        this,
                        resources.getQuantityText(
                            R.plurals.minimum_categories,
                            MINIMUM_SELECTED_CATEGORIES
                        ),
                        Toast.LENGTH_SHORT
                    )
                toast.setGravity(Gravity.BOTTOM, 0, 400)
                toast.show()
            }
        })

        categoriesFragment.viewModel.saveCategoriesLiveData.observe(this, Observer {
            Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT)
                .show()

            if (!isComingFromProfile!!) //from login or splash
                startActivity(Intent(this, HomeActivity::class.java))

            finish()
        })
    }

    private fun setListeners() {
        backArrowImageViewLayout.setOnClickListener {
            onBackPressed()
        }

        buttonSave.setOnClickListener {
            categoriesFragment.viewModel.handleActionButton(true)
        }
    }

}
