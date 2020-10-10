package eu.pcosta.flickergallery.ui.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import eu.pcosta.flickergallery.R
import eu.pcosta.flickergallery.services.Photo
import eu.pcosta.flickergallery.ui.preview.PhotoPreviewActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private val photosViewModel by viewModel<PhotosViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup adapter
        val adapter = PhotoAdapter(
            context = this,
            photosList = mutableListOf(),
            onClick = ::onListItemClicked
        )

        // Setup recycler view
        val isPortrait = resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT
        val layoutManager = GridLayoutManager(this, if (isPortrait) 2 else 3)
        am_recycler_view.layoutManager = layoutManager
        am_recycler_view.adapter = adapter
        am_recycler_view.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    photosViewModel.loadNextPage()
                }
            }
        })

        // Observe photos live data
        photosViewModel.getPhotos().observe(this) {
            adapter.setList(it.list)
            am_empty_state.visibility = if (it.status == Status.ERROR) View.VISIBLE else View.GONE
            am_progress_bar.visibility = if (it.status == Status.LOADING) View.VISIBLE else View.GONE
            am_recycler_view.visibility = if (it.status == Status.OK) View.VISIBLE else View.GONE
        }

        // Observe tags  filter button
        photosViewModel.getTags().observe(this) {
            if (it.list.isNotEmpty() && it.status == Status.OK) am_filter_action_btn.show() else am_filter_action_btn.hide()
        }
        am_filter_action_btn.setOnClickListener(::onFilterBtnClicked)
    }

    /**
     * When user clicks the Filter FAB, open a dialog with the available tags.
     * Tags live data is already populated with data so we are sure that we can use not-null assertion operator
     */
    private fun onFilterBtnClicked(view: View) {
        val tags = photosViewModel.getTags().value!!.list.toTypedArray()

        MaterialAlertDialogBuilder(this, R.style.CustomDialog)
            .setTitle(R.string.hot_tags_dialog_title)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .setSingleChoiceItems(tags, tags.indexOf(photosViewModel.currentSelectedCategory)) { dialog, selectedIndex ->
                dialog.dismiss()
                photosViewModel.updateCurrentCategory(tags[selectedIndex])
            }
            .show()
    }

    private fun onListItemClicked(photo: Photo, imageTransition: Pair<View, String>) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this, imageTransition).toBundle()
        startActivity(
            Intent(this, PhotoPreviewActivity::class.java).apply {
                putExtra("photo", photo)
            },
            options
        )
    }
}
