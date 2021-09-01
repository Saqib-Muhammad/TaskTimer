package com.codingsector.tasktimer

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.DatePicker
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.task_durations.*
import java.util.*

private const val TAG = "DurationsReport"

private const val DIALOG_FILTER = 1
private const val DIALOG_DELETE = 2

private const val DELETION_DATE = "Deletion Date"

class DurationsReport : AppCompatActivity(), View.OnClickListener, AppDialog.DialogEvents,
    DatePickerDialog.OnDateSetListener {

    //    private val viewModel by lazy { ViewModelProvider(this).get(DurationsViewModel::class.java) }
    private val viewModel: DurationsViewModel by viewModels()
    private val reportAdapter by lazy { DurationsRVAdapter(this, null) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_durations_report)
        setSupportActionBar(findViewById(R.id.toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        td_list.layoutManager = LinearLayoutManager(this)
        td_list.adapter = reportAdapter

        viewModel.cursor.observe(
            this,
            { cursor -> reportAdapter.swapCursor(cursor)?.close() })

        td_name_heading.setOnClickListener(this)
        td_description_heading?.setOnClickListener(this)    // Description will not be present in portrait
        td_start_heading.setOnClickListener(this)
        td_duration_heading.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.td_name_heading -> viewModel.sortOrder = SortColumns.NAME
            R.id.td_description_heading -> viewModel.sortOrder = SortColumns.DESCRIPTION
            R.id.td_start_heading -> viewModel.sortOrder = SortColumns.START_DATE
            R.id.td_duration_heading -> viewModel.sortOrder = SortColumns.DURATION
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_report, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.rm_filter_period -> {
                viewModel.toggleDisplayWeek()   // was showing a week, so now showing a day - or vice versa
                invalidateOptionsMenu()     // force to call onPrepareOptionsMenu to redraw our change menu
                return true
            }
            R.id.rm_filter_day -> {
                showDataPickerDialog(getString(R.string.date_title_filter), DIALOG_FILTER)
                return true
            }
            R.id.rm_delete -> {
                showDataPickerDialog(getString(R.string.date_title_delete), DIALOG_DELETE)
                return true
            }
            R.id.rm_settings -> {
                SettingsDialog().apply {
                    show(supportFragmentManager, "settings")
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val item = menu.findItem(R.id.rm_filter_period)
        if (item != null) {
            // switch icon and title to represent 7 days and 1 day, as appropriate to future function of the menu item.
            if (viewModel.displayWeek) {
                item.setIcon(R.drawable.ic_baseline_filter_1_24)
                item.setTitle(R.string.rm_title_filter_day)
            } else {
                item.setIcon(R.drawable.ic_baseline_filter_7_24)
                item.setTitle(R.string.rm_title_filter_week)
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    private fun showDataPickerDialog(title: String, dialogId: Int) {
        val dialogFragment = DatePickerFragment()

        val arguments = Bundle().apply {
            putInt(DATE_PICKER_ID, dialogId)
            putString(DATE_PICKER_TITLE, title)
            putSerializable(DATE_PICKER_DATE, viewModel.getFilterDate())

            putInt(DATE_PICKER_FDOW, viewModel.firstDayOfWeek)
        }
        dialogFragment.arguments = arguments
        dialogFragment.show(supportFragmentManager, "datePicker")
    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, dayOfMonth: Int) {
        Log.d(TAG, "onDateSet: called")

        // Check the id so we know what to do with the result
        when (view.tag as Int) {
            DIALOG_FILTER -> {
                viewModel.setReportDate(year, month, dayOfMonth)
            }
            DIALOG_DELETE -> {
                // we need to format the date for the user's local
                val cal = GregorianCalendar()
                cal.set(year, month, dayOfMonth, 0, 0, 0)
                val fromDate = DateFormat.getDateFormat(this).format(cal.time)

//                val dialog = AppDialog()
                val args = Bundle().apply {
                    putInt(DIALOG_ID, DIALOG_DELETE)   // use the same id value
                    putString(DIALOG_MESSAGE, getString(R.string.delete_timings_message, fromDate))

                    putLong(DELETION_DATE, cal.timeInMillis)
                }
                AppDialog().apply {
                    arguments = args
                    show(supportFragmentManager, null)
                }
            }
            else -> throw IllegalArgumentException("Invalid mode when receiving DataPickerDialog result")
        }
    }

    override fun onPositiveDialogResult(dialogId: Int, args: Bundle) {
        Log.d(TAG, "onPositiveDialogResult: called with id $dialogId")
        if (dialogId == DIALOG_DELETE) {
            // Retrieve data from the bundle
            val deleteDate = args.getLong(DELETION_DATE)
            viewModel.deleteRecords(deleteDate)
        }
    }

    //    override fun onDestroy() {
//        reportAdapter.swapCursor(null)?.close()
//        super.onDestroy()
//    }
}