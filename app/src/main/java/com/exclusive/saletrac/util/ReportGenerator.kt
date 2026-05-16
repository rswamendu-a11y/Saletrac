package com.exclusive.saletrac.util

import android.content.Context
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.exclusive.saletrac.data.entity.Transaction
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import org.json.JSONObject

class ReportGenerator(private val context: Context, private val analyticsEngine: SalesAnalyticsEngine) {

    fun generateReport(
        transactions: List<Transaction>,
        lmtdTransactions: List<Transaction>,
        reportTitle: String
    ): File? {
        SharingManager.cleanupOldReports(context)

        val reportsDir = File(context.cacheDir, "reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }

        val fileName = "SalesReport_${System.currentTimeMillis()}.pdf"
        val pdfFile = File(reportsDir, fileName)

        try {
            val writer = PdfWriter(pdfFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)
            document.setMargins(36f, 36f, 36f, 36f)

            // Header: Shop Name, Date Range, Report Type
            document.add(
                Paragraph("M/S EXCLUSIVE")
                    .setBold()
                    .setFontSize(20f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            document.add(
                Paragraph(reportTitle)
                    .setFontSize(14f)
                    .setTextAlignment(TextAlignment.CENTER)
            )

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val dateRangeStr = if (transactions.isNotEmpty()) {
                val minDate = transactions.minOf { it.timestamp }
                val maxDate = transactions.maxOf { it.timestamp }
                "Date Range: " + dateFormat.format(Date(minDate)) + " to " + dateFormat.format(Date(maxDate))
            } else {
                "Date Range: No transactions"
            }
            document.add(Paragraph(dateRangeStr).setFontSize(12f).setTextAlignment(TextAlignment.CENTER))
            document.add(Paragraph("Generated on: " + dateFormat.format(Date())).setFontSize(10f).setTextAlignment(TextAlignment.CENTER).setMarginBottom(20f))

            // --- Section 1: Summary ---
            val totalUnits = transactions.size
            val totalValue = transactions.sumOf { it.price }
            val lmtdValue = lmtdTransactions.sumOf { it.price }
            val growth = analyticsEngine.calculateGrowth(totalValue, lmtdValue)

            document.add(Paragraph("Executive Summary").setBold().setFontSize(14f))
            val summaryTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
            summaryTable.addCell(createCell("Total Units Sold", true))
            summaryTable.addCell(createCell(totalUnits.toString()))
            summaryTable.addCell(createCell("Total Value", true))
            summaryTable.addCell(createCell(CurrencyFormatter.formatINR(totalValue)))
            summaryTable.addCell(createCell("Growth vs LMTD", true))

            val growthStr = String.format("%.1f%%", growth)
            val growthCell = createCell(if (growth >= 0) "+$growthStr" else growthStr)
            if (growth >= 0) growthCell.setFontColor(ColorConstants.GREEN) else growthCell.setFontColor(ColorConstants.RED)
            summaryTable.addCell(growthCell)

            document.add(summaryTable)
            document.add(Paragraph("\n"))

            document.add(Paragraph("Payment Modes Breakdown").setBold().setFontSize(12f))
            val paymentTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()
            paymentTable.addHeaderCell(createCell("Mode", true, ColorConstants.LIGHT_GRAY))
            paymentTable.addHeaderCell(createCell("Value", true, ColorConstants.LIGHT_GRAY))

            val aggregatedPayments = mutableMapOf<String, Double>()
            for (tx in transactions) {
                try {
                    val json = org.json.JSONObject(tx.paymentMode)
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        val key = keys.next() as String
                        val value = json.getDouble(key)
                        aggregatedPayments[key] = aggregatedPayments.getOrDefault(key, 0.0) + value
                    }
                } catch (e: Exception) {}
            }
            for ((mode, amount) in aggregatedPayments.toSortedMap()) {
                paymentTable.addCell(createCell(mode))
                paymentTable.addCell(createCell(CurrencyFormatter.formatINR(amount)))
            }
            document.add(paymentTable)
            document.add(Paragraph("\n"))


            // --- Section 2: Segmentation ---
            document.add(Paragraph("Price Segmentation").setBold().setFontSize(14f))
            val segTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 30f, 30f))).useAllAvailableWidth()
            segTable.addHeaderCell(createCell("Segment", true, ColorConstants.LIGHT_GRAY))
            segTable.addHeaderCell(createCell("Units", true, ColorConstants.LIGHT_GRAY))
            segTable.addHeaderCell(createCell("Value", true, ColorConstants.LIGHT_GRAY))

            val segments = transactions.groupBy { analyticsEngine.getPriceSegment(it.price) }
            for ((segment, segmentTxs) in segments.toSortedMap()) { // Using tree map for natural order
                val segUnits = segmentTxs.size
                val segValue = segmentTxs.sumOf { it.price }
                segTable.addCell(createCell(segment))
                segTable.addCell(createCell(segUnits.toString()))
                segTable.addCell(createCell(CurrencyFormatter.formatINR(segValue)))
            }
            document.add(segTable)

            // Force a new page for Master Log to keep it modular
            document.add(com.itextpdf.layout.element.AreaBreak(com.itextpdf.layout.properties.AreaBreakType.NEXT_PAGE))

            // --- Section 3: Master Log ---
            document.add(Paragraph("Master Transaction Log").setBold().setFontSize(14f))

            // Date(15), Model(25), IMEI(20), Customer(15), Mode(15), Value(10)
            val logTable = Table(UnitValue.createPercentArray(floatArrayOf(15f, 25f, 20f, 15f, 15f, 10f))).useAllAvailableWidth()
            logTable.addHeaderCell(createCell("Date", true, ColorConstants.LIGHT_GRAY))
            logTable.addHeaderCell(createCell("Model", true, ColorConstants.LIGHT_GRAY))
            logTable.addHeaderCell(createCell("IMEI", true, ColorConstants.LIGHT_GRAY))
            logTable.addHeaderCell(createCell("Customer", true, ColorConstants.LIGHT_GRAY))
            logTable.addHeaderCell(createCell("Mode", true, ColorConstants.LIGHT_GRAY))
            logTable.addHeaderCell(createCell("Value", true, ColorConstants.LIGHT_GRAY))

            val logDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            for (tx in transactions) {
                logTable.addCell(createCell(logDateFormat.format(Date(tx.timestamp))))
                // Strict wrapping is implicit in iText 7, but we can enforce it by ensuring no word split issues
                logTable.addCell(createCell(tx.model))
                // Keep IMEI strictly on one line if possible, or wrapped gracefully.
                // iText auto-wraps based on column width and font size. We use a smaller font size here.
                logTable.addCell(createCell(tx.imei, fontSize = 8f))
                logTable.addCell(createCell(tx.customerName, fontSize = 8f))

                var displayMode = tx.paymentMode
                try {
                    val json = org.json.JSONObject(tx.paymentMode)
                    val modes = mutableListOf<String>()
                    val keys = json.keys()
                    while (keys.hasNext()) {
                        modes.add(keys.next() as String)
                    }
                    displayMode = modes.joinToString(", ")
                } catch(e: Exception) {}

                logTable.addCell(createCell(displayMode, fontSize = 8f))
                logTable.addCell(createCell(CurrencyFormatter.formatINR(tx.price), fontSize = 8f))
            }
            document.add(logTable)

            document.close()
            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun createCell(
        content: String,
        isBold: Boolean = false,
        bgColor: com.itextpdf.kernel.colors.Color? = null,
        fontSize: Float = 10f
    ): Cell {
        val paragraph = Paragraph(content).setFontSize(fontSize)
        if (isBold) paragraph.setBold()

        val cell = Cell().add(paragraph)
        if (bgColor != null) {
            cell.setBackgroundColor(bgColor)
        }
        // Strict wrapping: Keep together prevents row breaking across pages badly,
        // while iText inherently wraps text inside cells based on UnitValue percentages.
        cell.setKeepTogether(true)
        return cell
    }
}
