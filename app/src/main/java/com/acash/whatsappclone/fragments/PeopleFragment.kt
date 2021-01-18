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
import com.acash.whatsappclone.models.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_people.*

const val NORMAL_VIEW_TYPE = 1
const val DELETED_VIEW_TYPE = 2

class PeopleFragment : Fragment() {
    private lateinit var mAdapter: FirestorePagingAdapter<User, RecyclerView.ViewHolder>
    private val auth by lazy {
        FirebaseAuth.getInstance()
    }
    private val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
                .orderBy("name", Query.Direction.ASCENDING)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        setupAdapter()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    private fun setupAdapter() {
        val config = PagedList.Config.Builder()
                .setPageSize(10)
                .setEnablePlaceholders(false)
                .setPrefetchDistance(2)
                .build()

        val options = FirestorePagingOptions.Builder<User>()
                .setLifecycleOwner(viewLifecycleOwner)
                .setQuery(database, config, User::class.java)
                .build()

        mAdapter = object : FirestorePagingAdapter<User, RecyclerView.ViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
                    if (viewType == NORMAL_VIEW_TYPE)
                        UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false))
                    else EmptyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.empty_view, parent, false))

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                if (holder is UserViewHolder)
                    holder.bind(model){name, uid, thumbImg ->
                        val intent = Intent(requireContext(),ChatActivity::class.java)
                        intent.putExtra(NAME,name)
                        intent.putExtra(UID,uid)
                        intent.putExtra(THUMBIMG,thumbImg)
                        startActivity(intent)
                    }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if (item?.uid == auth.uid) {
                    DELETED_VIEW_TYPE
                } else NORMAL_VIEW_TYPE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pplRview.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}