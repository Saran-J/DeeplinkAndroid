package com.example.deeplinknext

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.deeplinknext.constant.baseUrlOauth
import com.example.deeplinknext.model.OauthRequest
import com.example.deeplinknext.model.OauthResponse
import com.example.deeplinknext.model.PaymentDeeplinkRequest
import com.example.deeplinknext.model.PaymentResponse
import com.example.deeplinknext.service.EncryptService
import com.example.deeplinknext.service.OauthService
import com.example.deeplinknext.service.PaymentService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class MainActivity : AppCompatActivity() {

    var disposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View) {


        createPaymentChain()

    }

    fun createPaymentChain() {
        val request = OauthRequest("4b4de5ae15","30357229d5afce5f82b493166e324b44")
        val observable = Observable.just(request)
            .flatMap { request -> getAccessToken(request) }
            .map { response -> prepareDeeplinkRequest(response) }
            .flatMap { (request,token) ->  getDeeplink(request,token)}

        disposable = observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { result ->
                    handleDeeplinkResponse(result)
                },
                { error -> Log.d("error",error.toString()) }
            )


    }

    fun getAccessToken (request: OauthRequest): Observable<OauthResponse> {
        val service by lazy {
            OauthService.create()
        }
        return service.getToken(request)

    }

    fun getDeeplink(request: PaymentDeeplinkRequest, accessToken: String): Observable<PaymentResponse> {
        val service by lazy {
            PaymentService.create(accessToken)
        }
        return service.getDeeplink(request)
    }

    fun prepareDeeplinkRequest(oauthResponse: OauthResponse): Pair<PaymentDeeplinkRequest,String> {
        Log.d("response token",oauthResponse.result.accessToken)
        var encryptService = EncryptService()
        val ref1 = "1234"
        var time = System.currentTimeMillis()
        val partnerToken = encryptService.getPartnerToken(ref1,(time / 1000).toString().split(".")[0], "1111111111111", "SB10Y")
        val deeplinkRequest = PaymentDeeplinkRequest(
            "1234",
            "2134",
            "432423",
            "100",
            "ABC",
            "abc",
            "10",
            null,
            partnerToken,
            "www.google.co.th",
            "pantabut",
            "",
            ""
        )

        var accessToken = oauthResponse.result.accessToken ?: ""
        return Pair(deeplinkRequest,accessToken)

    }

    fun handleDeeplinkResponse(response: PaymentResponse) {
        Log.d("response deeplink",response.result.deeplinkUrl)
        val myAction:Uri = Uri.parse(response.result.deeplinkUrl)
        val intent = Intent()

        intent?.let {
            it.setAction(Intent.ACTION_VIEW)
            it.setData(myAction)
            startActivityForResult(it,1)
            return
        }
    }
}
