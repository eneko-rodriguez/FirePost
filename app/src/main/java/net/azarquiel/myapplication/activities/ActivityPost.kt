package net.azarquiel.myapplication.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_post.*
import net.azarquiel.imgpicker.picker.Picker
import net.azarquiel.kk.model.Usuario
import net.azarquiel.myapplication.R
import org.jetbrains.anko.alert
import org.jetbrains.anko.customView
import org.jetbrains.anko.toast
import org.jetbrains.anko.verticalLayout
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ActivityPost() : AppCompatActivity() {
    private lateinit var  usuario:Usuario
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var picker: Picker
    companion object {
        const val REQUEST_PERMISSION = 200
        const val REQUEST_GALLERY = 1
        const val REQUEST_CAMERA = 2
        const val TAG = "ImgPicker"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        db = FirebaseFirestore.getInstance()
        usuario=intent.getSerializableExtra("usuario") as Usuario
        storage = FirebaseStorage.getInstance()
        ivpost.setImageDrawable(null)
        picker = Picker(this)
        btnpost.setOnClickListener(){
            subitPost()
        }
        ivgaleria.setOnClickListener(){
            picker.photoFromGallery()
        }
        ivcamara.setOnClickListener(){
            picker.photoFromCamera()
        }
    }
    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        picker.onActivityResult(requestCode, resultCode, data)
        ivpost.setImageBitmap(picker.bitmap)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        picker.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun subitPost() {
        if(etpost.text.toString() == ""){
            alert() {
                title = "Tienes que escribir algo antes de subirlo"
                customView {
                    verticalLayout {


                        positiveButton("Aceptar") {

                        }
                    }
                }
            }.show()
            return
        }
        Toast.makeText(this,"Subiendo...", Toast.LENGTH_SHORT).show();

        if(ivpost.drawable != null) {
            var storageRef = storage.reference
            var nombre = usuario.id + Date().toString()
            nombre = nombre.replace(" ", "_", true)
// Create a child reference
            val mountainsRef = storageRef.child("posts/$nombre.jpg")

// Create a reference to 'images/mountains.jpg'
            val mountainImagesRef = storageRef.child("posts/$nombre.jpg")

// While the file names are the same, the references point to different files
            mountainsRef.name == mountainImagesRef.name // true
            mountainsRef.path == mountainImagesRef.path // false
            ivpost.isDrawingCacheEnabled = true
            ivpost.buildDrawingCache()
            val bitmap = (ivpost.drawable as BitmapDrawable).bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            var uploadTask = mountainsRef.putBytes(data)
            uploadTask.addOnFailureListener {
                // Handle unsuccessful uploads
            }.addOnSuccessListener {
                // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
                // ...
                acabarSubir(nombre, storageRef)
            }

        }else if(etpost.text.toString() != ""){
            acabarSubirTexto()
        }




    }

    private fun acabarSubirTexto() {
        val post: MutableMap<String, Any> = HashMap() // diccionario key value
        post["usuarioid"] = usuario.id
        post["usuarionombre"] = usuario.nombre
        post["descripcion"] = etpost.text.toString()
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        post["fecha"] = Date().time
        post["imagen"] = ""
        post["votosP"] = ArrayList<String>()
        post["votosN"] = ArrayList<String>()

        db.collection("posts").add(post)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                Log.d(
                    "eneKEM", "DocumentSnapshot added with ID: " + documentReference.id

                )
                onBackPressed()
                toast("Post subido")
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w("eneKEM", "Error adding document", e)
            })
    }

    private fun acabarSubir(
        nombre: String,
        storageRef: StorageReference
    ) {

        storageRef.child("posts/$nombre.jpg").downloadUrl.addOnSuccessListener {
            // Got the download URL for 'users/me/profile.png'
            val post: MutableMap<String, Any> = HashMap() // diccionario key value
            post["usuarioid"] = usuario.id
            post["usuarionombre"] = usuario.nombre
            post["descripcion"] = etpost.text.toString()
            Log.d("eneKEM",it.toString())
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
            val currentDate = sdf.format(Date())
            post["fecha"] = Date().time
            post["imagen"] = it.toString()
            post["votosP"] = ArrayList<String>()
            post["votosN"] = ArrayList<String>()

            db.collection("posts").add(post)
                .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                    Log.d(
                        "eneKEM", "DocumentSnapshot added with ID: " + documentReference.id
                    )
                    onBackPressed()
                    toast("Post subido")
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w("eneKEM", "Error adding document", e)
                })
        }.addOnFailureListener {
            // Handle any errors
        }
    }




}
