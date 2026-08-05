package livefootball.footballstreamning.fifaworldcup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import livefootball.footballstreamning.fifaworldcup.utilities.HomeTrending
import livefootball.footballstreamning.fifaworldcup.R

class HomeTrendingAdapter(private val items: List<HomeTrending>) :
    RecyclerView.Adapter<HomeTrendingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_trending_title)
        val category: TextView = view.findViewById(R.id.text_category)
        val description: TextView = view.findViewById(R.id.text_trending_desc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_trending, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.category.text = item.category
        holder.description.text = item.description
    }

    override fun getItemCount() = items.size
}
