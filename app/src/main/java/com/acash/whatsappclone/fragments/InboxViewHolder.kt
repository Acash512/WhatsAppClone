package com.acash.whatsappclone.fragments

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.acash.whatsappclone.R
import com.acash.whatsappclone.models.Inbox
import com.acash.whatsappclone.utils.formatAsListItem
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.list_item_user.view.*

class InboxViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    fun bind(inbox:Inbox, onClick:(name:String, uid:String, thumbImg:String)->Unit) = with(itemView){
        countTv.isVisible = inbox.count>0
        countTv.text = inbox.count.toString()
        timeTv.text = inbox.time.formatAsListItem(context)

        titleTv.text = inbox.name
        subtitleTv.text = inbox.msg

        Picasso.get().load(inbox.image).placeholder(R.drawable.defaultavatar).error(R.drawable.defaultavatar).into(userImgView)

        setOnClickListener{
            onClick.invoke(inbox.name,inbox.from,inbox.image)
        }
    }
}