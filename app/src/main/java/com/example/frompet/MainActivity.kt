package com.example.frompet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.frompet.databinding.ActivityMainBinding
import com.example.frompet.ui.login.LoginActivity
import com.example.frompet.ui.login.LoginViewModel
import com.example.frompet.ui.login.LoginViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(this,
            LoginViewModelFactory()
        )[LoginViewModel::class.java]
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.myBottomNav.itemIconTintList = null

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            // 사용자가 로그인되어 있지 않은 경우
            // LoginActivity로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // 현재 화면 종료
        } else {
            setStartApp()
        }
       /* viewModel.user.observe(this) { firebaseUser ->
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                Log.d("ㅋㅋㅋㅋㅋ", "사용자 UID: $uid")
            }git pull origin release/1.0.0
        }*/
    }
    private fun setStartApp() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.my_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        _binding?.let { NavigationUI.setupWithNavController(it.myBottomNav, navController) }
    }
}