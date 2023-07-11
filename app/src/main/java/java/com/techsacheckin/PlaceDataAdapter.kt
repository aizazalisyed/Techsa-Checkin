package java.com.techsacheckin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlaceDataAdapter(private val placeList: MutableList<PlaceData>) :
    RecyclerView.Adapter<PlaceDataAdapter.PlaceViewHolder>() {

    // ViewHolder class for each item in the RecyclerView
    inner class PlaceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initialize the views in the ViewHolder
        val nameTextView: TextView = itemView.findViewById(R.id.placeName)
        val temperatureTextView: TextView = itemView.findViewById(R.id.temprature)
        val conditionTextView: TextView = itemView.findViewById(R.id.condition)
        val checkInTimeTextView: TextView = itemView.findViewById(R.id.checkin_time)
        val checkInPurposeTextView: TextView = itemView.findViewById(R.id.checkInText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        // Inflate the item layout and create a ViewHolder
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return PlaceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        // Bind the data to the views in the ViewHolder
        val place = placeList[position]
        holder.nameTextView.text = place.name
        holder.temperatureTextView.text = "Temperature: ${place.temp_c}°C"
        holder.conditionTextView.text = "Condition: ${place.condition}"
        holder.checkInTimeTextView.text = "Check-in Time: ${place.checkInTime}"
        holder.checkInPurposeTextView.text = "Check-in Purpose: ${place.checkInPurpose}"
    }

    override fun getItemCount(): Int {
        // Return the number of items in the list
        return placeList.size
    }
}
