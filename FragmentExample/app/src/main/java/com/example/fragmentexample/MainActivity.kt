package com.example.fragmentexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonFragment1 = findViewById<Button>(R.id.buttonFragment1)
        val buttonFragment2 = findViewById<Button>(R.id.buttonFragment2)
        val fragmentContainer = findViewById<FrameLayout>(R.id.fragmentContainer)

        // Kiểm tra xem các view có tồn tại không
        if (buttonFragment1 == null || buttonFragment2 == null || fragmentContainer == null) {
            // Xử lý lỗi nếu view không tồn tại, ví dụ: log lỗi hoặc hiển thị thông báo
            println("Error: One or more views not found!")
            return
        }

        // Hiển thị Fragment 1 khi ứng dụng khởi động
        if (savedInstanceState == null) {
            loadFragment(Fragment1())
        }

        buttonFragment1.setOnClickListener {
            loadFragment(Fragment1())
        }

        buttonFragment2.setOnClickListener {
            loadFragment(Fragment2())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val fragmentManager: FragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
}