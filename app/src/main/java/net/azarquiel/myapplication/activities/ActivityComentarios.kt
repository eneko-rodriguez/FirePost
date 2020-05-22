package net.azarquiel.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.View
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseException
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_comentarios.*
import kotlinx.android.synthetic.main.rowpost.view.*
import net.azarquiel.kk.adapters.tag
import net.azarquiel.kk.model.Comentario
import net.azarquiel.kk.model.Post
import net.azarquiel.kk.model.Usuario
import net.azarquiel.kk.model.Voto
import net.azarquiel.myapplication.R
import net.azarquiel.myapplication.adapters.AdapterComentarios
import org.jetbrains.anko.*
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class ActivityComentarios : AppCompatActivity() {
    private lateinit var post:Post
    private lateinit var usuario: Usuario
    private lateinit var db: FirebaseFirestore
    private lateinit var adaptercomentarios: AdapterComentarios
    var comentarios:List<Comentario> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comentarios)
        btnpostcomentario.setOnClickListener(){
            OnclikPostComentario()
        }
        db = FirebaseFirestore.getInstance()
        post = intent.getSerializableExtra("post") as Post
        usuario = intent.getSerializableExtra("usuario") as Usuario
        initRV()
        setListener()
    }

    private fun OnclikPostComentario() {
        if(etcomentario.text.toString().equals("")){
            alert() {
                title = "No puede subir un comentario vacio"
                customView {
                    verticalLayout {


                        positiveButton("Aceptar") {

                        }
                    }
                }
            }.show()
        }else {
            alert() {
                title =  "¿Seguro que quieres subir este comentario?"
                customView {
                    verticalLayout {



                        negativeButton("Cancelar") {

                        }
                        positiveButton("Aceptar") {
                            postComentario()

                        }
                    }
                }
            }.show()
        }


    }

    private fun postComentario() {
        val user: MutableMap<String, Any> = HashMap() // diccionario key value
        user["usuarioid"] = usuario.id
        user["usuarionombre"] = usuario.nombre
        user["texto"] = etcomentario.text.toString()
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        user["fecha"] = currentDate

        db.collection("posts").document(post.id).collection("comentarios")
            .add(user)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                Log.d(
                    "eneKEM","DocumentSnapshot added with ID: " + documentReference.id)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w("eneKEM","Error adding document", e)
            })
            etcomentario.setText("");
    }


    private fun setListener() {

        try {

        val docRef = db.collection("posts").document(post.id)


        docRef.addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w(tag, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null ) {
                documentTo(snapshot)
            } else {
                Log.d(tag, "Current data: null")
            }
        }

        val docReff = db.collection("posts").document(post.id).collection("comentarios")


        docReff.addSnapshotListener { snapshot, e ->

            if (e != null) {
                Log.w(tag, "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)
            } else {
                Log.d(tag, "Current data: null")
            }
        }
        }catch (e: FirebaseException){
            onBackPressed()
            toast("Este post ha sido borrado")
        }

    }
    private fun documentToList(documents: List<DocumentSnapshot>) {
        try {
            var c: ArrayList<Comentario> = ArrayList()
            documents.forEach { d ->
                val usuarioid = d["usuarioid"] as String
                val usuarionombre = d["usuarionombre"] as String
                val texto = d["texto"] as String
                val fecha = d["fecha"] as String
                val uid = d["usuarioid"] as String
                c.add(Comentario(usuarioid, usuarionombre, texto, fecha, uid))
            }

            var a = c.sortedWith(compareBy({ it.fecha })).reversed()
            comentarios = a
            if (!comentarios.isEmpty()) {
                tvVacio.isVisible = false
            }
            adaptercomentarios.setDatos(comentarios)
        }catch (e: FirebaseException){
            onBackPressed()
            toast("Este post ha sido borrado")
        }

    }
    private fun documentTo(document: DocumentSnapshot) {
        try {

            val docRef = db.collection("posts").orderBy("fecha")
            val user = document["usuarionombre"] as String
            val imagen = document["imagen"] as String
            val fecha = document["fecha"] as Long
            val descripcion = document["descripcion"] as String
            val votosP = document["votosP"] as ArrayList<String>
            val votosN = document["votosN"] as ArrayList<String>
            val uid = document["usuarioid"] as String
            val id = document.id
            post = Post(id, user, imagen, descripcion, fecha, votosP, votosN, uid)

            var cont = 0
            tvusuariopostcomentario.text = post.usuario
            tvdescripcionpostcomentario.text = post.descripcion
            if (post.imagen.isNotEmpty()) {
                Picasso.get().load(post.imagen).into(ivpostcomentario)
            } else {
                ivpostcomentario.setImageDrawable(null);
                ivpostcomentario.getLayoutParams().height = 0
            }
            tvvotospostcomentario.text = "5"
            ivupvotepostcomentario.tag = post
            ivdownvotepostcomentario.tag = post

            var positivo = false
            var negativo = false
            post.votosP.forEach() {


                cont++

                if (it.equals(usuario.id)) {
                    positivo = true

                }
            }
            post.votosN.forEach() {


                cont--

                if (it.equals(usuario.id)) {
                    negativo = true
                }
            }

            if (positivo) {
                ivupvotepostcomentario.setImageResource(R.drawable.upvoted)
                ivdownvotepostcomentario.setImageResource(R.drawable.downvote)
            } else if (negativo) {
                ivupvotepostcomentario.setImageResource(R.drawable.upvote)
                ivdownvotepostcomentario.setImageResource(R.drawable.downvoted)
            } else {
                ivupvotepostcomentario.setImageResource(R.drawable.upvote)
                ivdownvotepostcomentario.setImageResource(R.drawable.downvote)
            }
            tvvotospostcomentario.text = cont.toString()
        }catch (e: TypeCastException){
            onBackPressed()
            toast("Este post ha sido borrado")
        }
    }
    private fun initRV() {

        adaptercomentarios = AdapterComentarios(this, R.layout.rowcomentario)
        rvcomentarios.adapter = adaptercomentarios
        rvcomentarios.layoutManager = LinearLayoutManager(this)
        adaptercomentarios.setDatos(comentarios)
    }
    fun onClickUpvote(v: View){

        var post=v.tag as Post
        var positivo=false
        var negativo=false
        var index = 0
        for(i in 0..post.votosP.size-1){
            if(post.votosP.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")
                positivo=true
                index = i
            }
        }

        for(i in 0..post.votosN.size-1){
            if(post.votosN.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")
                negativo=true
                index = i
            }
        }

        if(positivo) {
            post.votosP.removeAt(index)
            db.collection("posts").document(post.id).update("votosP",post.votosP)
        }else if(negativo){
            post.votosN.removeAt(index)
            db.collection("posts").document(post.id).update("votosN",post.votosN)

        }else{
            post.votosP.add(usuario.id)
            db.collection("posts").document(post.id).update("votosP",post.votosP)
        }


    }
    fun onClickDownvotevote(v: View){
        var post=v.tag as Post
        var positivo=false
        var negativo=false
        var index = 0
        for(i in 0..post.votosP.size-1){
            if(post.votosP.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")
                positivo=true
                index = i
            }
        }

        for(i in 0..post.votosN.size-1){
            if(post.votosN.get(i).equals(usuario.id)){
                Log.d("eneKEM","YA VOTADO")
                negativo=true
                index = i
            }
        }


        if(negativo) {
            post.votosN.removeAt(index)
            db.collection("posts").document(post.id).update("votosN",post.votosN)
        }else if(positivo){
            post.votosP.removeAt(index)
            db.collection("posts").document(post.id).update("votosP",post.votosP)


        }else{
            post.votosN.add(usuario.id)
            db.collection("posts").document(post.id).update("votosN",post.votosN)
        }



    }
    fun onClickNombreComentario(v:View) {
        val card = v.parent.parent as (CardView)
        val comentario = card.tag as Comentario
        val docRef = db.collection("usuarios").document(comentario.usuarioid).get()
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
    fun onClickNombrePost(v:View) {

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
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("usuario",usuario)
        intent.putExtra("seguido",user)
        startActivity(intent)
    }
}
