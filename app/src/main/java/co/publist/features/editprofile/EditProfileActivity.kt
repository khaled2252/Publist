package co.publist.features.editprofile


import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.loadProfilePicture
import co.publist.databinding.ActivityEditProfileBinding
import co.publist.features.categories.CategoriesFragment
import kotlinx.android.synthetic.main.activity_edit_profile.*
import javax.inject.Inject


class EditProfileActivity : BaseActivity<EditProfileViewModel>() {

    @Inject
    lateinit var viewModel: EditProfileViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var categoriesFragment: CategoriesFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityEditProfileBinding>(
            this,
            R.layout.activity_edit_profile
        ).executePendingBindings()
        categoriesFragment =
            supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment

        viewModel.onCreated()
        setObservers()
        setListeners()
    }

    private fun setObservers() {
        viewModel.userLiveData.observe(this, Observer { user ->
            nameTextView.text = user.name
            loadProfilePicture(profilePictureImageView, user.profilePictureUrl)
        })

        categoriesFragment.viewModel.actionButtonLiveData.observe(this, Observer { viable ->
            if (viable)
            //todo navigate to home
            else
                Toast.makeText(
                    this,
                    "You must select at least 1 category",
                    Toast.LENGTH_SHORT
                ).show()

        })

        categoriesFragment.viewModel.saveCategoriesLiveData.observe(this, Observer {
            Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT)
                .show()
        })
    }

    private fun setListeners() {
        buttonSave.setOnClickListener {
            categoriesFragment.viewModel.handleActionButton("save")
        }
    }

}
