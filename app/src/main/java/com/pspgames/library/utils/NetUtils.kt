package com.pspgames.library.utils

import com.pspgames.library.base.corIo
import com.pspgames.library.base.corMain
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Parameters

object NetUtils {
    fun getAsString(
        url: String,
        success: (String) -> Unit,
        error: (() -> Unit)? = null
    ) {
        corIo {
            val (_, _, result) =
                Fuel.get(url)
                    .responseString()
            result.fold({
                corMain {
                    success.invoke(it)
                }
            }, {
                error?.invoke()
            })
        }
    }

    fun get(
        url: String
    ) {
        corIo {
            Fuel.get(url).responseString { _, _, _ ->  }
        }
    }

    fun postAsString(
        url: String,
        requestParams: Parameters? = null,
        success: (String) -> Unit,
        error: (() -> Unit)? = null
    ) {
        corIo {
            val (_, _, result) =
                Fuel.post(url, requestParams!!)
                    .responseString()
            result.fold({
                success.invoke(it)
            }, {
                error?.invoke()
            })
        }
    }

    fun postAsObject(
        url: String,
        requestParams: Parameters? = null,
        success: (JsonObject?) -> Unit,
        error: (() -> Unit)? = null
    ) {
        corIo {
            val (_, _, result) =
                Fuel.post(url, requestParams!!)
                    .responseString()
            result.fold({
                success.invoke(JsonParser.parseString(it).asJsonObject)
            }, {
                error?.invoke()
            })
        }
    }

    fun getAsObject(url: String, success: (JsonObject?) -> Unit, error: (() -> Unit)? = null) {
        corIo {
            val (_, _, result) =
                Fuel.get(url)
                    .responseString()
            result.fold({
                success.invoke(JsonParser.parseString(it).asJsonObject)
            }, {
                error?.invoke()
            })
        }
    }

    fun getAsArray(url: String, success: (JsonArray?) -> Unit, error: (() -> Unit)? = null) {
        corIo {
            val (_, _, result) =
                Fuel.get(url)
                    .responseString()
            result.fold({
                success.invoke(JsonParser.parseString(it).asJsonArray)
            }, {
                error?.invoke()
            })
        }
    }

    inline fun <reified T> getAsModel(url: String, crossinline callback: (T?) -> Unit) {
        corIo {
            val (_, _, result) =
                Fuel.get(url)
                    .responseString()
            result.fold({
                corMain {
                    callback.invoke(Gson().fromJson(it.replace("/null/i", "\"\""), T::class.java))
                }
            }, {
                callback.invoke(null)
            })
        }
    }
}