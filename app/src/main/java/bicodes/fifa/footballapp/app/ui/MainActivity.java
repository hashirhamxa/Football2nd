package bicodes.fifa.footballapp.app.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import bicodes.fifa.footballapp.R;
import bicodes.fifa.footballapp.feature.matches.ui.live.LiveFragment;
import bicodes.fifa.footballapp.feature.matches.ui.past.PastFragment;
import bicodes.fifa.footballapp.feature.tournaments.ui.TournamentsFragment;
import bicodes.fifa.footballapp.feature.matches.ui.upcoming.UpcomingFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_live) {
                replaceFragment(new LiveFragment(), getString(R.string.live_screen_title));
                return true;
            } else if (itemId == R.id.menu_past) {
                replaceFragment(new PastFragment(), getString(R.string.past_screen_title));
                return true;
            } else if (itemId == R.id.menu_upcoming) {
                replaceFragment(new UpcomingFragment(), getString(R.string.upcoming_screen_title));
                return true;
            } else if (itemId == R.id.menu_tournaments) {
                replaceFragment(new TournamentsFragment(), getString(R.string.tournaments_screen_title));
                return true;
            }
            return false;
        });

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(new LiveFragment(), getString(R.string.live_screen_title));
        }
    }

    private void replaceFragment(Fragment fragment, String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}

