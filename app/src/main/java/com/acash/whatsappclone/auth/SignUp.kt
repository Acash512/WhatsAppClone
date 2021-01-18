package com.acash.whatsappclone.auth

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.acash.whatsappclone.MainActivity
import com.acash.whatsappclone.R
import com.acash.whatsappclone.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {

    private val storage by lazy{
        FirebaseStorage.getInstance()
    }

    private val auth by lazy{
        FirebaseAuth.getInstance()
    }

    private val database by lazy{
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userImgView.setOnClickListener{
            checkPermissionsForImage()
            //Firebase Extensions-Image Thumbnail
        }

        nextBtn.setOnClickListener{
            nextBtn.isEnabled=false
            val name = nameEt.text.toString()
            if(name.isEmpty()){
                Toast.makeText(this,"Name cannot be empty!",Toast.LENGTH_SHORT).show()
                nextBtn.isEnabled=true
            }else if(!::downloadUrl.isInitialized){
                Toast.makeText(this,"Image cannot be empty!",Toast.LENGTH_SHORT).show()
            }else{
                val user = User(name,downloadUrl,downloadUrl,auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user)
                        .addOnSuccessListener {
                            startActivity(Intent(this, MainActivity::class.java)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                        }
                        .addOnFailureListener{
                            nextBtn.isEnabled=true
                        }
            }
        }
    }

    private fun checkPermissionsForImage() {
        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),121)
        }

        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
            requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),131)
        }

        if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)==PackageManager.PERMISSION_GRANTED)
            selectImageFromGallery()
    }

    private fun selectImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,141)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== RESULT_OK && requestCode==141){
            data?.data?.let{
                userImgView.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(uri: Uri) {
        nextBtn.isEnabled = false
        val ref =storage.reference.child("uploads/"+auth.uid.toString())
        val uploadTask = ref.putFile(uri)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot,Task<Uri>>{ task->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener{task->
            nextBtn.isEnabled = true
            if(task.isSuccessful){
                downloadUrl = task.result.toString()
            }
        }
    }
}