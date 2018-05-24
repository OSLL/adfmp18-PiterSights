package ru.spbau.mit.pitersights

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import ru.spbau.mit.pitersights.core.Sight

class HistoryFragment : Fragment() {
    private var columnCount: Int = 1

    private fun updateAdapterWithView(view: RecyclerView) {
        view.invalidate()
        view.swapAdapter(HistoryRecyclerViewAdapter(sights, listener),  false)
        view.invalidate()
        view.swapAdapter(HistoryRecyclerViewAdapter(sights, listener),  true)
        view.invalidate()
        view.adapter.notifyDataSetChanged()
        view.invalidate()
    }

    @Volatile var sights: List<Sight> = emptyList()
        set(value) {
            field = sortSights(value)
            if (view != null) {
                updateAdapterWithView(view as RecyclerView)
            }
        }

    private var listener: OnHistoryFragmentInteractionListener? = null

    private fun sortSights(sights: List<Sight>) : List<Sight> {
        return sights.sortedWith(Sight.COMPARATOR).reversed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history_list, container, false) as RecyclerView

        with(view) {
            layoutManager = when {
                columnCount <= 1 -> LinearLayoutManager(context)
                else -> GridLayoutManager(context, columnCount)
            }
            adapter = HistoryRecyclerViewAdapter(sights, listener)
        }
        updateAdapterWithView(view)
        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnHistoryFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnHistoryFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    interface OnHistoryFragmentInteractionListener : PhotoProvider {
        fun onHistoryFragmentInteraction(sight: Sight?)
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
                HistoryFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
