package com.pet.frompet.ui.more

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pet.frompet.R
import com.pet.frompet.databinding.ActivityLoginBinding
import com.pet.frompet.databinding.ActivityMoreBinding
import com.pet.frompet.ui.login.LoginActivity
import com.pet.frompet.ui.more.signout.SignOutActivity

class MoreActivity : AppCompatActivity() {

    private var _binding: ActivityMoreBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ibMore.setOnClickListener {
            showLogoutBottomSheet()
        }

        binding.ibMorePasswordReset.setOnClickListener {
            val intent = Intent(this,MorePasswordResetActivity::class.java)
            startActivity(intent)
        }
        binding.btMoreAuth.setOnClickListener {
            val i = Intent(this@MoreActivity, SignOutActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(i)
        }


    }




    private fun showLogoutBottomSheet() {
        val view = layoutInflater.inflate(R.layout.bottom_sheet_logout, null)
        val dialog = BottomSheetDialog(this)

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

        val dimView = View(this)
        dimView.setBackgroundColor(Color.parseColor("#80000000"))
        val parentLayout = findViewById<ViewGroup>(android.R.id.content)
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
            FirebaseAuth.getInstance().signOut()

            // LoginActivity로 이동이야
            val intent = Intent(this, LoginActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}