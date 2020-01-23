package co.publist.features.categories

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions

//class CategoryAdapter(
//    var data: List<Address>,
//    var options: FirebaseRecyclerOptions<>,
//    val listener: (address: Address, position: Int) -> Unit
//) :
//    FirebaseRecyclerAdapter<Category,CategoryAdapter.AddressViewHolder>(options) {
//    private var lastSelectedPosition = -1
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
//        val view =
//            LayoutInflater.from(parent.context).inflate(R.layout.address_view_item, parent, false)
//        return AddressViewHolder(view)
//    }
//
//    override fun getItemCount(): Int {
//        return data.size
//    }
//
//    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
//        holder.bind(data[position], listener, position)
//    }
//
//    inner class AddressViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        fun bind(
//            address: Address,
//            listener: (address: Address, position: Int) -> Unit,
//            position: Int
//        ) {
//            itemView.address_title.text = address.title
//            itemView.address_body.text = address.address
//            if (!edit) {
//                itemView.address_select.isChecked = lastSelectedPosition == position
//                itemView.setOnClickListener {
//                    lastSelectedPosition = position
//                    listener(address, position)
//                    notifyDataSetChanged()
//                }
//            } else {
//                itemView.address_select.visibility = View.INVISIBLE
//                itemView.img_trash.visibility = View.VISIBLE
//                itemView.img_trash.setOnClickListener {
//                    listener(address, position)
//                }
//            }
//        }
//    }
//}