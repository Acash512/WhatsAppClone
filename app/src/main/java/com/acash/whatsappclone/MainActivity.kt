package com.acash.whatsappclone

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import com.acash.whatsappclone.adapters.SlideScreenAdapter
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        viewPager.adapter = SlideScreenAdapter(this)
        TabLayoutMediator(tabs,viewPager
        ) { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Chats"
                }

                1 -> {
                    tab.text = "People"
                }
            }
        }.attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }
}