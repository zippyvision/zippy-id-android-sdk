package com.zippyid.zippydroid.wizard;

import android.os.Bundle
import android.support.v4.app.Fragment;
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.android.volley.VolleyError
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.network.AsyncResponse
import com.zippyid.zippydroid.network.model.Country
import com.zippyid.zippydroid.network.model.DocumentType
import kotlinx.android.synthetic.main.fragment_id_vertification.*

class IDVertificationFragment : Fragment() {

    private val apiClient = ApiClient(Zippy.secret, Zippy.key, Zippy.host)

    lateinit var countries: List<Country>
    lateinit var selectedCountry: Country
    lateinit var selectedDocumentType: DocumentType

    private var executeOnCountrySelected = true
    private var executeOnDocumentTypeSelected = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_id_vertification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let {
            apiClient.getCountries(it, object : AsyncResponse<List<Country>> {
                override fun onSuccess(response: List<Country>) {
                    countries = response
                    selectedCountry = response.first()
                    selectedDocumentType = selectedCountry.documentTypes!!.first()
                    setupCountrySpinner()
                    setupDocumentSpinner()
                }
                override fun onError(error: VolleyError) {
                    Log.e("error", error.toString())
                }
            })
        }

        continueBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onIDVertificationNextStep(selectedDocumentType)
        }
    }

    private fun setupCountrySpinner() {
        // TODO: fix
        if (executeOnCountrySelected) {
            var availableCountries = countries.filter { country -> country.documentTypes!!.contains(selectedDocumentType) }
            countrySpinner.adapter = ArrayAdapter(activity, R.layout.support_simple_spinner_dropdown_item, availableCountries.map { it.label })
            countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        executeOnCountrySelected = false
                        selectedCountry = availableCountries[position]
                        setupDocumentSpinner()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        } else {
            executeOnCountrySelected = true
            executeOnDocumentTypeSelected = true
        }
    }

    private fun setupDocumentSpinner() {
        // TODO: fix
        if (executeOnDocumentTypeSelected) {
            var availableDocumentTypes = selectedCountry.documentTypes
            documentSpinner.adapter = ArrayAdapter(activity, R.layout.support_simple_spinner_dropdown_item, availableDocumentTypes!!.map { it.label })
            documentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    executeOnDocumentTypeSelected = false
                    selectedDocumentType = availableDocumentTypes!![position]
                    setupCountrySpinner()
                }
                override fun onNothingSelected(parent: AdapterView<*>?) { }
            }
        } else {
            executeOnDocumentTypeSelected = true
            executeOnCountrySelected = true
        }
    }
}