package com.kye.coner.speedysimple

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels

class MainActivity : AppCompatActivity() {
    private val mVM by viewModels<MainViewModel>()
    private var helloText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        helloText = findViewById(R.id.helloText)
        mVM.result.observe(this) {
            helloText?.text = it
        }
        mVM.getResult()
    }
}