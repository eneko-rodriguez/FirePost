package net.azarquiel.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rowcomentario.view.*
import net.azarquiel.kk.model.Comentario

class AdapterComentarios(val context: Context,
                         val layout: Int
) : RecyclerView.Adapter<AdapterComentarios.ViewHolder>() {

    private var dataList: List<Comentario> = emptyList()

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

    internal fun setDatos(lineaViews: List<Comentario>) {
        this.dataList = lineaViews
        notifyDataSetChanged()
    }

    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Comentario){
            itemView.tvusuariocomentario.text = dataItem.nombre
            itemView.tvtextocomentario.text = dataItem.texto
            itemView.tag = dataItem
        }
    }
}