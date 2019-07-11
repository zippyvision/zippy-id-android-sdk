package com.zippyid.zippydroid.wizard

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.lifecycle.ViewModelProviders
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import com.zippyid.zippydroid.extension.observeLiveData
import com.zippyid.zippydroid.network.model.Country
import com.zippyid.zippydroid.network.model.DocumentType
import com.zippyid.zippydroid.viewModel.ZippyViewModel
import com.zippyid.zippydroid.viewModel.ZippyViewModelFactory
import kotlinx.android.synthetic.main.fragment_id_vertification.*

class IDVerificationFragment : Fragment() {
    private lateinit var viewModelFactory: ZippyViewModelFactory
    private lateinit var viewModel: ZippyViewModel

    var countries: List<Country> = ArrayList()
    var selectedCountry: Country? = null
    var selectedDocumentType: DocumentType? = null

    private lateinit var countryAdapter: ArrayAdapter<String>
    private lateinit var documentAdapter: ArrayAdapter<String>

    private var countrySpinnerOnItemSelectedListener: AdapterView.OnItemSelectedListener? = null
    private var documentTypeSpinnerOnItemSelectedListener: AdapterView.OnItemSelectedListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_id_vertification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        countryAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item)
        documentAdapter = ArrayAdapter(context!!, android.R.layout.simple_spinner_dropdown_item)

        countrySpinner.adapter = countryAdapter
        documentSpinner.adapter = documentAdapter

        viewModelFactory = ZippyViewModelFactory(context!!, (activity as ZippyActivity).getConfig())
        viewModel = ViewModelProviders.of((activity as ZippyActivity), viewModelFactory).get(ZippyViewModel::class.java)

        observeLiveData()
        viewModel.getCountries()

        countrySpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCountry = countries[position]
                documentSpinner.setSelection(0, false)
                selectedDocumentType = selectedCountry?.documentTypes!!.first()
                documentAdapter.clear()
                documentAdapter.addAll(selectedCountry?.documentTypes!!.map { it.label })
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("country spinner", "nothing selected")
            }
        }

        documentTypeSpinnerOnItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedDocumentType = selectedCountry?.documentTypes!![position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("document spinner", "nothing selected")
            }
        }

        continueBtn.setOnClickListener {
            selectedDocumentType?.apply {
                (activity as ZippyActivity).getConfig().documentType = this
            }
            (activity as? ZippyActivity)?.toWizardFragment()
        }
    }

    private fun observeLiveData() {
        viewLifecycleOwner.observeLiveData(viewModel.countriesLiveData) { loadedCountries ->
            countries = loadedCountries
            selectedCountry = loadedCountries.first()
            selectedDocumentType = selectedCountry?.documentTypes!!.first()

            countryAdapter.addAll(countries.map { it.label })
            documentAdapter.addAll(selectedCountry?.documentTypes!!.map { it.label })

            countrySpinner.onItemSelectedListener = countrySpinnerOnItemSelectedListener
            documentSpinner.onItemSelectedListener = documentTypeSpinnerOnItemSelectedListener
        }
        viewLifecycleOwner.observeLiveData(viewModel.volleyErrorLiveData) {
            Log.e("error", it.toString())
        }
    }
}