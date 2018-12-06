package br.edu.uepb.nutes.ocariot.view.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import br.edu.uepb.nutes.ocariot.R;
import br.edu.uepb.nutes.ocariot.data.model.ActivitiesList;
import br.edu.uepb.nutes.ocariot.data.model.Activity;
import br.edu.uepb.nutes.ocariot.data.model.ActivityLevel;
import br.edu.uepb.nutes.ocariot.data.model.User;
import br.edu.uepb.nutes.ocariot.data.model.UserAccess;
import br.edu.uepb.nutes.ocariot.data.repository.local.pref.AppPreferencesHelper;
import br.edu.uepb.nutes.ocariot.data.repository.remote.fitbit.FitBitNetRepository;
import br.edu.uepb.nutes.ocariot.data.repository.remote.ocariot.OcariotNetRepository;
import br.edu.uepb.nutes.ocariot.utils.DateUtils;
import br.edu.uepb.nutes.ocariot.view.adapter.PhysicalActivityListAdapter;
import br.edu.uepb.nutes.ocariot.view.adapter.base.OnRecyclerViewListener;
import br.edu.uepb.nutes.ocariot.view.ui.activity.MainActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.observers.DisposableObserver;

/**
 * A fragment representing a list of Items.
 *
 * @author Copyright (c) 2018, NUTES/UEPB
 */
public class PhysicalActivityListFragment extends Fragment {
    private final String LOG_TAG = "PhysicalActivityList";

    private PhysicalActivityListAdapter mAdapter;
    private FitBitNetRepository fitBitRepository;
    private OcariotNetRepository ocariotRepository;
    private OnClickActivityListener mListener;
    private UserAccess userAccess;

    /**
     * We need this variable to lock and unlock loading more.
     * We should not charge more when a request has already been made.
     * The load will be activated when the requisition is completed.
     */
    private boolean itShouldLoadMore = true;

    @BindView(R.id.activities_list)
    RecyclerView mRecyclerView;

    @BindView(R.id.data_swipe_refresh)
    SwipeRefreshLayout mDataSwipeRefresh;

    @BindView(R.id.no_data_textView)
    TextView mNoData;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PhysicalActivityListFragment() {
    }

    public static PhysicalActivityListFragment newInstance() {
        return new PhysicalActivityListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userAccess = AppPreferencesHelper.getInstance(getContext()).getUserAccessOcariot();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_physical_activity_list, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fitBitRepository = FitBitNetRepository.getInstance(getActivity());
        ocariotRepository = OcariotNetRepository.getInstance(getActivity());

        initComponents();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnClickActivityListener) {
            mListener = (OnClickActivityListener) context;
        } else {
            throw new ClassCastException();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (fitBitRepository != null) fitBitRepository.dispose();

        mListener = null;
    }

    /**
     * Initialize components
     */
    private void initComponents() {
        initRecyclerView();
        initDataSwipeRefresh();
        loadDataFitBit();
    }

    private void initRecyclerView() {
        mAdapter = new PhysicalActivityListAdapter(getContext());
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),
                new LinearLayoutManager(getContext()).getOrientation()));

        mAdapter.setListener(new OnRecyclerViewListener<Activity>() {
            @Override
            public void onItemClick(Activity item) {
                Log.w(LOG_TAG, "item: " + item.toString());
                if (mListener != null) mListener.onClickActivity(item);
            }

            @Override
            public void onLongItemClick(View v, Activity item) {

            }

            @Override
            public void onMenuContextClick(View v, Activity item) {

            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    /**
     * Initialize SwipeRefresh
     */
    private void initDataSwipeRefresh() {
        mDataSwipeRefresh.setOnRefreshListener(() -> {
            if (itShouldLoadMore) loadDataFitBit();
        });
    }

    /**
     * Load data in FitBit Server.
     */
    private void loadDataFitBit() {
        Log.w(LOG_TAG, "loadDataFitBit()");
        loading(true);
        String currentDate = DateUtils.getCurrentDatetime(getResources().getString(R.string.date_format1));

        fitBitRepository.listActivities(currentDate, null, "desc", 0, 100)
                .subscribe(new DisposableObserver<ActivitiesList>() {
                    @Override
                    public void onNext(ActivitiesList activityList) {
                        if (activityList != null && activityList.getActivities().size() > 0) {
                            sendActivitiesToOcariot(convertFitBitDataToOcariot(activityList.getActivities()));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.w(LOG_TAG, "FITIBIT - onError: " + e);
                        loadDataOcariot();
                    }

                    @Override
                    public void onComplete() {
                        Log.w(LOG_TAG, "LOAD Fitbit - onComplete()");
                        loadDataOcariot();
                    }
                });
    }

    /**
     * Load data in OCARIoT Server.
     * If there is no internet connection, we can display the local database.
     * Otherwise it displays from the remote server.
     */
    private void loadDataOcariot() {
        Log.w(LOG_TAG, "loadDataOcariotInit()");
        if (userAccess == null) return;
        loading(true);

        ocariotRepository
                .listActivities(userAccess.getSubject(), "start_time", 1, 100)
                .subscribe(new DisposableObserver<List<Activity>>() {
                    @Override
                    public void onNext(List<Activity> activities) {
                        if (activities.size() > 0) {
                            mAdapter.clearItems();
                            mAdapter.addItems(activities);
                            mNoData.setVisibility(View.GONE);
                        } else if (mAdapter.itemsIsEmpty()) {
                            mNoData.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        loading(false);
                        if (getContext() == null) return;
                        Toast.makeText(getContext(), R.string.error_500,
                                Toast.LENGTH_SHORT).show();
                        if (mAdapter.itemsIsEmpty()) {
                            mNoData.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onComplete() {
                        loading(false);
                    }
                });
    }

    /**
     * Enable/Disable display loading data.
     *
     * @param enabled boolean
     */
    private void loading(final boolean enabled) {
        if (!enabled) {
            mDataSwipeRefresh.setRefreshing(false);
            itShouldLoadMore = true;
        } else {
            mDataSwipeRefresh.setRefreshing(true);
            itShouldLoadMore = false;
        }
    }

    /**
     * Publish Activity in OCARioT API Service.
     *
     * @param activities {@link List<Activity>} List of activities to be published.
     */
    private void sendActivitiesToOcariot(List<Activity> activities) {
        if (activities == null) return;
        int total = activities.size();
        Log.w(LOG_TAG, "sendOcariot() TOTAL: " + activities.size());

        // TODO Enviar lista completa na mesma requisição, quando o servidor oferecer suporte.
        int count = 0;
        for (Activity activity : activities) {
            count++;
            final int aux = count;
            Log.w(LOG_TAG, "SendActivity-pre" + new Gson().toJson(activity));
            ocariotRepository.publishActivity(userAccess.getSubject(), activity)
                    .subscribe(new DisposableObserver<Activity>() {
                        @Override
                        public void onNext(Activity acty) {
                            Log.w(LOG_TAG, "Activity Published: " + acty);
                            if (aux == total) loadDataOcariot();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.w(LOG_TAG, "SendActivity: " + e + "Ac " + new Gson().toJson(activity));
                            if (aux == total) loadDataOcariot();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    /**
     * Handles data conversions from the FitBit API to data
     * supported by the OCARIoT API.
     *
     * @param activities List of physical activities.
     */
    private List<Activity> convertFitBitDataToOcariot(List<Activity> activities) {
        if (activities == null) return new ArrayList<>();

        for (Activity activity : activities) {
            if (activity.getLevels() == null) {
                activities.remove(activity);
                continue;
            }

            for (ActivityLevel level : activity.getLevels()) {
                // In the FitBit API the duration comes in minutes.
                // The OCARIoT API waits in milliseconds.
                // Converts the duration in minutes to milliseconds.
                level.setDuration(level.getDuration() * 60000);
            }
            activity.setEndTime(DateUtils.addMillisecondsToString(activity.getStartTime(),
                    (int) activity.getDuration()));
            User user = new User();
            user.set_id(userAccess.getSubject());
            activity.setUser(user);
        }
        return activities;
    }
}
