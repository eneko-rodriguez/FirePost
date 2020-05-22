package net.azarquiel.myapplication.activities

import android.R.attr.password
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_inicio.*
import net.azarquiel.kk.model.Usuario
import net.azarquiel.kk.model.UsuarioF
import net.azarquiel.myapplication.R
import org.jetbrains.anko.*


class ActivityInicio : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var usuarioShare: SharedPreferences
    private var salir:Int? = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        salir = intent.getIntExtra("cerrar", 0)
        if(salir == 0) {
            checkLogeado()
        }
        btnregistro.setOnClickListener{
            registrar()
        }
        bntlogin.setOnClickListener{
            logear()
        }


    }

    private fun checkLogeado() {
        val us=auth.currentUser
        if(us!=null){
            Log.d("checklog",us.uid.toString())
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("usuario", us.uid)
            startActivity(intent)
        }
        //usuarioShare = getSharedPreferences("usuarioShare", Context.MODE_PRIVATE)

        //val amigosShare = usuarioShare.all

        //var user: String? = null

       // for (entry in amigosShare.entries) {
        //    val jsonAmigo = entry.value.toString()
        //    user = entry.value.toString()
       // }

        //Log.d(tag,user)
       // if (user != null) {
         //   val intent = Intent(this, MainActivity::class.java)
           // intent.putExtra("usuario", user)
            //startActivity(intent)
        //}

    }
    fun guardarLogeado(us: String){
        usuarioShare = getSharedPreferences("usuarioShare", Context.MODE_PRIVATE)
        val edit = usuarioShare.edit()





        edit.putString("0", us)
        edit.apply()




    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {


    }
    fun registrar(){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.





                alert() {
                    title = "Registrarse"
                    customView {
                        verticalLayout {
                            lparams(width = wrapContent, height = wrapContent)
                            val etNombre = editText {
                                hint = "Nombre de Usuario"
                                padding = dip(16)
                            }
                            val etCorreo = editText {
                                hint = "Correo"
                                padding = dip(16)
                            }
                            val etContraseña = editText {
                                hint = "Contraseña"
                                padding = dip(16)

                            }
                            etContraseña.setInputType(
                                InputType.TYPE_CLASS_TEXT or
                                        InputType.TYPE_TEXT_VARIATION_PASSWORD
                            )
                            positiveButton("Aceptar") {
                                if (etNombre.text.toString().isEmpty() || etContraseña.text.toString().isEmpty() || etCorreo.text.toString().isEmpty())
                                    toast("Campos Obligatorios")
                                else if (!Patterns.EMAIL_ADDRESS.matcher(etCorreo.text.toString()).matches())
                                    toast("Rellena con un correo valido")
                                else{
                                    val usuarioAux= Usuario("",etNombre.text.toString(),etContraseña.text.toString())
                                    registrarUser(etNombre.text.toString(),etCorreo.text.toString(),etContraseña.text.toString())
                                }

                            }
                        }
                    }
                }.show()
        }
    private fun registrarUser(nombre:String,email:String,password:String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("eneKEM", "createUserWithEmail:success")
                    val user = auth.currentUser

                    db.collection("usuarios").document(user!!.uid).set(UsuarioF(user!!.uid,nombre))
                    updateUI(user)
                  //  guardarLogeado(user!!.uid)

                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("usuario", user!!.uid)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("eneKEM", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // ...
            }
    }

    fun logear(){
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        alert() {
            title = "Logearse"
            customView {
                verticalLayout {
                    lparams(width = wrapContent, height = wrapContent)
                    val etNombre = editText {
                        hint = "Correo electrónico"
                        padding = dip(16)
                    }
                    val etContraseña = editText {
                        hint = "Contraseña"
                        padding = dip(16)
                    }
                    etContraseña.setInputType(
                        InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                    )
                    positiveButton("Aceptar") {
                        if (etNombre.text.toString().isEmpty() || etContraseña.text.toString().isEmpty())
                            toast("Campos Obligatorios")
                        else if (!Patterns.EMAIL_ADDRESS.matcher(etNombre.text.toString()).matches())
                            toast("Rellena con un correo valido")
                        else{
                            val usuarioAux= Usuario("",etNombre.text.toString(),etContraseña.text.toString())
                            logearUser(etNombre.text.toString(),etContraseña.text.toString())
                        }

                    }
                }
            }
        }.show()
    }
    private fun logearUser(email:String,password:String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("eneKEM", "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                  //  guardarLogeado(user!!.uid)
                    val intent = Intent(this, MainActivity::class.java)
                    intent.putExtra("usuario", user!!.uid)
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("eneKEM", "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

            }
    }
    }



