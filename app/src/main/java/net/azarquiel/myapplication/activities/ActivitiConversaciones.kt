package net.azarquiel.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activiti_conversacinones.*
import net.azarquiel.kk.adapters.tag
import net.azarquiel.kk.model.Conversacion
import net.azarquiel.kk.model.ConversacionAux
import net.azarquiel.kk.model.Usuario
import net.azarquiel.myapplication.R
import net.azarquiel.myapplication.adapters.AdapterConversaciones

class ActivitiConversaciones : AppCompatActivity() {
    private lateinit var usuario: Usuario
    private lateinit var db: FirebaseFirestore
    private lateinit var adaptercomentarios: AdapterConversaciones
    var conversaciones:ArrayList<Conversacion> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activiti_conversacinones)

        db = FirebaseFirestore.getInstance()
        usuario = intent.getSerializableExtra("usuario") as Usuario
        initRV()
        setListener()
    }

     fun onClickChat(v:View) {
         val conversacion =v.tag as Conversacion

         val docRef = db.collection("usuarios").document(conversacion.seguido.id).get()
             .addOnCompleteListener { task ->
                 if (task.isSuccessful) {
                    val nombre = task.result!!["nombre"] as String
                     val avatar = task.result!!["avatar"] as String

                     abrirChat(Usuario(task.result!!.id,nombre,avatar),conversacion)
                 } else {

                 }
             }

    }

    private fun abrirChat(
        seguido: Usuario,
        conversacion: Conversacion
    ) {
        val intent = Intent(this, ActivityChat::class.java)
        intent.putExtra("usuario",usuario)
        intent.putExtra("conversacion",conversacion)
        intent.putExtra("seguido",seguido)
        Log.d(tag,conversacion.toString())
        startActivity(intent)
    }

    private fun initRV() {
        adaptercomentarios = AdapterConversaciones(this, R.layout.rowconversacion)
        rvconversaciones.adapter = adaptercomentarios
        rvconversaciones.layoutManager = LinearLayoutManager(this)
        adaptercomentarios.setDatos(conversaciones)

    }
    fun setListener() {
        val docReff = db.collection("conversaciones")
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
        



    private fun documentToList(conversaciones: MutableList<DocumentSnapshot>) {
        Log.d("achus","document")

        var c :ArrayList<ConversacionAux> = ArrayList()
        conversaciones.forEach { d ->
            var add=false
            val usuarios=d["usuarios"] as ArrayList<String>
            val mensaje = d["mensaje"] as String
            var usuarioid=""
            usuarios.forEach(){
                if (it != usuario.id) {
                    usuarioid = it
                    Log.d("achus",it)
                }else{
                    add=true
                }
            }

            if(add) {
                c.add(ConversacionAux(d.id, usuarioid,mensaje))
                Log.d("achusa",c.toString())
            }


        }
        completeData(c)
    }

    private fun completeData(c: ArrayList<ConversacionAux>) {
        conversaciones.clear()
        c.forEach {
            val docRef = db.collection("usuarios").document(it.usuario).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val avatar = task.result!!["avatar"] as String
                        Log.d("achusa",task.result!!["id"].toString())
                        val id = task.result!!.id
                        val nombre = task.result!!["nombre"] as String
                       
                        conversaciones.add(Conversacion(it.id,usuario,Usuario(id,nombre,avatar),it.mensaje))
                        adaptercomentarios.setDatos(conversaciones)
                    } else {
                        Log.d("eneKEM", "Error getting documents.", task.exception)
                    }
                }
        }
    }
}
