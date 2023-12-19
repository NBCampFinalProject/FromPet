package com.pet.frompet.ui.commnunity.community

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import com.pet.frompet.R
import com.pet.frompet.data.model.CommunityData
import com.pet.frompet.databinding.ActivityCommunityBinding
import com.pet.frompet.ui.commnunity.communityadd.CommunityAddActivity
import com.pet.frompet.ui.commnunity.communitydetail.CommunityDetailActivity
import com.pet.frompet.ui.commnunity.communityhome.CategoryClick
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CommunityActivity : AppCompatActivity() {



    private val viewModel by lazy {
        ViewModelProvider(this, CommunityViewModelFactory(
            FirebaseFirestore.getInstance())).get(CommunityViewModel::class.java)
    }


    companion object {
        const val COMMUNITY_DATA = "communityData"
        const val EXTRA_DATA = "extra_data"
        const val EXTRA_PET_TYPE = "petT"
    }

    private var _binding: ActivityCommunityBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
//    private val viewModel : CommunityViewModel by viewModels()
    private var originalList = mutableListOf<CommunityData>()
    private val communityAdapter: CommunityAdapter by lazy {
        CommunityAdapter(
            ListClick = { item ->
                // 기존의 startActivity(intent) 대신 onCommunityItemClick을 호출합니다.
                onCommunityItemClick(item)
            }
        )
    }

    private val startForDetailResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val docsId = result.data?.getStringExtra(CommunityDetailActivity.DOCS_ID)
            docsId?.let { id ->
                viewModel.deleteCommunityData(id)
            }
        }
    }
    private val startForAddResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

            val petType = intent.getStringExtra(EXTRA_PET_TYPE)
            petType?.let {
                fetchCommunityData(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerview.adapter = communityAdapter
        binding.listSearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                val searchTerms = query?.trim()?.split("\\s+".toRegex()) // 띄어쓰기로 단어 분리

                val filteredList = if (!searchTerms.isNullOrEmpty()) {
                    originalList.filter { item ->
                        // 검색어 중 하나라도 제목이나 내용에 포함되는 경우 필터링
                        searchTerms.any { term ->
                            item.title.contains(term, ignoreCase = true) || item.contents.contains(term, ignoreCase = true)
                        }
                    }
                } else {
                    originalList.toList() // query가 빈 문자열이거나 null인 경우, 원본 목록을 반환
                }
                communityAdapter.submitList(filteredList)
                return true // 검색 이벤트 처리 완료
            }


            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    communityAdapter.submitList(originalList.toList())
                    return true
                }
                Log.e("sshOriginList Tag","$newText")
                return true
            }
        })


        val petType = intent.getStringExtra(EXTRA_PET_TYPE)
        petType?.let {
            fetchCommunityData(it)
            setCategoryImage(it)
        }

        viewModel.filteredCommunityList.observe(this) { communityDataList ->
            if (communityDataList != null) {
                communityAdapter.submitList(communityDataList)
            }
        }

        binding.recyclerview.adapter = communityAdapter


        /*binding.ivCategory.setImageResource(petType)*/ //카테고리별 로고인데 int 값이라 안뜸
        binding.tvPetT.text = "$petType"


        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.ivPen.setOnClickListener {
            val intent: Intent =
                Intent(this@CommunityActivity, CommunityAddActivity::class.java)
            startForAddResult.launch(intent)
        }

        binding.chipGroup.setOnCheckedChangeListener { group, checkedId ->
            val filter = getFilter()
            if (petType != null) {
                viewModel.getFilterCommunityData(petType, filter).observe(this) { filteredList ->
                    communityAdapter.submitList(filteredList)
                }
            }
        }

        viewModel.event.observe(this) { categoryClick ->
            when (categoryClick) {
                is CategoryClick.PetCategory -> {
                    val item = categoryClick.item
                    // 클릭된 데이터(item)를 사용하여 화면을 업데이트하거나 필요한 작업을 수행합니다.
                    val intent = Intent(this, CommunityDetailActivity::class.java)
                    intent.putExtra(COMMUNITY_DATA, item)
                    startActivity(intent)
                }

                else -> {}
            }
        }
    }


    private fun fetchCommunityData(petType: String) {
        viewModel.getCommunityData(petType).observe(this) { communityDataList ->
            originalList.clear()
            originalList.addAll(communityDataList)
            communityAdapter.submitList(communityDataList.toList()) {

                binding.recyclerview.scrollToPosition(0)
            }
            Log.e("sshOriginList after","$originalList")
        }
    }
    private fun setCategoryImage(petType: String) {
        val petsType = when (petType) {
            "강아지" -> R.drawable.dog
            "고양이" -> R.drawable.cat
            "라쿤" -> R.drawable.raccoon
            "물고기" -> R.drawable.fish
            "여우" -> R.drawable.fox
            "파충류" -> R.drawable.snake
            "돼지" -> R.drawable.pig
            "새" -> R.drawable.chick
            else -> R.drawable.splash
        }
        binding.ivCategory.setImageResource(petsType)
    }

    private fun getFilter() =  when (binding.chipGroup.checkedChipId) {
        R.id.chip_share -> "나눔"
        R.id.chip_walk -> "산책"
        R.id.chip_love -> "사랑"
        R.id.chip_exchange -> "정보교환"
        R.id.chip_all -> "전체"
        else -> ""
    }
    private fun onCommunityItemClick(communityData: CommunityData) {
        val intent = Intent(this, CommunityDetailActivity::class.java).apply {
            putExtra(CommunityDetailActivity.COMMUNITY_DATA, communityData)
        }
        startForDetailResult.launch(intent)
    }
    override fun onResume() {
        super.onResume()

        val petType = intent.getStringExtra(EXTRA_PET_TYPE)
        petType?.let {
            fetchCommunityData(it)
        }
    }

    override fun onBackPressed() {
        if (!binding.listSearch.isIconified) {
            binding.listSearch.isIconified = true
        } else {
            super.onBackPressed()
        }
    }
}

