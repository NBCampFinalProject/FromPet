package com.pet.frompet.ui.home

//import FCMTokenManagerViewModel
import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Pair
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.LocationServices
import com.pet.frompet.databinding.FragmentHomeBinding
import com.pet.frompet.MatchSharedViewModel
import com.pet.frompet.R
import com.pet.frompet.data.model.Filter
import com.pet.frompet.ui.chat.activity.ChatClickUserDetailActivity
import com.pet.frompet.ui.setting.fcm.FCMNotificationViewModel
import com.pet.frompet.util.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.pet.frompet.data.repository.home.HomeFilterRepository
import com.pet.frompet.data.repository.home.HomeFilterRepositoryImpl
import com.yuyakaido.android.cardstackview.CardStackLayoutManager
import com.yuyakaido.android.cardstackview.CardStackListener
import com.yuyakaido.android.cardstackview.Direction

class HomeFragment : Fragment() {

    companion object {
        const val USER = "user"
        const val FILTER_DATA ="filter_data"

    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var manager : CardStackLayoutManager
    private val viewModel: MatchSharedViewModel by viewModels()
    private val fcmViewModel: FCMNotificationViewModel by viewModels()
    private val homeFilterRepository: HomeFilterRepository by lazy {
        HomeFilterRepositoryImpl(
            firestore = FirebaseFirestore.getInstance(),
            database = FirebaseDatabase.getInstance(),
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        )
    }

    private val filterViewModel: HomeFilterViewModel by viewModels {
        HomeFilterViewModelFactory(requireActivity().application, homeFilterRepository)
    }


    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val firestore = FirebaseFirestore.getInstance()
    private val database = FirebaseDatabase.getInstance().reference


    private val homeAdapter by lazy {
        HomeAdapter(this@HomeFragment)
    }
    private val filterActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.resultCode == Activity.RESULT_OK){
                val filterData = result.data?.getParcelableExtra<Filter>(FILTER_DATA)
                filterData?.let {
                    filterViewModel.filterUsers(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        init()

        checkAndShowSwipeTutorial()


        binding.btComplete.setOnClickListener {
            binding.tutorialOverlay.visibility = View.GONE
            setTutorialShown()
        }

        return binding.root
    }
    private fun init() {
        manager = CardStackLayoutManager(requireContext(), object : CardStackListener{
            override fun onCardDragging(direction: Direction?, ratio: Float) {

            }

            override fun onCardSwiped(direction: Direction?)  {

                when (direction) {
                    Direction.Left -> {
                        // 오른쪽으로 스와이프 (Like) 했을 때의 처리

                        val user  = homeAdapter.currentList[manager.topPosition-1]
                        user?.let {
                            // user를 이용하여 원하는 작업 수행
                            viewModel.like(user.uid)
                            filterViewModel.userSwiped(it.uid)
                            firestore.collection("User").document(currentUser?.uid!!).get().addOnSuccessListener { docs ->
                                val currentUserName = docs.getString("petName") ?:"nothing"
                                val title = "새로운 좋아요!"
                                val message = "${currentUserName}님이 당신을 좋아합니다."
                                fcmViewModel.sendFCMNotification(user.uid, title, message)
                            }

                            val btLike = binding.btLike
                            btLike.setImageResource(R.drawable.icon_sel_heart)

                            // ImageButton 크기 조절 애니메이션 적용
                            val scaleAnimation = ScaleAnimation(
                                1f, 1.2f, 1f, 1.2f, // 시작 크기와 끝 크기 (1.0f은 원래 크기)
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f // 중심 위치
                            )
                            scaleAnimation.duration = 200 // 애니메이션 지속 시간 (밀리초)
                            scaleAnimation.fillAfter = true // 애니메이션 이후 상태 유지
                            btLike.startAnimation(scaleAnimation)

                            Handler().postDelayed({
                                btLike.setImageResource(R.drawable.icon_unsel_heart)
                                val restoreAnimation = ScaleAnimation(
                                    1.2f, 1f, 1.2f, 1f, // 시작 크기와 끝 크기 (1.0f은 원래 크기)
                                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f // 중심 위치
                                )
                                restoreAnimation.duration = 200 // 애니메이션 지속 시간 (밀리초)
                                restoreAnimation.fillAfter = false // 애니메이션 이후 상태 유지
                                btLike.startAnimation(restoreAnimation)
                            }, 500) // 500밀리초(0.5초) 후에 복원

                        }

                    }
                    Direction.Right -> {
                        // 왼쪽으로 스와이프 (Dislike) 했을 때의 처리
                        val user = homeAdapter.currentList[manager.topPosition -1]
                        user?.let {
                            viewModel.dislike(user.uid)
                            filterViewModel.userSwiped(it.uid)
                            val btLike = binding.btDislike
                            btLike.setImageResource(R.drawable.icon_sel_cross)

                            // ImageButton 크기 조절 애니메이션 적용
                            val scaleAnimation = ScaleAnimation(
                                1f, 1.2f, 1f, 1.2f, // 시작 크기와 끝 크기 (1.0f은 원래 크기)
                                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f // 중심 위치
                            )
                            scaleAnimation.duration = 200 // 애니메이션 지속 시간 (밀리초)
                            scaleAnimation.fillAfter = true // 애니메이션 이후 상태 유지
                            btLike.startAnimation(scaleAnimation)

                            Handler().postDelayed({
                                btLike.setImageResource(R.drawable.icon_unsel_cross)
                                val restoreAnimation = ScaleAnimation(
                                    1.2f, 1f, 1.2f, 1f, // 시작 크기와 끝 크기 (1.0f은 원래 크기)
                                    Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f // 중심 위치
                                )
                                restoreAnimation.duration = 200 // 애니메이션 지속 시간 (밀리초)
                                restoreAnimation.fillAfter = false // 애니메이션 이후 상태 유지
                                btLike.startAnimation(restoreAnimation)
                            }, 500) // 500밀리초(0.5초) 후에 복원


                        }

                    }
                    Direction.Top -> {

                    }
                    else -> {
                        // 다른 방향으로 스와이프한 경우
                    }
                }

                    if (manager.topPosition == homeAdapter.currentList.size || homeAdapter.currentList.isEmpty()) {
                       requireContext().showToast(getString(R.string.this_is_last_card), Toast.LENGTH_SHORT)
                        val transaction = parentFragmentManager.beginTransaction()
                        transaction.replace(R.id.home_container, HomeEmptyFragment())
                        transaction.addToBackStack(null)
                        transaction.commit()
                    }
                }


                override fun onCardRewound() {}

            override fun onCardCanceled() {}

            override fun onCardAppeared(view: View?, position: Int) {}

            override fun onCardDisappeared(view: View?, position: Int) {}

        })
        manager.setVisibleCount(3)
        manager.setTranslationInterval(0.6f)
        manager.setScaleInterval(0.8f)
        manager.setMaxDegree(20.0f)
        manager.setDirections(Direction.HORIZONTAL)

        with(binding.cardStackView) {
            layoutManager = manager
            itemAnimator = DefaultItemAnimator()
            adapter = homeAdapter
            addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
                override fun onChildViewAttachedToWindow(view: View) {
                    val imageView: ImageView = view.findViewById(R.id.iv_pet_image)
                    val imageButton: ImageButton = view.findViewById(R.id.ib_up)
                    imageButton.setOnClickListener {
                        val currentPosition = manager.topPosition
                        if (currentPosition < homeAdapter.currentList.size) {
                            val user = homeAdapter.currentList[currentPosition]
                            user?.let {
                                val intent = Intent(requireActivity(), HomeDetailPage::class.java)
                                intent.putExtra(ChatClickUserDetailActivity.USER, user)
                                val options: ActivityOptions = ActivityOptions.makeSceneTransitionAnimation(
                                    requireActivity(),
                                    Pair.create(imageView, "imageTransition")
                                )
                                startActivity(intent, options.toBundle())
                            }
                        }
                    }
                }

                override fun onChildViewDetachedFromWindow(view: View) {
                    val imageView: ImageView = view.findViewById(R.id.iv_pet_image)
                    imageView.setOnClickListener(null)
                }
            })
        }
    }


    private fun checkAndShowSwipeTutorial() {
        _binding?.let { binding ->
            checkIfTutorialShow { isShown ->
                if (!isShown) {
                    binding.tutorialOverlay.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkIfTutorialShow(onComplete: (Boolean) -> Unit) {
        currentUser?.let {
            database.child("usersTutorial").child(it.uid).child("isTutorialShown").get()
                .addOnSuccessListener { snapShot->
                    val isTutorialShown = snapShot.getValue(Boolean::class.java)?:false
                    onComplete(isTutorialShown)
                }
                .addOnFailureListener{ onComplete(false) }
        }
    }

    private fun setTutorialShown() {
        currentUser?.let {
            database.child("usersTutorial").child(it.uid).child("isTutorialShown").setValue(true)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        filterViewModel.filteredUsers.observe(viewLifecycleOwner) { users ->
            if (users.isNullOrEmpty()) {
                showEmptyFragment()
                homeAdapter.submitList(null) // 현재 카드 스택 뷰를 비우는 처리
            } else {
                hideEmptyFragment() // 사용자 목록이 있을 때 빈 화면을 숨깁니다
                homeAdapter.submitList(users)
            }
        }
        btLike.setOnClickListener {
            likeUser()
        }
        btDislike.setOnClickListener {
            dislikeUser()
        }

        filterViewModel.loadFilteredUsers()
        ivFilter.setOnClickListener {
            val intent = Intent(requireContext(), HomeFilterActivity::class.java)
            filterActivityResultLauncher.launch(intent)
        }
    }
    private fun showEmptyFragment() {
        // 백 스택을 정리합니다
        parentFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        // 홈앰프티프래그먼트로 전환합니다
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.home_container, HomeEmptyFragment())
            commit()
        }
    }
    private fun hideEmptyFragment() {
        // 홈앰프티프래그먼트 인스턴스를 찾습니다
        val existingFragment = parentFragmentManager.findFragmentById(R.id.home_container)
        if (existingFragment is HomeEmptyFragment) {
            // 홈앰프티프래그먼트를 제거합니다
            parentFragmentManager.beginTransaction().remove(existingFragment).commit()
        }
    }
    private fun likeUser()= with(binding) { //탑포지션은 실험해봐야함
        val currentPosition = manager.topPosition
        if (currentPosition < homeAdapter.currentList.size) {
            val user = homeAdapter.currentList[currentPosition]
            user?.let {
                viewModel.like(user.uid)
                filterViewModel.userSwiped(it.uid)
                firestore.collection("User").document(currentUser?.uid!!).get().addOnSuccessListener { docs ->
                    val currentUserName = docs.getString("petName") ?: "nothing"
                    val title = "새로운 좋아요!"
                    val message = "${currentUserName}님이 당신을 좋아합니다."
                    fcmViewModel.sendFCMNotification(user.uid, title, message)
                }
                cardStackView.swipe()
            }
        } else {
        }
    }

    private fun dislikeUser() = with(binding){
        val currentPosition = manager.topPosition
        if (currentPosition < homeAdapter.currentList.size) {
            val user = homeAdapter.currentList[currentPosition]
            user?.let {
                viewModel.dislike(user.uid)
                filterViewModel.userSwiped(it.uid)
                cardStackView.swipe()
            }
        } else {
            // 카드가 더 이상 없을 때 처리
        }
    }

    override fun onDestroyView() {
            _binding = null
            super.onDestroyView()
        }
    }


