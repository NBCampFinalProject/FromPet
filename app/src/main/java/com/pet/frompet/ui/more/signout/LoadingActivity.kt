package com.pet.frompet.ui.more.signout

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat.getSystemService
import com.pet.frompet.MainActivity
import com.pet.frompet.R
import com.pet.frompet.databinding.ActivityLoadingBinding
import com.pet.frompet.databinding.ActivitySignOutBinding

class LoadingActivity : AppCompatActivity() {
    private var _binding: ActivityLoadingBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startActivityAfterDelay()

    }


    private fun startActivityAfterDelay() {
        val delayMillis = 1000 // 1초 후에 앱을 재시작합니다. 원하는 시간으로 조절하세요.
        val intent = Intent(this, MainActivity::class.java) // 재시작할 액티비티를 지정
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
            AlarmManager.RTC,
            System.currentTimeMillis() + delayMillis,
            pendingIntent
        )
    }

    @Deprecated("Deprecated in Java",
        ReplaceWith("super.onBackPressed()", "androidx.appcompat.app.AppCompatActivity")
    )
    override fun onBackPressed() {
        val i = Intent(this@LoadingActivity, MainActivity::class.java)
        i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(i)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}