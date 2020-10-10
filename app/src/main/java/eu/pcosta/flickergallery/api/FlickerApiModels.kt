package eu.pcosta.flickergallery.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

//region Photos response
@JsonClass(generateAdapter = true)
data class FlickerPhoto(
    @field:Json(name = "id") val id: String,
    @field:Json(name = "owner") val owner: String,
    @field:Json(name = "secret") val secret: String,
    @field:Json(name = "server") val server: String,
    @field:Json(name = "title") val title: String,
    @field:Json(name = "farm") val farm: Int,
    @field:Json(name = "ispublic") val isPublic: Int,
    @field:Json(name = "isfriend") val isFriend: Int,
    @field:Json(name = "isfamily") val isFamily: Int,
)

@JsonClass(generateAdapter = true)
data class FlickerPhotos(
    @field:Json(name = "photo") val photoList: List<FlickerPhoto>,
    @field:Json(name = "page") val page: Int,
    @field:Json(name = "pages") val pages: Int,
    @field:Json(name = "perpage") val perPage: Int,
    @field:Json(name = "total") val totalPhotos: Int
)

@JsonClass(generateAdapter = true)
data class FlickerPhotosResponse(
    @field:Json(name = "photos") val photos: FlickerPhotos,
    @field:Json(name = "stat") val responseCode: String
)
//endregion

//region Sizes response
@JsonClass(generateAdapter = true)
data class FlickerSize(
    @field:Json(name = "label") val size: String,
    @field:Json(name = "width") val width: Int,
    @field:Json(name = "height") val height: Int,
    @field:Json(name = "source") val source: String,
    @field:Json(name = "url") val url: String,
    @field:Json(name = "media") val media: String
)

@JsonClass(generateAdapter = true)
data class FlickerSizes(
    @field:Json(name = "size") val sizeList: List<FlickerSize>,
    @field:Json(name = "canprint") val canPrint: Int,
    @field:Json(name = "candownload") val canDownload: Int,
    @field:Json(name = "canblog") val canBlog: Int
)

@JsonClass(generateAdapter = true)
data class FlickerSizesResponse(
    @field:Json(name = "sizes") val sizes: FlickerSizes,
    @field:Json(name = "stat") val responseCode: String
)
//endregion

//region Hot tags
@JsonClass(generateAdapter = true)
data class FlickerTag(
    @field:Json(name = "_content") val name: String
)

@JsonClass(generateAdapter = true)
data class FlickerHotTags(
    @field:Json(name = "tag") val tagList: List<FlickerTag>,
)

@JsonClass(generateAdapter = true)
data class FlickerHotTagsResponse(
    @field:Json(name = "hottags") val tags: FlickerHotTags,
    @field:Json(name = "period") val period: String,
    @field:Json(name = "count") val count: Int,
    @field:Json(name = "stat") val responseCode: String
)
//endregion
