package com.example.frompet.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.frompet.databinding.ActivityLoginBinding
import com.example.frompet.MainActivity
import com.example.frompet.R
import com.example.frompet.ui.login.googlelog.GoogleAcceptUpFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "랄라라"
        private const val RC_SIGN_IN = 9001
    }

    private var _binding: ActivityLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
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
            binding?.apply {
                loginGoogle.setOnClickListener {
                    Log.d(TAG, "Google Sign-In button clicked")
                    val fragment = GoogleAcceptUpFragment.newInstance()

                    val transaction = supportFragmentManager.beginTransaction()
                    transaction.add(fragment, "GoogleAcceptUpFragment")
                    transaction.commit()
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
                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                        } else {
                            // 사용자 정보가 Firestore에 없는 경우, MemberInfoActivity로 이동
                            val intent = Intent(this@LoginActivity, MemberInfoActivity::class.java)
                            startActivity(intent)
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


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

}

