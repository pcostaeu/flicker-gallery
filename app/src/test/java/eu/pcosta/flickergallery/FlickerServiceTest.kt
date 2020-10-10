package eu.pcosta.flickergallery

import eu.pcosta.flickergallery.api.*
import eu.pcosta.flickergallery.services.FlickerServiceImpl
import eu.pcosta.flickergallery.services.Photo
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Single
import org.amshove.kluent.itReturns
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FlickerServiceTest {

    private val photosMockResponse = FlickerPhotosResponse(
        photos = FlickerPhotos(
            page = 1,
            pages = 1,
            perPage = 1,
            totalPhotos = 1,
            photoList = listOf(
                FlickerPhoto(
                    id = "id",
                    owner = "owner",
                    secret = "secret",
                    server = "server",
                    title = "title",
                    farm = 123,
                    isFamily = 0,
                    isFriend = 0,
                    isPublic = 1
                ),
                FlickerPhoto(
                    id = "id",
                    owner = "owner",
                    secret = "secret",
                    server = "server",
                    title = "title",
                    farm = 123,
                    isFamily = 0,
                    isFriend = 0,
                    isPublic = 1
                )
            )
        ),
        responseCode = "ok"
    )

    private val sizesMockResponse = FlickerSizesResponse(
        sizes = FlickerSizes(
            sizeList = listOf(
                FlickerSize(
                    size = "Large Square",
                    width = 150,
                    height = 150,
                    source = "source_small",
                    url = "url",
                    media = "media"
                ),
                FlickerSize(
                    size = "Large",
                    width = 1500,
                    height = 750,
                    source = "source_large",
                    url = "url",
                    media = "media"
                )
            ),
            canBlog = 0,
            canPrint = 0,
            canDownload = 1
        ),
        responseCode = "ok"
    )

    private val tagsMockResponse = FlickerHotTagsResponse(
        tags = FlickerHotTags(
            tagList = listOf(FlickerTag("dogs"), FlickerTag("kittens"))
        ),
        period = "day",
        count = 2,
        responseCode = "ok"
    )

    @Mock
    private lateinit var flickerApiService: FlickerApiService

    private fun buildService(): FlickerServiceImpl {
        return FlickerServiceImpl(flickerApiService)
    }

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)

        whenever(flickerApiService.getPhotos(any(), any(), any())) itReturns Single.just(photosMockResponse)
        whenever(flickerApiService.getSizes(any())) itReturns Single.just(sizesMockResponse)
        whenever(flickerApiService.getTagsHotList(any())) itReturns Single.just(tagsMockResponse)
    }

    @Test
    fun getPhotos() {
        val photos: List<Photo> = buildService().getPhotos(1, 1, "dogs").toList().blockingGet()
        verify(flickerApiService, timeout(1000)).getPhotos(eq(1), eq(1), eq("dogs"))
        verify(flickerApiService, times(2)).getSizes(eq("id"))

        photos.size shouldBeEqualTo 2
        photos[0].id shouldBeEqualTo "id"
        photos[0].photo.url shouldBeEqualTo "source_large"
        photos[0].smallPhoto.url shouldBeEqualTo "source_small"

        photos[1].id shouldBeEqualTo "id"
        photos[1].photo.url shouldBeEqualTo "source_large"
        photos[1].smallPhoto.url shouldBeEqualTo "source_small"
    }

    @Test
    fun getTags() {
        val tags = buildService().getTags().blockingGet()

        verify(flickerApiService, timeout(1000)).getTagsHotList(eq(20))

        tags.size shouldBeEqualTo 2
        tags[0] shouldBeEqualTo "dogs"
        tags[1] shouldBeEqualTo "kittens"
    }

}
