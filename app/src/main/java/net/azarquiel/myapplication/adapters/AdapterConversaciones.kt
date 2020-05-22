package net.azarquiel.myapplication.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rowconversacion.view.*
import net.azarquiel.kk.model.Conversacion
import net.azarquiel.kk.model.Usuario
import net.azarquiel.myapplication.R

class AdapterConversaciones(val context: Context,
                            val layout: Int
) : RecyclerView.Adapter<AdapterConversaciones.ViewHolder>() {

    private var dataList: List<Conversacion> = emptyList()

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

    internal fun setDatos(lineaViews: List<Conversacion>) {
        this.dataList = lineaViews
        notifyDataSetChanged()
    }

    class ViewHolder(viewlayout: View, val context: Context) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Conversacion){
            itemView.tvconversacionnombrerow.text = dataItem.seguido.nombre
            itemView.tvultimomensajerow.text = dataItem.mensaje
            if(!dataItem.seguido.avatar.equals("")) {
                Picasso.get().load(dataItem.seguido.avatar).resize(2000,2000).into(itemView.ivconversacionrow)
            }else{
                itemView.ivconversacionrow.setImageResource(R.drawable.user)
            }
            itemView.tag = dataItem
        }
    }
}