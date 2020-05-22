package net.azarquiel.myapplication.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.ScaleAnimation
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_main.*
import kotlinx.android.synthetic.main.nav_header_main.view.*
import kotlinx.android.synthetic.main.rowpost.view.*
import net.azarquiel.kk.model.Post
import net.azarquiel.kk.model.Usuario
import net.azarquiel.kk.model.Voto
import net.azarquiel.myapplication.R
import net.azarquiel.myapplication.adapters.ViewPageAdapter
import net.azarquiel.myapplication.fragments.PostsFragment
import net.azarquiel.myapplication.fragments.SeguidosFragment
import org.jetbrains.anko.backgroundColor

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener{

    private val icons = arrayOf(android.R.drawable.ic_input_add,android.R.drawable.ic_input_delete,android.R.drawable.ic_input_get)
    private lateinit var db: FirebaseFirestore
    private lateinit var usuario:Usuario
    private lateinit var postfragment:PostsFragment
    private var votosP = ArrayList<String>()
    private var votosN = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = FirebaseFirestore.getInstance()
        getusuario()
        nav_view.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        ivchat.setOnClickListener(){
            onClickChat()
        }
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        fbp.setOnClickListener(){
            onClickFloatingPost()
        }
    }

    private fun onClickChat() {
        val intent = Intent(this, ActivitiConversaciones::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }

    private fun getusuario() {
        val id=intent.getStringExtra("usuario")
        val docRef = db.collection("usuarios").whereEqualTo("id",id)




        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w("eneKEM", "Listen failed.", e)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                documentToList(snapshot.documents)
                updateHeader()
            } else {
                Log.d("eneKEM", "Current data: null")
            }
        }
    }
    private fun documentToList(documents: List<DocumentSnapshot>) {

        documents.forEach { d ->
            val nombre = d["nombre"] as String
            val imagen = d["avatar"] as String
            usuario= Usuario(intent.getStringExtra("usuario"),nombre,imagen)


        }
        setupViewPager(view_pager)
        setupTabs()
    }
    private fun updateHeader() {
        val miivavatar = nav_view.getHeaderView(0).ivavatar
        miivavatar.setOnClickListener{
            Toast.makeText(this, "Pulsaste sobre el avatar..",Toast.LENGTH_LONG).show()

        }
        val mitvavatar = nav_view.getHeaderView(0).tvavatar
        mitvavatar.text = usuario.nombre
        if (usuario.avatar.isNotEmpty()) {
            Picasso.get().load(usuario.avatar).resize(2000,2000).into(miivavatar)
        } else {
            miivavatar.setImageResource(R.drawable.user);
        }
    }



    private fun setupViewPager(viewPager: ViewPager) {
        postfragment=PostsFragment(usuario)
        val adapter = ViewPageAdapter(this, supportFragmentManager)
        adapter.addFragment(postfragment, "Todo")
        adapter.addFragment(SeguidosFragment(usuario), "Seguidos")
        viewPager.adapter = adapter
    }

    private fun setupTabs() {
        tabs.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                view_pager.setCurrentItem(tab.getPosition())
                animateFab(tab.getPosition())

                if(tab.position == 0){
                    fbp.setOnClickListener(){
                        onClickFloatingPost()
                    }
                }else{
                    fbp.setOnClickListener(){
                        buscarUsuarios()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        tabs.setupWithViewPager(view_pager)
        // Ponerles icono, opcional
        for (i in 0 .. view_pager.adapter!!.count) {
            //tabs.getTabAt(i)!!.icon = ContextCompat.getDrawable(this, icon)
        }
    }
    private fun buscarUsuarios() {
        val intent = Intent(this, ActivityBuscarUsuario::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }
    private fun onClickFloatingPost() {


        val intent = Intent(this, ActivityPost::class.java)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var fragment: Fragment? = null

        return true
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {

        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        var fragment:Fragment? = null
        when (item.itemId) {
            R.id.nav_myprofile -> {
                val intent = Intent(this, ProfileActivity::class.java)

                intent.putExtra("usuario",usuario)
                intent.putExtra("seguido",usuario)
                startActivity(intent)
            }
            R.id.nav_registro -> {
                val intent = Intent(this, ActivityInicio::class.java)
                intent.putExtra("cerrar",1)
                startActivity(intent)
            }
            R.id.nav_acercade -> {
                Toast.makeText(this,"Desarrollado por Eneko Rodríguez", Toast.LENGTH_LONG).show()
            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
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
    fun onClickPost(v: View){
        val post = v.tag as Post
        val intent = Intent(this, ActivityComentarios::class.java)
        intent.putExtra("post", post)
        intent.putExtra("usuario", usuario)
        startActivity(intent)
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
    fun onClickSeguido(v:View){
        val seguido = v.tag as Usuario
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra("usuario",usuario)
        intent.putExtra("seguido",seguido)
        startActivity(intent)
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

    var colorIntArray = intArrayOf(
        R.color.colorPrimaryDark,
        R.color.colorAzulo
    )
    var iconIntArray = intArrayOf(
        android.R.drawable.ic_menu_edit,
        android.R.drawable.ic_menu_search
    )

    protected fun animateFab(position: Int) {
        fbp.clearAnimation()
        // Scale down animation
        val shrink = ScaleAnimation(
            1f,
            0.2f,
            1f,
            0.2f,
            Animation.RELATIVE_TO_SELF,
            0.5f,
            Animation.RELATIVE_TO_SELF,
            0.5f
        )
        shrink.setDuration(150) // animation duration in milliseconds
        shrink.setInterpolator(DecelerateInterpolator())
        shrink.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            override fun onAnimationEnd(animation: Animation?) {
                // Change FAB color and icon
                fbp.setBackgroundTintList(
                    resources.getColorStateList(
                        colorIntArray[position]
                    )
                )
                fbp.setImageDrawable(resources.getDrawable(iconIntArray[position], null))

                // Scale up animation
                val expand = ScaleAnimation(
                    0.2f,
                    1f,
                    0.2f,
                    1f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f
                )
                expand.setDuration(100) // animation duration in milliseconds
                expand.setInterpolator(AccelerateInterpolator())
                fbp.startAnimation(expand)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        fbp.startAnimation(shrink)
    }

}
