package com.example.administrator.rtmpscreen

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_2.setOnClickListener{
            _->
            val intent = Intent(this@MainActivity, RecordActivity::class.java)
            startActivity(intent)
        }

    }


}
