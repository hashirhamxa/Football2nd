package livefootball.footballstreamning.fifaworldcup.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.models.Inning

class CricketInningScoreAdapter(private val items: List<Inning>) :
    RecyclerView.Adapter<CricketInningScoreAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val inning: TextView = view.findViewById(R.id.text_inning_name)
        val score: TextView = view.findViewById(R.id.text_inning_score)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_score_line, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.inning.text = item.inning
        holder.score.text = "${item.runs}/${item.wickets} (${item.overs})"
    }

    override fun getItemCount() = items.size
}
