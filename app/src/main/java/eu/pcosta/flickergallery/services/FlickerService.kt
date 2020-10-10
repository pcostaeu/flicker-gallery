package eu.pcosta.flickergallery.services

import eu.pcosta.flickergallery.api.FlickerApiService
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.rxkotlin.toFlowable
import java.io.Serializable

data class PhotoUrl(
    val url: String
) : Serializable

data class Photo(
    val id: String,
    val title: String,
    val smallPhoto: PhotoUrl,
    val photo: PhotoUrl
) : Serializable

/**
 * Flicker Service. Handle API calls and data transform for upper layers
 */
interface FlickerService {

    /**
     * Observe the loading of photos from online service in UI format ready
     *
     * @param page Page to be loaded
     * @param pageSize Number of items to be loaded per page
     * @param tags Tag to be loaded
     * @return Single observable with list of photos
     */
    fun getPhotos(page: Int, pageSize: Int, tags: String): Flowable<Photo>

    /**
     * Get tags to filter the photo list
     *
     * @return A list of possible tags
     */
    fun getTags(): Single<List<String>>
}

/**
 * Flicker Service Implementation
 *
 * @param flickerApiService Flicker API
 */
class FlickerServiceImpl(
    private val flickerApiService: FlickerApiService
) : FlickerService {

    companion object {
        private const val TAGS_COUNT = 20
    }

    override fun getPhotos(page: Int, pageSize: Int, tags: String): Flowable<Photo> {
        return flickerApiService.getPhotos(page, pageSize, tags)
            .flatMapPublisher {
                it.photos.photoList.toFlowable()
            }
            .concatMapSingle { flickerPhoto ->
                flickerApiService.getSizes(flickerPhoto.id)
                    .map { sizesResponse ->
                        val smallPhoto = sizesResponse.sizes.sizeList.first { it.size == "Large Square" }
                        // If large is not available (it can happen) pick the last from the list,
                        val photo = sizesResponse.sizes.sizeList.firstOrNull { it.size == "Large" } ?: sizesResponse.sizes.sizeList.last()

                        Photo(
                            id = flickerPhoto.id,
                            title = flickerPhoto.title,
                            smallPhoto = PhotoUrl(
                                url = smallPhoto.source
                            ),
                            photo = PhotoUrl(
                                url = photo.source
                            )
                        )
                    }
            }
    }

    override fun getTags(): Single<List<String>> {
        return flickerApiService.getTagsHotList(TAGS_COUNT)
            .map { response ->
                response.tags.tagList.map { it.name }
            }
    }
}