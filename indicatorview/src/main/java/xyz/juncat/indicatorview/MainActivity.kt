package xyz.juncat.indicatorview

import android.content.res.Resources
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {
    private val titleData = arrayListOf("All", "Video", "Picture")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val indicatorView: ViewPagerIndicatorView<String, AppCompatTextView> =
            findViewById(R.id.indicator_view)
        val viewPager2 = findViewById<ViewPager2>(R.id.view_pager)

        viewPager2.adapter = object : FragmentStateAdapter(supportFragmentManager, lifecycle) {
            override fun getItemCount(): Int {
                return 3
            }

            override fun createFragment(position: Int): Fragment {
                return MainFragment.newInstance(position)
            }

        }
        indicatorView.adapter =
            object : ViewPagerIndicatorView.IndicatorAdapter<String, AppCompatTextView>() {

                override fun createItemView(pos: Int): AppCompatTextView {
                    return AppCompatTextView(this@MainActivity).apply {
                        setTextColor(Color.GRAY)
                        setText(titleData[pos])
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply {

                            val paddingHorizontal = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 8f,
                                Resources.getSystem().displayMetrics
                            ).toInt()
                            val paddingVertical = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP, 4f,
                                Resources.getSystem().displayMetrics
                            ).toInt()

                            setPadding(
                                paddingHorizontal,
                                paddingVertical,
                                paddingHorizontal,
                                paddingVertical
                            )
                        }
                    }
                }

                override fun onItemViewSelected(
                    pos: Int,
                    itemView: AppCompatTextView,
                    selected: Boolean
                ) {
                    if (selected) {
                        itemView.setTextColor(Color.BLACK)
                    } else {
                        itemView.setTextColor(Color.GRAY)
                    }
                }

                override fun onItemViewStartScroll(
                    pos: Int,
                    currentItem: AppCompatTextView
                ) {
                    currentItem.setTextColor(Color.GRAY)
                }

                override fun onItemViewChanging(
                    pos: Int,
                    positionOffset: Float,
                    currentItem: AppCompatTextView
                ) {
                }

                override fun getItemCount(): Int {
                    return titleData.size
                }

                override fun getItem(pos: Int): String {
                    return titleData[pos]
                }

                override fun onItemViewEndScroll(pos: Int, currentItem: AppCompatTextView) {
                    currentItem.setTextColor(Color.BLACK)
                }

            }
        indicatorView.registerViewPager2(viewPager2)
    }
}