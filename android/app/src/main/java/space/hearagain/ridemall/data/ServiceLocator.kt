package space.hearagain.ridemall.data

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import space.hearagain.ridemall.BuildConfig
import space.hearagain.ridemall.data.api.ApiService
import space.hearagain.ridemall.data.repository.StoreRepository

/**
 * 轻量手动依赖装配(D1:不上 Hilt)。在 Application.onCreate 调 [init] 一次。
 */
object ServiceLocator {
    lateinit var repository: StoreRepository
        private set
    lateinit var deviceIdStore: DeviceIdStore
        private set

    fun init(context: Context) {
        val baseUrl = BuildConfig.API_BASE_URL
        val store = DeviceIdStore(context)
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(DeviceIdInterceptor(store))
            .addInterceptor(logging)
            .build()
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        val api = retrofit.create(ApiService::class.java)
        repository = StoreRepository(api, baseUrl)
        deviceIdStore = store
    }
}
