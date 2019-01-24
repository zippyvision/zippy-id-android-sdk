package com.zippyid.zippydroid.wizard;

import android.os.Bundle
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.zippyid.zippydroid.Zippy
import com.zippyid.zippydroid.network.ApiClient
import com.zippyid.zippydroid.R
import com.zippyid.zippydroid.ZippyActivity
import kotlinx.android.synthetic.main.fragment_id_vertification.*

class IDVertificationFragment : Fragment() {

    private val apiClient = ApiClient(Zippy.secret, Zippy.key, Zippy.host)

    var countries = arrayOf("Latvia", "Lithuania", "Estonia", "Russia")
    var documentTypes = arrayOf("id card", "passport", "drivers licence")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_id_vertification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        context?.let { apiClient.getCountries(it) }

        setupCountrySpinner()
        setupDocumentSpinner()

        continueBtn.setOnClickListener {
            (activity as? ZippyActivity)?.switchToWizard()
        }
    }

    private fun setupCountrySpinner() {
        countrySpinner.adapter = ArrayAdapter(activity, R.layout.support_simple_spinner_dropdown_item, countries)
        countrySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun setupDocumentSpinner() {
        documentSpinner.adapter = ArrayAdapter(activity, R.layout.support_simple_spinner_dropdown_item, documentTypes)
        documentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
}