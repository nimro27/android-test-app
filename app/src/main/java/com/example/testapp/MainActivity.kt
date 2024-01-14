package com.example.testapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import androidx.activity.contextaware.withContextAvailable
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create
import java.net.PasswordAuthentication
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var namesAdapter: NamesAdapter
    private lateinit var unifiApiService: UnifiApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        namesAdapter = NamesAdapter(emptyList())
        recyclerView.adapter = namesAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val btnEditConnection = findViewById<Button>(R.id.btnEditConnection)
        btnEditConnection.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        // Load variables and fetch names
        val sharedPreferences = getSharedPreferences("ConnectionSettings", MODE_PRIVATE)
        val controllerDomain = sharedPreferences.getString("TODO", "https://unifi.local").toString()
        val site = sharedPreferences.getString("TODO", "site").toString()
        val apiUsername = sharedPreferences.getString("TODO", "api").toString()
        val apiPassword = sharedPreferences.getString("TODO", "password").toString()

        initUnifiApiService(controllerDomain)
        println("Main Activity Started")
        lifecycleScope.launch(Dispatchers.IO) {
            val names: List<String>
            // if cookie is not valid try login
            if (!cookieIsValid(site)) {
                apiLogin(apiUsername, apiPassword)
            }
            // if now valid fetch data else return error message
            if (cookieIsValid(site)) {
                names = fetchNames(site)
            } else {
                names = listOf("No valid Cookies", "Hello")
            }
            withContext(Dispatchers.Main) {
                namesAdapter.updateData(names)
            }

        }

    }

    private fun initUnifiApiService(controllerDomain: String) {
        val client = OkHttpClient.Builder()
            // Remove when ssl is available BEGIN:
            .apply {
                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                    override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {}

                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                        return arrayOf()
                    }
                })
                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                val sslSocketFactory = sslContext.socketFactory

                sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)

                // Disable hostname verification
                hostnameVerifier(HostnameVerifier { _, _ -> true })
            } // :END
            .cookieJar(CookieManager())
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(controllerDomain)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        unifiApiService = retrofit.create(UnifiApiService::class.java)
    }

    private suspend fun cookieIsValid(site: String): Boolean {
        return try {
            val response = unifiApiService.getStatus(site).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("GET_STATUS_ERROR", "Get status failed with error: $e")
            false
        }
    }

    private suspend fun apiLogin(username: String, password: String): Boolean {
        return try {
            val credentials = Credentials(username, password)
            val response = unifiApiService.login(credentials).execute()
            response.isSuccessful
        } catch (e: Exception) {
            Log.e("LOGIN_ERROR", "Login failed with error: $e")
            false
        }
    }

    private suspend fun fetchNames(site: String): List<String> {
        delay(3000)
        return listOf("Tom", "Jerry")
    }

}


class NamesAdapter(private var names: List<String>) : RecyclerView.Adapter<NamesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameTextView.text = names[position]
    }

    override fun getItemCount() = names.size

    fun updateData(newNames: List<String>) {
        names = newNames
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(android.R.id.text1)
    }
}