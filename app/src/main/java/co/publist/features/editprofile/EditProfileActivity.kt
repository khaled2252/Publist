package co.publist.features.editprofile


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.Constants.SAVE_ACTION
import co.publist.core.utils.Utils.loadProfilePicture
import co.publist.databinding.ActivityEditProfileBinding
import co.publist.features.categories.CategoriesFragment
import co.publist.features.home.HomeActivity
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

        categoriesFragment.viewModel.actionButtonLiveData.observe(this, Observer {viable ->
            if(!viable)
                Toast.makeText(
                    this,
                    R.string.minimum_categories,
                    Toast.LENGTH_SHORT
                ).show()

        })

        categoriesFragment.viewModel.saveCategoriesLiveData.observe(this, Observer {
            Toast.makeText(this, getString(R.string.saved_successfully), Toast.LENGTH_SHORT)
                .show()
            finish()
            val intent = Intent(this,HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP //If coming already from home (get last HomeActivity on top stack)
            startActivity(intent)
        })
    }

    private fun setListeners() {
        buttonSave.setOnClickListener {
            categoriesFragment.viewModel.handleActionButton(SAVE_ACTION)
        }
    }

}
