package com.pet.frompet

import com.pet.frompet.ui.setting.fcm.FCMTokenManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.pet.frompet.databinding.ActivityMainBinding
import com.pet.frompet.ui.login.LoginActivity
import com.google.firebase.auth.FirebaseAuth
import android.Manifest
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import com.pet.frompet.ui.home.HomeFilterViewModel
import com.pet.frompet.ui.home.HomeFilterViewModelFactory
import com.pet.frompet.util.showToast

class MainActivity : AppCompatActivity() {
    //    private val fcmTokenManagerViewModel by lazy {
//        ViewModelProvider(this)[FCMTokenManagerViewModel::class.java]
//    }
    private var _binding: ActivityMainBinding? = null

    private val fcmTokenManager = FCMTokenManager()
    private val binding get() = _binding!!
   private val homeFilterViewModel: HomeFilterViewModel by viewModels { HomeFilterViewModelFactory(application) }

    private val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private val requestLocationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                showToast("위치 권한이 승인되었습니다.\n필터기능을 위해서 앱을 재실행 해주세요.", Toast.LENGTH_SHORT)
                homeFilterViewModel.getCurrentUserLocation()
                homeFilterViewModel.currentFilter?.let {
                    homeFilterViewModel.filterUsers(it)
                }
            } else {
                showToast("위치 권한이 필요합니다.", Toast.LENGTH_LONG)
            }
        }




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.myBottomNav.itemIconTintList = null
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


        // Android 13 PostNotification 처리
        checkAppPushNotification()

        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.let {
            fcmTokenManager.fetchAndBaseFCMToken(it.uid)  // FCM 토큰을 가져와서 저장
        } //세준

        if (currentUser == null) {
            // 사용자가 로그인되어 있지 않은 경우
            // LoginActivity로 이동
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // 현재 화면 종료
        } else {
            setStartApp()
            checkLocationPermission()
        }
    }

    private fun setStartApp() {
        val navHostFragment = supportFragmentManager.findFragmentById(com.pet.frompet.R.id.my_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        _binding?.let { NavigationUI.setupWithNavController(it.myBottomNav, navController) }
    }

    private fun checkAppPushNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_DENIED) {
            // 푸시 권한 요청
            permissionPostNotification.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // 권한이 허용된 경우의 동작을 구현
    }

    private val permissionPostNotification = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            // 권한 허용된 경우의 동작
        } else {
            // 권한 비허용된 경우의 동작
            showToast( "푸시 알림 권한이 필요합니다.", Toast.LENGTH_LONG)
        }
    }


    private fun checkLocationPermission() {
        val allPermissionsGranted = locationPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!allPermissionsGranted) {
            requestLocationPermissionLauncher.launch(locationPermissions)
        }
    }


    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }

}