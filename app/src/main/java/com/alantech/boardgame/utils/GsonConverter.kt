package com.alantech.boardgame.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken




fun <T> Gson.fromJsonWithTypeToken(value: String): T =
    this.fromJson(value, object : TypeToken<T>() {}.type)

fun <T> Gson.toJsonWithTypeToken(obj: T): String =
    this.toJson(obj, object : TypeToken<T>() {}.type)

