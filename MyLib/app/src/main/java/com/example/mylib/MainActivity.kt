package com.example.mylib

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.drawerlayout.widget.DrawerLayout
import com.example.mylib.ui.theme.MyLibTheme
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_screen)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, BlankFragment())
                .commit()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView2, MenuBar())
                .commit()
        }

        val prev:Button = findViewById(R.id.button)
        val next:Button = findViewById(R.id.button2)
        val menuButton: ImageButton = findViewById(R.id.MenuButton)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)


        prev.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, BlankFragment())
                .commit()
        }

        next.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, BlankFragment2())
                .commit()
        }

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(navView)
        }

        navView.setNavigationItemSelectedListener { menuItem ->
            // Xử lý sự kiện khi người dùng chọn một mục menu
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Xử lý khi chọn Home
                }
                R.id.nav_settings -> {
                    // Xử lý khi chọn Settings
                }
                // Xử lý các mục menu khác
            }
            // Đóng menu sau khi chọn
            drawerLayout.closeDrawer(navView)
            true
        }


    }


}



