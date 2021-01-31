package com.example.trelloclone.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.example.trelloclone.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        btn_sign_up_intro_activity.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            //we are not finish this intro activity because later we might need this intro activity
        }
        btn_sign_in_intro_activity.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            //we are not finish this intro activity because later we might need this intro activity
        }
    }
}
