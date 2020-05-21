package co.publist.features.onboarding


import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.TextSwitcher
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import co.publist.R
import co.publist.core.platform.BaseActivity
import co.publist.core.platform.ViewModelFactory
import co.publist.features.login.LoginActivity
import com.smarteist.autoimageslider.IndicatorAnimations
import kotlinx.android.synthetic.main.activity_onboarding.*
import javax.inject.Inject


class OnBoardingActivity : BaseActivity<OnBoardingViewModel>() {

    @Inject
    lateinit var viewModel: OnBoardingViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    lateinit var sliderAdapter: SliderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        onCreated()
        setListeners()
        setObservers()
    }

    private fun onCreated() {
        setupImageSlider()
        onBoardingTextSwitcher.setupTextSwitcher()
    }

    private fun setListeners() {
        skipTextView.setOnClickListener {
            viewModel.finishedOnBoarding()
        }

        buttonNext.setOnClickListener {
            if (imageSlider.currentPagePosition == sliderAdapter.count - 1)
                viewModel.finishedOnBoarding()
            else
                imageSlider.slideToNextPosition()
        }

        imageSlider.setCurrentPageListener { pageNum ->
            when (pageNum) {
                0 -> {
                    buttonNext.text = this.getText(R.string.next)
                    onBoardingTextSwitcher.setText(getString(R.string.onboarding_text1))
                }

                1 -> {
                    buttonNext.text = this.getText(R.string.next)
                    onBoardingTextSwitcher.setText(getString(R.string.onboarding_text2))
                }

                2 -> {
                    buttonNext.text = this.getText(R.string.next)
                    onBoardingTextSwitcher.setText(getString(R.string.onboarding_text3))
                }

                3 -> {
                    buttonNext.text = this.getText(R.string.get_started)
                    onBoardingTextSwitcher.setText(getString(R.string.onboarding_text4))
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.onBoardingFinished.observe(this, Observer {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        })
    }

    private fun setupImageSlider() {
        sliderAdapter = SliderAdapter(
            arrayListOf(
                R.drawable.onboarding_two,
                R.drawable.onboarding_four,
                R.drawable.onboarding_two,
                R.drawable.onboarding_four
            )
        )
        imageSlider.setSliderAdapter(sliderAdapter)
        imageSlider.setIndicatorAnimation(IndicatorAnimations.SLIDE)
    }
}

private fun TextSwitcher.setupTextSwitcher() {
    setFactory {
        val textView = TextView(context)
        textView.typeface = ResourcesCompat.getFont(
            context,
            R.font.sfprodisplaybold
        )
        textView.gravity = Gravity.CENTER
        val textSizeDimen =
            textView.resources.getDimension(R.dimen.headline_text_size)
        textView.textSize =
            textSizeDimen / textView.resources.displayMetrics.scaledDensity
        textView.text = context.getString(R.string.onboarding_text1)
        textView
    }
}
