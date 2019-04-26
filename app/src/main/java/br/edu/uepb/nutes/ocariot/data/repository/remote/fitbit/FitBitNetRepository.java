package br.edu.uepb.nutes.ocariot.data.repository.remote.fitbit;

import android.content.Context;
import android.util.Log;

import net.openid.appauth.AuthState;
import net.openid.appauth.AuthorizationService;

import java.util.List;

import br.edu.uepb.nutes.ocariot.data.model.ActivitiesList;
import br.edu.uepb.nutes.ocariot.data.model.Child;
import br.edu.uepb.nutes.ocariot.data.model.PhysicalActivity;
import br.edu.uepb.nutes.ocariot.data.model.Sleep;
import br.edu.uepb.nutes.ocariot.data.model.SleepList;
import br.edu.uepb.nutes.ocariot.data.model.fitbit.CaloriesLogList;
import br.edu.uepb.nutes.ocariot.data.model.fitbit.LogData;
import br.edu.uepb.nutes.ocariot.data.model.fitbit.MinutesFairlyActiveLogList;
import br.edu.uepb.nutes.ocariot.data.model.fitbit.MinutesVeryActiveLogList;
import br.edu.uepb.nutes.ocariot.data.model.fitbit.StepsLogList;
import br.edu.uepb.nutes.ocariot.data.repository.local.pref.AppPreferencesHelper;
import br.edu.uepb.nutes.ocariot.data.repository.remote.BaseNetRepository;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.Request;

/**
 * Repository to consume the FitBit API.
 *
 * @author Copyright (c) 2018, NUTES/UEPB
 */
public class FitBitNetRepository extends BaseNetRepository {
    private FitBitService fitBitService;
    private AuthorizationService authService;

    private FitBitNetRepository(Context context) {
        super(context);

        super.addRequestInterceptor(requestInterceptor());
        fitBitService = super.provideRetrofit(FitBitService.BASE_URL_FITBIT)
                .create(FitBitService.class);
        authService = new AuthorizationService(context);
    }

    public static FitBitNetRepository getInstance(Context context) {
        return new FitBitNetRepository(context);
    }

    /**
     * Dispose AuthorizationService oAuth2 service.
     */
    public void dispose() {
        if (authService != null) authService.dispose();
    }

    /**
     * Provide intercept with header according to fitbit.
     *
     * @return Interceptor
     */
    private Interceptor requestInterceptor() {
        return chain -> {
            Request original = chain.request();
            final Request.Builder requestBuilder = original.newBuilder()
                    .header("Accept", "application/json")
                    .header("Content-type", "application/json")
                    .method(original.method(), original.body());

            final AuthState authState = AppPreferencesHelper.getInstance(this.mContext)
                    .getAuthStateFitBit();

            authState.performActionWithFreshTokens(authService, (accessToken, idToken, exception) -> {
                if (accessToken != null) {
                    requestBuilder.header(
                            "Authorization",
                            "Bearer ".concat(accessToken)
                    );
                }
            });
            Log.w("InterceptorFitBit", requestBuilder.build().headers().toString());
            Log.w("InterceptorFitBit", "| REQUEST: " + requestBuilder.build().method() + " "
                    + requestBuilder.build().url().toString());
            return chain.proceed(requestBuilder.build());
        };
    }

    public Single<List<PhysicalActivity>> listActivities(String beforeDate, String afterDate,
                                                         String sort, int offset, int limit) {
        return fitBitService.listActivities(beforeDate, afterDate, sort, offset, limit)
                .map(ActivitiesList::getActivities)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<Sleep>> listSleep(String beforeDate, String afterDate,
                                         String sort, int offset, int limit) {
        return fitBitService.listSleep(beforeDate, afterDate, sort, offset, limit)
                .map(SleepList::getSleepList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<Child> getProfile() {
        return fitBitService.getProfile()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<LogData>> getStepsLog(String date, String period) {
        return fitBitService.getStepsLog(date, period)
                .map(StepsLogList::getSteps)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<LogData>> getCaloriesLog(String date, String period) {
        return fitBitService.getCaloriesLog(date, period)
                .map(CaloriesLogList::getCalories)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<LogData>> getMinutesFairlyActiveLog(String date, String period) {
        return fitBitService.getMinutesFairlyActiveLog(date, period)
                .map(MinutesFairlyActiveLogList::getMinutesFairlyActive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Single<List<LogData>> getMinutesVeryActiveLog(String date, String period) {
        return fitBitService.getMinutesVeryActiveLog(date, period)
                .map(MinutesVeryActiveLogList::getMinutesVeryActive)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}