package com.example.deeplinknext.model

import com.google.gson.annotations.SerializedName



data class BaseStatus (
    var code: String,
    var message: String
)

data class OauthResult(
    var accessToken: String?,
    var expireAt: Long?,
    var expireIn: Int?
)

data class OauthResponse(
    var status: BaseStatus,
    var result: OauthResult
)

class OauthRequest(
    var apiKey:String,
    var apiSecret:String
)

data class PaymentDeeplinkRequest(
    var reference1: String,
    var reference2: String,
    var reference3: String?,
    var amount: String,
    var shopNameEn: String,
    var shopNameTh: String,
    var fee: String,
    var extraData: String?,
    var partnerToken: String,
    var returnUrl: String?,
    var productName: String?,
    var quantity: String?,
    var compcode: String?
)


data class PaymentResult(
    var deeplinkUrl: String?,
    var expireAt: Long?,
    var expireIn: Int?,
    var orderId: String?
)

data class PaymentResponse(
    var status:BaseStatus,
    var result:PaymentResult
)
