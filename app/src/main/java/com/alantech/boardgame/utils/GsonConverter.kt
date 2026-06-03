package com.alantech.boardgame.utils

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken




inline fun <reified T> Gson.fromJsonWithTypeToken(value: String): T =
    this.fromJson(value, object : TypeToken<T>() {}.type)

inline fun <reified T> Gson.toJsonWithTypeToken(obj: T): String =
    this.toJson(obj, object : TypeToken<T>() {}.type)
