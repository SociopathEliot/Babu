package be.buithg.etghaifgte.presentation.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import be.buithg.etghaifgte.data.local.entity.PredictionEntity
import be.buithg.etghaifgte.databinding.ItemHistoryPredictionBinding
import be.buithg.etghaifgte.databinding.ItemPredictionBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import be.buithg.etghaifgte.utils.parseUtcToLocal

class PredictionsAdapter(
    private val items: List<PredictionEntity>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private companion object {
        const val TYPE_ITEM = 0
        const val TYPE_EMPTY = 1
    }

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    inner class PredictionViewHolder(
        private val binding: ItemPredictionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PredictionEntity) {
            val dt = item.dateTime.parseUtcToLocal()
            
            binding.textTime.text = dt?.format(timeFormatter) ?: item.dateTime
            binding.textDate.text = dt?.format(dateFormatter) ?: item.dateTime

            binding.textPrediction.text = item.pick // or "Pick: ${item.pick}"

            binding.textTeams.text = "${item.teamA} - ${item.teamB}"
            val isUpcoming = if (item.upcoming == 1) {
                true
            } else {
                dt?.isAfter(LocalDateTime.now()) ?: false
            }

            if (isUpcoming) {
                binding.textPrediction.text = "Upcoming"
                binding.imageResult.setImageResource(be.buithg.etghaifgte.R.drawable.icon_upcoming)
            } else {
                val result = when (item.wonMatches) {
                    1 -> if (item.pick == item.teamA) "Win" else "Lose"
                    2 -> if (item.pick == item.teamB) "Win" else "Lose"
                    else -> "Draw"
                }
                binding.textPrediction.text = "Your Pick: $result"

                val imageRes = if (result == "Lose") {
                    be.buithg.etghaifgte.R.drawable.ic_check_red
                } else {
                    be.buithg.etghaifgte.R.drawable.ic_check_green
                }
                binding.imageResult.setImageResource(imageRes)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_ITEM) {
            val binding = ItemPredictionBinding.inflate(inflater, parent, false)
            PredictionViewHolder(binding)
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
        if (holder is PredictionViewHolder) {
            holder.bind(items[position])
        }
    }

    inner class EmptyViewHolder(binding: be.buithg.etghaifgte.databinding.ItemEmptyStateBinding) :
        RecyclerView.ViewHolder(binding.root)
}
