package xyz.juncat.bottompanel

import android.os.Bundle
import com.jianjun.base.compontent.BaseActivity

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val layout: BottomPanelLayout = findViewById(R.id.panel_layout)
        layout.dimClickedCallback = {
            layout.startCloseAnimate()
        }
        layout.penalCloseCallback = {
            finish()
        }
        layout.showPanelWithAnimation()
    }
}