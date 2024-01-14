package com.example.testapp

import android.util.Log
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class CookieManager: CookieJar {
    private val cookieStore = HashMap<String, List<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        // Save cookies
        cookieStore[url.host] = cookies
        Log.i("COOKIE_SAVED", "The following cookies were saved: $cookies")
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        // Load cookies
        Log.i("COOKIE_LOADED", "The cookies were loaded")
        return cookieStore[url.host] ?: ArrayList()
    }
}