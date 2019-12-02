package br.edu.uepb.nutes.ocariot.data.repository.remote;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import br.edu.uepb.nutes.ocariot.BuildConfig;
import br.edu.uepb.nutes.ocariot.OcariotApp;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

/**
 * Base class to be inherited for repository implementation.
 *
 * @author Copyright (c) 2018, NUTES/UEPB
 */
public abstract class BaseNetRepository {
    private OkHttpClient.Builder mClient;

    public BaseNetRepository() {
        if(!BuildConfig.UNSAFE_CONNECTION) mClient = new OkHttpClient().newBuilder();
        else mClient = getUnsafeOkHttpClient();

        mClient.followRedirects(false).cache(provideHttpCache());
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(s -> Timber.tag("OkHttp").d(s));
            logging.level(HttpLoggingInterceptor.Level.BODY);
            this.addInterceptor(logging);
            this.addInterceptor(new NetworkConnectionInterceptor());
        }
    }

    private Cache provideHttpCache() {
        int cacheSize = 10 * 1024 * 1024;
        return new Cache(OcariotApp.getContext().getCacheDir(), cacheSize);
    }

    private Gson provideGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        return gsonBuilder.create();
    }
//
//    private OkHttpClient provideOkHttpClient() {
//        if (mClient == null) mClient = new OkHttpClient().newBuilder();
//        mClient.followRedirects(false).cache(provideHttpCache());
//        return mClient.build();
//    }

    protected void addInterceptor(Interceptor interceptor) {
        if (interceptor == null) return;
        if (mClient == null) mClient = this.getUnsafeOkHttpClient();

        mClient.addInterceptor(interceptor);
    }

    protected Retrofit provideRetrofit(String baseUrl) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create(provideGson()))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(mClient.build())
                .build();
    }

    private OkHttpClient.Builder getUnsafeOkHttpClient() {
        Timber.d("Unsafe OkHttpClient initialized!");
        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            // Not implemented!
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            // Not implemented!
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(1, TimeUnit.MINUTES)
                    .readTimeout(1, TimeUnit.MINUTES)
                    .writeTimeout(1, TimeUnit.MINUTES)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
                    .hostnameVerifier((hostname, session) -> true);
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
