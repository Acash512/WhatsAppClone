package com.acash.whatsappclone.models

import com.google.firebase.firestore.FieldValue

class User(
        val name:String,
        val imgUrl:String,
        val thumbImg:String,
        val uid:String,
        val deviceToken:String,
        val status:String,
        val onlineStatus: String
) {
    constructor():this("","","","","","","")

    constructor(name: String,imgUrl: String,thumbImg: String,uid: String):this(
            name,
            imgUrl,
            thumbImg,
            uid,
            "",
            "Hey there, I am using WhatsApp!",
            ""
    )

}