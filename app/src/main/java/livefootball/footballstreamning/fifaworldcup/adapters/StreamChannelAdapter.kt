package livefootball.footballstreamning.fifaworldcup.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import androidx.recyclerview.widget.RecyclerView
import livefootball.footballstreamning.fifaworldcup.database.LinkEntity
import livefootball.footballstreamning.fifaworldcup.R

data class Channel(
    val name: String,
    val quality: String,
    val link: LinkEntity? = null,
    val iconRes: Int = R.drawable.about_icon,
    val isHighlight: Boolean = false,
    val thumbnailLink: String? = null
)

class StreamChannelAdapter(private val items: List<Channel>, private val onChannelClick: (Channel) -> Unit) :
    RecyclerView.Adapter<StreamChannelAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text_channel_name)
        val quality: TextView = view.findViewById(R.id.text_quality)
        val btnPlay: View = view.findViewById(R.id.btn_play)
        val imgChannel: ImageView = view.findViewById(R.id.img_channel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_channel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.quality.text = item.quality
        var imageLink: String?=""
        // Set channel image
        if (item.isHighlight && !item.thumbnailLink.isNullOrEmpty()) {
            imageLink=item.thumbnailLink
        } else if (!item.link?.linkImage.isNullOrEmpty()) {
            imageLink=item.link.linkImage
        }

        Glide.with(holder.itemView.context)
            .load(imageLink)
            .placeholder(R.mipmap.ic_launcher)
            .error(R.mipmap.ic_launcher)
            .into(object : CustomTarget<Drawable>() {

                override fun onResourceReady(
                    resource: Drawable,
                    transition: Transition<in Drawable>?
                ) {
                    holder.imgChannel.background = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    holder.imgChannel.background = placeholder
                }

                override fun onLoadFailed(errorDrawable: Drawable?) {
                    holder.imgChannel.setBackgroundResource(R.mipmap.ic_launcher)
                }
            })

        holder.itemView.setOnClickListener { onChannelClick(item) }
        holder.btnPlay.setOnClickListener { onChannelClick(item) }
    }

    override fun getItemCount() = items.size
}
