package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myapplication.models.OnboardingItem

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var nextButton: Button

    private val onboardingItems = listOf(
        OnboardingItem(R.raw.cars, "Gerencie seu veículo com facilidade", "Acompanhe manutenções, abastecimentos e despesas em um só lugar.\n"),
        OnboardingItem(R.raw.manage, "Nunca perca uma data importante", "Receba lembretes de revisões, troca de óleo e vencimento de documentos.\n" +
                "\n"),
        OnboardingItem(R.raw.money, "Controle seus gastos", "Monitore seus custos com combustível, manutenção e muito mais.\n" +
                "\n")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        nextButton = findViewById(R.id.buttonNext)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = onboardingItems.size
            override fun createFragment(position: Int): Fragment {
                return OnboardingFragment.newInstance(onboardingItems[position])
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val rootLayout = findViewById<View>(R.id.main)
                when (position) {
                    0 -> rootLayout.setBackgroundResource(R.color.white)
                    1 -> rootLayout.setBackgroundResource(R.color.yellow)
                    2 -> rootLayout.setBackgroundResource(R.color.purpleDark)
                }
            }
        })

        nextButton.setOnClickListener {
            if (viewPager.currentItem + 1 < onboardingItems.size) {
                viewPager.currentItem += 1
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }
    }
}
