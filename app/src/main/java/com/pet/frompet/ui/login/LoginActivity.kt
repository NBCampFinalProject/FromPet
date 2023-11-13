package com.pet.frompet.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.pet.frompet.databinding.ActivityLoginBinding
import com.pet.frompet.MainActivity
import com.pet.frompet.R
import com.pet.frompet.ui.intro.IntroActivity
import com.pet.frompet.ui.login.googlelog.GoogleViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "랄라라"
        private const val RC_SIGN_IN = 1

    }

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }
    private val _viewModel by lazy {
        ViewModelProvider(this)[GoogleViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        listenToChannels()
        registerObservers()

        binding?.apply {
            signInButton.setOnClickListener {
                progressBarSignin.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()
                viewModel.signInUser(email, password)

            }

                loginGoogle.setOnClickListener {
                    Log.d(TAG, "Google Sign-In button clicked")
                    startGoogleSignIn()
                }

            _viewModel.currentUser.observe(this@LoginActivity) { firebaseUser ->
                val uid = firebaseUser?.uid
                if (uid != null) {
                    Log.e(TAG, "$uid")
                    val userDocRef = FirebaseFirestore.getInstance().collection("User").document(uid)
                    userDocRef.get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                startMainActivity()
                            } else {
                                startMemberInfoActivity()
                            }
                        }
                } else {
                    Log.e(TAG, "null")
                }
            }


            signUpTxt.setOnClickListener {
                val intent = Intent(this@LoginActivity, SingUpActivity::class.java)
                startActivity(intent)
            }

            forgotPassTxt.setOnClickListener {
                val intent = Intent(this@LoginActivity, PasswordResetActivity::class.java)
                startActivity(intent)
            }
        }
        binding.userEmailEtv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 이전 텍스트 변경
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 중
            }
        })

        binding.userPasswordEtv.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                checkButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // 이전 텍스트 변경
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // 텍스트 변경 중
            }
        })
    }

    private fun registerObservers() {
        viewModel.currentUser.observe(this@LoginActivity) { user ->
            user?.let {
                val uid = user.uid // 사용자의 고유 식별자 (UID)
                val userDocRef = FirebaseFirestore.getInstance().collection("User").document(uid)

                userDocRef.get()
                    .addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            // 사용자 정보가 Firestore에 있는 경우, MainActivity로 이동
                            val i = Intent(this@LoginActivity, MainActivity::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                        } else {
                            // 사용자 정보가 Firestore에 없는 경우, MemberInfoActivity로 이동
                            val i = Intent(this@LoginActivity, MemberInfoActivity::class.java)
                            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(i)
                        }
                    }
                    .addOnFailureListener {
                        // Firestore에서 데이터를 가져오는 동안 오류가 발생한 경우

                    }
            }
        }
    }

    private fun listenToChannels() {
        lifecycleScope.launch {
            viewModel.allEventsFlow.collect { event ->
                when (event) {
                    is LoginViewModel.AllEvents.Error -> {
                        val errorMessage = event.getKoreanMessage()
                        showSnackbar(errorMessage)
                        binding.progressBarSignin.isInvisible = true
                    }

                    is LoginViewModel.AllEvents.Message -> {
                        showSnackbar(event.message)
                    }

                    is LoginViewModel.AllEvents.ErrorCode -> {
                        when (event.code) {
                            1 -> {
                                binding.userEmailEtvl.error = "메일이 비어있어요"
                            }

                            2 -> {
                                binding.userPasswordEtvl.error = "비밀번호가 비어있어요"
                            }
                        }
                        binding.progressBarSignin.isInvisible = true
                    }

                    else -> {}
                }
            }
        }
    }

    private fun startGoogleSignIn() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // 이미 로그인한 사용자가 있으면 MainActivity로 이동
            startMainActivity()
        } else {
            // Google 로그인 진행
            val webClientId = getString(R.string.web_client_id)
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
            _viewModel.mGoogleSignInClient.signOut().addOnCompleteListener {
                // 사용자가 로그아웃된 후 로그인 프로세스 시작
                val signInIntent = _viewModel.mGoogleSignInClient.signInIntent
                startActivityForResult(signInIntent, RC_SIGN_IN)
            }
        }
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(IntroActivity.TAG, "onActivityResult: requestCode = $requestCode, resultCode = $resultCode")
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    val idToken = account.idToken
                    Log.e(IntroActivity.TAG, "$idToken")
                    if (idToken != null) {
                        _viewModel.signInGoogle(idToken)
                    } else {
                        Log.e(IntroActivity.TAG, "idToken is null")
                    }
                } catch (e: ApiException) {
                    Log.e(IntroActivity.TAG, "Google sign-in failed", e)
                }
            } else {
                Log.e(IntroActivity.TAG, "Google sign-in result is not OK")
            }
        }

    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    private fun startMainActivity() {
        val intent = Intent(this, com.pet.frompet.MainActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun startMemberInfoActivity() {
        val intent = Intent(this, MemberInfoActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun checkFirestoreForUserInfo() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.uid?.let { uid ->
            val userDocRef = FirebaseFirestore.getInstance().collection("User").document(uid)
            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // 사용자 정보가 Firestore에 있는 경우, MainActivity로 이동
                        startMainActivity()
                    } else {
                        // 사용자 정보가 Firestore에 없는 경우, MemberInfoActivity로 이동
                        startMemberInfoActivity()
                    }
                }
                .addOnFailureListener {
                    // Firestore에서 데이터를 가져오는 동안 오류가 발생한 경우
                    // 오류 처리 코드를 추가하세요
                }
        }
    }
    private fun checkButtonState() {
        val userEmailText = binding.userEmailEtv.text.toString()
        val userPasswordText = binding.userPasswordEtv.text.toString()

        if (userEmailText.isNotEmpty() && userPasswordText.isNotEmpty()) {
            // 두 EditText 모두 값이 있을 때 버튼 스타일 변경
            binding.signInButton.setBackgroundResource(R.drawable.custom_button_background)
            binding.signInButton.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.white))
        } else {
            // 하나 이상의 EditText가 비어 있을 때 버튼 스타일 초기화
            binding.signInButton.setBackgroundResource(R.drawable.button_background)
            binding.signInButton.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.black))
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        viewModel
    }
}

