package com.gbazilio.qrcodescanner.app

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val subscription: (String) -> Unit = { qrcode ->
        runOnUiThread {
            textView.text = qrcode
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSubscribe.setOnClickListener {
            scannerView.subscribe(subscription)
        }

        buttonUnsubscribe.setOnClickListener {
            scannerView.subscribe {  }
        }

        lifecycle.addObserver(scannerView)
    }

    override fun onResume() {
        super.onResume()
        scannerView.start(subscription)
    }
}
