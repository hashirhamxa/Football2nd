package livefootball.footballstreamning.fifaworldcup.adapters

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
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
    val thumbnailLink: String? = null,
    val isPromotion: Boolean = false,
    val promotionUrl: String? = null,
    val promotionTitle: String? = null,
    val promotionDescription: String? = null,
    val promotionImageUrl: String? = null)

class StreamChannelAdapter(
    private val items: List<Channel>,
    private val onChannelClick: (Channel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_CHANNEL = 1
        private const val TYPE_PROMOTION = 2
    }

    override fun getItemViewType(position: Int): Int {
        return if (items[position].isPromotion) TYPE_PROMOTION else TYPE_CHANNEL
    }

    class ChannelViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.text_channel_name)
        val quality: TextView = view.findViewById(R.id.text_quality)
        val btnPlay: View = view.findViewById(R.id.btn_play)
        val imgChannel: ImageView = view.findViewById(R.id.img_channel)
    }

    class PromotionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.text_promo_title)
        val description: TextView = view.findViewById(R.id.text_promo_description)
        val imgIcon: ImageView = view.findViewById(R.id.img_promo_icon)
        val btnInstall: View = view.findViewById(R.id.btn_install)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType ==TYPE_PROMOTION) {
            val view = inflater.inflate(R.layout.item_promotion_channel, parent, false)
            PromotionViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_channel, parent, false)
            ChannelViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        if (holder is PromotionViewHolder) {
            holder.title.text = item.promotionTitle ?: item.name
            holder.description.text = item.promotionDescription ?: ""

            Glide.with(holder.itemView.context)
                .load(item.promotionImageUrl)
                .placeholder(R.mipmap.ic_launcher)
                .error(R.mipmap.ic_launcher)
                .into(holder.imgIcon)

            val openUrlAction = {
                item.promotionUrl?.let { url ->
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        holder.itemView.context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            holder.itemView.setOnClickListener { openUrlAction() }
            holder.btnInstall.setOnClickListener { openUrlAction() }
        } else if (holder is ChannelViewHolder) {
            holder.name.text = item.name
            holder.quality.text = item.quality
            var imageLink: String? = ""
            if (item.isHighlight && !item.thumbnailLink.isNullOrEmpty()) {
                imageLink = item.thumbnailLink
            } else if (!item.link?.linkImage.isNullOrEmpty()) {
                imageLink = item.link.linkImage
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
    }

    override fun getItemCount() = items.size
}
