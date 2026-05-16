package com.exclusive.saletrac.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.exclusive.saletrac.R

sealed class DashboardItem {
    data class Summary(val ftdValue: String, val mtdValue: String, val growthPercent: Double) : DashboardItem()
    data class BrandShare(val brandData: Map<String, Double>) : DashboardItem()
    data class PriceSegments(val segmentData: Map<String, Int>) : DashboardItem()
}

class DashboardAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<DashboardItem> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    companion object {
        private const val TYPE_SUMMARY = 0
        private const val TYPE_BRAND_SHARE = 1
        private const val TYPE_PRICE_SEGMENTS = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DashboardItem.Summary -> TYPE_SUMMARY
            is DashboardItem.BrandShare -> TYPE_BRAND_SHARE
            is DashboardItem.PriceSegments -> TYPE_PRICE_SEGMENTS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SUMMARY -> SummaryViewHolder(inflater.inflate(R.layout.item_dashboard_summary, parent, false))
            TYPE_BRAND_SHARE -> BrandShareViewHolder(inflater.inflate(R.layout.item_dashboard_piechart, parent, false))
            TYPE_PRICE_SEGMENTS -> PriceSegmentViewHolder(inflater.inflate(R.layout.item_dashboard_barchart, parent, false))
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SummaryViewHolder -> holder.bind(item as DashboardItem.Summary)
            is BrandShareViewHolder -> holder.bind(item as DashboardItem.BrandShare)
            is PriceSegmentViewHolder -> holder.bind(item as DashboardItem.PriceSegments)
        }
    }

    override fun getItemCount(): Int = items.size

    class SummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvFtdValue: TextView = view.findViewById(R.id.tvFtdValue)
        private val tvMtdValue: TextView = view.findViewById(R.id.tvMtdValue)
        private val tvGrowth: TextView = view.findViewById(R.id.tvGrowth)

        fun bind(item: DashboardItem.Summary) {
            tvFtdValue.text = item.ftdValue
            tvMtdValue.text = item.mtdValue

            val formattedGrowth = String.format("%.1f%%", item.growthPercent)
            if (item.growthPercent >= 0) {
                tvGrowth.text = "+$formattedGrowth"
                tvGrowth.setTextColor(Color.parseColor("#388E3C")) // Green
            } else {
                tvGrowth.text = formattedGrowth
                tvGrowth.setTextColor(Color.parseColor("#D32F2F")) // Red
            }
        }
    }

    class BrandShareViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val pieChart: PieChart = view.findViewById(R.id.pieChart)

        fun bind(item: DashboardItem.BrandShare) {
            val entries = item.brandData.map { PieEntry(it.value.toFloat(), it.key) }
            val dataSet = PieDataSet(entries, "")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            dataSet.valueTextSize = 12f
            dataSet.valueTextColor = Color.BLACK

            pieChart.data = PieData(dataSet)
            pieChart.description.isEnabled = false
            pieChart.isDrawHoleEnabled = true
            pieChart.setUsePercentValues(true)
            pieChart.legend.isWordWrapEnabled = true
            pieChart.invalidate()
        }
    }

    class PriceSegmentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val barChart: HorizontalBarChart = view.findViewById(R.id.barChart)

        fun bind(item: DashboardItem.PriceSegments) {
            // Sort segments by predefined order or rely on given order
            val segments = item.segmentData.keys.toList()
            val entries = item.segmentData.values.mapIndexed { index, count ->
                BarEntry(index.toFloat(), count.toFloat())
            }

            val dataSet = BarDataSet(entries, itemView.context.getString(R.string.units_sold))
            dataSet.colors = ColorTemplate.PASTEL_COLORS.toList()
            dataSet.valueTextSize = 10f

            barChart.data = BarData(dataSet)
            barChart.description.isEnabled = false

            // X-Axis Configuration to prevent label clipping
            val xAxis = barChart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(segments)
            xAxis.granularity = 1f
            xAxis.labelCount = segments.size

            // Critical: Add extra offset to the left so long text (like "20,000 - 25,000") fits perfectly
            barChart.setExtraOffsets(0f, 0f, 0f, 10f) // Add slight bottom offset too if needed
            xAxis.textSize = 10f
            // To ensure the actual label area width is computed enough for large strings:
            barChart.extraLeftOffset = 80f

            barChart.axisLeft.axisMinimum = 0f
            barChart.axisRight.isEnabled = false
            barChart.invalidate()
        }
    }
}
