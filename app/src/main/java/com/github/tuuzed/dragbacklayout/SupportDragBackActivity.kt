package com.github.tuuzed.dragbacklayout

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SupportDragBackActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_supportdragback)
        findViewById<View>(R.id.btn_tryClick).setOnClickListener {
            Toast.makeText(this, "Click!!!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        DragBackLayout(this).attachToActivity(this, object : DragBackLayout.Callback() {
            override fun onBack() {
                finish()
                overridePendingTransition(0, 0)
            }
        })
    }
}