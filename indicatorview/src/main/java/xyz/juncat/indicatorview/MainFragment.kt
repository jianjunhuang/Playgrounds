package xyz.juncat.indicatorview

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return AppCompatTextView(requireContext()).apply {
            text = arguments?.getInt("key_pos")?.toString()
            gravity = Gravity.CENTER
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    companion object {
        fun newInstance(index: Int): MainFragment {
            val args = Bundle().apply {
                putInt("key_pos", index)
            }
            val fragment = MainFragment()
            fragment.arguments = args
            return fragment
        }
    }
}