package com.acash.whatsappclone

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.acash.whatsappclone.adapters.ChatAdapter
import com.acash.whatsappclone.models.*
import com.acash.whatsappclone.utils.isSameDayAs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*

const val NAME = "name"
const val UID = "uid"
const val THUMBIMG = "thumbImg"
const val FROM_INBOX= "fromInbox"

class ChatActivity : AppCompatActivity() {
    private val friendId by lazy{
        intent.getStringExtra(UID)!!
    }

    private val friendName by lazy{
        intent.getStringExtra(NAME)!!
    }

    private val friendImg by lazy{
        intent.getStringExtra(THUMBIMG)!!
    }

    private val currentUid by lazy{
        FirebaseAuth.getInstance().uid!!
    }

    private val db by lazy{
        FirebaseDatabase.getInstance()
    }

    private var listChatEvents = mutableListOf<ChatEvent>()
    private lateinit var chatAdapter:ChatAdapter
    private lateinit var currentUser:User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        toolbar.title = ""
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        nameTv.text = friendName
        Picasso.get().load(friendImg).into(userImgView)

        chatAdapter = ChatAdapter(listChatEvents,currentUid)

        msgRv.apply{
            layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }

        val emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(msgEdtv)
        smileBtn.setOnClickListener{
            emojiPopup.toggle()
        }

        listenMessages()

        FirebaseFirestore.getInstance().collection("users").document(currentUid).get()
            .addOnSuccessListener{
                currentUser = it.toObject(User::class.java)!!
            }

        sendBtn.setOnClickListener{
            msgEdtv.text?.let {
                if(it.isNotEmpty()) {
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }

        if(intent.getBooleanExtra(FROM_INBOX,false)) {
            updateReadCount()
        }
    }

    private fun listenMessages() {
        getMessages(friendId)
                .orderByKey()
                .addChildEventListener(object : ChildEventListener{

                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val msgMap = snapshot.getValue(Messages::class.java)!!
                        addMessage(msgMap)
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
    }

    private fun addMessage(msgMap: Messages) {
        val eventBefore = listChatEvents.lastOrNull()
        if((eventBefore!=null) && !eventBefore.sentAt.isSameDayAs(msgMap.sentAt) || (eventBefore==null)){
            listChatEvents.add(
                    DateHeader(this,msgMap.sentAt)
            )
        }

        listChatEvents.add(msgMap)
        chatAdapter.notifyDataSetChanged()
        msgRv.scrollToPosition(listChatEvents.size-1)
    }

    private fun sendMessage(msg:String) {
        val id = getMessages(friendId).push().key
        checkNotNull(id){"Cannot be null"}
        val msgMap = Messages(msg,currentUid,id)
        getMessages(friendId).child(id).setValue(msgMap)
        updateLastMsg(msgMap)
    }

    private fun updateLastMsg(msgMap: Messages) {
        val inboxMap = Inbox(msgMap.msg,friendId,friendName,friendImg,0)
        getInbox(currentUid,friendId).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId,currentUid).addListenerForSingleValueEvent(object:ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)
                    inboxMap.apply {
                        from=currentUid
                        name=currentUser.name
                        image=currentUser.thumbImg
                        count=1
                    }

                    value?.let {
                        inboxMap.count = value.count+1
                    }
                    getInbox(friendId,currentUid).setValue(inboxMap)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }
    }

    private fun updateReadCount(){
        getInbox(currentUid,friendId).child("count").setValue(0)
    }

    private fun getMessages(friend_id: String) =
        db.reference.child("messages/${getId(friend_id)}")

    private fun getInbox(toUser:String,fromUser:String) =
        db.reference.child("inbox/$toUser/$fromUser")

    private fun getId(friend_id:String):String =
        if(friend_id>currentUid)
            currentUid+friend_id
        else friend_id+currentUid

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->
                onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }
}