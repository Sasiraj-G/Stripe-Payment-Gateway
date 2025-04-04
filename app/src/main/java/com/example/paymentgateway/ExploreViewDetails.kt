package com.example.paymentgateway


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.Manifest
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.airbnb.epoxy.CarouselModel_
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.carousel
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieTask
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.paymentgateway.databinding.ActivityExploreViewDetailsBinding

import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

import com.example.paymentgateway.wishlist.placeholder
import com.example.paymentgateway.wishlist.wishlistItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


class ExploreViewDetails : AppCompatActivity() {
    private var _binding: ActivityExploreViewDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var apolloClient: ApolloClient

    private lateinit var heartButton: ImageView
    private lateinit var bottomSheetDialog: BottomSheetDialog
     val wishlistItems = mutableListOf<WishlistItem>()
    private lateinit var wishlistController: WishlistController

    private var isLoading = false

    private var listId: String? = null






    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityExploreViewDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.lottieLoader.visibility=View.VISIBLE
        binding.lottieLoader.playAnimation()

        Log.d("anime","msg")

        apolloClient =
            ApolloClient.Builder().serverUrl("https://staging1.flutterapps.io/api/graphql").build()

        handleDeepLink(intent)

        fetchExploreData("732")



        // Initialize heart button
        heartButton = findViewById(R.id.btnFavorite)
        heartButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.heart_outline))
        heartButton.tag = "unselected"
        heartButton.setOnClickListener {
            toggleHeartAndShowBottomSheet()
        }

        setupBottomSheet()


        binding.btnBack.setOnClickListener {
         onBackPressed()
        }



    }




    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            setIntent(it)
            handleDeepLink(it)
        }
    }

    private fun extractNumericId(rawData: String): Int? {
        return try {
            val parts = rawData.split("-")
            val numericPart = parts.lastOrNull()

            numericPart?.toInt() ?: run {
                Log.e("ID Extraction", "No numeric part found in rawData: $rawData")
                null
            }
        } catch (e: NumberFormatException) {
            Log.e("ID Extraction", "Invalid numeric format in rawData: $rawData", e)
            null
        }
    }


    private fun handleDeepLink(intent: Intent) {
        val data = intent.data ?: run {
            return
        }
        if (data.host == "staging1.flutterapps.io") {
            val pathSegments = data.pathSegments
            if (pathSegments.size >= 2 && pathSegments[0] == "rooms") {
                val rawData = pathSegments[1]
                listId = extractNumericId(rawData).toString()
                listId?.let { fetchExploreData(it) }
            } else {
                Log.d("DeepLink", "Invalid path segments: $pathSegments")
            }
        }
    }



    private fun fetchExploreData(itemId : String?) {
       val id = itemId?.toInt()
        val roomId = id!!  // convert Int? to Int
        Log.d("DeepLink", "Inside Extracted ID: $roomId")
        Log.d("anime","fetching msg")
        binding.lottieLoader.visibility=View.VISIBLE
        binding.bottomSpace.visibility=View.GONE

        lifecycleScope.launch {
            try {

                Log.d("anime","try block msg")
                val query = ViewListingDetailsQuery(listId = roomId, preview = Optional.present(false))
                val viewListingDetailsResponse = apolloClient.query(query).execute().data?.viewListing
                Log.d("response","Viewlisting $viewListingDetailsResponse")
               handleResponse(viewListingDetailsResponse)
                binding.lottieLoader.visibility=View.GONE
                binding.bottomSpace.visibility=View.VISIBLE
                binding.epoxyRecyclerView.visibility=View.VISIBLE

            } catch (e: Exception) {
                Log.e("GraphQL", "Error fetching data", e)
                binding.lottieLoader.visibility=View.VISIBLE
                binding.bottomSpace.visibility=View.GONE
                binding.epoxyRecyclerView.visibility=View.GONE
                Log.d("anime"," catch block msg")

            }finally {

             //   binding.progressBar.visibility = View.GONE
                Log.d("anime"," final block msg")
             //   binding.epoxyRecyclerView.visibility=View.VISIBLE
            //    binding.lottieLoader.visibility = View.GONE
             //   binding.bottomSpace.visibility=View.VISIBLE
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
        val reviewsUser = getReviews()
        val similarData = getSimilarListings()
        binding.epoxyRecyclerView.withModels{



            // vertical scroll view

          items.forEachIndexed { index, item ->
             hostSelection {
                 id("host_selection")
                 host(item)

              }
           }
            items.forEachIndexed { index, item ->
                detailsPlace {
                    id("details_place")
                }
            }
            items.forEachIndexed { index, item ->
                mapDetails {
                    id("map_details")
                    exploreDetails(item)
                }
            }

            //horizonal scrolling
            val reviews = reviewsUser.map { review ->
                ReviewsBindingModel_()
                    .id("reviews")
                    .review(review)
            }
            CarouselModel_()
                .id("reviews")
                .models(reviews)
                .addTo(this)

            items.forEachIndexed{ index, item ->
                lastDetails {
                    id("lastDetails $index")
                    lastDetails(item)

                }

            }

               // horizontal scroll view using carousel model
                 val similar = similarData.map { item ->
                     SimilarListingsBindingModel_()
                         .id("godfather")
                         .similar(item)
                 }

                 CarouselModel_()
                     .id("similar_listings")
                     .models(similar)
                     .addTo(this)
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


    override fun onPause() {
        super.onPause()
        binding.epoxyRecyclerView.visibility=View.GONE
    }

    override fun onResume() {
        super.onResume()
        fetchExploreData("732")
    }



    override fun onDestroy() {
        super.onDestroy()
        _binding=null

    }





}

