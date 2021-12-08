package xyz.juncat.custom_layoutmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = VerticalLayoutManager()
        val adapter = object : RecyclerView.Adapter<Holder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
                Log.i(TAG, "onCreateViewHolder: ")
                return Holder(TextView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100)
                    gravity = Gravity.CENTER
                })
            }

            override fun onBindViewHolder(holder: Holder, position: Int) {
                (holder.itemView as TextView).text = "$position"
                Log.i(TAG, "onBindViewHolder: $position")
            }

            override fun getItemCount(): Int {
                return 500
            }

        }
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.btn_scroll_to).setOnClickListener {
            recyclerView.scrollToPosition(10)
        }

        findViewById<Button>(R.id.btn_smooth_scroll).setOnClickListener {
            recyclerView.smoothScrollToPosition(10)
        }
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    companion object {
        private const val TAG = "MainActivity"
    }
}