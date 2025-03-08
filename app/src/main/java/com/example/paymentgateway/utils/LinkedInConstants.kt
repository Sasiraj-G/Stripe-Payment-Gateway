package com.example.paymentgateway.utils


object LinkedInConstants {
    const val CLIENT_ID = "86uxtto9rfz1t2"
    const val CLIENT_SECRET = "WPL_AP1.hrUOizjX6xVYVmJr.ajJ7kw=="
    const val REDIRECT_URI = "https://www.linkedin.com/developers/tools/oauth/redirect" // e.g., "https://yourapp.com/callback"
    const val SCOPE = "r_liteprofile r_emailaddress"

    const val AUTHORIZATION_URL = "https://www.linkedin.com/oauth/v2/authorization"
    const val ACCESS_TOKEN_URL = "https://www.linkedin.com/oauth/v2/accessToken"
    const val PROFILE_URL = "https://api.linkedin.com/v2/me"
    const val EMAIL_URL = "https://api.linkedin.com/v2/emailAddress?q=members&projection=(elements*(handle~))"
}