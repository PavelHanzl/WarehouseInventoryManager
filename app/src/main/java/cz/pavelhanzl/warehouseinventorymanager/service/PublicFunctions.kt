package cz.pavelhanzl.warehouseinventorymanager.repository

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.full.memberProperties

fun getDateTime(): String {
    return DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withLocale(Locale.getDefault())
        .withZone(
            ZoneId.systemDefault()
        ).format(Instant.now())
}

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Check if no view has focus
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

@Throws(IllegalAccessException::class, ClassCastException::class)
inline fun <reified T> Any.getField(fieldName: String): T? {
    this::class.memberProperties.forEach { kCallable ->
        if (fieldName == kCallable.name) {
            return kCallable.getter.call(this) as T?
        }
    }
    return null
}


fun vibratePhoneSuccess(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, 50))
    } else {
        vibrator.vibrate(100)
    }
}


fun vibratePhoneError(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(400, 255))
    } else {
        vibrator.vibrate(400)
    }
}
