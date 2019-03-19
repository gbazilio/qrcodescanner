package com.gbazilio.qrcodescanner.app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val intent = Intent(this, Main2Activity::class.java)
            startActivity(intent)
        }

        button2.setOnClickListener {
            scannerView.subscribe {
                Log.v("ImageReaderListener", "Image read $it")
            }
        }

        button3.setOnClickListener {
            scannerView.subscribe {  }
        }

        lifecycle.addObserver(scannerView)
    }
}
