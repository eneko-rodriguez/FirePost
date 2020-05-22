package net.azarquiel.kk.model

import java.io.Serializable
import java.sql.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

data class Post(var id:String,var usuario:String="", var imagen:String="", var descripcion:String="", var fecha:Long, var votosP:ArrayList<String> = arrayListOf<String>(), var votosN:ArrayList<String> = arrayListOf<String>(), var usuarioid:String):Serializable
data class Voto(var id:String,var positivo:Boolean,var usuario:String="")
data class Usuario(var id:String,var nombre:String="", var avatar:String=""):Serializable
data class UsuarioF(var id:String,var nombre:String="",var seguidos:ArrayList<String> = ArrayList(), var avatar:String="")
data class Conversacion(var id:String, var usuario:Usuario, var seguido:Usuario, var mensaje:String):Serializable
data class ConversacionAux(val id:String,var usuario:String,var mensaje:String)
data class Mensaje(var usuario:String,var fecha:Long, var texto: String=""):Serializable
data class Comentario (
    var id: String,
    var nombre: String,
    var texto: String
    , var fecha:String
    , var usuarioid:String
):Serializable