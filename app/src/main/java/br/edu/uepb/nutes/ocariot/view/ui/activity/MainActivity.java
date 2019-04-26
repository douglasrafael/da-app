package br.edu.uepb.nutes.ocariot.view.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import java.util.Objects;

import br.edu.uepb.nutes.ocariot.R;
import br.edu.uepb.nutes.ocariot.data.model.PhysicalActivity;
import br.edu.uepb.nutes.ocariot.data.model.Sleep;
import br.edu.uepb.nutes.ocariot.data.repository.local.pref.AppPreferencesHelper;
import br.edu.uepb.nutes.ocariot.view.ui.fragment.EnvironmentFragment;
import br.edu.uepb.nutes.ocariot.view.ui.fragment.PhysicalActivityListFragment;
import br.edu.uepb.nutes.ocariot.view.ui.fragment.SleepListFragment;
import br.edu.uepb.nutes.ocariot.view.ui.fragment.WelcomeFragment;
import br.edu.uepb.nutes.ocariot.view.ui.preference.LoginFitBit;
import br.edu.uepb.nutes.ocariot.view.ui.preference.SettingsActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * MainActivity implementation.
 *
 * @author Copyright (c) 2018, NUTES/UEPB
 */
public class MainActivity extends AppCompatActivity implements
        PhysicalActivityListFragment.OnClickActivityListener,
        SleepListFragment.OnClickSleepListener,
        WelcomeFragment.OnClickWelcomeListener,
        BottomNavigationView.OnNavigationItemSelectedListener {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    public final String KEY_DO_NOT_LOGIN_FITBIT = "key_do_not_login_fitbit";

    private AppPreferencesHelper appPref;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.navigation)
    BottomNavigationView mBuBottomNavigationView;

    @BindView(R.id.frame_content)
    FrameLayout mWelcomeContent;

    private PhysicalActivityListFragment physicalActivityListFragment;
    private SleepListFragment sleepListFragment;
    private EnvironmentFragment environmentFragment;
    private int lastViewIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.title_physical_activities);

        lastViewIndex = 0; // 0 - Physical Activity, 1 - Sleep, 2 - Environment
        appPref = AppPreferencesHelper.getInstance(this);
        physicalActivityListFragment = PhysicalActivityListFragment.newInstance();
        sleepListFragment = SleepListFragment.newInstance();
        environmentFragment = EnvironmentFragment.newInstance();

        mBuBottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (appPref.getAuthStateFitBit() == null && !appPref.getBoolean(KEY_DO_NOT_LOGIN_FITBIT)) {
            replaceFragment(WelcomeFragment.newInstance());
            mBuBottomNavigationView.setVisibility(View.GONE);
        } else {
            mBuBottomNavigationView.setVisibility(View.VISIBLE);
            if (lastViewIndex == 1) loadSleepView();
            else if (lastViewIndex == 2) loadEnvironmentsView();
            else loadPhysicalActivitiesView();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.navigation_activities:
                loadPhysicalActivitiesView();
                break;
            case R.id.navigation_sleep:
                loadSleepView();
                break;
            case R.id.navigation_temp_humi:
                loadEnvironmentsView();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onClickActivity(PhysicalActivity activity) {
        Intent intent = new Intent(this, PhysicalActivityDetail.class);
        intent.putExtra(PhysicalActivityDetail.ACTIVITY_DETAIL, activity);
        startActivity(intent);
    }

    @Override
    public void onClickSleep(Sleep sleep) {
        Intent intent = new Intent(this, SleepDetail.class);
        intent.putExtra(SleepDetail.SLEEP_DETAIL, sleep);
        startActivity(intent);
    }

    @Override
    public void onClickFitBit() {
        new LoginFitBit(this).doAuthorizationCode();
    }

    @Override
    public void onDoNotLoginFitBitClick() {
        replaceFragment(physicalActivityListFragment);
        appPref.addBoolean(KEY_DO_NOT_LOGIN_FITBIT, true);
    }

    /**
     * Replace fragment.
     *
     * @param fragment {@link Fragment}
     */
    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_content, fragment).commit();
        }
    }

    /**
     * Replace fragment physical activities view.
     */
    private void loadPhysicalActivitiesView() {
        replaceFragment(physicalActivityListFragment);
        mBuBottomNavigationView.getMenu().getItem(0).setChecked(true);
        Objects.requireNonNull(getSupportActionBar())
                .setTitle(R.string.title_physical_activities);
        lastViewIndex = 0;
    }

    /**
     * Replace fragment sleep view.
     */
    private void loadSleepView() {
        replaceFragment(sleepListFragment);
        mBuBottomNavigationView.getMenu().getItem(1).setChecked(true);
        Objects.requireNonNull(getSupportActionBar())
                .setTitle(R.string.title_sleep);
        lastViewIndex = 1;
    }

    /**
     * Replace fragment environments view.
     */
    private void loadEnvironmentsView() {
        replaceFragment(environmentFragment);
        mBuBottomNavigationView.getMenu().getItem(2).setChecked(true);
        Objects.requireNonNull(getSupportActionBar())
                .setTitle(R.string.title_temperature_humidity);
        lastViewIndex = 2;
    }
}