package com.udacity.project4.utils

import android.view.View
import android.widget.EditText
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.udacity.project4.base.BaseRecyclerViewAdapter
import kotlin.text.toFloatOrNull

object BindingAdapters {

    /**
     * Use binding adapter to set the recycler view data using livedata object
     */
    @Suppress("UNCHECKED_CAST")
    @BindingAdapter("android:liveData")
    @JvmStatic
    fun <T> setRecyclerViewData(recyclerView: RecyclerView, items: LiveData<List<T>>?) {
        items?.value?.let { itemList ->
            (recyclerView.adapter as? BaseRecyclerViewAdapter<T>)?.apply {
                clear()
                addData(itemList)
            }
        }
    }

    /**
     * Use this binding adapter to show and hide the views using boolean variables
     */
    @BindingAdapter("android:fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, visible: Boolean? = true) {
        if (view.tag == null) {
            view.tag = true
            view.visibility = if (visible == true) View.VISIBLE else View.GONE
        } else {
            view.animate().cancel()
            if (visible == true) {
                if (view.visibility == View.GONE)
                    view.fadeIn()
            } else {
                if (view.visibility == View.VISIBLE)
                    view.fadeOut()
            }
        }
    }

    @BindingAdapter("android:doubleText")
    @JvmStatic
    fun setDoubleText(view: EditText, value: Double?) {
        val currentText = view.text.toString()
        val newValue = value?.toString() ?: ""
        if (currentText != newValue) {
            val currentDouble = currentText.toDoubleOrNull()
            if (currentDouble != value) {
                view.setText(newValue)
            }
        }
    }

    @InverseBindingAdapter(attribute = "android:doubleText", event = "android:textAttrChanged")
    @JvmStatic
    fun getDoubleText(view: EditText): Double? {
        return try {
            view.text.toString().toDoubleOrNull()
        } catch (e: NumberFormatException) {
            null
        }
    }

}