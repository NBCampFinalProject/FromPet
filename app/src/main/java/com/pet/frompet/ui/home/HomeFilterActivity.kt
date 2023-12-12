package com.pet.frompet.ui.home

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewTreeObserver
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import com.pet.frompet.R
import com.pet.frompet.data.model.Filter
import com.pet.frompet.databinding.ActivityHomeFilterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore


class HomeFilterActivity : AppCompatActivity() {
    private var _binding: ActivityHomeFilterBinding? = null
    private val binding get() = _binding!!

    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private var selectedDistance: Float = 10.0f // 기본 최소 거리

    companion object {
        const val FILTER_DATA = "filter_data"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.chipGroup.check(R.id.chip_all)
        binding.chipGroup2.check(R.id.chip_dont_care)

        setupCloseButton()
        setupPetTypeSpinner()
        setupDistanceSlider()
        restoreFilterOptions()

        binding.btComplete.setOnClickListener {
            val selectedPetType = binding.spPetType.selectedItem.toString()
            val selectedGender = getSelectedGender()
            val selectNeuter = getSelectedNeuter()
            val filter = Filter(
                petType = selectedPetType,
                petGender = selectedGender,
                petNeuter = selectNeuter,
                distanceFrom = selectedDistance
            )
            saveFilterOptions(selectedPetType, selectedGender, selectNeuter, selectedDistance)
            returnFilterResult(filter)
        }
    }

    private fun setupDistanceSlider()= with(binding) {
        slider.value = selectedDistance
        slider.valueFrom = 10.0f // 슬라이더의 최소값
        slider.valueTo = 600.0f // 슬라이더의 최대값

        slider.setLabelFormatter { value: Float ->
            "${value.toInt()} km"
        }
        slider.addOnChangeListener { slider, value, _ ->
            selectedDistance = value // 사용자가 선택한 최소 거리
            tvKm.text = "${value.toInt()} km"
        }
        tvKm.text = "${selectedDistance.toInt()} km"
    }


        private fun setupPetTypeSpinner() {
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.pet_types,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spPetType.adapter = adapter
        val petTypes = resources.getStringArray(R.array.pet_types)
        val defaultPosition = petTypes.indexOf("전체")
        binding.spPetType.setSelection(defaultPosition)
        binding.spPetType.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 스피너의 넓이
                val spinnerWidth = binding.spPetType.width
                // 드롭다운 리스트의 넓이를 스피너의 넓이로 설정
                binding.spPetType.dropDownWidth = spinnerWidth
                // 레이아웃 리스너를 제거(최종넓이를 얻기위해서 호출)
                binding.spPetType.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }


    private fun getSelectedGender(): String? {
        return when (binding.chipGroup.checkedChipId) {
            R.id.chip_male -> "수컷"
            R.id.chip_female -> "암컷"
            else -> "all"
        }
    }

    private fun getSelectedNeuter(): String? {
        return when (binding.chipGroup2.checkedChipId) {
            R.id.chip_done -> "중성화"
            R.id.chip_nope -> "중성화 안함"
            else -> "상관없음"
        }
    }

    private fun saveFilterOptions(petType: String, petGender: String?, petNeuter: String?, distance: Float) {
        val filterOptions =
            mapOf("petType" to petType, "petGender" to petGender, "petNeuter" to petNeuter, "distanceFrom" to distance)
        database.reference.child("userSaveFilter").child(currentUserUid).setValue(filterOptions)
    }

    private fun restoreFilterOptions()= with(binding) {
        database.reference.child("userSaveFilter").child(currentUserUid)
            .addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val petType = snapshot.child("petType").getValue(String::class.java) ?: "전체"
                    val petGender =
                        snapshot.child("petGender").getValue(String::class.java) ?: "all"
                    val petNeuter =
                        snapshot.child("petNeuter").getValue(String::class.java) ?: "상관없음"
                    val petTypePosition =
                        resources.getStringArray(R.array.pet_types).indexOf(petType)
                    val distance = snapshot.child("distanceFrom").getValue(Float::class.java) ?: 10.0f



                    spPetType.setSelection(petTypePosition)

                    val genderChipId = when (petGender) {
                        "수컷" -> R.id.chip_male
                        "암컷" -> R.id.chip_female
                        else -> R.id.chip_all
                    }
                    chipGroup.check(genderChipId)

                    val neuterChipId = when (petNeuter) {
                        "중성화" -> R.id.chip_done
                        "중성화 안함" -> R.id.chip_nope
                        else -> R.id.chip_dont_care
                    }
                    chipGroup2.check(neuterChipId)
                    slider.value = distance
                    selectedDistance = distance
                    tvKm.text = "${selectedDistance.toInt()} km"
                }


                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun returnFilterResult(filter: Filter) {
        val result = Intent().apply {
            putExtra(FILTER_DATA, filter)
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }
    private fun setupCloseButton()= with(binding) {
        ivClose.setOnClickListener {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
