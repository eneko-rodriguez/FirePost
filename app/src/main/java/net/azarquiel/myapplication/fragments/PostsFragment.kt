package net.azarquiel.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_comentarios.*
import kotlinx.android.synthetic.main.fff.*
import kotlinx.android.synthetic.main.rowpost.*
import net.azarquiel.kk.adapters.AdapterPosts
import net.azarquiel.kk.model.Post
import net.azarquiel.kk.model.Usuario
import net.azarquiel.kk.model.Voto
import net.azarquiel.myapplication.R
import net.azarquiel.myapplication.activities.ActivityComentarios
import net.azarquiel.myapplication.activities.ActivityPost
import java.util.*
import kotlin.collections.ArrayList

import org.jetbrains.anko.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class PostsFragment(usuario: Usuario) : Fragment() {
    private val usuario=usuario
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterposts: AdapterPosts
     var posts:ArrayList<Post> = ArrayList()
    private var leo=false
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fff, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        initRV()

        setListener()
        fbrefrescar.setOnClickListener(){
            setListener()
            Toast.makeText(activity,"Cargando...",Toast.LENGTH_SHORT).show();
        }

    }



    fun setListener() {

        posts.clear()
        val docRef = db.collection("posts").orderBy("fecha").get()
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
                        val id=document.id

                        val uid=document["usuarioid"] as String
                        posts.add(Post(id,usuario,imagen,descripcion,fecha,votosP,votosN,uid))

                    }
                    var sortedList = posts.sortedWith(compareBy({ it.fecha})).reversed()

                    adapterposts.setDatos(sortedList)
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


        adapterposts.setDatos(sortedList)

    }

    private fun initRV() {


        val rvprueba = view!!.findViewById(R.id.rvposts) as RecyclerView
        adapterposts = AdapterPosts(activity!!.baseContext, R.layout.rowpost,usuario.id)
        rvprueba.adapter = adapterposts
        rvprueba.layoutManager = LinearLayoutManager(activity)

        adapterposts.setDatos(posts)

    }






}
