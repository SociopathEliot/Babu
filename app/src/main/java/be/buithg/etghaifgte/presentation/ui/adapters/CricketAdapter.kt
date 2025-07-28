package be.buithg.etghaifgte.presentation.ui.adapters

import android.graphics.Color
import android.content.res.ColorStateList
import be.buithg.etghaifgte.databinding.MatchItemBinding


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CricketAdapter(
    private val items: ArrayList<Data>,
    private val onItemClick: (Data) -> Unit
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
            CricketViewHolder(binding)
        } else {
            val binding = be.buithg.etghaifgte.databinding.ItemEmptyStateBinding.inflate(
                inflater,
                parent,
                false
            )
            EmptyViewHolder(binding)
        }
    }

    override fun getItemCount(): Int = if (items.isEmpty()) 1 else items.size

    override fun getItemViewType(position: Int): Int = if (items.isEmpty()) TYPE_EMPTY else TYPE_MATCH

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CricketViewHolder) {
            val item = items[position]
            holder.bind(item, position)
            holder.itemView.setOnClickListener { onItemClick(item) }
        }
    }

    inner class CricketViewHolder(
        private val binding: MatchItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Data, position: Int) {
//            // 1) Время
//            val ldt = runCatching { LocalDateTime.parse(item.dateTimeGMT ?: "") }.getOrNull()
//            val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
//            binding.tvTime.text = ldt?.format(timeFormatter) ?: "-"
//
//            // 2) Статус
//            val statusText = if (!item.matchEnded) "Upcoming" else item.status ?: "-"
//            binding.tvStatus.text = statusText.truncate(MAX_STATUS_LEN)
//
//            // 3) Лига
//            val country = item.teamInfo?.getOrNull(0)?.name
//                ?: item.teams?.getOrNull(0).orEmpty()
//            binding.tvLeague.text = country
//            val color = Color.parseColor(leagueColors[position % leagueColors.size])
//            binding.tvLeague.backgroundTintList = ColorStateList.valueOf(color)
//
//            // 4) Описание матча (один TextView вместо двух)
//            val rawTeam1 = item.teamInfo?.getOrNull(0)?.shortname
//                ?: item.teams?.getOrNull(0).orEmpty()
//            val rawTeam2 = item.teamInfo?.getOrNull(1)?.shortname
//                ?: item.teams?.getOrNull(1).orEmpty()
//
//            val t1 = rawTeam1.truncate(MAX_TEAM_LEN)
//            val t2 = rawTeam2.truncate(MAX_TEAM_LEN)
//            binding.tvMatchDescription.text = "$t1 – $t2"
        }

        private fun String.truncate(max: Int): String =
            if (length > max) take(max) + "…" else this
        }
    inner class EmptyViewHolder(binding: be.buithg.etghaifgte.databinding.ItemEmptyStateBinding) :
        RecyclerView.ViewHolder(binding.root)
}


