package livefootball.footballstreamning.fifaworldcup.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import livefootball.footballstreamning.fifaworldcup.viewmodels.ScoreViewModel
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.activities.ScoreDetailActivity
import livefootball.footballstreamning.fifaworldcup.adapters.MatchesAdapter
import livefootball.footballstreamning.fifaworldcup.ads.AdsHelper

@AndroidEntryPoint
class CricketScoreFragment : Fragment() {

    private val viewModel: ScoreViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_matches, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvMatches = view.findViewById<RecyclerView>(R.id.recycler_matches)
        val swipeRefresh = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh)
        val textEmpty = view.findViewById<TextView>(R.id.text_empty)

        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.surface)
        swipeRefresh.setColorSchemeResources(R.color.primary, R.color.secondary)

        rvMatches.layoutManager = LinearLayoutManager(context)
        
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.matches.collectLatest { matches ->
                        rvMatches.adapter = MatchesAdapter(matches) { match ->
                            AdsHelper.getInstance(requireContext()).showAd_Mob_X_Inter_With_Time(requireActivity())
                            val intent = Intent(context, ScoreDetailActivity::class.java)
                            intent.putExtra("MATCH_ID", match.id)
                            startActivity(intent)
                        }
                        textEmpty.visibility = if (matches.isEmpty() && !swipeRefresh.isRefreshing) View.VISIBLE else View.GONE
                    }
                }

                launch {
                    viewModel.isRefreshing.collectLatest { isRefreshing ->
                        swipeRefresh.isRefreshing = isRefreshing
                        if (isRefreshing) {
                            textEmpty.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }
}
