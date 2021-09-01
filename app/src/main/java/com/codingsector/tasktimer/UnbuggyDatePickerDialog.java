package com.codingsector.tasktimer;

import android.app.DatePickerDialog;
import android.content.Context;

/**
 * Replacing tryNotifyDateSet() with nothing - this is a work around for Android bug in 4.x
 *
 * @see <a href="https://issuetracker.google.com/issues/36951008"&gt;https://issuetracker.google.com/issues/36951008&lt;/a>
 * <p>
 * Fix by Wojtek Jarosz.
 */

public class UnbuggyDatePickerDialog extends DatePickerDialog {

    UnbuggyDatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    @Override
    protected void onStop() {
        // do nothing - do NOT call super method.
    }
}