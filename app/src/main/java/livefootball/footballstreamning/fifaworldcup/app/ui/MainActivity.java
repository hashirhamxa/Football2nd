package livefootball.footballstreamning.fifaworldcup.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import livefootball.footballstreamning.fifaworldcup.R;
import livefootball.footballstreamning.fifaworldcup.feature.matches.ui.live.LiveFragment;
import livefootball.footballstreamning.fifaworldcup.feature.matches.ui.past.PastFragment;
import livefootball.footballstreamning.fifaworldcup.feature.tournaments.ui.TournamentsFragment;
import livefootball.footballstreamning.fifaworldcup.feature.matches.ui.upcoming.UpcomingFragment;
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_privacy_policy) {
            Intent intent = new Intent(this, PrivacyActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + getString(R.string.app_name) + " app! https://play.google.com/store/apps/details?id=" + getPackageName());
            startActivity(Intent.createChooser(shareIntent, "Share via"));
            return true;
        } else if (itemId == R.id.action_rate_us) {
            String packageName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName)));
            }
            return true;
        } else if (itemId == R.id.action_more_apps) {
            String devId = getString(R.string.developer_id);
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=" + devId)));
            } catch (android.content.ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=" + devId)));
            }
            return true;
        } else if (itemId == R.id.action_about_us) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.about_us_url)));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

