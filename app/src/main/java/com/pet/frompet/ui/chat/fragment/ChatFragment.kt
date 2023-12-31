package com.pet.frompet.ui.chat.fragment

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.pet.frompet.MatchSharedViewModel
import com.pet.frompet.R
import com.pet.frompet.ui.chat.adapter.ViewPagerAdapter
import com.pet.frompet.databinding.FragmentChatBinding
import com.pet.frompet.ui.chat.activity.ChatUserDetailActivity
import com.pet.frompet.ui.chat.adapter.ChatListAdapter
import com.pet.frompet.ui.chat.viewmodel.ChatViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val chatHomeFrag: ChatHomeFragment by lazy { ChatHomeFragment() }
    private val chatListFrag: ChatLikeListFragment by lazy { ChatLikeListFragment() }
    private val matchSharedViewModel: MatchSharedViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        val fragments = listOf(chatHomeFrag, chatListFrag)
        val adapter = ViewPagerAdapter(this, fragments)

        binding.viewPager.adapter = adapter
        binding.viewPager.let { binding.dots.setViewPager2(it) }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtonColors(position)
            }
        })
        binding.btCanChat.setOnClickListener {
            binding.viewPager.currentItem = 0
        }
        binding.btLikeMe.setOnClickListener {
            binding.viewPager.currentItem = 1
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        matchSharedViewModel.matchedList.observe(viewLifecycleOwner) { users ->
            chatViewModel.getLastTimeSorted(users) {
                val text = "${it.size}마리와 대화가 가능해요"
                val spannable = SpannableStringBuilder(text)
                val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context?:return@getLastTimeSorted, R.color.lip_pink))
                spannable.setSpan(colorSpan,0,"${it.size}".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                binding.btCanChat?.text = spannable
            }
            }
            matchSharedViewModel.likeList.observe(viewLifecycleOwner) { users ->
            users?.let {
                val text = "${it.size}마리가 나를 좋아해요"
                val spannable = SpannableStringBuilder(text)
                val colorSpan = ForegroundColorSpan(ContextCompat.getColor(context?:return@let, R.color.lip_pink))
                spannable.setSpan(colorSpan, 0, "${it.size}".length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                binding.btLikeMe?.text = spannable
            }
          }
        matchSharedViewModel.loadlike()

    }

    private fun updateButtonColors(currentPage: Int) {
        val isFirstPage = currentPage == 0
        binding.btCanChat.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isFirstPage) R.color.black else R.color.gray
            )
        )
        val isSecondPage = currentPage == 1
        binding.btLikeMe.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                if (isSecondPage) R.color.black else R.color.gray
            )
        )
    }
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
