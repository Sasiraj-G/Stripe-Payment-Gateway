package com.example.paymentgateway.wishlist

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.widget.ImageView
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.example.paymentgateway.R
import com.example.paymentgateway.databinding.ItemPlaceHolderWishlistBinding

@EpoxyModelClass
abstract class PlaceholderModel : EpoxyModelWithHolder<PlaceholderModel.Holder>() {

    @EpoxyAttribute
    lateinit var name: String

    @EpoxyAttribute
    var position: Int = 0

    @EpoxyAttribute
    lateinit var onRemoveClickListener: (Int) -> Unit

    @EpoxyAttribute
    lateinit var onAddClickListener: (Int) -> Unit

    override fun getDefaultLayout(): Int = R.layout.item_place_holder_wishlist

    override fun bind(holder: Holder) {

        holder.binding.textWishlistName.text=name

        // Hide the heart initially for placeholder
        holder.binding.imageHeart.visibility=View.VISIBLE


        holder.binding.cardView.setOnClickListener {


            holder.binding.imageHeart.visibility=View.INVISIBLE

            holder.binding.imageHeart.setImageResource(R.drawable.white_heart)

            holder.binding.cardView.postDelayed({
                onAddClickListener(position)
                holder.binding.imageHeart.visibility=View.VISIBLE

            }, 300)
        }

        holder.binding.imageHeart.setOnClickListener {


            holder.binding.imageHeart.visibility=View.VISIBLE

            holder.binding.imageHeart.setImageResource(R.drawable.white_heart)

            holder.binding.imageHeart.postDelayed({
                onAddClickListener(position)
                holder.binding.imageHeart.visibility=View.VISIBLE

            }, 300)
        }




        holder.binding.imageHeart.setOnClickListener {

            onRemoveClickListener(position)
            holder.binding.imageHeart.visibility=View.VISIBLE
        }
    }



    class Holder : EpoxyHolder() {

        lateinit var binding: ItemPlaceHolderWishlistBinding

        override fun bindView(itemView: View) {
            binding=ItemPlaceHolderWishlistBinding.bind(itemView)

        }
    }
}