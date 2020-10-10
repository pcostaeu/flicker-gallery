package eu.pcosta.flickergallery

import android.app.Application
import eu.pcosta.flickergallery.api.FlickerApiService
import eu.pcosta.flickergallery.api.FlickerApiServiceImpl
import eu.pcosta.flickergallery.services.ConnectivityService
import eu.pcosta.flickergallery.services.ConnectivityServiceImpl
import eu.pcosta.flickergallery.services.FlickerService
import eu.pcosta.flickergallery.services.FlickerServiceImpl
import eu.pcosta.flickergallery.ui.main.PhotosViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

val serviceModule = module {
    single<ConnectivityService> { ConnectivityServiceImpl(get()) }
    single<FlickerApiService> { FlickerApiServiceImpl(get(), get()) }
    single<FlickerService> { FlickerServiceImpl(get()) }
}

val viewModelModule = module {
    viewModel { PhotosViewModel(get()) }
}

/**
 * Flicker Main Application. Start koin and necessary modules to be injected
 */
class FlickerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@FlickerApplication)
            modules(listOf(serviceModule, viewModelModule))
        }
    }
}