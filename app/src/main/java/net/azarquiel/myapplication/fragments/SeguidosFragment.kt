package net.azarquiel.myapplication.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.app_main.*
import kotlinx.android.synthetic.main.fragment_seguidos.*
import net.azarquiel.kk.adapters.AdapterPosts
import net.azarquiel.kk.model.Post
import net.azarquiel.kk.model.Usuario
import net.azarquiel.kk.model.Voto
import net.azarquiel.myapplication.R
import net.azarquiel.myapplication.activities.ActivitiConversaciones
import net.azarquiel.myapplication.activities.ActivityBuscarUsuario
import net.azarquiel.myapplication.adapters.AdapterSeguidos

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SeguidosFragment(usuario:Usuario) : Fragment() {
    private val usuario = usuario
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterseguidos: AdapterSeguidos
    var seguidos: ArrayList<String> = ArrayList()
    var usuarios: ArrayList<Usuario> = ArrayList()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_seguidos, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        initRV()

        setListener()

    }



    private fun initRV() {


        val rvprueba = view!!.findViewById(R.id.rvseguidos) as RecyclerView
        adapterseguidos = AdapterSeguidos(activity!!.baseContext, R.layout.rowamigo)
        rvprueba.adapter = adapterseguidos
        rvprueba.layoutManager = LinearLayoutManager(activity)

        adapterseguidos.setDatos(usuarios)

    }
    private fun documentToList(d: DocumentSnapshot) {

            val seguidos = d["seguidos"] as ArrayList<String>
        usuarios.clear()
        val docRef = db.collection("usuarios").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    var usr:ArrayList<Usuario> = ArrayList()
                    for (document in task.result!!) {
                        Log.d("kk","todo:  "+document.id.toString())
                        seguidos.forEach {
                            Log.d("kk",it)
                            if (document.id.toString().equals(it)) {
                                val nombre = document["nombre"] as String
                                val avatar = document["avatar"] as String
                                val id = document.id

                                usr.add(Usuario(id,nombre, avatar))

                            }
                        }


                    }

                    usuarios=usr
                    if (!usuarios.isEmpty() && tvs != null) {
                        tvs.isVisible = false
                    }
                    adapterseguidos.setDatos(usuarios)
                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }

    }
    fun setListener() {

        seguidos.clear()
        val docRef = db.collection("usuarios").document(usuario.id)

        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("eneKEM", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                documentToList(snapshot)

            } else {
                Log.d("eneKEM", "Current data: null")
            }


        }





    }}
