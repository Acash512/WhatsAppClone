package com.acash.whatsappclone.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.acash.whatsappclone.fragments.ChatsFragment
import com.acash.whatsappclone.fragments.PeopleFragment

class SlideScreenAdapter(fa:FragmentActivity) : FragmentStateAdapter(fa) {

    override fun getItemCount():Int = 2

    override fun createFragment(position: Int): Fragment = when(position) {
        0 -> ChatsFragment()
        else -> PeopleFragment()
    }

}
