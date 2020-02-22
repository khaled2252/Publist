package co.publist.features.wishes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.core.common.data.models.Item
import co.publist.core.utils.Utils.Constants.MAX_VISIBLE_TODOS
import kotlinx.android.synthetic.main.item_todo.view.*


class TodosAdapter(
    var itemList: ArrayList<Item>,
    private val moreTextView: TextView,
    private val arrowImageView: ImageView,
    private val adapterIndex : Int,
    val listener : (Int) -> Unit
) :
    RecyclerView.Adapter<TodosAdapter.TodoViewHolder>() {
    private val expandableList = ArrayList<Item>()
    private var isExpanded = false

    init {
        setExpandingConditions()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(itemList[position])
        if (position >= MAX_VISIBLE_TODOS) {
            expandableList.add(itemList[position])
        }
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            item: Item
        ) {
            itemView.todoTextView.text = item.name
        }
    }

    private fun setExpandingConditions() {
        if (itemList.size <= MAX_VISIBLE_TODOS) {
            moreTextView.visibility = View.GONE
            arrowImageView.visibility = View.GONE
        } else if (!isExpanded) {
            val extraTodosNumber = itemList.size - MAX_VISIBLE_TODOS
            if(extraTodosNumber==1)
                moreTextView.text = "$extraTodosNumber More Check Point"
            else
                moreTextView.text = "$extraTodosNumber More Check Points"

            moreTextView.setOnClickListener {
                expandList()
                listener(adapterIndex)
            }
        }
        else {
            //todo navigate to details
        }
    }

    private fun expandList() {
        moreTextView.visibility = View.VISIBLE
        arrowImageView.visibility = View.VISIBLE
        moreTextView.text = moreTextView.context.getString(R.string.go_to_details)
        //animation
//                val anim =  RotateAnimation(0f, -90f, Animation.RELATIVE_TO_SELF,
//                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
//                anim.interpolator = LinearInterpolator()
//                anim.duration = 500
//                anim.isFillEnabled = true
//                anim.fillAfter = true
//                arrowImageView.startAnimation(anim)
        itemList.addAll(expandableList)
        isExpanded = true
        notifyDataSetChanged()
    }

    fun collapseExtraTodosPostLoading() {
        moreTextView.visibility = View.GONE
        arrowImageView.visibility = View.GONE
        for (position in MAX_VISIBLE_TODOS until itemList.size)
            itemList.removeAt(itemList.size - 1)
        notifyDataSetChanged()
    }
}