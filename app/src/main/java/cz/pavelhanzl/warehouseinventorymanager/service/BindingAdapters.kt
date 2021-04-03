package cz.pavelhanzl.warehouseinventorymanager.service

import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import cz.pavelhanzl.warehouseinventorymanager.R
import de.hdodenhof.circleimageview.CircleImageView

object BindingAdapters {

    //nastavuje errorMessage
    @JvmStatic
    @BindingAdapter("app:errorText")
    fun setErrorMessage(view: TextInputLayout, errorMessage: String?) {
        view.error = errorMessage
    }


    @JvmStatic
    @BindingAdapter("imageUrl")
    fun loadImage(view: CircleImageView, url: String?) {
            Glide.with(view.context)
                .load(url)
                .placeholder(R.drawable.avatar_warehouse_item_primary_color)
                .error(R.drawable.avatar_warehouse_item_primary_color)
                .into(view)
    }

    @JvmStatic
    @BindingAdapter("itemDetailImageUrl")
    fun loadItemDetailImage(view: CircleImageView, url: String?) {

        Glide.with(view.context)
            .load(url)
            .placeholder(R.drawable.avatar_warehouse_item_detail_primary_color)
            .error(R.drawable.avatar_warehouse_item_detail_primary_color)
            .into(view)
    }



}