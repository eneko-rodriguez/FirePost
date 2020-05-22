package net.azarquiel.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rowamigo.view.*
import kotlinx.android.synthetic.main.rowpost.view.*
import net.azarquiel.kk.model.Comentario
import net.azarquiel.kk.model.Usuario
import net.azarquiel.myapplication.R

class AdapterSeguidos(val context: Context,
                      val layout: Int
) : RecyclerView.Adapter<AdapterSeguidos.ViewHolder>() {

    private var dataList: List<Usuario> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size

    }

    internal fun setDatos(lineaViews: List<Usuario>) {
        this.dataList = lineaViews
        notifyDataSetChanged()
    }

    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Usuario){
            itemView.tvseguido.text = dataItem.nombre

            if(!dataItem.avatar.equals("")) {

                Picasso.get().load(dataItem.avatar).resize(2000,2000).into(itemView.ivseguido)
            }else{
                itemView.ivseguido.setImageResource(R.drawable.user)
            }
            itemView.tag = dataItem
        }
    }
}