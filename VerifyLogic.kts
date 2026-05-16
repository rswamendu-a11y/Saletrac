import java.io.File

fun getPriceSegment(price: Double): String {
    return when {
        price < 0 -> "Unknown"
        price <= 10000.0 -> "0 - 10,000 (Entry)"
        price <= 15000.0 -> "10,000 - 15,000 (Budget)"
        price <= 20000.0 -> "15,000 - 20,000 (Lower Mid)"
        price <= 25000.0 -> "20,000 - 25,000"
        price <= 30000.0 -> "25,000 - 30,000"
        price <= 35000.0 -> "30,000 - 35,000"
        price <= 40000.0 -> "35,000 - 40,000"
        price <= 45000.0 -> "40,000 - 45,000"
        price <= 50000.0 -> "45,000 - 50,000"
        price <= 100000.0 -> "50,000 - 1,00,000 (Premium)"
        price <= 200000.0 -> "1,00,000 - 2,00,000 (Luxury)"
        else -> "2,00,000+ (Ultra-Luxury)"
    }
}

println("Segment for ₹27,500: ${getPriceSegment(27500.0)}")
