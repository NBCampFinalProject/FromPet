package com.pet.frompet.ui.more.signout

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.pet.frompet.MainActivity
import com.pet.frompet.R
import com.pet.frompet.databinding.ActivityMorePasswordResetBinding
import com.pet.frompet.databinding.ActivitySignOutBinding
import com.pet.frompet.ui.intro.IntroActivity
import com.pet.frompet.ui.login.LoginActivity
import com.pet.frompet.ui.login.LoginViewModel
import com.pet.frompet.ui.more.MorePasswordResetViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignOutActivity : AppCompatActivity() {
    private var _binding: ActivitySignOutBinding? = null
    private val binding get() = _binding!!

    private val viewModel by lazy {
        ViewModelProvider(this).get(MorePasswordResetViewModel::class.java)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignOutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSignOut()

    }
    private fun setSignOut(){
        binding.btOut.setOnClickListener {
            viewModel.deleteAccount()
            checkLoginState()
        }
    }

    private fun checkLoginState() {
        val intent = if (isLoggedIn()) {
            Intent(this, IntroActivity::class.java)
        } else {
            Intent(this, LoginActivity::class.java) // 원하는 액티비티로 변경 가능
        }

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(Intent(this, LoadingActivity::class.java))
        // 현재 액티비티를 종료하여 앱 재시작을 시뮬레이션
        finish()
    }
    private fun isLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}