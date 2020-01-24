package co.publist.features.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.publist.R
import co.publist.core.platform.BaseFragment
import co.publist.core.platform.ViewModelFactory
import com.firebase.ui.database.SnapshotParser
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject


class CategoriesFragment : BaseFragment<CategoriesViewModel>() {

    @Inject
    lateinit var viewModel: CategoriesViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    override fun getBaseViewModel() = viewModel

    override fun getBaseViewModelFactory() = viewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_categories, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView()
    }

    private fun initView() {
        val parser =
            SnapshotParser { snapshot ->
                val category = snapshot.getValue(Category::class.java)
                if (category != null) {
                    category.id = snapshot.key!!
                }
                category!!
            }
        var mFirebaseFirestore =  FirebaseFirestore.getInstance()

        val query = FirebaseFirestore.getInstance()
            .collection("chats")
            .orderBy("timestamp")
            .limit(50)
//        val options: FirestoreRecyclerOptions<Category> = Builder<Category>()
//            .setQuery(query, Category::class.java)
//            .build()
    }

}
