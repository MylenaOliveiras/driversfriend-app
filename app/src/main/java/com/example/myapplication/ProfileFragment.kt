// ProfileFragment.kt (SEM AuthManager)

package com.example.myapplication

import android.content.Context

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {

    private lateinit var textViewUserName: TextView
    private lateinit var textViewUserEmail: TextView
    private lateinit var buttonLogout: Button

    private lateinit var headerSupport: LinearLayout
    private lateinit var headerPrivacyPolicy: LinearLayout
    private lateinit var headerTermsOfUse: LinearLayout

    private lateinit var contentSupport: LinearLayout
    private lateinit var contentPrivacyPolicy: LinearLayout
    private lateinit var contentTermsOfUse: LinearLayout

    private lateinit var arrowSupport: ImageView
    private lateinit var arrowPrivacyPolicy: ImageView
    private lateinit var arrowTermsOfUse: ImageView

    private val USER_PREFS = "user_prefs"
    private val KEY_USER_NAME = "user_name"
    private val KEY_USER_EMAIL = "user_email"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        textViewUserName = view.findViewById(R.id.textViewUserName)
        textViewUserEmail = view.findViewById(R.id.textViewUserEmail)

        buttonLogout = view.findViewById(R.id.buttonLogout)

        headerSupport = view.findViewById(R.id.headerSupport)
        headerPrivacyPolicy = view.findViewById(R.id.headerPrivacyPolicy)
        headerTermsOfUse = view.findViewById(R.id.headerTermsOfUse)

        contentSupport = view.findViewById(R.id.contentSupport)
        contentPrivacyPolicy = view.findViewById(R.id.contentPrivacyPolicy)
        contentTermsOfUse = view.findViewById(R.id.contentTermsOfUse)

        arrowSupport = view.findViewById(R.id.arrowSupport)
        arrowPrivacyPolicy = view.findViewById(R.id.arrowPrivacyPolicy)
        arrowTermsOfUse = view.findViewById(R.id.arrowTermsOfUse)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadUserData()

        headerSupport.setOnClickListener {
            toggleVisibility(contentSupport, arrowSupport)
        }

        headerPrivacyPolicy.setOnClickListener {
            toggleVisibility(contentPrivacyPolicy, arrowPrivacyPolicy)
        }

        headerTermsOfUse.setOnClickListener {
            toggleVisibility(contentTermsOfUse, arrowTermsOfUse)
        }

        buttonLogout.setOnClickListener {
            performLogout()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }


    private fun loadUserData() {
        val sharedPrefs = requireContext().getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
        val userName = sharedPrefs.getString(KEY_USER_NAME, null)
        val userEmail = sharedPrefs.getString(KEY_USER_EMAIL, null)

        if (!userName.isNullOrEmpty() && !userEmail.isNullOrEmpty()) {
            textViewUserName.text = "Nome: $userName"
            textViewUserEmail.text = "Email: $userEmail"
            Log.d("ProfileFragment", "Dados do usuário carregados do SharedPreferences: Nome='$userName', Email='$userEmail'")
        } else {
            textViewUserName.text = "Nome: Não disponível"
            textViewUserEmail.text = "Email: Não disponível"
            Toast.makeText(
                requireContext(),
                "Sessão expirada ou dados de usuário não encontrados. Por favor, faça login novamente.",
                Toast.LENGTH_LONG
            ).show()
            Log.w("ProfileFragment", "Dados de usuário ausentes no SharedPreferences. Forçando logout.")
            requireActivity().finishAffinity()
        }
    }



    private fun performLogout() {
        val sharedPrefs = requireContext().getSharedPreferences(USER_PREFS, Context.MODE_PRIVATE)
        sharedPrefs.edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .apply()
        Toast.makeText(requireContext(), "Você foi desconectado. O aplicativo será fechado.", Toast.LENGTH_SHORT).show()
        Log.i("ProfileFragment", "Dados do usuário removidos do SharedPreferences. Finalizando aplicativo.")

        requireActivity().finishAffinity()
    }


    private fun toggleVisibility(contentLayout: LinearLayout, arrowIcon: ImageView) {
        if (contentLayout.visibility == View.GONE) {
            contentLayout.visibility = View.VISIBLE
            arrowIcon.animate().rotation(180f).setDuration(200).start()
        } else {
            contentLayout.visibility = View.GONE
            arrowIcon.animate().rotation(0f).setDuration(200).start()
        }
    }
}