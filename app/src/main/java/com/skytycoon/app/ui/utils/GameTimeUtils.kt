package com.skytycoon.app.ui.utils

fun Long.toGameTimeString(): String {
    val h = (this % 1440) / 60
    val m = this % 60
    return "%02d:%02d".format(h, m)
}
