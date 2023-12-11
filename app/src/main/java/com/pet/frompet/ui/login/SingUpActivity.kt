package com.pet.frompet.ui.login

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.pet.frompet.R
import com.pet.frompet.databinding.ActivitySingUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SingUpActivity : AppCompatActivity() {
    private var _binding: ActivitySingUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(this)[LoginViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySingUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerObservers()
        listenToChannels()

        binding.apply {
            signUpButton.setOnClickListener {
                progressBarSignup.isVisible = true
                val email = userEmailEtv.text.toString()
                val password = userPasswordEtv.text.toString()
                val confirmPass = confirmPasswordEtv.text.toString()
                if (isValidEmail(email)) {
                    if (password.length < 6) {
                        showToast("비밀번호는 6자리 이상이어야 합니다.")
                    } else {
                        viewModel.signUpUser(email, password, confirmPass)
                    }
                } else {
                    showToast("올바른 이메일 형식이 아닙니다.")
                }


            }
            signInTxt.setOnClickListener {
                val intent = Intent(this@SingUpActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        binding.userEmailEtv.addTextChangedListener(textWatcher)
        binding.userPasswordEtv.addTextChangedListener(textWatcher)
        binding.confirmPasswordEtv.addTextChangedListener(textWatcher)

        checkButtonState()
    }
    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            checkButtonState()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // 이전 텍스트 변경
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            // 텍스트 변경 중
        }
    }
    private fun checkButtonState() {
        val userEmailText = binding.userEmailEtv.text.toString()
        val userPasswordText = binding.userPasswordEtv.text.toString()
        val confirmPasswordText = binding.confirmPasswordEtv.text.toString()

        if (userEmailText.isNotEmpty() && userPasswordText.isNotEmpty() && confirmPasswordText.isNotEmpty()) {
            // 모든 EditText가 값이 있을 때 버튼 스타일 변경
            binding.signUpButton.setBackgroundResource(R.drawable.custom_button_background)
            binding.signUpButton.setTextColor(resources.getColor(R.color.white))
        } else {
            // 하나 이상의 EditText가 비어 있을 때 버튼 스타일 초기화
            binding.signUpButton.setBackgroundResource(R.drawable.button_background)
            binding.signUpButton.setTextColor(resources.getColor(R.color.black))
        }
    }
    private fun registerObservers() {
        viewModel.currentUser.observe(this@SingUpActivity) { user ->
            user?.let {
                val intent = Intent(this@SingUpActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun listenToChannels() {
        lifecycleScope.launch {
            viewModel.allEventsFlow.collect { event ->
                when(event){
                    is LoginViewModel.AllEvents.Error -> {
                        binding.apply {
                            errorTxt.text = event.error
                            progressBarSignup.isInvisible = true
                        }
                    }
                    is LoginViewModel.AllEvents.Message -> {
                        Toast.makeText(this@SingUpActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                    is LoginViewModel.AllEvents.ErrorCode -> {
                        if (event.code == 1)
                            binding.apply {
                                userEmailEtvl.error = "메일이 비어있어요"
                                progressBarSignup.isInvisible = true
                            }


                        if(event.code == 2)
                            binding.apply {
                                userPasswordEtvl.error = "비밀번호가 비어있어요"
                                progressBarSignup.isInvisible = true
                            }

                        if(event.code == 3)
                            binding.apply {
                                confirmPasswordEtvl.error = "비밀번호가 맞지 않네요"
                                progressBarSignup.isInvisible = true
                            }
                        if(event.code == 4){
                            Toast.makeText(this@SingUpActivity,event.code,Toast.LENGTH_SHORT).show()
                        }
                    }

                    else ->{

                    }
                }

            }
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(this@SingUpActivity, message, Toast.LENGTH_SHORT).show()
    }
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})")
        return emailRegex.matches(email)
    }
}
