package com.acash.whatsappclone.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.acash.whatsappclone.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit

const val PHONE_NUMBER = "phoneNumber"

class OtpActivity : AppCompatActivity(), View.OnClickListener {
    private var phnNumber:String? = null
    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mVerificationId:String?=null
    private var mResendToken:PhoneAuthProvider.ForceResendingToken? = null
    private var mCountDownTimer:CountDownTimer? = null
    private lateinit var progressDialog:ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
        phnNumber = intent.getStringExtra(PHONE_NUMBER)
        initializeViews()
        verifyUser()
    }

    private fun verifyUser() {
        phnNumber?.let {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    it,
                    60,
                    TimeUnit.SECONDS,
                    this,
                    callbacks
            )
        }
        showCountdownTimer(60000)
        progressDialog = this.createProgressDialog("Sending Verification code",false)
        progressDialog.show()
    }

    private fun showCountdownTimer(millisInFuture:Long) {
        resendBtn.isEnabled = false
        mCountDownTimer = object:CountDownTimer(millisInFuture,1000){
            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible = true
                counterTv.text = getString(R.string.countdown,millisUntilFinished/1000)
            }

            override fun onFinish() {
                resendBtn.isEnabled = true
                counterTv.isVisible = false
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer?.cancel()
    }

    private fun initializeViews() {
        verifyTv.text = getString(R.string.verify_phnNumber,phnNumber)
        setSpannableString()
        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)
        callbacks =  object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.

                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                val smsCode = credential.smsCode
                otpEt.setText(smsCode)
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // ...
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                }

                // Show a message and update the UI
                // ...
                notifyUserAndRetry("Your phone number might be wrong or connection error.Try again")
            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                // Save verification ID and resending token so we can use them later
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                counterTv.isVisible = false
                mVerificationId = verificationId
                mResendToken = token

                // ...
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener{
                    if(it.isSuccessful){
                        startActivity(Intent(this@OtpActivity, SignUp::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                    }else{
                        notifyUserAndRetry("Verification Failed.Try Again")
                    }
                }

    }

    private fun notifyUserAndRetry(message: String) {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setCancelable(false)
            setPositiveButton("OK"){_,_->
                showLoginActivity()
            }
            setNegativeButton("Cancel"){dialog,_->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    private fun setSpannableString() {
        val span = SpannableString(getString(R.string.waiting_for_OTP,phnNumber))
        val clickableSpan = object:ClickableSpan(){
            override fun onClick(widget: View) {
                showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
        span.setSpan(clickableSpan,span.length-13,span.length,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    private fun showLoginActivity() {
        startActivity(Intent(this, Login::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun onBackPressed() {}

    override fun onClick(v: View?) {
        when(v){
            verificationBtn->{
                val code = otpEt.text.toString()
                if(code.isNotEmpty() && !mVerificationId.isNullOrBlank()) {
                    progressDialog = createProgressDialog("Please wait...", false)
                    progressDialog.show()
                    val credential = PhoneAuthProvider.getCredential(mVerificationId!!,code)
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn->{
                if(mResendToken!=null) {
                    showCountdownTimer(60000)
                    progressDialog = createProgressDialog("Resending Verification code", false)
                    progressDialog.show()
                    phnNumber?.let {
                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                it,
                                60,
                                TimeUnit.SECONDS,
                                this,
                                callbacks,
                                mResendToken
                        )
                    }
                }
            }
        }
    }
}

fun Context.createProgressDialog(message: String,isCancelable:Boolean): ProgressDialog {
    return ProgressDialog(this).apply{
        setMessage(message)
        setCancelable(isCancelable)
        setCanceledOnTouchOutside(false)
    }
}
