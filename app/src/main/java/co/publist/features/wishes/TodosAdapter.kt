package co.publist.features.wishes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import co.publist.R
import co.publist.features.wishes.data.Todo
import kotlinx.android.synthetic.main.item_todo.view.*


class TodosAdapter(
    var todoList: ArrayList<Todo>,
    moreTextView: TextView
) :
    RecyclerView.Adapter<TodosAdapter.TodoViewHolder>() {
    private val expandableList = ArrayList<Todo>()
    private var isExpanded = false

    init {
        if (todoList.size <= 3)
            moreTextView.visibility = View.GONE
        else if (!isExpanded)
            moreTextView.setOnClickListener {
                moreTextView.text = "Go to details"
                expandList()
            }
        else {
            //todo navigate to details
        }
    }


    private fun expandList() {
        todoList.addAll(expandableList)
        isExpanded = true
        notifyDataSetChanged()
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

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bind(todoList[position])
        if (position >= 3) {
            expandableList.add(todoList[position])
        }
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(
            todo: Todo
        ) {
            itemView.todoTextView.text = todo.name
        }
    }


    override fun getItemCount(): Int {
        return todoList.size
    }

    fun removeExtra() {
        for (position in 3 until todoList.size)
            todoList.removeAt(position)
        notifyDataSetChanged()
    }
}