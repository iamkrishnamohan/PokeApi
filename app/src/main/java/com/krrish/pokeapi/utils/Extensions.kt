package com.krrish.pokeapi.utils

import android.app.Activity
import android.content.Context
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible

fun Context.toast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun String.extractId() = this.substringAfter("pokemon").replace("/", "").toInt()

fun String.getPicUrl(): String {
    val id = this.extractId()
    return "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/${id}.png"
}

fun View.toggle(show: Boolean) {
    val transition: Transition = Slide(Gravity.BOTTOM)
    transition.duration = TOGGLE_DURATION
    transition.addTarget(this)
    TransitionManager.beginDelayedTransition(this.parent as ViewGroup?, transition)
    this.isVisible = show
}

fun hideSoftKeyboard(activity: Activity) {
    val view = activity.currentFocus
    view?.let {
        val imm =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}


