package com.codingsector.tasktimer

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import kotlinx.android.synthetic.main.fragment_add_edit.*

private const val TAG = "AddEditFragment"

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_TASK = "task"

/**
 * A simple [Fragment] subclass.
 * Use the [AddEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AddEditFragment : Fragment() {
    private var task: Task? = null
    private var listener: OnSavedClicked? = null

    //    private val viewModel by lazy { ViewModelProvider(requireActivity()).get(TaskTimerViewModel::class.java) }
    private val viewModel: TaskTimerViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate: starts")
        super.onCreate(savedInstanceState)
        task = arguments?.getParcelable(ARG_TASK)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: starts")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_edit, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewCreated: starts")
        if (savedInstanceState == null) {
            val task = task
            if (task != null) {
                Log.d(TAG, "onViewCreated: Task details found, editing task ${task.id}")
                addedit_name.setText(task.name)
                addedit_description.setText(task.description)
                addedit_sortorder.setText(task.sortOrder.toString())

                viewModel.startEditing(task.id)
            } else {
                // No task, we must be editing a new task, and NOT editing an existing one
                Log.d(TAG, "onViewCreated: No arguments, adding new record")
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        Log.d(TAG, "onPrepareOptionsMenu: called")
        menu.clear()
    }

    private fun taskFromUi(): Task {
        val sortOrder = if (addedit_sortorder.text.isNotEmpty()) {
            Integer.parseInt(addedit_sortorder.text.toString())
        } else {
            0
        }

        val newTask =
            Task(addedit_name.text.toString(), addedit_description.text.toString(), sortOrder)
        newTask.id = task?.id ?: 0

        return newTask
    }

    fun isDirty(): Boolean {
        val newTask = taskFromUi()
        return ((newTask != task) &&
                (newTask.name.isNotBlank()
                        || newTask.description.isNotBlank()
                        || newTask.sortOrder != 0)
                )
    }

    private fun saveTask() {
        val newTask = taskFromUi()
        if (newTask != task) {
            Log.d(TAG, "saveTask: saving task, id is ${newTask.id}")
            task = viewModel.saveTask(newTask)
            Log.d(TAG, "saveTask: id is ${task?.id}")
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        Log.d(TAG, "onActivityCreated: starts")

        if (activity is AppCompatActivity) {
            val actionBar = (activity as AppCompatActivity?)?.supportActionBar
            actionBar?.setDisplayHomeAsUpEnabled(true)
        }

        addedit_save.setOnClickListener {
            saveTask()
            listener?.onSavedClicked()
        }
        super.onActivityCreated(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        Log.d(TAG, "onAttach: starts")
        super.onAttach(context)

        if (context is OnSavedClicked) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnSavedClicked")
        }
    }

    override fun onDetach() {
        Log.d(TAG, "onDetach: starts")
        super.onDetach()
        listener = null

        viewModel.stopEditing()
    }

    /**
     * This interface must be implement by the activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragment contain in that
     * activity.
     * */
    interface OnSavedClicked {
        fun onSavedClicked()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param task The task to be edited or null to add a new task.
         * @return A new instance of fragment AddEditFragment.
         */
        @JvmStatic
        fun newInstance(task: Task?) =
            AddEditFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TASK, task)
                }
            }
    }

    // TODO: Delete all these functions before release.

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        Log.d(TAG, "onViewStateRestored: starts")
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: starts")
        super.onStart()
    }

    override fun onResume() {
        Log.d(TAG, "onResume: starts")
        super.onResume()
    }

    override fun onPause() {
        Log.d(TAG, "onPause: starts")
        super.onPause()
    }

    override fun onStop() {
        Log.d(TAG, "onStop: starts")
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d(TAG, "onSaveInstanceState: starts")
        super.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: starts")
        super.onDestroyView()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: starts")
        super.onDestroy()
    }
}