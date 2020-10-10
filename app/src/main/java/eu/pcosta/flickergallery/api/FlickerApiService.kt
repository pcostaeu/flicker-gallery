package eu.pcosta.flickergallery.api

import android.content.Context
import eu.pcosta.flickergallery.services.ConnectivityService
import io.reactivex.Single
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Flicker API Methods
 */
interface FlickerApi {
    @GET("?method=flickr.photos.search&api_key=9a95c68a9c6ec61104cd3967dcbb8bd3&format=json&nojsoncallback=1")
    fun getPhotos(
        @Query("api_key") apiKey: String,
        @Query("tags") tags: String,
        @Query("page") page: Int,
        @Query("per_page") pageSize: Int
    ): Single<FlickerPhotosResponse>

    @GET("?method=flickr.photos.getSizes&format=json&nojsoncallback=1")
    fun getSizes(
        @Query("api_key") apiKey: String,
        @Query("photo_id") photoId: String
    ): Single<FlickerSizesResponse>

    @GET("?method=flickr.tags.getHotList&count=20&format=json&nojsoncallback=1")
    fun getTagsHotList(
        @Query("api_key") apiKey: String,
        @Query("count") count: Int
    ): Single<FlickerHotTagsResponse>
}

/**
 * Flicker API Service for other services to consume
 */
interface FlickerApiService {
    /**
     * Retrieve photos by page and tag
     *
     * @param page Page to be loaded
     * @param pageSize Number of items to be loaded per page
     * @param tags Tag to be loaded
     * @return Single observable with the available photos for the page and tag
     */
    fun getPhotos(page: Int, pageSize: Int, tags: String): Single<FlickerPhotosResponse>

    /**
     * Get download links for an image by id
     *
     * @param photoId The photo id to retrieve the links
     * @return Single observable with the available sizes
     */
    fun getSizes(photoId: String): Single<FlickerSizesResponse>

    /**
     * Get the hottest tags for the current day
     *
     * @param count The number of tags to be returned
     * @return List of the current hot tags
     */
    fun getTagsHotList(count: Int): Single<FlickerHotTagsResponse>
}

/**
 * Flicker API Service implementation
 *
 * @param connectivityService Connectivity service for cache implementation
 */
class FlickerApiServiceImpl(
    context: Context,
    connectivityService: ConnectivityService
) : FlickerApiService {

    companion object {
        private const val FLICKER_API_KEY = "api key here"
    }

    private val flickerApi by lazy {

        val cache = Cache(context.cacheDir, (5 * 1024 * 1024).toLong()) //5MB cache
        val client = OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor { chain ->
                val request = chain.request().let {
                    if (connectivityService.isConnectedToInternet()) {
                        // Cache for 5 minutes if we have internet
                        it.newBuilder().header("Cache-Control", "public, max-age=${5 * 60}").build()
                    } else {
                        // Cache for a day if we have no internet
                        it.newBuilder().header(
                            "Cache-Control",
                            "public, only-if-cached, max-stale=${60 * 60 * 24}"
                        ).build()
                    }
                }
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .baseUrl("https://www.flickr.com/services/rest/")
            .client(client)
            .build()
            .create(FlickerApi::class.java)
    }

    override fun getPhotos(page: Int, pageSize: Int, tags: String): Single<FlickerPhotosResponse> {
        return flickerApi.getPhotos(FLICKER_API_KEY, tags, page, pageSize)
    }

    override fun getSizes(photoId: String): Single<FlickerSizesResponse> {
        return flickerApi.getSizes(FLICKER_API_KEY, photoId)
    }

    override fun getTagsHotList(count: Int): Single<FlickerHotTagsResponse> {
        return flickerApi.getTagsHotList(FLICKER_API_KEY, count)
    }


}