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

    lateinit var countryAdapter: ArrayAdapter<String>
    lateinit var documentAdapter: ArrayAdapter<String>

    private lateinit var countrySpinnerOnItemSelectedListener: AdapterView.OnItemSelectedListener
    private lateinit var documentTypeSpinnerOnItemSelectedListener: AdapterView.OnItemSelectedListener

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_id_vertification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countryAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item)
        documentAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_dropdown_item)

        countrySpinner.adapter = countryAdapter
        documentSpinner.adapter = documentAdapter

        context?.let {
            apiClient.getCountries(it, object : AsyncResponse<List<Country>> {
                override fun onSuccess(response: List<Country>) {
                    countries = response
                    selectedCountry = response.first()
                    selectedDocumentType = selectedCountry.documentTypes!!.first()

                    countryAdapter.addAll(countries.map { it.label })
                    documentAdapter.addAll(selectedCountry.documentTypes!!.map { it.label })

                    countrySpinner.onItemSelectedListener = countrySpinnerOnItemSelectedListener
                    documentSpinner.onItemSelectedListener = documentTypeSpinnerOnItemSelectedListener
                }
                override fun onError(error: VolleyError) {
                    Log.e("error", error.toString())
                }
            })
        }

        continueBtn.setOnClickListener {
            (activity as? ZippyActivity)?.onIDVertificationNextStep(selectedDocumentType)
        }

        countrySpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCountry = countries[position]
                documentSpinner.setSelection(0, false)
                selectedDocumentType = selectedCountry.documentTypes!!.first()
                documentAdapter.clear()
                documentAdapter.addAll(selectedCountry.documentTypes!!.map { it.label })
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("country spinner", "nothing selected")
            }
        }

        documentTypeSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDocumentType = selectedCountry.documentTypes!![position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("document spinner", "nothing selected")
            }
        }
    }
}


