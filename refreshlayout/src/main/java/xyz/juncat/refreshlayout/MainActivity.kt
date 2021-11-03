package xyz.juncat.refreshlayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jianjun.base.adapter.BaseAdapter
import com.jianjun.base.adapter.BaseViewHolder
import com.jianjun.base.ext.layout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initRecyclerView()
        val refreshLayout = findViewById<RefreshLayout>(R.id.refresh)
        refreshLayout.refreshCallback = object : RefreshLayout.OnRefreshCallback {
            override fun onRefreshTriggered() {
                refreshLayout.hideRefreshHeader()
            }
        }
    }

    private fun initRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = Adapter()
        recyclerView.adapter = adapter
        val list = ArrayList<Int>()
        for (index in 0..100) {
            list.add(index)
        }
        recyclerView.post {
            adapter.update(list)
        }
    }

    class ViewHolder(view: View) : BaseViewHolder<Int>(view) {
        override fun onBindingView(data: Int, pos: Int) {
            (itemView as TextView).setText(data.toString())
        }
    }

    class Adapter : BaseAdapter<Int, ViewHolder>() {
        override fun onDiffCallback(): DiffUtil.ItemCallback<Int> {
            return object : DiffUtil.ItemCallback<Int>() {
                override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }

                override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
                    return oldItem == newItem
                }

            }
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int, data: Int) {
            holder.onBindingView(data, position)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(TextView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                setTextColor(ContextCompat.getColor(parent.context, R.color.purple_200))
            })
        }

    }
}