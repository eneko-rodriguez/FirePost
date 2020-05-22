package net.azarquiel.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rowmimensaje.view.*
import net.azarquiel.kk.model.Mensaje
import net.azarquiel.kk.model.Usuario

class AdapterChat(val context: Context,
                  val layoutmio: Int,
                  val layoutotro: Int,
                  val usuario:Usuario
) : RecyclerView.Adapter<AdapterChat.ViewHolder>() {

    private var dataList: List<Mensaje> = emptyList()
    private var mio=true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if(viewType == 0){
            val layoutInflater = LayoutInflater.from(parent.context)
            val viewlayout = layoutInflater.inflate(layoutmio, parent, false)
            return ViewHolder(viewlayout, context)
        }else{
            val layoutInflater = LayoutInflater.from(parent.context)
            val viewlayout = layoutInflater.inflate(layoutotro, parent, false)
            return ViewHolder(viewlayout, context)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size

    }

    internal fun setDatos(lineaViews: List<Mensaje>) {
        this.dataList = lineaViews
        notifyDataSetChanged()
    }

    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Mensaje){
            itemView.tvmimensaje.text = dataItem.texto
            itemView.tag = dataItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(dataList.get(position).usuario.equals(usuario.id)){
            return 0
        }
        return 1

    }

}
