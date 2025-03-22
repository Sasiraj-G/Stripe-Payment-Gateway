package com.example.paymentgateway.wishlist

import android.content.Context
import android.util.Log
import com.airbnb.epoxy.EpoxyController
import com.example.paymentgateway.WishlistItem

class WishlistController(private val context: Context) : EpoxyController() {
    private var items: List<WishlistItem> = emptyList()

    fun setData(data: List<WishlistItem>) {
        //this.items=data.toList()
        items = data.toList()
        requestModelBuild()
    }


    override fun buildModels() {
        items.forEachIndexed { index, item ->
            if (item.isPlaceholder) {
                placeholder {
                    id("placeholder_${item.name}_${index}")
                    name("${item.name} (${item.count})")
                    position(index)
                    onAddClickListener { pos ->

                        val newList = items.toMutableList()
                        newList[pos] = WishlistItem(newList[pos].name, 1, false)
                        setData(newList)

                    }
                }
            } else {
                wishlistItem {
                    id("item_${item.name}_${index}")
                    name("${item.name} (${item.count})")
                    imageUrl("https://staging1.flutterapps.io/images/upload/x_medium_c2951bcf126816850b377ea63d886682.png") // In real app, use actual image URL
                    position(index)

                    onRemoveClickListener { pos ->
                        // Replace with placeholder
                        val newList = items.toMutableList()
                        newList[pos] = WishlistItem(newList[pos].name, 0, true)
                        Log.d("MYTAGS","${item.name} ${item.count}")
                        setData(newList)

                    }
                }
            }
        }
    }
}