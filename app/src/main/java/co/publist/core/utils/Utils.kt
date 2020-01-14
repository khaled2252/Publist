package co.publist.core.utils

import android.app.Activity
import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager

object Utils {

    fun hideSoftKeyboard(context: Context, iBinder: IBinder) {
        val inputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(iBinder, 0)
    }
}
