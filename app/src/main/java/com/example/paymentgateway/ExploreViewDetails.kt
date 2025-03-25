package com.example.paymentgateway


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.view.View

import android.widget.ImageButton
import android.widget.ImageView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import androidx.lifecycle.lifecycleScope

import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.paymentgateway.databinding.ActivityExploreViewDetailsBinding

import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

import com.example.paymentgateway.wishlist.placeholder
import com.example.paymentgateway.wishlist.wishlistItem


class ExploreViewDetails : AppCompatActivity() {
    private lateinit var binding: ActivityExploreViewDetailsBinding
    private lateinit var apolloClient: ApolloClient

    private lateinit var heartButton: ImageView
    private lateinit var bottomSheetDialog: BottomSheetDialog
     val wishlistItems = mutableListOf<WishlistItem>()
    private lateinit var wishlistController: WishlistController

    private var isLoading = false




    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExploreViewDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apolloClient =
            ApolloClient.Builder().serverUrl("https://staging1.flutterapps.io/api/graphql").build()

        fetchExploreData()

        // Initialize heart button
        heartButton = findViewById(R.id.btnFavorite)
        heartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.heart_outline))
        heartButton.tag = "unselected"
        heartButton.setOnClickListener {
            toggleHeartAndShowBottomSheet()
        }

        setupBottomSheet()


        binding.btnBack.setOnClickListener {
            val intent= Intent(this,RentAllMainAactivity::class.java)
            startActivity(intent)
        }

    }

    private fun fetchExploreData() {
        if (isLoading) return
        isLoading = true
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {


                val query = ViewListingDetailsQuery(listId = 732, preview = Optional.present(false))
                val viewListingDetailsResponse = apolloClient.query(query).execute().data?.viewListing
                Log.d("response","Viewlisting $viewListingDetailsResponse")
               handleResponse(viewListingDetailsResponse)

            } catch (e: Exception) {
                Log.e("GraphQL", "Error fetching data", e)

            }finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun handleResponse(response: ViewListingDetailsQuery.ViewListing?) {


        if(response!=null){
            val viewLists = response.results?.let { data ->

                ViewListingModel(
                    id = data.id.toString(),
                    title = data.title.toString(),
                    personCapacity = data.personCapacity.toString(),
                    beds = data.beds.toString(),
                    bookingType = data.bookingType.toString(),
                    imageUrl = "https://staging1.flutterapps.io/images/upload/x_medium_" + data.listPhotoName.toString(),
                    reviewsCount = data.reviewsCount.toString(),
                    reviewsStarRating = data.reviewsStarRating.toString(),
                    price = data.listingData?.basePrice.toString(),
                    currency = data.listingData?.currency.toString(),
                    roomType = data.roomType.toString(),
                    wishListStatus = data.wishListStatus.toString(),
                    description = data.description.toString(),
                    houseType = data.houseType.toString(),
                    bedrooms = data.bedrooms.toString(),
                    profileImage = "https://staging1.flutterapps.io/images/avatar/medium_" + data.user?.profile?.picture.toString(),
                    address = "${data.city}, ${data.state}, ${data.country}",
                    displayName = data.user?.profile?.displayName.toString(),
                    userAmenties = data.userAmenities!!.map {
                      userAmenties(
                          itemName = it?.itemName.toString(),
                          itemIcon = it?.image.toString()
                      )

                    }

                )
            }
            val viewListingItem = mutableListOf(viewLists)
            displayData(viewListingItem)
        }

    }

    private fun displayData(items: MutableList<ViewListingModel?>) {

        binding.epoxyRecyclerView.withModels{
            val similarItem = items.map {

                ExploreViewBindingModel_()
                    .id("views")
                    .exploreDetails(it)
            }

            CarouselModel_()
                .id("recommended_listings")
                .models(similarItem)
                .numViewsToShowOnScreen(1f)
                .addTo(this)

//            items.forEachIndexed { index, item ->
//                similarListings {
//                    id("similar_listings")
//                    similar(item)
//                }
//            }

//
//            val similar = items.map {
//                SimilarListingsBindingModel_()
//                    .id("godfather")
//                    .similar(it)
//            }
//
//            CarouselModel_()
//                .id("similar_listings")
//                .models(similar)
//                .numViewsToShowOnScreen(1.25f)
//                .addTo(this)

//            val review=items.map{
//                ReviewsBindingModel_()
//                    .id("godfather")
//                    .review(it)
//            }
//            CarouselModel_()
//                .id("reviews")
//                .models(review)
//                .numViewsToShowOnScreen(1.25f)
//                .addTo(this)

        }


    }


    private fun toggleHeartAndShowBottomSheet() {

        val heartDrawable = if (heartButton.tag == "selected") {
            heartButton.tag = "unselected"
            ContextCompat.getDrawable(this, R.drawable.heart_outline)
        } else {
            heartButton.tag = "selected"
            ContextCompat.getDrawable(this, R.drawable.red_heart)
        }



        // Apply animation


        // Set the drawable
        heartButton.setImageDrawable(heartDrawable)
        binding.btnFavorite.setOnClickListener {
            showBottomSheet()
        }


        // Show bottom sheet if heart is selected
        if (heartButton.tag == "selected") {
            showBottomSheet()
        }

    }


    @SuppressLint("MissingInflatedId")
    private fun setupBottomSheet() {
        bottomSheetDialog = BottomSheetDialog(this)
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_wishlist, null)
        bottomSheetDialog.setContentView(bottomSheetView)

        val addButton = bottomSheetView.findViewById<ImageButton>(R.id.addItem)
        addButton.setOnClickListener {
            addNewWishlistGroup()
        }

        // Setup epoxy recyclerview
        val epoxyRecyclerView = bottomSheetView.findViewById<EpoxyRecyclerView>(R.id.epoxyRecyclerViewBottomSheet)

        wishlistController = WishlistController(this, object : WishlistCallbacks {
            override fun onWishlistUpdated(hasActiveItems: Boolean) {
                updateHeartState(hasActiveItems)
            }
            override fun syncWishlistItems(items: List<WishlistItem>) {
                wishlistItems.clear()
                wishlistItems.addAll(items)
            }
        })

        epoxyRecyclerView.setController(wishlistController)



        // Initialize with a placeholder
        if (wishlistItems.isEmpty()) {
            wishlistItems.add(WishlistItem("Gh", 0, true))

        }

        bottomSheetDialog.setOnDismissListener {
            updateHeartStateBasedOnWishlist()
            Log.d("WishlistActivity", "Bottom sheet dismissed, current items: ${wishlistItems.size}")
        }


        wishlistController.setData(wishlistItems.toList())
    }

    private fun showBottomSheet() {
        bottomSheetDialog.show()
    }

    private fun addNewWishlistGroup() {

        val currentItems = wishlistController.getCurrentItems().toMutableList()

        Log.d("checkwishlist","before "+wishlistItems.toString())
        wishlistItems.add(WishlistItem("wwe", 0, true))



        Log.d("GH","${wishlistItems.contains(WishlistItem("Gh", 0, true))}")
//        wishlistController.setData(wishlistItems)
        Log.d("checkwishlist","after "+wishlistItems.toString())
        wishlistController.setData(wishlistItems.toList())

        // Scroll to the newly added item
        binding.epoxyRecyclerView.smoothScrollToPosition(wishlistItems.size - 1)



    }

    private fun updateHeartState(hasActiveItems: Boolean) {
        if (hasActiveItems) {
            heartButton.tag = "selected"
            heartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.red_heart))

        } else {
            heartButton.tag = "unselected"
            heartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.heart_outline))

        }
    }

    private fun updateHeartStateBasedOnWishlist() {

        val currentItems = wishlistController.getCurrentItems()

        val hasActiveItems = wishlistItems.any { !it.isPlaceholder }
        updateHeartState(hasActiveItems)
    }

    // Interface for communication between controller and activity
    interface WishlistCallbacks {
        fun onWishlistUpdated(hasActiveItems: Boolean)
        fun syncWishlistItems(items: List<WishlistItem>)
    }

    class WishlistController(private val context: Context,  private val callbacks: WishlistCallbacks) : EpoxyController() {
        private var items: List<WishlistItem> = emptyList()

        // Add a method to get the current items
        fun getCurrentItems(): List<WishlistItem> {
            return items.toList()
        }

        fun setData(data: List<WishlistItem>) {

            this.items=data.toList()

            val hasActiveItems = items.any { !it.isPlaceholder }
            callbacks.onWishlistUpdated(hasActiveItems)

            // Sync back the data to the activity
            callbacks.syncWishlistItems(items)

            requestModelBuild()
        }


        fun updateItem(position: Int, updatedItem: WishlistItem) {
            if (position >= 0 && position < items.size) {
                val newList = items.toMutableList()
                newList[position] = updatedItem
                this.items = newList
                setData(newList)
            }
        }

        override fun buildModels() {

            items.forEachIndexed { index, item ->
                if (item.isPlaceholder) {
                    placeholder {
                        id("placeholder_${item.name}_${index}")
                        name("${item.name} (${item.count})")
                        position(index)

                        onRemoveClickListener { pos ->
                            // Remove this item
                            val newList = items.toMutableList()
                          // newList.removeAt(pos)
                            if (newList.isEmpty()) {
                                newList.add(WishlistItem("Gh", 0, true)) //true to false change now
                            }
                            setData(newList)
                        }

                        onAddClickListener { pos ->
                            val updatedItem = WishlistItem(items[pos].name, 1, false)
                            Log.d("WishlistController", "$pos to actual item")
                            updateItem(pos, updatedItem)

                        }
                    }
                } else {
                    wishlistItem {
                        id("item_${item.name}_${index}")
                        name("${item.name} (${item.count})")
                        imageUrl("https://staging1.flutterapps.io/images/upload/x_medium_c2951bcf126816850b377ea63d886682.png")
                        position(index)

                        onRemoveClickListener { pos ->
                            // Replace with placeholder
                            val updatedItem = WishlistItem(items[pos].name, 0, true)
                            Log.d("WishlistController", "Converting actual item at position $pos to placeholder")
                            updateItem(pos, updatedItem)

                        }
                    }
                }
            }
        }
    }



}