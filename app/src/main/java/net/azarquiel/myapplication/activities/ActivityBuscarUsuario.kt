package net.azarquiel.myapplication.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_buscar_usuario.*
import kotlinx.android.synthetic.main.activity_my_profile.*
import net.azarquiel.kk.model.Usuario
import net.azarquiel.myapplication.R
import net.azarquiel.myapplication.adapters.AdapterPerfil
import net.azarquiel.myapplication.adapters.AdapterSeguidos

class ActivityBuscarUsuario : AppCompatActivity(), SearchView.OnQueryTextListener  {
    private lateinit var usuario:Usuario
    private lateinit var db: FirebaseFirestore
    private lateinit var adapterseguidos: AdapterSeguidos
    var usuarios: ArrayList<Usuario> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buscar_usuario)
        usuario = intent.getSerializableExtra("usuario") as Usuario
        db = FirebaseFirestore.getInstance()
        initRV()
        setListener()
    }
    private fun setListener() {

        val docRef = db.collection("usuarios").orderBy("nombre").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    var user:ArrayList<Usuario> = ArrayList()
                    for (document in task.result!!) {
                        val avatar=document["avatar"] as String
                        val nombre=document["nombre"] as String
                        val id=document.id
                       user.add(Usuario(id,nombre,avatar))

                    }

                    usuarios=user
                    adapterseguidos.setDatos(user)
                } else {
                    Log.d("eneKEM", "Error getting documents.", task.exception)
                }
            }

    }
    private fun initRV() {
        adapterseguidos = AdapterSeguidos(this, R.layout.rowamigo)
        rvusuariosbuscados.adapter = adapterseguidos
        rvusuariosbuscados.layoutManager = LinearLayoutManager(this)

        adapterseguidos.setDatos(usuarios)

    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        val searchItem = menu.findItem(R.id.search)
        var searchView = searchItem.actionView as SearchView
        searchView.setQueryHint("Search...")
        searchView.setOnQueryTextListener(this)
        return true
    }
    override fun onQueryTextSubmit(text: String): Boolean {
        return false
    }

    override fun onQueryTextChange(query: String): Boolean {
        val original = ArrayList<Usuario>(usuarios)
        adapterseguidos.setDatos(original.filter { usuario ->
            usuario.nombre.contains(query,ignoreCase = true) })
        return false
    }
    fun onClickSeguido(v: View){
        val seguido = v.tag as Usuario
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("usuario",usuario)
        intent.putExtra("seguido",seguido)
        startActivity(intent)
    }
}
