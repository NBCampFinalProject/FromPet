package com.pet.frompet.ui.chat.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import coil.load
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.MatchSharedViewModel
import com.pet.frompet.R
import com.pet.frompet.util.showToast
import com.pet.frompet.databinding.ActivityChatUserDetailBinding
import com.pet.frompet.data.model.User
import com.pet.frompet.data.repository.home.HomeFilterRepository
import com.pet.frompet.data.repository.home.HomeFilterRepositoryImpl
import com.pet.frompet.ui.home.HomeFilterViewModel
import com.pet.frompet.ui.home.HomeFilterViewModelFactory
import com.pet.frompet.ui.setting.fcm.FCMNotificationViewModel

class ChatUserDetailActivity : AppCompatActivity() {
    companion object {
        const val MATCHED_USERS = "matchedUser"
        const val USER = "user"
        const val ACTION = "action"
        const val MATCH = "match"
        const val DISLIKE = "dislike"
    }

    private lateinit var binding: ActivityChatUserDetailBinding
    private val matchSharedViewModel: MatchSharedViewModel by viewModels()
    private val fcmViewModel: FCMNotificationViewModel by viewModels()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val currentUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser

    private val homeFilterRepository: HomeFilterRepository by lazy {
        HomeFilterRepositoryImpl(
            firestore = FirebaseFirestore.getInstance(),
            database = FirebaseDatabase.getInstance(),
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        )
    }

    private val filterViewModel: HomeFilterViewModel by viewModels {
        HomeFilterViewModelFactory(this.application, homeFilterRepository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatUserDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val user: User? = intent.getParcelableExtra(USER)
        user?.let {
            displayUserInfo(it)
        }
        setClickListeners(user)
    }

    private fun setClickListeners(user: User?)= with(binding) {
        likeBtn.setOnClickListener {
            user?.uid?.let { userId ->
                firestore.collection("User").document(currentUser?.uid?:"").get().addOnSuccessListener { docs->
                    val currentName = docs.getString("petName")
                    val title ="새로운 매칭!"
                    val message = "${currentName}님과 매칭되었습니다"
                    fcmViewModel.sendFCMNotification(user.uid,title,message)
                }
                filterViewModel.userSwiped(user.uid)
                matchSharedViewModel.matchUser(userId)
                showToast("${user.petName}님과 매치 되었습니다\n 대화방이 생성되었습니다!",Toast.LENGTH_LONG)
                setResultAndFinish(userId, MATCH)
            }
        }

        dislikeBtn.setOnClickListener {
            user?.uid?.let { userId ->
                filterViewModel.userSwiped(user.uid)
                matchSharedViewModel.dislike(userId)
                showToast("${user.petName}님과 매칭에 실패 했습니다!",Toast.LENGTH_LONG)
                setResultAndFinish(userId, DISLIKE)
            }
        }
       ivPetProfile2.setOnClickListener {
            val intent = Intent(this@ChatUserDetailActivity, ChatPullScreenActivity::class.java)
            intent.putExtra(ChatPullScreenActivity.IMAGE_URL, user?.petProfile)
            startActivity(intent)
        }
        backBtn.setOnClickListener {
            onBackPressed()
        }
    }
    private fun setResultAndFinish(userId: String, action: String) {
        val result = Intent().apply {
         putExtra(MATCHED_USERS, userId)
        putExtra(ACTION, action)}
        setResult(Activity.RESULT_OK, result)
        finish()
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
                ivPetProfile2.load(user.petProfile){
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

