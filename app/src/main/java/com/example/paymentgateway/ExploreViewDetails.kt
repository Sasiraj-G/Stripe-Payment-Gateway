package com.example.paymentgateway

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.airbnb.epoxy.CarouselModel_
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.example.paymentgateway.databinding.ActivityExploreViewDetailsBinding
import com.example.paymentgateway.databinding.ItemExploreViewBinding
import kotlinx.coroutines.launch

class ExploreViewDetails : AppCompatActivity() {
    private lateinit var binding: ActivityExploreViewDetailsBinding
    private lateinit var apolloClient: ApolloClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityExploreViewDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        apolloClient = ApolloClient.Builder().serverUrl("https://staging1.flutterapps.io/api/graphql").build()


        fetchExploreData()

//        Handler(Looper.getMainLooper()).postDelayed({
//            throw RuntimeException()
//
//        },4000)

    }

    private fun fetchExploreData() {
        lifecycleScope.launch {
            try {
                val query = ViewListingDetailsQuery(listId = 732, preview = Optional.present(false))
                val viewListingDetailsResponse = apolloClient.query(query).execute().data?.viewListing
                Log.d("response","Viewlisting $viewListingDetailsResponse")
               handleResponse(viewListingDetailsResponse)

            } catch (e: Exception) {
                Log.e("GraphQL", "Error fetching data", e)

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
                        profileImage = "https://staging1.flutterapps.io/images/avatar/"+data.user?.profile.toString(),
                        address = "${data.city}, ${data.state}, ${data.country}",
                        displayName = data.user?.profile?.displayName.toString(),
                        userAmenties = data.userAmenities?.size.toString(),


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


//
//                SimilarListingsBindingModel_()
//                    .id("similar")
//                    .similar(it)


            }
            CarouselModel_()
                .id("recommended_listings")
                .models(similarItem)
                .numViewsToShowOnScreen(1f)
                .addTo(this)


        }

    }


}