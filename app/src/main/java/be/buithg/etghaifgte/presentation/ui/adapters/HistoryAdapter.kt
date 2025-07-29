package be.buithg.etghaifgte.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.databinding.ItemHistoryPredictionBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import be.buithg.etghaifgte.utils.parseUtcToLocal

class HistoryAdapter(
    private val items: List<PredictionEntity>,
    private val onViewDetails: (PredictionEntity) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_ITEM = 0
        const val TYPE_EMPTY = 1
    }

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    inner class HistoryViewHolder(private val binding: ItemHistoryPredictionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: PredictionEntity) {
            val dt = item.dateTime.parseUtcToLocal()

            binding.textTime.text = dt?.format(timeFormatter) ?: item.dateTime
            binding.textDate.text = dt?.format(dateFormatter) ?: item.dateTime

            binding.textTeams.text = "${item.teamA} - ${item.teamB}"

            val isUpcoming = if (item.upcoming == 1) {
                true
            } else {
                dt?.isAfter(LocalDateTime.now()) ?: false
            }

            val result = if (isUpcoming) {
                "Upcoming"
            } else {
                when (item.wonMatches) {
                    1 -> if (item.pick == item.teamA) "Wins" else "Lose"
                    2 -> if (item.pick == item.teamB) "Wins" else "Lose"
                    else -> "Draw"
                }
            }
            binding.textPick.text = "Pick: ${item.pick} $result"
            binding.textResult.text = result

            binding.btnViewDetails.setOnClickListener {
                onViewDetails(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ITEM) {
            val binding = ItemHistoryPredictionBinding.inflate(inflater, parent, false)
            HistoryViewHolder(binding)
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

    override fun getItemViewType(position: Int): Int = if (items.isEmpty()) TYPE_EMPTY else TYPE_ITEM

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HistoryViewHolder) {
            holder.bind(items[position])
        }
    }

    inner class EmptyViewHolder(binding: be.buithg.etghaifgte.databinding.ItemEmptyStateBinding) :
        RecyclerView.ViewHolder(binding.root)
}