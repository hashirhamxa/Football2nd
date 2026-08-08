package livefootball.footballstreamning.fifaworldcup.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.activities.TournamentListActivity
import livefootball.footballstreamning.fifaworldcup.database.EventEntity
import livefootball.footballstreamning.fifaworldcup.database.TournamentEntity
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.activities.EventsListActivity
import livefootball.footballstreamning.fifaworldcup.activities.StreamSelectionActivity
import livefootball.footballstreamning.fifaworldcup.adapters.SportCategoryAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDisplayItem
import livefootball.footballstreamning.fifaworldcup.viewmodels.LiveMatchesViewModel

/**
 * LiveMatchesFragment: Displays live content for Cricket, Football, and Trending.
 * Uses a highly stable XML approach with post-processing to fix layout measurement issues.
 */
@AndroidEntryPoint
class LiveMatchesFragment : Fragment() {

    private val viewModel: LiveMatchesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Initialize static UI components
        val layoutMulti = view.findViewById<LinearLayout>(R.id.layout_multi_sections)
        val layoutSingle = view.findViewById<LinearLayout>(R.id.layout_single_section)

        val rvCricket = view.findViewById<RecyclerView>(R.id.rv_cricket)
        val rvFootball = view.findViewById<RecyclerView>(R.id.rv_football)
        val rvTrending = view.findViewById<RecyclerView>(R.id.rv_trending)
        val rvSingle = view.findViewById<RecyclerView>(R.id.rv_single)

        val sectionCricket = view.findViewById<View>(R.id.section_cricket)
        val sectionFootball = view.findViewById<View>(R.id.section_football)
        val sectionTrending = view.findViewById<View>(R.id.section_trending)

        val textSingleTitle = view.findViewById<TextView>(R.id.text_single_title)

        // 2. Pre-configure LayoutManagers (Once only) - MATCHING USER DESIGN
        rvTrending.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        rvCricket.layoutManager = LinearLayoutManager(context)
        rvFootball.layoutManager = LinearLayoutManager(context)
        rvSingle.layoutManager = LinearLayoutManager(context)

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_home)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        // 3. Observe data flow
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                launch {
                    viewModel.isSingleSection.collectLatest { isSingle ->
                        layoutSingle.visibility = if (isSingle) View.VISIBLE else View.GONE
                        layoutMulti.visibility = if (isSingle) View.GONE else View.VISIBLE
                    }
                }

                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                    }
                }

                launch {
                    viewModel.sections.collectLatest { sections ->
                        view.post {
                            if (!isAdded) return@post
                            
                            val isSingle = viewModel.isSingleSection.value

                            if (isSingle && sections.isNotEmpty()) {
                                val section = sections[0]
                                textSingleTitle.text = section.title
                                rvSingle.layoutManager = LinearLayoutManager(context)
                                rvSingle.adapter = SportCategoryAdapter(section.items, true, false) { handleItemClick(it) }
                            } else {
                                // Multi section mode

                                // 1. Reset state and remove all views for reordering
                                listOf(sectionCricket, sectionFootball, sectionTrending, rvCricket, rvFootball, rvTrending)
                                    .forEach { it.visibility = View.GONE }
                                layoutMulti.removeAllViews()

                                // 2. Iterate and enable active sections in order
                                sections.forEach { section ->
                                    when (section.sportType) {
                                        "cricket" -> {
                                            sectionCricket.visibility = View.VISIBLE
                                            rvCricket.apply {
                                                visibility = View.VISIBLE
                                                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                                adapter = SportCategoryAdapter(section.items, false, false) { handleItemClick(it) }
                                            }
                                            layoutMulti.addView(sectionCricket)
                                            layoutMulti.addView(rvCricket)
                                        }
                                        "football" -> {
                                            sectionFootball.visibility = View.VISIBLE
                                            rvFootball.apply {
                                                visibility = View.VISIBLE
                                                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                                adapter = SportCategoryAdapter(section.items, false, false) { handleItemClick(it) }
                                            }
                                            layoutMulti.addView(sectionFootball)
                                            layoutMulti.addView(rvFootball)
                                        }
                                        "other" -> {
                                            sectionTrending.visibility = View.VISIBLE
                                            rvTrending.apply {
                                                visibility = View.VISIBLE
                                                layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                                adapter = SportCategoryAdapter(section.items, false, false) { handleItemClick(it) }
                                            }
                                            layoutMulti.addView(sectionTrending)
                                            layoutMulti.addView(rvTrending)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        setupStaticClickListeners(view)
    }

    private fun handleItemClick(item: HomeDisplayItem) {
        AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
        when (val original = item.originalObject) {
            is EventEntity -> {
                val intent = Intent(context, StreamSelectionActivity::class.java).apply {
                    putExtra("MATCH_TITLE", original.eventName)
                    putExtra("TOURNAMENT", item.subtitle)
                    putExtra("EVENT_ID", original.id)
                    putExtra("EVENT_THUMB_URL", original.eventThumbUrl)
                    putExtra("START_TIME", original.startTime)
                    putExtra("IS_HIGHLIGHTS_MODE", false)
                }
                startActivity(intent)
            }
            is TournamentEntity -> {
                val intent = Intent(context, EventsListActivity::class.java).apply {
                    putExtra("TOURNAMENT_ID", original.id)
                    putExtra("TOURNAMENT_NAME", original.name)
                    putExtra("TOURNAMENT_THUMB_URL", original.thumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", false)
                }
                startActivity(intent)
            }
        }
    }

    private fun openMatchListActivity(category: String) {
        AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
        val intent = Intent(context, TournamentListActivity::class.java)
        intent.putExtra("CATEGORY", category)
        intent.putExtra("IS_HIGHLIGHTS_MODE", false)
        startActivity(intent)
    }

    private fun setupStaticClickListeners(view: View) {
        view.findViewById<View>(R.id.btn_see_all_cricket)?.setOnClickListener { 
            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
            openMatchListActivity("CRICKET") 
        }
        view.findViewById<View>(R.id.btn_see_all_football)?.setOnClickListener { 
            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
            openMatchListActivity("FOOTBALL") 
        }
        view.findViewById<View>(R.id.btn_see_all_trending)?.setOnClickListener { 
            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
            openMatchListActivity("TRENDING NOW")
        }
    }
}
