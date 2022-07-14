package dev.nycode.regenbogenice.util

fun <T> List<T>.takeOptional(size: Int?): List<T> = if (size == null) this else take(size)
