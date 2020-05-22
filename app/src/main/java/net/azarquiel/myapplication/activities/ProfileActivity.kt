package net.azarquiel.myapplication.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import kotlinx.android.synthetic.main.activity_post.*
import kotlinx.android.synthetic.main.rowpost.view.*
import kotlinx.android.synthetic.main.rowpost.view.ivpost
import net.azarquiel.imgpicker.picker.Picker
import net.azarquiel.kk.model.*
import net.azarquiel.myapplication.R

import net.azarquiel.myapplication.adapters.AdapterPerfil
import org.jetbrains.anko.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ProfileActivity : AppCompatActivity() {
    private lateinit var usuario : Usuario
    private lateinit var storage: FirebaseStorage
    private lateinit var seguido : Usuario
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterprofile: AdapterPerfil
    var posts:ArrayList<Post> = ArrayList()
    private var listSeguidos = ArrayList<String>()

    private lateinit var picker: Picker
    private var votosP = ArrayList<String>()
    private var votosN = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        seguido= intent.getSerializableExtra("seguido") as Usuario
        usuario= intent.getSerializableExtra("usuario") as Usuario
        picker = Picker(this)
        floatingActionButton2.setOnClickListener(){
            onClickFloating()
        }
        if(usuario.id == seguido.id){
            ivaddfriend.isEnabled = false
            ivaddfriend.isVisible = false

            ivchat.isEnabled = false
            ivchat.isVisible = false

            ivprofile.setOnClickListener(){
                picker.photoFromGallery()
            }
        }

        if (seguido.avatar.isNotEmpty()) {
            Picasso.get().load(seguido.avatar).resize(2000,2000).into(ivprofile)
        } else {
            ivprofile.setImageResource(R.drawable.user);
        }
        tvnombreprofile.text=seguido.nombre
        verseguidos()
        initRV()
        setListener()
    }
    private fun onClickFloating() {
        val intent = Intent(this, ActivityPost::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }
    fun onClickBorrar(v:View){
        val card = v.parent.parent as (CardView)
        val post = card.tag as Post
        alert() {
            title = "¿Quieres borrar este post?"
            customView {
                verticalLayout {


                    positiveButton("Aceptar") {
                        borrarPost(post)

                    }
                    negativeButton("Cancelar"){

                    }
                }
            }
        }.show()


    }

    private fun borrarPost(post: Post) {
        db.collection("posts").document(post.id)
            .delete()
            .addOnSuccessListener { Log.d("eneKEM", "DocumentSnapshot successfully deleted!")
                toast("Tu post ha sido borrado")
            setListener()}
            .addOnFailureListener { e -> Log.w("eneKEM", "Error deleting document", e)
                toast("Error al borrar")}
    }

    override fun onActivityResult(requestCode:Int, resultCode:Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        picker.onActivityResult(requestCode, resultCode, data)
        ivaux.setImageBitmap(picker.bitmap)
        subirFotoPerfil()
    }
    private fun subirFotoPerfil() {
        var storageRef = storage.reference
        var nombre = usuario.id
// Create a child reference
        val mountainsRef = storageRef.child("profile/$nombre.jpg")

// Create a reference to 'images/mountains.jpg'
        val mountainImagesRef = storageRef.child("profile/$nombre.jpg")

// While the file names are the same, the references point to different files
        mountainsRef.name == mountainImagesRef.name // true
        mountainsRef.path == mountainImagesRef.path // false
        ivaux.isDrawingCacheEnabled = true
        ivaux.buildDrawingCache()
        val bitmap = (ivaux.drawable as BitmapDrawable).bitmap
        if(bitmap == null)
            return
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        var uploadTask = mountainsRef.putBytes(data)
        uploadTask.addOnFailureListener {
            // Handle unsuccessful uploads
        }.addOnSuccessListener {
            // taskSnapshot.metadata contains file metadata such as size, content-type, etc.
            // ...
            acabarSubir(nombre,storageRef)
        }
    }
        private fun acabarSubir(
            nombre: String,
            storageRef: StorageReference
        ) {

            storageRef.child("profile/$nombre.jpg").downloadUrl.addOnSuccessListener {

                db.collection("usuarios").document(usuario.id).update("avatar",it.toString()).addOnCompleteListener {



                    toast("Tu foto de perfil ha cambiado")
                }
                Picasso.get().load(it.toString()).resize(2000,2000).into(ivprofile)

            }.addOnFailureListener {
                // Handle any errors
            }
        }



    private fun setListener() {

        posts.clear()
        val docRef = db.collection("posts").whereEqualTo("usuarioid",seguido.id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    var votos:ArrayList<Voto> = ArrayList()
                    for (document in task.result!!) {
                        val usuario=document["usuarionombre"] as String
                        val imagen=document["imagen"] as String
                        val fecha=document["fecha"] as Long
                        val descripcion=document["descripcion"] as String
                        val votosP=document["votosP"] as ArrayList<String>
                        val votosN=document["votosN"] as ArrayList<String>
                        val uid=document["usuarioid"] as String
                        val id=document.id
                        posts.add(Post(id,usuario,imagen,descripcion,fecha,votosP,votosN,uid))
                        if (!posts.isEmpty()) {
                            tvp.isVisible = false
                        }
                    }
                    var sortedList = posts.sortedWith(compareBy({ it.fecha})).reversed()

                    adapterprofile.setData(sortedList)
                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }

    }


    private fun documentToList(documents: List<DocumentSnapshot>) {
        Log.d("eneKEM","LEO")
        posts.clear()
        documents.forEach { d ->

            db.collection("posts").document(d.id).collection("votos")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        var votos:ArrayList<Voto> = ArrayList()
                        for (document in task.result!!) {
                            val usuario =  document["usuario"] as String
                            val positivo =  document["positivo"] as Boolean
                            votos.add(Voto(document.id,positivo= positivo,usuario = usuario))

                        }
                        addrestofdata(d,votos)
                    } else {
                        Log.d("eneKEM", "Error getting documents.", task.exception)
                    }
                }

        }

    }

    private fun addrestofdata(d: DocumentSnapshot, vot: ArrayList<Voto>) {
        val usuario=d["usuarionombre"] as String
        val imagen=d["imagen"] as String
        val fecha=d["fecha"] as Long
        val descripcion=d["descripcion"] as String
        val votosP=d["votosP"] as ArrayList<String>
        val votosN=d["votosN"] as ArrayList<String>
        val id=d.id

        val uid=d["usuarioid"] as String
        posts.add(Post(id,usuario,imagen,descripcion,fecha,votosP,votosN,uid))
        var sortedList = posts.sortedWith(compareBy({ it.fecha})).reversed()



        adapterprofile.setData(sortedList)

    }
    private fun initRV() {
        adapterprofile = AdapterPerfil(this, R.layout.rowpost,usuario.id)
        rvprofile.adapter = adapterprofile
        rvprofile.layoutManager = LinearLayoutManager(this)

        adapterprofile.setData(posts)

    }
    fun onClickPost(v: View){
        val post = v.tag as Post
        val intent = Intent(this, ActivityComentarios::class.java)
        intent.putExtra("post", post)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }

    fun onClickUpvote(v: View){


        val card = v.parent.parent.parent as (CardView)
        var votos = card.tvvotospost.text.toString().toInt()
        var post=v.tag as Post
        var positivo=false
        var negativo=false
        var index = 0
        for(i in 0..votosP.size-1){
            if(votosP.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")
                positivo=true
                index = i
            }
        }

        for(i in 0..votosN.size-1){
            if(votosN.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")
                negativo=true
                index = i
            }
        }

        if(positivo) {
            votosP.removeAt(index)
            v.ivupvotepost.setImageResource(R.drawable.upvote)
            card.tvvotospost.text = (votos-1).toString()
        }else if(negativo){
            votosN.removeAt(index)
            votosP.add(usuario.id)
            db.collection("posts").document(post.id).update("votosN",votosN)
            card.ivdownvotepost.setImageResource(R.drawable.downvote)
            v.ivupvotepost.setImageResource(R.drawable.upvoted)
            card.tvvotospost.text = (votos+2).toString()
        }else{
            votosP.add(usuario.id)
            v.ivupvotepost.setImageResource(R.drawable.upvoted)
            card.tvvotospost.text = (votos+1).toString()
        }
        db.collection("posts").document(post.id).update("votosP",votosP)

    }
    fun onClickDownvotevote(v: View){
        var post=v.tag as Post
        val card = v.parent.parent.parent as (CardView)
        var votos = card.tvvotospost.text.toString().toInt()
        Log.d("enekem",post.toString())
        var positivo=false
        var negativo=false
        var index = 0
        for(i in 0..votosP.size-1){
            if(votosP.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")

                positivo=true
                index = i
            }
        }

        for(i in 0..votosN.size-1){
            if(votosN.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")
                negativo=true
                index = i
            }
        }

        Log.d("eneKEM",positivo.toString()+negativo.toString())
        if(negativo) {
            votosN.removeAt(index)
            v.ivdownvotepost.setImageResource(R.drawable.downvote)
            card.tvvotospost.text = (votos+1).toString()
        }else if(positivo){
            votosP.removeAt(index)
            votosN.add(usuario.id)
            db.collection("posts").document(post.id).update("votosP",votosP)
            v.ivdownvotepost.setImageResource(R.drawable.downvoted)
            card.ivupvotepost.setImageResource(R.drawable.upvote)
            card.tvvotospost.text = (votos-2).toString()
        }else{
            v.ivdownvotepost.setImageResource(R.drawable.downvoted)
            votosN.add(usuario.id)
            card.tvvotospost.text = (votos-1).toString()
        }
        db.collection("posts").document(post.id).update("votosN",votosN)
    }

    fun up(v:View){
        val post = v.tag as Post
        getVotos(post.id,true,v)
    }
    fun down(v:View){
        val post = v.tag as Post
        getVotos(post.id,false,v)
    }
    fun getVotos(id:String,up:Boolean,v:View) {


        val docRef = db.collection("posts").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    var votos:ArrayList<Voto> = ArrayList()
                    for (document in task.result!!) {
                        if (document.id.equals(id)) {
                            Log.d("kk",votosP.toString())
                            Log.d("kk",votosN.toString())
                            votosP = document["votosP"] as ArrayList<String>
                            votosN = document["votosN"] as ArrayList<String>
                            Log.d("kk",votosP.toString())
                            Log.d("kk",votosN.toString())
                            if(up){
                                onClickUpvote(v)
                            }else{
                                onClickDownvotevote(v)
                            }

                        }
                    }

                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }

    }
    fun onClickNombre(v:View) {
        Log.d("nombre","1")
        val card = v.parent.parent as (CardView)
        val post = card.tag as Post
        Log.d("post",post.toString())
        val docRef = db.collection("usuarios").document(post.usuarioid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val avatar = task.result!!["avatar"] as String
                    val id = task.result!!.id
                    val nombre = task.result!!["nombre"] as String

                    abrirUsuario(Usuario(id,nombre,avatar))
                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }


    }

    private fun abrirUsuario(user: Usuario?) {
        Log.d("nombre","3")
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("usuario",usuario)
        intent.putExtra("seguido",user)
        startActivity(intent)
    }

    fun onClickChatPerfil(v:View){
        val docRef = db.collection("conversaciones").whereArrayContains("usuarios",usuario.id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    var conversacion:ConversacionAux
                    var existe = false
                    for (d in task.result!!) {
                        val usuarios=d["usuarios"] as ArrayList<String>
                        usuarios.forEach {
                            if(it.equals(seguido.id)) {
                                var user: String = ""
                                var seguido: String = ""

                                val mensaje = d["mensaje"] as String
                                var usuarioid = ""
                                val id = d.id
                                usuarios.forEach() {
                                    if (it != usuario.id) {
                                        user = it
                                    } else {
                                        seguido = it
                                    }
                                }
                                existe = true
                                buscarSeguido(ConversacionAux(id, seguido, mensaje))
                            }
                        }

                    }
                    if(!existe){
                        var conversacion:ConversacionAux
                        openChat(Conversacion("",usuario,seguido,""))
                    }
                } else {
                    var conversacion:ConversacionAux
                    openChat(Conversacion("",usuario,seguido,""))
                }
            }
    }

    private fun buscarSeguido(conversacionAux: ConversacionAux) {
        val con = conversacionAux
        val docRef = db.collection("usuarios").document(con.usuario).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    var us:Usuario
                        val avatar=task.result!!["avatar"] as String
                        val nombre = task.result!!["nombre"] as String
                        val id= task.result!!.id

                        us = Usuario(id,nombre,avatar)
                        Conversacion(con.id,usuario,us,con.mensaje)
                        openChat(Conversacion(con.id,usuario,us,con.mensaje))

                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }
    }

    private fun openChat(conversacion: Conversacion) {
        val intent = Intent(this, ActivityChat::class.java)
        intent.putExtra("usuario",usuario)
        intent.putExtra("conversacion",conversacion)
        intent.putExtra("seguido",seguido)
        startActivity(intent)
    }
    fun onClickaddamigo(v:View) {

        val docRef = db.collection("usuarios").document(usuario.id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val se = task.result!!["seguidos"] as ArrayList<String>
                    var ysseguido = false
                    listSeguidos = se
                    listSeguidos.forEach(){
                        if(it == seguido.id){
                            ysseguido = true
                        }
                    }
                    if(ysseguido){
                        dejarSeguir()
                    }else{
                        seguir()
                    }
                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }

    } private fun verseguidos() {
        val docRef = db.collection("usuarios").document(usuario.id).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val se = task.result!!["seguidos"] as ArrayList<String>
                    listSeguidos = se
                    var yaseguido = false

                    listSeguidos.forEach(){
                        if(it == seguido.id){
                            yaseguido = true
                        }
                    }
                    if(yaseguido){
                        ivaddfriend.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)

                    }else{
                        ivaddfriend.setImageResource(android.R.drawable.ic_menu_add)
                    }
                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }
    }

    private fun dejarSeguir() {
        alert() {
            title = "¿Quieres dejar de seguir a ${seguido.nombre}?"
            customView {
                verticalLayout {


                    positiveButton("Aceptar") {
                        listSeguidos.remove(seguido.id)
                        ivaddfriend.setImageResource(android.R.drawable.ic_menu_add)
                        updateSeguidos()

                    }
                    negativeButton("Cancelar"){

                    }
                }
            }
        }.show()
    }

    private fun seguir() {
        alert() {
            title = "¿Quieres empezar a seguir a ${seguido.nombre}?"
            customView {
                verticalLayout {


                    positiveButton("Aceptar") {

                        listSeguidos.add(seguido.id)
                        ivaddfriend.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
                        updateSeguidos()
                    }
                    negativeButton("Cancelar"){
                        onBackPressed()
                    }
                }
            }
        }.show()
    }

    private fun updateSeguidos() {
        db.collection("usuarios").document(usuario.id).update("seguidos",listSeguidos)
    }

}
