package be.buithg.etghaifgte.presentation.ui.adapters

import android.graphics.Color
import android.content.res.ColorStateList
import android.util.Log
import be.buithg.etghaifgte.databinding.MatchItemBinding


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.time.format.DateTimeFormatter
import be.buithg.etghaifgte.utils.parseUtcToLocal

import be.buithg.etghaifgte.domain.model.Match

class MatchAdapter(
    private val items: ArrayList<Match>,
    private val onItemClick: (Match) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val MAX_TEAM_LEN = 12
        const val MAX_STATUS_LEN = 15
        const val TYPE_MATCH = 0
        const val TYPE_EMPTY = 1
    }

    private val leagueColors = listOf("#D2F61D", "#F6771D", "#1DF6BC", "#D2F61D")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_MATCH) {
            val binding = MatchItemBinding.inflate(inflater, parent, false)
            MatchViewHolder(binding)
        } else {
            val binding = be.buithg.etghaifgte.databinding.ItemEmptyStateBinding.inflate(
                inflater,
                parent,
                false
            )
            EmptyViewHolder(binding)
        }
    }

    override fun getItemCount(): Int {
        val cnt = if (items.isEmpty()) 1 else items.size
        Log.d("MatchAdapter", "getItemCount = $cnt")
        return cnt
    }
    override fun getItemViewType(position: Int): Int = if (items.isEmpty()) TYPE_EMPTY else TYPE_MATCH

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MatchViewHolder) {
            Log.d("MatchAdapter", "  binding item: ${items[position]}")
            val item = items[position]
            holder.bind(item, position)
            holder.itemView.setOnClickListener { onItemClick(item) }
        }
    }

    inner class MatchViewHolder(
        private val binding: MatchItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Match, position: Int) {
            val ldt = item.dateTimeGMT.parseUtcToLocal()
            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            binding.tvTime.text = ldt?.format(timeFormatter) ?: "12:29"

            val statusText = if (!item.matchEnded) "Upcoming" else item.status ?: "-"
            binding.tvStatus.text = statusText.truncate(MAX_STATUS_LEN)

            binding.tvLeague.text = item.league ?: "-"
            val color = Color.parseColor(leagueColors[position % leagueColors.size])
            binding.tvLeague.backgroundTintList = ColorStateList.valueOf(color)

            val t1 = item.teamA.orEmpty().truncate(MAX_TEAM_LEN)
            val t2 = item.teamB.orEmpty().truncate(MAX_TEAM_LEN)
            binding.tvMatchDescription.text = "$t1 – $t2"
        }

        private fun String.truncate(max: Int): String =
            if (length > max) take(max) + "…" else this
    }

    inner class EmptyViewHolder(binding: be.buithg.etghaifgte.databinding.ItemEmptyStateBinding) :
        RecyclerView.ViewHolder(binding.root)
}


