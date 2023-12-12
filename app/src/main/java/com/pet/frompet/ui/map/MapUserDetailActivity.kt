package com.pet.frompet.ui.map

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.view.isVisible
import coil.load
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.MatchSharedViewModel
import com.pet.frompet.R
import com.pet.frompet.data.model.User
import com.pet.frompet.data.repository.home.HomeFilterRepository
import com.pet.frompet.data.repository.home.HomeFilterRepositoryImpl
import com.pet.frompet.databinding.ActivityMapUserDetailBinding
import com.pet.frompet.ui.chat.activity.ChatPullScreenActivity
import com.pet.frompet.ui.home.HomeFilterViewModel
import com.pet.frompet.ui.home.HomeFilterViewModelFactory
import com.pet.frompet.ui.setting.fcm.FCMNotificationViewModel
import com.pet.frompet.util.showToast

class MapUserDetailActivity : AppCompatActivity() {
    companion object {
        const val USER = "user"
    }

    private lateinit var binding: ActivityMapUserDetailBinding
    private val viewModel: MatchSharedViewModel by viewModels()
    private val currentUid = FirebaseAuth.getInstance().currentUser?.uid
    private val firestore = FirebaseFirestore.getInstance()
    private val fcmViewModel :FCMNotificationViewModel by viewModels()
    private val homeFilterRepository: HomeFilterRepository by lazy {

        HomeFilterRepositoryImpl(
            firestore = FirebaseFirestore.getInstance(),
            database = FirebaseDatabase.getInstance(),
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        )
    }

    private val homeFilterViewModel: HomeFilterViewModel by viewModels { HomeFilterViewModelFactory(application,homeFilterRepository) }
    val swipedUsersRef = FirebaseDatabase.getInstance().getReference("swipedUsers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        val user: User? = intent.getParcelableExtra(USER)
        user?.let {
            displayUserInfo(it)
        }
        binding.ivPetProfile.setOnClickListener {
            val intent = Intent(this@MapUserDetailActivity, ChatPullScreenActivity::class.java)
            intent.putExtra(ChatPullScreenActivity.IMAGE_URL, user?.petProfile)
            startActivity(intent)
        }
        setClickListeners(user)
    }


    private fun setClickListeners(user: User?) {
        binding.likeBtn.setOnClickListener {
            user?.let { otherUser ->
                if (otherUser.uid == currentUid) {
                    showToast("본인에게는 친구신청을 할 수 없습니다.", Toast.LENGTH_SHORT)
                } else {
                    swipedUsersRef.child(otherUser.uid).child(currentUid ?: "").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likeBtn.isVisible = false
                                showToast("이미 상대방과 친구이거나 거절당한 상태입니다.", Toast.LENGTH_SHORT) // 상대방이 이미 스와이프하여 거절한 상태
                            } else {
                                viewModel.like(otherUser.uid)
                                homeFilterViewModel.userSwiped(user.uid)
                                showToast("${otherUser.petName}님에게 친구신청을 걸었습니다!", Toast.LENGTH_SHORT)  // 상대방이 아직 스와이프하지 않았거나 거절하지 않은 상태
                                firestore.collection("User").document(currentUid ?: "").get().addOnSuccessListener { docs ->
                                    val currentUserName = docs.getString("petName") ?: "알 수 없음"
                                    val title = "새로운 좋아요"
                                    val message = "${currentUserName}님이 당신을 좋아합니다."
                                    fcmViewModel.sendFCMNotification(otherUser.uid, title, message)
                                }
                                finish()
                            }
                        }
                        override fun onCancelled(error: DatabaseError) {}
                    })}
            }
        }
    }



    private fun displayUserInfo(user: User)=with(binding) {
            tvPetName.text = user.petName
            tvPetAge.text = "${user.petAge}세"
            tvPetGender.text = user.petGender
            tvPetType.text = user.petType
            tvPetDes.text = user.petDescription
            tvPetIntro.text = user.petIntroduction
            tvPetNeuter.text = user.petNeuter
            user.petProfile.let {
                ivPetProfile.load(user.petProfile) {
                    error(R.drawable.kakaotalk_20230825_222509794_01)
                }
            }
        val typeText = user.petType
        val typeImageView = ivPetType
        when (typeText) {
            "고양이" -> typeImageView.setImageResource(R.drawable.cat)
            "강아지" -> typeImageView.setImageResource(R.drawable.dog)
            "라쿤" -> typeImageView.setImageResource(R.drawable.raccoon)
            "여우" -> typeImageView.setImageResource(R.drawable.fox)
            "새" -> typeImageView.setImageResource(R.drawable.chick)
            "돼지" -> typeImageView.setImageResource(R.drawable.pig)
            "파충류" -> typeImageView.setImageResource(R.drawable.snake)
            "물고기" -> typeImageView.setImageResource(R.drawable.fish)
        }
        val genderText = user.petGender
        val genderImageView = ivPetGender
        when (genderText) {
            "수컷" -> genderImageView.setImageResource(R.drawable.icon_male)
            "암컷" -> genderImageView.setImageResource(R.drawable.icon_female)
        }
        val neuterText = user.petNeuter
        val neuterImageView = ivPetNeuter
        when (neuterText) {
            "중성화" -> neuterImageView.setImageResource(R.drawable.icon_neuter)
            "중성화 안함" -> neuterImageView.setImageResource(R.drawable.icon_non_neuter)
        }
        }
    }

