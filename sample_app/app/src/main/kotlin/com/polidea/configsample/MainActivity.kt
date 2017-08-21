package com.polidea.configsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.polidea.samplelibrary.LibSettings

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        R.id.appVariant.setText("${BuildConfig.FLAVOR}_${BuildConfig.BUILD_TYPE}")
        R.id.appProperty.setText(Settings.properties.property_1)
        R.id.libVariant.setText(LibSettings().buildVariant)
        R.id.libProperty.setText(LibSettings().propertyValue)
    }

    private fun Int.setText(text: String) {
        (findViewById(this) as TextView).text = text
    }
}
