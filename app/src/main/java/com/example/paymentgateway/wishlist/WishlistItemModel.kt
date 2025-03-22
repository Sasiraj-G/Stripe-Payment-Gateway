package com.example.paymentgateway.wishlist

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.example.paymentgateway.R
import com.example.paymentgateway.databinding.ItemWishlistBinding

@EpoxyModelClass
abstract class WishlistItemModel : EpoxyModelWithHolder<WishlistItemModel.Holder>() {

    @EpoxyAttribute
    lateinit var name: String

    @EpoxyAttribute
    lateinit var imageUrl: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    lateinit var onRemoveClickListener: (Int) -> Unit

    override fun getDefaultLayout(): Int = R.layout.item_wishlist

    override fun bind(holder: Holder) {

        holder.binding.textWishlistName.text=name

        // Load image with Glide

        Glide.with(holder.binding.imageWishlist.context)
            .load(imageUrl)
            .centerCrop()
            .into(holder.binding.imageWishlist)

        // Make sure the heart is visible and red
        holder.binding.imageHeart.visibility = View.VISIBLE
        holder.binding.imageHeart.setImageResource(R.drawable.red_heart)

        holder.binding.imageHeart.setOnClickListener {

            onRemoveClickListener(position)
            holder.binding.imageHeart.visibility = View.INVISIBLE
            holder.binding.imageHeart.setImageResource(R.drawable.white_heart)
            holder.binding.imageHeart.visibility = View.VISIBLE

        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemWishlistBinding
        override fun bindView(itemView: View) {
            binding = ItemWishlistBinding.bind(itemView)

        }
    }
}