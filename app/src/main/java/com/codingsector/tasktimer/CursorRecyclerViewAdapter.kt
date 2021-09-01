package com.codingsector.tasktimer

import android.database.Cursor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.task_list_items.view.*

class TaskViewHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

// bug
/*
    lateinit var task: Task
*/

    fun bind(task: Task, listener: CursorRecyclerViewAdapter.OnTaskClickListener) {
// bug
/*
        this.task = task
*/
        containerView.tli_name.text = task.name
        containerView.tli_description.text = task.description
        containerView.tli_edit.visibility = View.VISIBLE
        containerView.tli_delete.visibility = View.VISIBLE

        containerView.tli_edit.setOnClickListener {
            listener.onEditClick(task)
        }

        containerView.tli_delete.setOnClickListener {
            listener.onDeleteClick(task)
        }

        containerView.setOnLongClickListener {
            listener.onTaskLongClick(task)
            true
        }
    }
}

private const val TAG = "CursorRecyclerViewAdapt"

class CursorRecyclerViewAdapter(
    private var cursor: Cursor?,
    private var listener: OnTaskClickListener
) : RecyclerView.Adapter<TaskViewHolder>() {

    interface OnTaskClickListener {
        fun onEditClick(task: Task)
        fun onDeleteClick(task: Task)
        fun onTaskLongClick(task: Task)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        Log.d(TAG, "onCreateViewHolder: new view requested")
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.task_list_items, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
//        Log.d(TAG, "onBindViewHolder: starts")

        val cursor = cursor

        if (cursor == null || cursor.count == 0) {
            Log.d(TAG, "onBindViewHolder: providing instructions")
            holder.containerView.tli_name.setText(R.string.instructions_heading)
            holder.containerView.tli_description.setText(R.string.instructions)
            holder.containerView.tli_edit.visibility = View.GONE
            holder.containerView.tli_delete.visibility = View.GONE
        } else {
            if (!cursor.moveToPosition(position)) {
                throw IllegalStateException("Couldn't move cursor to position $position")
            }

            // Create a Task object from the data in the cursor
            with(cursor) {
                val task = Task(
                    getString(getColumnIndex(TasksContract.Columns.TASK_NAME)),
                    getString(getColumnIndex(TasksContract.Columns.TASK_DESCRIPTION)),
                    getInt(getColumnIndex(TasksContract.Columns.TASK_SORT_ORDER))
                )
                task.id = getLong(getColumnIndex(TasksContract.Columns.ID))

                holder.bind(task, listener)
            }
        }
    }

    override fun getItemCount(): Int {
        val cursor = cursor
        return if (cursor == null || cursor.count == 0) {
            1   // fib, because we populate a single ViewHolder with instructions
        } else {
            cursor.count
        }
    }

    /**
     * Swap in a new Cursor, return the old Cursor
     * The returned old Cursor is *not* closed.
     *
     * @param newCursor the new cursor to be used
     * @return Returns the previously set Cursor, or null if there wasn't
     *  one.
     *  If the given new cursor is same as the same instance as the previously set
     *  Cursor, null is also returned.
     *  */
    fun swapCursor(newCursor: Cursor?): Cursor? {
        if (newCursor == cursor) {
            return null
        }

        val numItems = itemCount
        val oldCursor = cursor
        cursor = newCursor
        if (newCursor != null) {
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, numItems)
        }
        return oldCursor
    }
}