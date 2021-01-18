package com.acash.whatsappclone.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.widget.addTextChangedListener
import com.acash.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*

class Login : AppCompatActivity() {
    private lateinit var countryCode:String
    private lateinit var phnNumber:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneNumberEt.addTextChangedListener {
            nextBtn.isEnabled = !it.isNullOrEmpty() && it.length>=10
            //Hint Requests
        }

        nextBtn.setOnClickListener{
            checkNumber()
        }
    }

    private fun checkNumber() {
        countryCode = ccp.selectedCountryCodeWithPlus
        phnNumber = countryCode + phoneNumberEt.text.toString()

        notifyUser()
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage("Is this number $phnNumber OK, or would you like to edit the number?")
            setCancelable(false)
            setPositiveButton("OK"){_,_ ->
                showOtpActivity()
            }
            setNegativeButton("Edit"){dialog,_->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this, OtpActivity::class.java).putExtra(PHONE_NUMBER,phnNumber)
            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}
