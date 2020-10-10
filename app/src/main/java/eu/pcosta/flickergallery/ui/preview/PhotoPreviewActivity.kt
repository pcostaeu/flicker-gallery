package eu.pcosta.flickergallery.ui.preview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.pcosta.flickergallery.R
import eu.pcosta.flickergallery.services.Photo
import eu.pcosta.flickergallery.ui.utils.loadImage
import kotlinx.android.synthetic.main.activity_photo_preview.*

class PhotoPreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_preview)
        setSupportActionBar(app_toolbar)
        app_toolbar.setNavigationOnClickListener { onBackPressed() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Load photo, postpone enter transition so we can load the image first and have a smooth transition
        (intent.extras?.getSerializable("photo") as? Photo)?.let {
            supportActionBar?.title = it.title

            supportPostponeEnterTransition()
            app_photo.transitionName = it.id
            app_photo.loadImage(it.photo.url) {
                supportStartPostponedEnterTransition()
            }
        }
    }
}