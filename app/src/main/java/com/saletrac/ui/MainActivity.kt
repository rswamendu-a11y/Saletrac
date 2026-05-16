package com.saletrac.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.saletrac.R

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set layout directly, hosting the fragment
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SalesEntryFragment())
                .commit()
        }
    }
}
