package co.publist.features.editprofile


import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils.loadImage
import co.publist.databinding.ActivityEditProfileBinding
import co.publist.features.categories.CategoriesFragment
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_intro.buttonFindWishes
import javax.inject.Inject


class EditProfileActivity : BaseActivity<EditProfileViewModel>() {

    @Inject
    lateinit var viewModel: EditProfileViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityEditProfileBinding>(this, R.layout.activity_edit_profile)
        val categoriesFragment= supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        buttonFindWishes.setOnClickListener {
            if(categoriesFragment.viewModel.selectedCategories.size<1)
                Toast.makeText(this,"You must select at least 1 category",
                    Toast.LENGTH_SHORT).show()
            else {
                //todo navigate to home
            }
        }
        viewModel.onCreated()
        viewModel.userLiveData.observe(this, Observer {user ->
            nameTextView.text =user.name
            loadImage(profilePictureImageView,user?.profilePictureUrl)
            //todo loadSelectedCategories()
        })

    }

}
