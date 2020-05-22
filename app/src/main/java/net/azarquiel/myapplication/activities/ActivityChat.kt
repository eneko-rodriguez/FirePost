package net.azarquiel.myapplication.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.activity_comentarios.*
import net.azarquiel.kk.adapters.tag
import net.azarquiel.kk.model.Conversacion
import net.azarquiel.kk.model.ConversacionAux
import net.azarquiel.kk.model.Mensaje
import net.azarquiel.kk.model.Usuario
import net.azarquiel.myapplication.R
import net.azarquiel.myapplication.adapters.AdapterChat
import org.jetbrains.anko.editText
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ActivityChat : AppCompatActivity() {

    private lateinit var usuario: Usuario
    private lateinit var seguido: Usuario
    private lateinit var conversacion: Conversacion
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterchat: AdapterChat
    private var mensajes =ArrayList<Mensaje>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        rvchat.setHasFixedSize(true)
        var llayout = LinearLayoutManager(applicationContext)
        llayout.stackFromEnd= true
        rvchat.layoutManager = llayout
        db = FirebaseFirestore.getInstance()
        usuario = intent.getSerializableExtra("usuario") as Usuario
        seguido = intent.getSerializableExtra("seguido") as Usuario
        conversacion = intent.getSerializableExtra("conversacion") as Conversacion
        initRV()
        if(conversacion.id != "") {
            setListener()
        }
        btnchat.setOnClickListener(){
            postMensaje()
        }
        if(seguido.avatar != ""){
            Picasso.get().load(seguido.avatar).resize(2000,2000).into(ivchatc)
        }
        tvchatt.text = seguido.nombre
    }
    private fun initRV() {
        adapterchat = AdapterChat(this, R.layout.rowmimensaje,R.layout.rowotromensaje,usuario)
        rvchat.adapter = adapterchat
        rvchat.layoutManager = LinearLayoutManager(this)

        adapterchat.setDatos(mensajes)

    }

    fun setListener() {
        val docReff = db.collection("conversaciones").document(conversacion.id).collection("mensajes").orderBy("fecha")
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
    }

    private fun documentToList(msg: MutableList<DocumentSnapshot>) {

        var c :ArrayList<Mensaje> = ArrayList()
        msg.forEach { d ->
            val fecha=d["fecha"] as Long
            val texto=d["texto"] as String
            val usuario = d["usuario"] as String

            c.add(Mensaje(usuario, fecha, texto))



        }

        rvchat.smoothScrollToPosition(c.size-1)
        adapterchat.setDatos(c)

    }
    private fun postMensaje() {
        if(etchat.text.toString().equals(""))
            return

        if(conversacion.id == ""){
            val con: MutableMap<String, Any> = HashMap() // diccionario key value
            var al =ArrayList<String>()
            al.add(usuario.id)
            al.add(conversacion.seguido.id)
            con["ultimomensaje"] = Date().time
            con["mensaje"] = etchat.text.toString()
            con["usuarios"] = al

            db.collection("conversaciones").add(con)
                .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                    Log.d(
                        "eneKEM","DocumentSnapshot added with ID: " + documentReference.id)
                         conversacion.id = documentReference.id
                        enviarMensaje(true)
                })
                .addOnFailureListener(OnFailureListener { e ->
                    Log.w("eneKEM","Error adding document", e)
                })
        }else{
            enviarMensaje(false)
        }



    }

    private fun enviarMensaje(nuevo : Boolean) {
        val user: MutableMap<String, Any> = HashMap() // diccionario key value
        user["usuario"] = usuario.id
        user["texto"] = etchat.text.toString()
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = Date().time
        user["fecha"] = currentDate

        db.collection("conversaciones").document(conversacion.id).collection("mensajes")
            .add(user)
            .addOnSuccessListener(OnSuccessListener<DocumentReference> { documentReference ->
                Log.d(
                    "eneKEM","DocumentSnapshot added with ID: " + documentReference.id)
            })
            .addOnFailureListener(OnFailureListener { e ->
                Log.w("eneKEM","Error adding document", e)
            })


        db.collection("conversaciones").document(conversacion.id).update("mensaje",etchat.text.toString())

        etchat.setText("")
        if(nuevo){
            setListener()
        }
    }


}
