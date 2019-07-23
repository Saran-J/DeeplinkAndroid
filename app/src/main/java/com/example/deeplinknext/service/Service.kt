package com.example.deeplinknext.service

import android.util.Base64
import android.util.Log
import com.example.deeplinknext.constant.baseUrlOauth
import com.example.deeplinknext.constant.baseUrlOauthLocal
import com.example.deeplinknext.constant.baseUrlPaymentLocal
import com.example.deeplinknext.model.OauthRequest
import com.example.deeplinknext.model.OauthResponse
import com.example.deeplinknext.model.PaymentDeeplinkRequest
import com.example.deeplinknext.model.PaymentResponse
import io.reactivex.Observable
import okhttp3.Interceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import javax.xml.datatype.DatatypeConstants.SECONDS




interface OauthService {

    companion object {
        fun create(): OauthService {
            val builder = OkHttpClient().newBuilder()
            builder.readTimeout(10, TimeUnit.SECONDS)
            builder.connectTimeout(5, TimeUnit.SECONDS)

            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder().addHeader("Content-Type", "application/json")
                    .addHeader("X-Request-ID", "D7BEF5E871C3FD8EE5EDB85998566").build()
                chain.proceed(request)
            }
            val client = builder.build()


            val retrofit = Retrofit.Builder()
                .addConverterFactory(
                    GsonConverterFactory.create())
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
                .client(client)
                .baseUrl(baseUrlOauthLocal)
                .build()

            return retrofit.create(OauthService::class.java)
        }
    }

    @POST("/v1/oauth/token")
    fun getToken(
        @Body request: OauthRequest
    ):Observable<OauthResponse>

}

interface PaymentService {

    companion object {
        fun create(token: String): PaymentService {
            val builder = OkHttpClient().newBuilder()
            builder.readTimeout(10, TimeUnit.SECONDS)
            builder.connectTimeout(5, TimeUnit.SECONDS)

            builder.addInterceptor { chain ->
                val request = chain.request().newBuilder().addHeader("Content-Type", "application/json")
                    .addHeader("X-Request-ID", "D7BEF5E871C3FD8EE5EDB85998566")
                    .addHeader("Authorization","Bearer "+token).build()
                chain.proceed(request)
            }

            builder.addInterceptor(
                HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC).setLevel
                (HttpLoggingInterceptor.Level.BODY).setLevel(HttpLoggingInterceptor.Level.HEADERS))
            val client = builder.build()
            val retrofit = Retrofit.Builder()
                .addConverterFactory(
                    GsonConverterFactory.create())
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create())
                .client(client)
                .baseUrl(baseUrlPaymentLocal)
                .build()

            return retrofit.create(PaymentService::class.java)
        }
    }

    @POST("/v1/payment/deeplink")
    fun getDeeplink(
        @Body request: PaymentDeeplinkRequest
    ):Observable<PaymentResponse>
}

class EncryptService() {
    fun getPartnerToken(ref1: String, time: Long): String {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val partnerObject = padString("{\"reference1\": \"$ref1\",\"payment_init_time\": \"$time\"}")
        val secretKeySpec = SecretKeySpec("6EC77FB50566B4DB6EC77FB50566B4DB".toByteArray(Charsets.UTF_8), "AES")
        val ivSpec = IvParameterSpec("A4628E5F5CC98BF5".toByteArray(Charsets.UTF_8))
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec)
        val encryptVal = cipher.doFinal(partnerObject.toByteArray())
        return Base64.encodeToString(encryptVal,Base64.DEFAULT)
    }

    private fun padString(source: String): String {
        var source = source
        val paddingChar = ' '
        val size = 16
        val x = source.length % size
        val padLength = size - x

        for (i in 0 until padLength) {
            source += paddingChar
        }
        return source
    }
}

