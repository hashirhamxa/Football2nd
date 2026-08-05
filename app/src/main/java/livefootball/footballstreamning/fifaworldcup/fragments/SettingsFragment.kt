package livefootball.footballstreamning.fifaworldcup.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.viewmodels.SettingsViewModel
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.models.SocialMediaLink

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_settings)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        val headerFollowUs = view.findViewById<TextView>(R.id.header_follow_us)
        val cardFollowUs = view.findViewById<View>(R.id.card_follow_us)
        val containerSocialLinks = view.findViewById<LinearLayout>(R.id.container_social_links)

        setupStaticClickListeners(view)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                    }
                }

                launch {
                    viewModel.socialLinks.collectLatest { links ->
                        if (links.isNotEmpty()) {
                            headerFollowUs.visibility = View.VISIBLE
                            cardFollowUs.visibility = View.VISIBLE
                            
                            // To prevent blinking, only clear and rebuild if the size or data is actually different
                            // Simple check: compare current view count with list size
                            if (containerSocialLinks.childCount != links.size) {
                                containerSocialLinks.removeAllViews()
                                links.forEachIndexed { index, link ->
                                    addSocialLinkView(containerSocialLinks, link, index == links.size - 1)
                                }
                            }
                        } else {
                            headerFollowUs.visibility = View.GONE
                            cardFollowUs.visibility = View.GONE
                            containerSocialLinks.removeAllViews()
                        }
                    }
                }
            }
        }
    }

    private fun setupStaticClickListeners(view: View) {
        view.findViewById<View>(R.id.btn_share).setOnClickListener { shareApp() }
        view.findViewById<View>(R.id.btn_rate).setOnClickListener { openPlayStore() }
        view.findViewById<View>(R.id.btn_privacy).setOnClickListener { 
            openUrl("https://thebicodes.com/CricPulse/privacypolicy")
        }
        view.findViewById<View>(R.id.btn_more_apps).setOnClickListener { 
            openDeveloperPage()
        }
        view.findViewById<View>(R.id.btn_about).setOnClickListener { 
            openUrl("https://www.thebicodes.com/") 
        }
    }

    private fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            val shareMessage = "https://play.google.com/store/apps/details?id=${requireContext().packageName}\n\n"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Share via"))
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun openPlayStore() {
        val packageName = requireContext().packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    private fun openDeveloperPage() {
        // You can replace this with your actual developer ID or account URL
        val developerName = "New+Gen+Developers" // Example
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=pub:$developerName")))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=$developerName")))
        }
    }

    private fun openUrl(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun addSocialLinkView(container: LinearLayout, link: SocialMediaLink, isLast: Boolean) {
        val view = LayoutInflater.from(context).inflate(R.layout.item_setting_row, container, false) as RelativeLayout
        
        val icon = view.findViewById<ImageView>(R.id.row_icon)
        val title = view.findViewById<TextView>(R.id.row_title)
        val divider = view.findViewById<View>(R.id.row_divider)

        title.text = link.name
        Glide.with(this).load(link.imageUrl).into(icon)
        
        divider.visibility = if (isLast) View.GONE else View.VISIBLE
        
        view.setOnClickListener {
            link.link?.let { url ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }

        container.addView(view)
    }
}
