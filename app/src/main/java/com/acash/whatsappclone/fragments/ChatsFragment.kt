package com.acash.whatsappclone.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.acash.whatsappclone.*
import com.acash.whatsappclone.models.Inbox
import com.acash.whatsappclone.models.User
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_chats.*
import kotlinx.android.synthetic.main.fragment_people.*

class ChatsFragment : Fragment() {
    private lateinit var mAdapter: FirebaseRecyclerAdapter<Inbox,InboxViewHolder>

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val database by lazy {
        FirebaseDatabase.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setupAdapter()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    private fun setupAdapter() {
        val baseQuery = database.reference.child("inbox/${auth.uid}")

        val options = FirebaseRecyclerOptions.Builder<Inbox>()
            .setLifecycleOwner(viewLifecycleOwner)
            .setQuery(baseQuery,Inbox::class.java)
            .build()

        mAdapter = object : FirebaseRecyclerAdapter<Inbox,InboxViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):InboxViewHolder =
                InboxViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false))

            override fun onBindViewHolder(holder:InboxViewHolder, position: Int, inbox: Inbox) {
                holder.bind(inbox) { name, uid, thumbImg ->
                    val intent = Intent(requireContext(), ChatActivity::class.java)
                    intent.putExtra(NAME, name)
                    intent.putExtra(UID, uid)
                    intent.putExtra(THUMBIMG, thumbImg)
                    intent.putExtra(FROM_INBOX,true)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatRview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}