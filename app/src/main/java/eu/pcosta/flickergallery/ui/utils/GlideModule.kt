package eu.pcosta.flickergallery.ui.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import eu.pcosta.flickergallery.R

@GlideModule
class FlickerGlideModule : AppGlideModule() {

    /**
     * Update Glide with cache strategy so we don't hit the network all the time
     */
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        super.applyOptions(context, builder)
        builder.apply { RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL) }
    }

}

fun ImageView.loadImage(url: String, onLoadFinished: (() -> Unit)? = null) {
    val glideCallback = object : RequestListener<Drawable> {
        override fun onResourceReady(
            resource: Drawable?,
            model: Any?,
            target: Target<Drawable>?,
            dataSource: DataSource?,
            isFirstResource: Boolean
        ): Boolean {
            onLoadFinished?.invoke()
            return false
        }

        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            onLoadFinished?.invoke()
            return false
        }
    }

    GlideApp.with(this).load(url)
        .placeholder(R.drawable.ic_image)
        .error(R.drawable.ic_broken_image)
        .listener(glideCallback)
        .into(this)
}