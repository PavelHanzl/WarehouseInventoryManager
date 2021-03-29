package cz.pavelhanzl.warehouseinventorymanager.service

import android.renderscript.Sampler
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputLayout

object BindingAdapters {

    //nastavuje errorMessage
    @JvmStatic
    @BindingAdapter("app:errorText")
    fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
        view.error = errorMessage
    }

    //nastavuje
    @JvmStatic
    @BindingAdapter("progressCurrentValue")
    fun setCurrentProgress(view: LinearProgressIndicator, value: Int) {
        view.progress = value
    }

    //nastavuje
    @JvmStatic
    @BindingAdapter("progressMaxValue")
    fun setMaxProgress(view: LinearProgressIndicator, value: Int) {
        view.max = value!!
    }



}