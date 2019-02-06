package com.zippyid.zippydroid.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zippyid.zippydroid.network.model.AuthToken
import com.zippyid.zippydroid.network.model.ZippyResponse
import com.zippyid.zippydroid.network.model.Country


class ApiClient(private val secret: String, private val key: String, private val baseUrl: String, context: Context) {
    companion object {
        private const val TAG = "ApiClient"
        private const val REQUEST_TOKEN = "request_tokens"
        private const val VERIFICATION = "verifications"
        private const val RESULT = "result"
        private lateinit var token: String
    }

    private val gson = GsonBuilder().create()
    private val queue: RequestQueue = Volley.newRequestQueue(context)


    fun getToken(asyncResponse: AsyncResponse<String>) {
        val request = object : StringRequest(Request.Method.POST, "$baseUrl/v1/$REQUEST_TOKEN",
            Response.Listener<String> {
                val authToken = gson.fromJson<AuthToken>(it, AuthToken::class.java)
                token = authToken.token
                asyncResponse.onSuccess(it)
            }, Response.ErrorListener {
                asyncResponse.onError(it)
                Log.e(TAG, "Error getting token!")
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["api_key"] = key
                params["secret_key"] = secret
                return params
            }
        }

        queue.add(request)
    }

    fun sendImages(documentType: String, encodedFaceImage: String, encodedDocumentFront: String, encodedDocumentBack: String?, customerUid: String, asyncResponse: AsyncResponse<Any?>) {
        Log.d(TAG, "Trying to send images!")

        val request = object : StringRequest(Request.Method.POST, "$baseUrl/v1/$VERIFICATION",
            Response.Listener<String> {
                Log.d(TAG, "Successfully sending images!")
                asyncResponse.onSuccess(null)

            }, Response.ErrorListener {
                Log.e(TAG, "Error sending images!")
            }) {
            override fun getParams(): MutableMap<String, String> {
                Log.i(TAG, "Token: $token")
                val params = HashMap<String, String>()
                params["token"] = token
                params["document_country"] = "lv"
                params["document_type"] = documentType
                params["image_data[selfie]"] = encodedFaceImage
                params["image_data[idFront]"] = encodedDocumentFront
                params["image_data[idBack]"] = encodedDocumentBack ?: ""
                params["customer_uid"] = customerUid
                return params
            }
        }
        queue.add(request)
    }

    fun getResult(customerUid: String, asyncResponse: AsyncResponse<ZippyResponse?>) {
        val uri = "$baseUrl/v1/$RESULT?customer_uid=$customerUid&secret_key=$secret&api_key=$key"

        val request = StringRequest(Request.Method.GET, uri,
            Response.Listener<String> {
                val listType = object : TypeToken<List<ZippyResponse>>() {}.type
                val successResponseList = gson.fromJson<List<ZippyResponse>>(it, listType)
                asyncResponse.onSuccess(successResponseList.firstOrNull())
            }, Response.ErrorListener {
                Log.e(TAG, "Error getting result!")
                asyncResponse.onError(it)
            })

        queue.add(request)
    }

    fun getCountries(asyncResponse: AsyncResponse<List<Country>>) {
        val uri = "$baseUrl/sdk/countries"

        val request = StringRequest(Request.Method.GET, uri,
            Response.Listener<String> {
                val listType = object : TypeToken<List<Country>>() {}.type
                val countries = gson.fromJson<List<Country>>(it, listType)
                asyncResponse.onSuccess(countries)
            }, Response.ErrorListener {
                Log.e(TAG, "Error getting result!")
                asyncResponse.onError(it)
            })

        queue.add(request)
    }
}

