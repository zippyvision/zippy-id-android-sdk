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


class ApiClient(private val apiKey: String, private val baseUrl: String, context: Context) {
    companion object {
        private const val TAG = "ApiClient"
        private const val REQUEST_TOKEN = "request_tokens"
        private const val VERIFICATIONS = "verifications"
        private const val VERIFICATION = "verification"
        private const val RESULT = "result"
        private lateinit var requestToken: String
    }

    private val gson = GsonBuilder().create()
    private val queue: RequestQueue = Volley.newRequestQueue(context)

    fun getToken(asyncResponse: AsyncResponse<String>) {
        val request = object : StringRequest(
            Method.POST, "$baseUrl/v1/$REQUEST_TOKEN",
            Response.Listener<String> {
                val authToken = gson.fromJson<AuthToken>(it, AuthToken::class.java)
                requestToken = authToken.token
                asyncResponse.onSuccess(it)
            }, Response.ErrorListener {
                asyncResponse.onError(it)
                Log.e(TAG, "Error getting token!")
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap(super.getHeaders())
                params["Authorization"] = "Token token=$apiKey"
                return params
            }
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["token"] = apiKey
                return params
            }
        }

        queue.add(request)
    }

    fun sendImages(documentType: String, encodedFaceImage: String, encodedDocumentFront: String, encodedDocumentBack: String?, customerUid: String, asyncResponse: AsyncResponse<Any?>) {
        Log.d(TAG, "Trying to send images!")

        val request = object : StringRequest(
            Method.POST, "$baseUrl/v1/$VERIFICATIONS",
            Response.Listener<String> {
                Log.d(TAG, "Successfully sending images!")
                asyncResponse.onSuccess(null)

            }, Response.ErrorListener {
                Log.e(TAG, "Error sending images!")
            }) {
            override fun getParams(): MutableMap<String, String> {
                Log.i(TAG, "Token: $requestToken")
                val params = HashMap<String, String>()
                params["token"] = requestToken
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
        val uri = "$baseUrl/v1/$VERIFICATION?customer_uid=$customerUid"

        val request = object : StringRequest(
            Method.GET, uri,
            Response.Listener<String> {
                val listType = object : TypeToken<List<ZippyResponse>>() {}.type
                val successResponseList = gson.fromJson<List<ZippyResponse>>(it, listType)
                asyncResponse.onSuccess(successResponseList.firstOrNull())
            }, Response.ErrorListener {
                Log.e(TAG, "Error getting result!")
                asyncResponse.onError(it)
            }) {
            override fun getHeaders(): MutableMap<String, String> {
                val params = HashMap(super.getHeaders())
                params["Authorization"] = "Token token=$apiKey"
                return params
            }
        }

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
                Log.e(TAG, "Error getting countries!")
                asyncResponse.onError(it)
            })

        queue.add(request)
    }
}

