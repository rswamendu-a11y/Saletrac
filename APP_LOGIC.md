# SWAMENDU ROY - M/S EXCLUSIVE: App Business Logic

## 1. Data Integrity & IMEI Rules
- IMEI Input: Must support both Barcode Scanning and Manual Entry.
- Validation: Every IMEI must be exactly 15 digits.
- Uniqueness: Database must block any attempt to save a duplicate IMEI.
- History: No end-date limit. Data must remain searchable and persistent for decades (2025, 2030, 3000+).

## 2. Dynamic Price Segmentation Logic
Every sale must be automatically categorized into these specific segments based on Value:
- 0 - 10,000 (Entry)
- 10,000 - 15,000 (Budget)
- 15,000 - 20,000 (Lower Mid)
- 20,000 - 25,000
- 25,000 - 30,000
- 30,000 - 35,000
- 35,000 - 40,000
- 40,000 - 45,000
- 45,000 - 50,000
- 50,000 - 1,00,000 (Premium)
- 1,00,000 - 2,00,000 (Luxury)
- 2,00,000+ (Ultra-Luxury)

## 3. Payment & Transaction Logic
- Modes: Cash, Card, Finance (Bajaj, IDFC, HDB, etc.).
- Split Payments: Allow a single sale to be split across multiple modes (e.g., Cash + Finance).
- Metrics: Track FTD (For The Day), MTD (Month to Date), LMTD (Last Month to Date), and YTD (Year to Date).

## 4. Robust Reporting Engine (PDF Export)
- Modular PDF Layout:
  - Page 1: Executive Summary (Key metrics & Growth %).
  - Page 2: Visual Charts (Pie Chart for Brand Share, Horizontal Bars for Price Segments).
  - Page 3+: Detailed Transaction Log (Date, IMEI, Model, Customer, Mode, Value).
- Filters: Allow reports to be generated for any custom Date Range.

## 5. Security & Audit
- Admin Role: Password protected access to delete/edit and see total profit.
- Audit Log: Record all deletions and edits with timestamps.
