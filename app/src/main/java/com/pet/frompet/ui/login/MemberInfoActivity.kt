package com.pet.frompet.ui.login

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.pet.frompet.databinding.ActivityMemberInfoBinding
import com.pet.frompet.data.model.User
import com.pet.frompet.MainActivity
import com.pet.frompet.R
import com.pet.frompet.data.model.CommunityHomeData
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.pet.frompet.util.showToast
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemberInfoActivity : AppCompatActivity() {
    private var _binding: ActivityMemberInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            MemberInfoViewModelFactory(this, storage, FirebaseFirestore.getInstance())
        )[MemberInfoViewModel::class.java]
    }
    private val PICK_IMAGE_FROM_ALBUM = 1
    private var selectedSpinnerItem: CommunityHomeData? = null

    // FirebaseStorage 초기화
    val storage = FirebaseStorage.getInstance()
    private var petGender: String = "" // 성별 정보 저장 변수
    private var petAge: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMemberInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
        viewModel.getMemberInfoSpinner()
        binding.btSelectPetPhoto.setOnClickListener {
            goGallery()
        }
        viewModel.saveFireStore.observe(this) { result ->
            if (result.isSuccess) {
                showToast("회원정보 등록 성공")
            } else {
                showToast("Firestore에 저장 중 에러 발생: ${result.exceptionOrNull()?.message}")
            }
        }
        viewModel.petProfileLiveData.observe(this) { imageUrl ->
            Glide.with(this)
                .load(imageUrl)
                .into(binding.imageViewAddPet)
        }

        binding.btComplete.setOnClickListener {
            val petName = binding.textInputEditTextAddPetName.text.toString()
            val petDescription = binding.textInputEditTextPetDescription.text.toString()
            val petIntroduction = binding.textInputEditTextPetCharacter.text.toString()
            val selectedGenderId = binding.toggleButtonPetGender.checkedButtonId
            if (selectedGenderId == R.id.buttonHe) {
                petGender = "수컷"
            } else if (selectedGenderId == R.id.buttonShe) {
                petGender = "암컷"
            }
            val selectedNeuterId = when (binding.toggleButtonNeuter.checkedButtonId) {
                R.id.buttonNeuter -> "중성화"
                R.id.buttonNoNeuter -> "중성화 안함"
                else -> " "
            }
            val spinnerPetType = selectedSpinnerItem?.petType ?: ""

            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // LiveData를 통해 이미지 URL을 가져옴
                val imageUrl = viewModel.petProfileLiveData.value

                // 예외 처리: 입력 값이 비어 있는 경우
                if (petName.isEmpty() || petDescription.isEmpty() || petIntroduction.isEmpty()) {
                    showToast("모든 항목을 입력하세요.")
                    return@setOnClickListener
                }

                if (imageUrl != null) {
                    val user = User(
                        petAge,
                        petDescription,
                        petGender,
                        petIntroduction,
                        petName,
                        imageUrl,
                        spinnerPetType,
                        selectedNeuterId.toString()
                    )
                    user.uid = currentUser.uid

                    // Firestore에 저장
                    viewModel.saveToFireStore(user)

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    showToast("이미지가 업로드되지 않았습니다.")
                }
            }
        }

        binding.textInputPetBirthText.setOnClickListener {
            val currentMillis = System.currentTimeMillis()

            val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.before(currentMillis)) // Allow dates in the past

            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("날짜선택")
                .setSelection(currentMillis)
                .setCalendarConstraints(constraintsBuilder.build())
                .build()

            datePicker.show(supportFragmentManager, datePicker.toString())

            datePicker.addOnPositiveButtonClickListener { selectedDateInMillis ->
                val selectedDate = Date(selectedDateInMillis)
                val currentDate = Date()

                val selectDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedDate)
                binding.textInputPetBirthText.setText(selectDate)

                // 선택한 날짜를 이용하여 나이를 계산하고 petAge 변수에 할당
                val birthDate =
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(selectDate)
                if (birthDate != null) {
                    val ageMilliseconds = currentDate.time - birthDate.time
                    val ageYears = (ageMilliseconds / (1000L * 60 * 60 * 24 * 365.25)).toInt()
                    petAge = ageYears
                }
            }
        }
    }

    private fun initView() = with(binding) {
        val adapter = MemberInfoAdapter(this@MemberInfoActivity, emptyList())
        spPetType.adapter = adapter
        spPetType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectedSpinnerItem = adapter.getItem(position) as? CommunityHomeData
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {

            }
        }
        viewModel.petSpinnerType.observe(this@MemberInfoActivity) { spinnerType ->
            adapter.updateData(spinnerType)
        }
        spPetType.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val spinnerWidth = spPetType.width
                spPetType.dropDownWidth = spinnerWidth
                spPetType.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }

        })
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM && resultCode == RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                contentUpload(uri.toString())
            }
        }
    }

    // contentUpload() 함수 내부에서 이미지를 Firebase Storage에 업로드할 수 있습니다.
    private fun contentUpload(uri: String?) {
        viewModel.contentUpload(uri)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // 뒤로 가기 버튼 동작을 비활성화
        // 아무 동작도 하지 않도록 설정
    }

    private fun goGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, PICK_IMAGE_FROM_ALBUM)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}

fun StorageReference.putFile(petProfileUri: String): UploadTask {
    val file = Uri.parse(petProfileUri) // 문자열 URI를 Uri 객체로 변환
    return putFile(file)
}

