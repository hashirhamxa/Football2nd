package livefootball.footballstreamning.fifaworldcup.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livecricket.livecrickettv.cricketstreaming.activities.TournamentActivity
import livecricket.livecrickettv.cricketstreaming.viewmodels.HighlightsViewModel
import livefootball.footballstreamning.fifaworldcup.database.EventEntity
import livefootball.footballstreamning.fifaworldcup.database.TournamentEntity
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.activities.EventActivity
import livefootball.footballstreamning.fifaworldcup.activities.LinksActivity
import livefootball.footballstreamning.fifaworldcup.adapters.CategoryAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper
import livefootball.footballstreamning.fifaworldcup.viewmodels.HomeDisplayItem
import kotlin.collections.isNotEmpty

/**
 * HighlightsFragment (History): Displays archived highlight matches for Cricket, Football, and Other sports.
 * Mirrors HomeFragment's dynamic multi/single layout logic.
 */
@AndroidEntryPoint
class HighlightsFragment : Fragment() {

    private val viewModel: HighlightsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_highlights, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI references
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

        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_highlights)
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        // Lifecycle-aware observation of highlight data
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                
                // Toggle root layouts based on active category count
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

                // Render dynamic highlight sections
                launch {
                    viewModel.sections.collectLatest { sections ->
                        val isSingle = viewModel.isSingleSection.value

                        if (isSingle && sections.isNotEmpty()) {
                            // Render a single vertical list if only one sport highlight type is active
                            val section = sections[0]
                            textSingleTitle.text = section.title
                            rvSingle.apply {
                                layoutManager = LinearLayoutManager(context)
                                adapter = CategoryAdapter(section.items, true) { handleItemClick(it) }
                            }
                        } else {
                            // Render multiple sections with horizontal carousels
                            
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
                                            adapter = CategoryAdapter(section.items) { handleItemClick(it) }
                                        }
                                        layoutMulti.addView(sectionCricket)
                                        layoutMulti.addView(rvCricket)
                                    }
                                    "football" -> {
                                        sectionFootball.visibility = View.VISIBLE
                                        rvFootball.apply {
                                            visibility = View.VISIBLE
                                            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                                            adapter = CategoryAdapter(section.items) { handleItemClick(it) }
                                        }
                                        layoutMulti.addView(sectionFootball)
                                        layoutMulti.addView(rvFootball)
                                    }
                                    "other" -> {
                                        sectionTrending.visibility = View.VISIBLE
                                        rvTrending.apply {
                                            visibility = View.VISIBLE
                                            layoutManager = LinearLayoutManager(context)
                                            adapter = CategoryAdapter(section.items) { handleItemClick(it) }
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

        setupStaticClickListeners(view)
    }

    private fun setupStaticClickListeners(view: View) {
        view.findViewById<TextView>(R.id.btn_see_all_cricket).setOnClickListener {
            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
            openTournamentActivity("CRICKET")
        }
        view.findViewById<TextView>(R.id.btn_see_all_football).setOnClickListener {
            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
            openTournamentActivity("FOOTBALL")
        }
        view.findViewById<TextView>(R.id.btn_see_all_trending).setOnClickListener {
            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
            openTournamentActivity("TRENDING NOW")
        }
    }

    private fun handleItemClick(item: HomeDisplayItem) {
        AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
        when (val original = item.originalObject) {
            is EventEntity -> {
                // Navigate directly to links for promoted events
                val intent = Intent(context, LinksActivity::class.java).apply {
                    putExtra("MATCH_TITLE", original.eventName)
                    putExtra("TOURNAMENT", item.subtitle)
                    putExtra("EVENT_ID", original.id)
                    putExtra("EVENT_THUMB_URL", original.eventThumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", true)
                }
                startActivity(intent)
            }
            is TournamentEntity -> {
                // Navigate to event list for highlights (only show highlight events)
                val intent = Intent(context, EventActivity::class.java).apply {
                    putExtra("TOURNAMENT_ID", original.id)
                    putExtra("TOURNAMENT_NAME", original.name)
                    putExtra("TOURNAMENT_THUMB_URL", original.thumbUrl)
                    putExtra("IS_HIGHLIGHTS_MODE", true)
                }
                startActivity(intent)
            }
        }
    }

    private fun openTournamentActivity(category: String) {
        AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
        val intent = Intent(context, TournamentActivity::class.java)
        intent.putExtra("CATEGORY", category)
        intent.putExtra("IS_HIGHLIGHTS_MODE", true)
        startActivity(intent)
    }
}
