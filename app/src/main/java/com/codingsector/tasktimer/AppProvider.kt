package com.codingsector.tasktimer

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log

/**
 * Provider for the TaskTimer app. This the only class that knows about [AppDatabase].
 * */

private const val TAG = "AppProvider"

const val CONTENT_AUTHORITY = "com.codingsector.tasktimer.provider"

private const val TASKS = 100
private const val TASKS_ID = 101

private const val TIMINGS = 200
private const val TIMINGS_ID = 201

private const val CURRENT_TIMING = 300

private const val TASK_DURATION = 400

private const val PARAMETERS = 500
private const val PARAMETERS_ID = 501

val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

class AppProvider : ContentProvider() {

    private val uriMatcher by lazy { buildUriMatcher() }

    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "buildUriMatcher starts")
        return UriMatcher(UriMatcher.NO_MATCH).apply {

            // e.g. content://com.codingsector.tasktimer.provider/Tasks
            addURI(CONTENT_AUTHORITY, TasksContract.TABLE_NAME, TASKS)

            // e.g. content://com.codingsector.tasktimer.provider/Tasks/8
            addURI(CONTENT_AUTHORITY, "${TasksContract.TABLE_NAME}/#", TASKS_ID)

            addURI(CONTENT_AUTHORITY, TimingsContract.TABLE_NAME, TIMINGS)
            addURI(CONTENT_AUTHORITY, "${TimingsContract.TABLE_NAME}/#", TIMINGS_ID)

            addURI(CONTENT_AUTHORITY, CurrentTimingContract.TABLE_NAME, CURRENT_TIMING)

            addURI(CONTENT_AUTHORITY, DurationsContract.TABLE_NAME, TASK_DURATION)

            addURI(CONTENT_AUTHORITY, ParametersContract.TABLE_NAME, PARAMETERS)
            addURI(CONTENT_AUTHORITY, "${ParametersContract.TABLE_NAME}/#", PARAMETERS_ID)
        }
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: starts")
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query: called with uri $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match $match")

        val queryBuilder = SQLiteQueryBuilder()

        when (match) {
            TASKS -> queryBuilder.tables = TasksContract.TABLE_NAME

            TASKS_ID -> {
                queryBuilder.tables = TasksContract.TABLE_NAME
                val taskId = TasksContract.getId(uri)
                queryBuilder.appendWhere("${TasksContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }

            TIMINGS -> queryBuilder.tables = TimingsContract.TABLE_NAME

            TIMINGS_ID -> {
                queryBuilder.tables = TimingsContract.TABLE_NAME
                val taskId = TimingsContract.getId(uri)
                queryBuilder.appendWhere("${TimingsContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }

            CURRENT_TIMING -> {
                queryBuilder.tables = CurrentTimingContract.TABLE_NAME
            }

            TASK_DURATION -> queryBuilder.tables = DurationsContract.TABLE_NAME

            PARAMETERS -> queryBuilder.tables = ParametersContract.TABLE_NAME

            PARAMETERS_ID -> {
                queryBuilder.tables = ParametersContract.TABLE_NAME
                val parameterId = ParametersContract.getId(uri)
                queryBuilder.appendWhere("${ParametersContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$parameterId")
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }

        val context =
            context ?: throw NullPointerException("In query function. Context can't be null here!")

        val db = AppDatabase.getInstance(context).readableDatabase
        val cursor = queryBuilder.query(
            db, projection, selection, selectionArgs, null,
            null, sortOrder
        )
        Log.d(TAG, "query: rows in return cursor: ${cursor.count}")   // TODO remove this line

        return cursor
    }

    override fun getType(uri: Uri): String {

        return when (uriMatcher.match(uri)) {
            TASKS -> TasksContract.CONTENT_TYPE

            TASKS_ID -> TasksContract.CONTENT_ITEM_TYPE

            TIMINGS -> TimingsContract.CONTENT_TYPE

            TIMINGS_ID -> TimingsContract.CONTENT_ITEM_TYPE

            CURRENT_TIMING -> CurrentTimingContract.CONTENT_ITEM_TYPE

            TASK_DURATION -> DurationsContract.CONTENT_TYPE

            PARAMETERS -> ParametersContract.CONTENT_TYPE

            PARAMETERS_ID -> ParametersContract.CONTENT_ITEM_TYPE

            else -> throw IllegalArgumentException("unknown uri: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri {
        Log.d(TAG, "insert: called with uri: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "insert: match $match")

        val recordId: Long
        val returnUri: Uri

        val context = context
            ?: throw java.lang.NullPointerException("In insert function. Context can't be null here!")

        when (match) {

            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db!!.insert(TasksContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = TasksContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                recordId = db!!.insert(TimingsContract.TABLE_NAME, null, values)
                if (recordId != -1L) {
                    returnUri = TasksContract.buildUriFromId(recordId)
                } else {
                    throw SQLException("Failed to insert, Uri was $uri")
                }
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        if (recordId > 0) {
            Log.d(TAG, "insert: Setting notifyChange with $uri")
            context.contentResolver?.notifyChange(uri, null)
        }

        Log.d(TAG, "Exiting insert, returning $returnUri")
        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete: called with uri: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "delete: match is $match")

        val count: Int
        var selectionCriteria: String

        val context = context
            ?: throw java.lang.NullPointerException("In delete function. Context can't be null here!")

        when (match) {

            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db!!.delete(TasksContract.TABLE_NAME, selection, selectionArgs)
            }

            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND $selection"
                }

                count = db!!.delete(
                    TasksContract.TABLE_NAME, selectionCriteria,
                    selectionArgs
                )
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db!!.delete(TimingsContract.TABLE_NAME, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND $selection"
                }

                count = db!!.delete(
                    TimingsContract.TABLE_NAME, selectionCriteria,
                    selectionArgs
                )
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        if (count > 0) {
            Log.d(TAG, "delete: Setting notifyChange with $uri")
            context.contentResolver?.notifyChange(uri, null)
        }

        Log.d(TAG, "Exiting delete, returning count $count")
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        Log.d(TAG, "update: called with uri: $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "update: match is $match")

        val count: Int
        var selectionCriteria: String

        val context = context
            ?: throw java.lang.NullPointerException("In update function. Context can't be null here!")

        when (match) {

            TASKS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db!!.update(TasksContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TASKS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TasksContract.getId(uri)
                selectionCriteria = "${TasksContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND $selection"
                }

                count = db!!.update(
                    TasksContract.TABLE_NAME, values, selectionCriteria,
                    selectionArgs
                )
            }

            TIMINGS -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                count = db!!.update(TimingsContract.TABLE_NAME, values, selection, selectionArgs)
            }

            TIMINGS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = TimingsContract.getId(uri)
                selectionCriteria = "${TimingsContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += " AND $selection"
                }

                count = db!!.update(
                    TimingsContract.TABLE_NAME, values, selectionCriteria,
                    selectionArgs
                )
            }

            PARAMETERS_ID -> {
                val db = AppDatabase.getInstance(context).writableDatabase
                val id = ParametersContract.getId(uri)
                selectionCriteria = "${ParametersContract.Columns.ID} = $id"

                if (selection != null && selection.isNotEmpty()) {
                    selectionCriteria += "AND ($selection)"
                }

                count = db.update(
                    ParametersContract.TABLE_NAME,
                    values,
                    selectionCriteria,
                    selectionArgs
                )
            }

            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }

        if (count > 0) {
            Log.d(TAG, "update: Setting notifyChange with $uri")
            context.contentResolver?.notifyChange(uri, null)
        }

        Log.d(TAG, "Exiting update, returning count $count")
        return count
    }
}