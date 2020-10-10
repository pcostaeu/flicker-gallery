package eu.pcosta.flickergallery.ui.main

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import eu.pcosta.flickergallery.R
import eu.pcosta.flickergallery.services.Photo
import eu.pcosta.flickergallery.ui.utils.loadImage
import kotlinx.android.synthetic.main.item_list_photo.view.*

class PhotoAdapter(
    private val context: Context,
    private var photosList: List<Photo>,
    private val onClick: (photo: Photo, imageTransition: Pair<View, String>) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        return PhotoViewHolder(
            LayoutInflater.from(context).inflate(R.layout.item_list_photo, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.onBind(photosList[position])
    }

    override fun getItemCount() = photosList.size

    /**
     * Replace adapter list with the items provided
     *
     * @param photos List with photos to replace the existent ones
     */
    fun setList(photos: List<Photo>) {
        photosList = photos
        notifyDataSetChanged()
    }

    inner class PhotoViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private val root: ViewGroup = view.ilp_root
        private val photoView: ImageView = view.ilp_photo
        private val titleView: TextView = view.ilp_title

        init {
            root.setOnClickListener {
                onClick(
                    photosList[layoutPosition],
                    Pair(photoView, photoView.transitionName)
                )
            }
        }

        fun onBind(photo: Photo) {
            titleView.text = photo.title

            photoView.loadImage(photo.smallPhoto.url)
            photoView.transitionName = photo.id
        }
    }
}