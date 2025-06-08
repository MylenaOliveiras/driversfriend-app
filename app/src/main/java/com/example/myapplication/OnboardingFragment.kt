package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.myapplication.models.OnboardingItem

class OnboardingFragment : Fragment() {

    private var animationResId: Int = 0
    private var title: String = ""
    private var description: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            animationResId = it.getInt(ARG_ANIMATION)
            title = it.getString(ARG_TITLE).orEmpty()
            description = it.getString(ARG_DESC).orEmpty()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding, container, false)


        view.findViewById<LottieAnimationView>(R.id.lottieAnimation).setAnimation(animationResId)
        view.findViewById<TextView>(R.id.textTitle).text = title
        view.findViewById<TextView>(R.id.textDescription).text = description
        
        return view
    }

    companion object {
        private const val ARG_ANIMATION = "animationResId"
        private const val ARG_TITLE = "title"
        private const val ARG_DESC = "description"

        fun newInstance(item: OnboardingItem): OnboardingFragment {
            val fragment = OnboardingFragment()
            val args = Bundle()
            args.putInt(ARG_ANIMATION, item.animationResId)
            args.putString(ARG_TITLE, item.title)
            args.putString(ARG_DESC, item.description)
            fragment.arguments = args
            return fragment
        }
    }
}
