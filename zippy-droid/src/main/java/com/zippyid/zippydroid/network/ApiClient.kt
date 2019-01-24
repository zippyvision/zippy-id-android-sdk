package com.zippyid.zippydroid.network

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.zippyid.zippydroid.network.model.AuthToken
import com.zippyid.zippydroid.network.model.SuccessResponse
import com.zippyid.zippydroid.network.model.Country


class ApiClient(private val secret: String, private val key: String, private val baseUrl: String) {
    companion object {
        private const val TAG = "ApiClient"
        private const val REQUEST_TOKEN = "request_tokens"
        private const val VERIFICATION = "verifications"
        private const val RESULT = "result"
        private lateinit var token: String
    }

    private val gson = GsonBuilder().create()

    fun getToken(context: Context, asyncResponse: AsyncResponse<String>) {
        val queue = Volley.newRequestQueue(context)

        val request = object : StringRequest(Request.Method.POST, baseUrl + "/v1/" + REQUEST_TOKEN,
            Response.Listener<String> {
                val authToken = gson.fromJson<AuthToken>(it, AuthToken::class.java)
                token = authToken.token
                Log.e(TAG, "Got token: $token")
                asyncResponse.onSuccess(it)
                Log.d(TAG, it)
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

    fun sendImages(context: Context, documentType: String, encodedFaceImage: String, encodedDocumentFront: String, encodedDocumentBack: String, customerUid: String, asyncResponse: AsyncResponse<SuccessResponse?>) {
        val queue = Volley.newRequestQueue(context)

        Log.e(TAG, "Trying to send images!")

        val request = object : StringRequest(Request.Method.POST, baseUrl + "/v1/" + VERIFICATION,
            Response.Listener<String> {
                Log.e(TAG, "Wow! Done!")
                Log.d(TAG, it)
                getResult(context, customerUid, asyncResponse)
            }, Response.ErrorListener {
                Log.e(TAG, "Error sending images!")
            }) {
            override fun getParams(): MutableMap<String, String> {
                Log.e(TAG, "Token: $token")
                val params = HashMap<String, String>()
                params["token"] = token
                params["document_country"] = "lv"
                params["document_type"] = documentType
                params["image_data[selfie]"] = "data:image/png;base64,$encodedFaceImage"
                params["image_data[idFront]"] = "data:image/png;base64,$encodedDocumentFront"
                params["image_data[idBack]"] = "data:image/png;base64,$encodedDocumentBack"
                params["customer_uid"] = customerUid
                return params
            }
        }
        queue.add(request)
    }

    fun getResult(context: Context, customerUid: String, asyncResponse: AsyncResponse<SuccessResponse?>) {
        val queue = Volley.newRequestQueue(context)

        val uri = "$baseUrl&/v1/$RESULT?customer_uid=$customerUid&secret_key=$secret&api_key=$key"

        val request = StringRequest(Request.Method.GET, uri,
            Response.Listener<String> {
                val listType = object : TypeToken<List<SuccessResponse>>() {}.type
                val successResponseList = gson.fromJson<List<SuccessResponse>>(it, listType)
                Log.e(TAG, "Wow! Result!")
                Log.d(TAG, it)
                asyncResponse.onSuccess(successResponseList.firstOrNull())
            }, Response.ErrorListener {
                Log.e(TAG, "Error getting result!")
                asyncResponse.onError(it)
            })

        queue.add(request)
    }

    fun getCountries(context: Context) {
        val queue = Volley.newRequestQueue(context)
        val uri = "$baseUrl/sdk/countries"

        val request = StringRequest(Request.Method.GET, uri,
            Response.Listener<String> {
                val listType = object : TypeToken<List<Country>>() {}.type
                val countries = gson.fromJson<List<Country>>(it, listType)
                Log.e(TAG, uri)
                Log.e(TAG, countries.toString())
                Log.e(TAG, "Wow! Result!")
            }, Response.ErrorListener {
                Log.e(TAG, uri)
                Log.e(TAG, "Error getting result!")
            })

        queue.add(request)
    }
}

