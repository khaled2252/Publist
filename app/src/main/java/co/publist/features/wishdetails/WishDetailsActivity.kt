package co.publist.features.wishdetails

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.common.data.models.wish.Wish
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.core.utils.Utils
import co.publist.core.utils.Utils.Constants.DETAILS
import co.publist.core.utils.Utils.Constants.WISH_DETAILS_INTENT
import co.publist.features.createwish.CreateWishActivity
import co.publist.features.wishes.WishesFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_wish_details.*
import kotlinx.android.synthetic.main.edit_wish_bottom_sheet.*
import javax.inject.Inject


class WishDetailsActivity : BaseActivity<WishDetailsViewModel>() {

    @Inject
    lateinit var viewModel: WishDetailsViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    private lateinit var wishesFragment: WishesFragment
    private lateinit var sheetBehavior: BottomSheetBehavior<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wish_details)
        onCreated()
        setObservers()
        setListeners()
    }

    private fun setObservers() {
        wishesFragment.viewModel.wishDeletedLiveData.observe(this, Observer {
            Toast.makeText(this, getString(R.string.delete_wish), Toast.LENGTH_SHORT).show()
            finish()
        })

        wishesFragment.viewModel.editWishLiveData.observe(this, Observer {wish ->
            val intent = Intent(this, CreateWishActivity::class.java)
            intent.putExtra(Utils.Constants.EDIT_WISH_INTENT,wish)
            startActivity(intent)
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        })

        wishesFragment.viewModel.isFavoriteAdded.observe(this, Observer {isFavoriteAdded ->
            if(isFavoriteAdded)
                Toast.makeText(this,getString(R.string.add_favorite), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this,getString(R.string.remove_favorite), Toast.LENGTH_SHORT).show()

        })

    }
    override fun onStart() {
        wishesFragment.viewModel.loadData(DETAILS)  // To reload data when coming back from another activity
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        super.onStart()
    }
    private fun onCreated() {
        wishesFragment =
            supportFragmentManager.findFragmentById(R.id.wishesFragment) as WishesFragment
        sheetBehavior = BottomSheetBehavior.from(editWishBottomSheet)
        wishesFragment.viewModel.selectedWish = intent.getParcelableExtra(WISH_DETAILS_INTENT)!!
    }

    private fun setListeners() {
        backArrowImageViewLayout.setOnClickListener {
            onBackPressed()
        }

        sheetBehavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                blurredBgView.visibility = View.VISIBLE
                //Change alpha on sliding
                blurredBgView.alpha = slideOffset
//                window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    blurredBgView.visibility = View.GONE
//                    window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
        })

        blurredBgView.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        editWishTextView.setOnClickListener {
            wishesFragment.viewModel.editSelectedWish()
        }

        deleteWishTextView.setOnClickListener {
            sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            showDeleteDialog()
        }

        editWishBottomSheet.setOnClickListener {
            //Do nothing when clicking on empty space to avoid triggering blurredBg thus collapsing bottomsheet
        }
    }

    private fun showDeleteDialog() {
        val deleteDialog =
            AlertDialog.Builder(this)
        deleteDialog.setTitle(getString(R.string.delete_dialog_title))
        deleteDialog.setPositiveButton(getString(R.string.yes)) { _, _ ->
            wishesFragment.viewModel.deleteSelectedWish()
            sheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
        deleteDialog.setNegativeButton(getString(R.string.cancel)) { _, _ ->
        }
        deleteDialog.show()
    }

    fun showEditWishDialog(wish: Wish) {
        wishesFragment.viewModel.selectedWish = wish
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

}