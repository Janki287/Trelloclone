package com.example.trelloclone.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import com.example.trelloclone.R
import com.example.trelloclone.firestore.FireStoreClass
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN)
        //this will set the full screen OR the bar(containing time,sim cards,signals) will be gone

        val typeFace : Typeface = Typeface.createFromAsset(assets,"carbon bl.ttf")
        tv_splash_text.typeface = typeFace
        //this will set the font style to CARBON BL

        Handler().postDelayed({
            val currentUserUID = FireStoreClass().getCurrentUserID()
            if(currentUserUID.isNotEmpty()){
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        },3000)
        //this will open the intro activity after 3 sec OR splash screen will be remained for 3 sec
    }
}
