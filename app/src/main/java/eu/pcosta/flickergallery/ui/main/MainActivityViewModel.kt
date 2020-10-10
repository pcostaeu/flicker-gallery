package eu.pcosta.flickergallery.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import eu.pcosta.flickergallery.services.FlickerService
import eu.pcosta.flickergallery.services.Photo
import eu.pcosta.flickergallery.ui.utils.applySchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.BehaviorProcessor
import io.reactivex.schedulers.Schedulers

enum class Status {
    ERROR, LOADING, OK
}

data class Response<T>(
    val status: Status,
    val list: List<T> = emptyList()
)

class PhotosViewModel(
    flickerService: FlickerService
) : ViewModel() {

    companion object {
        private const val PAGE_SIZE = 16
    }

    /**
     * Composite disposable to kill stream when activity dies
     */
    private val compositeDisposable = CompositeDisposable()

    /**
     * Live data with photos response
     */
    private val photosData = MutableLiveData<Response<Photo>>()

    /**
     * Live data with tags response
     */
    private val tagsData = MutableLiveData<Response<String>>()

    /**
     * Category processor. Holds the last selected category
     */
    private val categoryProcessor = BehaviorProcessor.create<String>()
    val currentSelectedCategory: String
        get() = categoryProcessor.value!!

    /**
     * Page processor to load more pages as we scroll
     */
    private val pageProcessor = BehaviorProcessor.createDefault(1)
    private val currentPage: Int
        get() = pageProcessor.value!!

    /**
     * On view model init, load current hot tags for the day. On result OK, publish the first tag to the category processor
     */
    init {
        compositeDisposable.add(
            flickerService.getTags()
                .map {
                    Response(Status.OK, it)
                }
                .doOnSubscribe {
                    tagsData.postValue(Response(Status.LOADING))
                    photosData.postValue(Response(Status.LOADING))
                }
                .onErrorReturnItem(Response(Status.ERROR))
                .doOnSuccess {
                    tagsData.postValue(it)
                    categoryProcessor.onNext(it.list.firstOrNull() ?: "")
                }
                .applySchedulers()
                .subscribe()
        )

        compositeDisposable.add(
            categoryProcessor // emission to this stream can come from UI thread, change to io
                .observeOn(Schedulers.io())
                .doOnNext { photosData.postValue(Response(Status.LOADING)) }
                .switchMap { category ->
                    /**
                     * Switching here allow us to accumulate multiple pages into scan function.
                     * When category changes, it will be reset, since we switch to new a flowable starting again with an empty list.
                     * If we change only the page, it will keep accumulating the photo list
                     */
                    pageProcessor // emission to this stream can come from UI thread, change to io
                        .observeOn(Schedulers.io())
                        .switchMap { page ->
                            flickerService.getPhotos(page, PAGE_SIZE, category)
                        }
                        .scan(emptyList<Photo>()) { accumulated, newValue ->
                            accumulated + newValue
                        }
                        .map { Response(Status.OK, it) }
                }
                .onErrorReturnItem(Response(Status.ERROR))
                .applySchedulers()
                .subscribe { photosData.postValue(it) }
        )
    }

    fun getPhotos(): LiveData<Response<Photo>> = photosData

    fun getTags(): LiveData<Response<String>> = tagsData

    /**
     * Change category on processor to the user selected tag
     * Reset page processor to initial page
     */
    fun updateCurrentCategory(tag: String) {
        pageProcessor.onNext(1)
        categoryProcessor.onNext(tag)
    }

    /**
     * Load next page when reaching the bottom of list
     * Ensure that we emit only if last page is already complete
     */
    fun loadNextPage() {
        if (photosData.value!!.list.size == PAGE_SIZE * currentPage) {
            pageProcessor.onNext(currentPage + 1)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}