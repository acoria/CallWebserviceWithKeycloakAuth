package com.example.callwebservicewithkeycloakauth

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.GsonBuilder
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    lateinit var retrofit: Retrofit
    private val compositeDisposable = CompositeDisposable()
    lateinit var responseText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRetrofitInstance()
        findViewById<Button>(R.id.ui_btn_private_access).setOnClickListener { callPrivateWebservice() }
        findViewById<Button>(R.id.ui_btn_public_access).setOnClickListener { callPublicWebservice() }
        findViewById<Button>(R.id.ui_btn_clear).setOnClickListener { findViewById<TextView>(R.id.txt_response).text = "" }
        responseText = findViewById(R.id.txt_response)
    }
    private fun setupRetrofitInstance() {

        val authToken = "toBeAdded"
        val httpClient = createHttpClient(authToken)

        val gson = GsonBuilder()
            .setLenient()
            .create()

        retrofit = Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8082/api/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(httpClient)
            .build()
    }

    private fun createHttpClient(authToken: String): OkHttpClient? {
        //Create a new interceptor
        val headerAuthorizationInterceptor = Interceptor {
            val newRequest = it.request().newBuilder()
                .addHeader("Authorization", "Bearer $authToken")
                .build()
            it.proceed(newRequest)
        }
        //Add the interceptor to the client builder
        return OkHttpClient.Builder()
            .addInterceptor(headerAuthorizationInterceptor)
            .build()
    }

    private fun callPrivateWebservice() {
        val webservice = retrofit.create(Webservice::class.java)
        compositeDisposable.add(webservice.requestPrivateAccess()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { responseText.text = it.text },
                onError = {responseText.text = it.message}
            ))
    }
    private fun callPublicWebservice() {
        val webservice = retrofit.create(Webservice::class.java)
        compositeDisposable.add(webservice.requestPublicAccess()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { responseText.text = it.text },
                onError = { responseText.text = it.message }
            ))
    }

    override fun onDestroy() {
        if(!compositeDisposable.isDisposed){
            compositeDisposable.dispose()
        }
        super.onDestroy()
    }
}
