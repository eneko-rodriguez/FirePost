package net.azarquiel.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rowpost.view.*
import net.azarquiel.kk.model.Post
import net.azarquiel.myapplication.R

/**
 * Created by pacopulido on 9/10/18.
 */
class AdapterPerfil(val context: Context,
                    val layout: Int,
                    val usuario:String
                    ) : RecyclerView.Adapter<AdapterPerfil.ViewHolder>() {

    private var dataList: List<Post> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewlayout = layoutInflater.inflate(layout, parent, false)
        return ViewHolder(viewlayout, context,usuario)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = dataList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    internal fun setData(bares: List<Post>) {
        this.dataList = bares
        notifyDataSetChanged()
    }


    class ViewHolder(viewlayout: View, val context: Context,val usuario:String) : RecyclerView.ViewHolder(viewlayout) {
        fun bind(dataItem: Post){
            if(usuario != dataItem.usuarioid ){
                itemView.ivborrar.isVisible = false
                itemView.ivborrar.isEnabled = false
            }
            var cont = 0
            itemView.tvusuariopost.text = dataItem.usuario
            itemView.tvdescripcionpost.text = dataItem.descripcion
            if (dataItem.imagen.isNotEmpty()) {
                Picasso.get().load(dataItem.imagen).into(itemView.ivpost)

            } else {
                itemView.ivpost.setImageDrawable(null);

                itemView.ivpost.getLayoutParams().height = 0
            }
            itemView.tvvotospost.text = "5"
            itemView.ivupvotepost.tag = dataItem
            itemView.ivdownvotepost.tag = dataItem

            var positivo=false
            var negativo=false
            dataItem.votosP.forEach() {


                cont++

                if (it.equals(usuario)) {
                    positivo=true

                }
            }
            dataItem.votosN.forEach() {


                cont--

                if (it.equals(usuario)) {
                    negativo=true
                }
            }

            if(positivo){
                itemView.ivupvotepost.setImageResource(R.drawable.upvoted)
                itemView.ivdownvotepost.setImageResource(R.drawable.downvote)
            }else if(negativo){
                itemView.ivupvotepost.setImageResource(R.drawable.upvote)
                itemView.ivdownvotepost.setImageResource(R.drawable.downvoted)
            }else{
                itemView.ivupvotepost.setImageResource(R.drawable.upvote)
                itemView.ivdownvotepost.setImageResource(R.drawable.downvote)
            }
            itemView.tvvotospost.text=cont.toString()
            itemView.ivupvotepost.tag = dataItem
            itemView.ivdownvotepost.tag = dataItem
            itemView.tag = dataItem


        }


    }
}