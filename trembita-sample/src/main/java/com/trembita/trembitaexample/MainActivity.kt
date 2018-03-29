package com.trembita.trembitaexample

import android.app.Activity
import android.os.Bundle
import com.trembita.annotation.Trembita

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        SomeClass().setListener {
            success { response, code -> processResponse(response, code) }
            error { _ -> }
        }
    }

    private fun processResponse(response: String, code: Int) {
    }

    @Trembita
    interface Callback {

        fun success(response: String, code: Int)

        fun error(errorCode: Int)

    }

    class SomeClass {

        lateinit var callBack: Callback

        inline fun setListener(init: TrembitaCallback.() -> Unit) {
            callBack = TrembitaCallback().apply(init)
        }

        fun responseReceived() {
            callBack.success("response", 200)
        }
    }
}
