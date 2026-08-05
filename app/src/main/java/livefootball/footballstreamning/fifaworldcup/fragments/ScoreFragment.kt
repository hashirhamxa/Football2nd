package livefootball.footballstreamning.fifaworldcup.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import livefootball.footballstreamning.fifaworldcup.R
import livefootball.footballstreamning.fifaworldcup.viewmodels.MainViewModel

@AndroidEntryPoint
class ScoreFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private var currentType: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_score_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.scoreConfig.collectLatest { config ->
                    val type = config?.type?.lowercase()
                    if (type != currentType) {
                        currentType = type
                        updateChildFragment(type)
                    }
                }
            }
        }
    }

    private fun updateChildFragment(type: String?) {
        val fragment = when (type) {
            "cricket" -> CricketScoreFragment()
            "football" -> FootballScoreFragment()
            else -> null
        }

        if (fragment != null) {
            childFragmentManager.beginTransaction()
                .replace(R.id.score_fragment_container, fragment)
                .commit()
        } else {
            val existing = childFragmentManager.findFragmentById(R.id.score_fragment_container)
            if (existing != null) {
                childFragmentManager.beginTransaction()
                    .remove(existing)
                    .commit()
            }
        }
    }
}
