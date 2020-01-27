package co.publist.features.editprofile


import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.databinding.BindingAdapter
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.features.categories.CategoriesFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
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
        //binding = DataBindingUtil.setContentView<editProfileDataBinding>(this, R.layout.activity_edit_profile)

        val categoriesFragment= supportFragmentManager.findFragmentById(R.id.categoriesFragment) as CategoriesFragment
        buttonFindWishes.setOnClickListener {
            if(categoriesFragment.viewModel.selectedCategories.size<1)
                Toast.makeText(this,"You must select at least 1 category",
                    Toast.LENGTH_SHORT).show()
            else {
                //todo navigate to home
            }
        }
        val user = viewModel.onCreated()
        if(user!=null)
        nameTextView.text =user.name
        //binding.loadImage(user?.profilePictureUrl)
        //todo loadSelectedCategories()
    }

    @BindingAdapter("profilePicture")
    fun loadImage(view: ImageView, imageUrl: String?) {
        Glide.with(view)
            .load(imageUrl)
            .apply(RequestOptions.circleCropTransform())
            .into(view)
    }
}
