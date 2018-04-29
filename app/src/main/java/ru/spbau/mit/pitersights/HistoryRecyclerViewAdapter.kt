package ru.spbau.mit.pitersights

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import ru.spbau.mit.pitersights.HistoryFragment.OnHistoryFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_history.view.*
import ru.spbau.mit.pitersights.core.Sight

class HistoryRecyclerViewAdapter(
        private val mValues: List<Sight>,
        private val mListener: OnHistoryFragmentInteractionListener?)
    : RecyclerView.Adapter<HistoryRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Sight
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onHistoryFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.labelView.text = item.name
        holder.contentView.setImageResource(R.drawable.logo)
//        holder.contentView.image
//        holder.mIdView.text = item.id
//        holder.mContentView.text = item.label

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
//        val mIdView: TextView = mView.item_number
//        val mContentView: TextView = mView.label
        val labelView: TextView = mView.history_text
        val contentView: ImageView = mView.history_image_content

        override fun toString(): String {
            return super.toString() + " '" + labelView.text + "': " + contentView.toString()
        }
    }
}
