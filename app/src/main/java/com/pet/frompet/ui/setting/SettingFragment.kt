package com.pet.frompet.ui.setting

//import FCMTokenManagerViewModel
import android.app.AlertDialog

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import androidx.fragment.app.viewModels
import coil.load
import com.pet.frompet.R
import com.pet.frompet.databinding.FragmentSettingBinding
import com.pet.frompet.ui.chat.activity.ChatPullScreenActivity
import com.pet.frompet.ui.login.LoginActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pet.frompet.ui.more.MoreActivity

class SettingFragment : Fragment() {

    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatSwitch: SwitchMaterial
    private lateinit var friendsSwitch: SwitchMaterial
    private val database = FirebaseDatabase.getInstance().getReference()
    private val auth = FirebaseAuth.getInstance()



    private val viewModel: SettingViewModel by viewModels()
    private lateinit var progressBar: ProgressBar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadUserPetProfile()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSettingBinding.inflate(inflater, container, false)
        progressBar = binding.progressBar


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.petProfile.observe(viewLifecycleOwner) { petProfile ->
            petProfile?.let {
                binding.ivPet.load(it)//혹시모르니 코일로 바꿔놨습니다 승현님.
                Log.d("sooj", "${it}")
            }
        }

        viewModel.petName.observe(viewLifecycleOwner) { petName ->
            binding.tvPetName.text = petName
        }

        viewModel.petType.observe(viewLifecycleOwner) { petType ->
            binding.tvPetType.text = petType
        }


        binding.ivPet.setOnClickListener {
            val intent = Intent(requireActivity(), ChatPullScreenActivity::class.java)
            val imageUrl = viewModel.petProfile.value
            intent.putExtra("image_url", imageUrl)
            startActivity(intent)
        }

        binding.ibMore.setOnClickListener {
            val intent = Intent(requireActivity(),MoreActivity::class.java)
            startActivity(intent)
        }



        binding.btModify.setOnClickListener {
            val intent = Intent(requireActivity(), SettingProfileActivity::class.java)
            startActivity(intent)
        }

        chatSwitch = binding.chatSwitch


        binding.ibNotification.setOnClickListener {

            chatSwitch.isChecked = !chatSwitch.isChecked
        }

        friendsSwitch = binding.friendsSwitch


        binding.ibFriendsNoti.setOnClickListener {

            friendsSwitch.isChecked = !friendsSwitch.isChecked
        }

        binding.ibFriends.setOnClickListener {
            val intent = Intent(requireActivity(), FriendsListActivity::class.java)
            startActivity(intent)
        }
        binding.chatSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {

                database.child("usersToken").child(auth.currentUser?.uid ?: "").child("chatNotificationsEnabled").setValue(true)
            } else {

                database.child("usersToken").child(auth.currentUser?.uid ?: "").child("chatNotificationsEnabled").setValue(false)
            }
        }
        database.child("usersToken").child(auth.currentUser?.uid ?: "")
            .child("chatNotificationsEnabled").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatNotificationsEnabled = snapshot.value as? Boolean ?: true
                    chatSwitch.isChecked = chatNotificationsEnabled
                    if (chatNotificationsEnabled) {

                    } else {

                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        binding.friendsSwitch.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked) {

                database.child("usersToken").child(auth.currentUser?.uid ?: "").child("notificationsEnabled").setValue(true)
            } else {

                database.child("usersToken").child(auth.currentUser?.uid ?: "").child("notificationsEnabled").setValue(false)
            }
        }
        //앱 시작시 리얼타임베이스에서 noti의 상태를 불러와서 버튼에 적용!
        database.child("usersToken").child(auth.currentUser?.uid ?: "")
            .child("notificationsEnabled").addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notificationsEnabled = snapshot.value as? Boolean ?: true
                    friendsSwitch.isChecked = notificationsEnabled
                    if (notificationsEnabled) { //true면 on

                    } else {        //false면 off

                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showLogoutBottomSheet() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_logout, null)
        val dialog = BottomSheetDialog(requireContext())

        dialog.setContentView(view)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val buttonYes = view.findViewById<Button>(R.id.btn_yes)
        val buttonNo = view.findViewById<Button>(R.id.btn_no)

        buttonYes.setOnClickListener {
            performLogout()
            dialog.dismiss()
        }

        buttonNo.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

        val dimView = View(requireContext())
        dimView.setBackgroundColor(Color.parseColor("#80000000"))
        val parentLayout = requireActivity().findViewById<ViewGroup>(android.R.id.content)
        parentLayout.addView(dimView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

        dialog.setOnDismissListener {
            parentLayout.removeView(dimView)
        }
    }


    private fun performLogout() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // 현재 로그인된 사용자가 있는 경우에만 실행!
            val userId = currentUser.uid


            val database = FirebaseDatabase.getInstance().getReference()
            database.child("usersToken").child(userId).child("fcmToken").setValue(null)
//            // FCM 토큰을 삭제하는 코드 추가 해야함


            // 사용자 로그아웃
            FirebaseAuth.getInstance().signOut()

            // LoginActivity로 이동이야
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        viewModel.loadUserPetProfile()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
