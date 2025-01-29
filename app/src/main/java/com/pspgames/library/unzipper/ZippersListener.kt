package com.pspgames.library.unzipper

import java.io.File

open class ZippersListener {
    open fun onStart(){}
    open fun onComplete(result: File){}
    open fun onError(e: Exception){}
}