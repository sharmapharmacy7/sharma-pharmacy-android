package in.sharmapharmacy.app

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class MainActivity : ComponentActivity() {
    // Confirm this WhatsApp number before publishing. International format without +.
    private val whatsappNumber = "918279846477"
    private val stock = mutableListOf<Medicine>()
    private var prescriptionUri: Uri? = null
    private var selectedCsvName = ""

    data class Medicine(val name: String, val quantity: String, val price: String)

    private val csvPicker = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            readCsv(uri)
        }
    }

    private val imagePicker = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            prescriptionUri = uri
            findViewById<TextView>(R.id.prescriptionInfo).text = "Prescription photo selected. Tap WhatsApp to send it."
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.importCsvButton).setOnClickListener {
            csvPicker.launch(arrayOf("text/*", "text/csv", "application/csv", "application/vnd.ms-excel"))
        }
        findViewById<Button>(R.id.pickPrescriptionButton).setOnClickListener {
            imagePicker.launch(arrayOf("image/*"))
        }
        findViewById<Button>(R.id.whatsappButton).setOnClickListener { sendWhatsApp() }
        findViewById<Button>(R.id.deliveryButton).setOnClickListener {
            Toast.makeText(this, "Call Sharma Pharmacy: 8279846477. Confirm delivery area and charges with the pharmacy.", Toast.LENGTH_LONG).show()
        }
        findViewById<EditText>(R.id.searchInput).addTextChangedListener(SimpleTextWatcher {
            renderMedicines(it)
        })
        renderMedicines("")
    }

    private fun readCsv(uri: Uri) {
        try {
            stock.clear()
            contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).useLines { lines ->
                    lines.forEachIndexed { index, raw ->
                        val line = raw.trim().removePrefix("\uFEFF")
                        if (line.isBlank()) return@forEachIndexed
                        val cells = splitCsvLine(line)
                        if (index == 0 && cells.firstOrNull()?.contains("name", true) == true) return@forEachIndexed
                        if (cells.isNotEmpty()) {
                            val name = cells.getOrNull(0).orEmpty().trim().trim('"')
                            if (name.isNotBlank()) {
                                stock.add(Medicine(name, cells.getOrNull(1).orEmpty().trim().trim('"'), cells.getOrNull(2).orEmpty().trim().trim('"')))
                            }
                        }
                    }
                }
            }
            selectedCsvName = uri.lastPathSegment ?: "CSV"
            findViewById<TextView>(R.id.stockInfo).text = "Imported ${stock.size} rows from $selectedCsvName"
            renderMedicines(findViewById<EditText>(R.id.searchInput).text.toString())
            Toast.makeText(this, "Stock CSV imported", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "CSV read failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Handles simple CSV rows with quoted commas.
    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val cell = StringBuilder()
        var quoted = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (quoted && i + 1 < line.length && line[i + 1] == '"') {
                    cell.append('"'); i++
                } else quoted = !quoted
            } else if (c == ',' && !quoted) {
                result.add(cell.toString()); cell.setLength(0)
            } else cell.append(c)
            i++
        }
        result.add(cell.toString())
        return result
    }

    private fun renderMedicines(query: String) {
        val out = findViewById<TextView>(R.id.medicineResults)
        if (stock.isEmpty()) {
            out.text = "No stock file loaded. Admin: import a CSV with columns: Medicine Name, Stock, Price"
            return
        }
        val filtered = stock.filter { it.name.contains(query.trim(), ignoreCase = true) }.take(100)
        out.text = if (filtered.isEmpty()) "No matching medicines." else filtered.joinToString("\n\n") {
            "💊 ${it.name}\nStock: ${it.quantity.ifBlank { "Not listed" }}   Price: ${it.price.ifBlank { "Confirm at pharmacy" }}"
        }
    }

    private fun sendWhatsApp() {
        val query = findViewById<EditText>(R.id.searchInput).text.toString().trim()
        val message = "Namaste Sharma Pharmacy Modinagar, mujhe order karna hai.\nMedicine/search: ${query.ifBlank { "Please check attached prescription" }}\nPlease confirm availability, price and home delivery."
        val uri = prescriptionUri
        try {
            val intent = if (uri != null) {
                Intent(Intent.ACTION_SEND).apply {
                    type = contentResolver.getType(uri) ?: "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, message)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    setPackage("com.whatsapp")
                }
            } else {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsappNumber?text=${Uri.encode(message)}"))
            }
            startActivity(intent)
        } catch (_: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsappNumber?text=${Uri.encode(message)}"))
            startActivity(fallback)
            Toast.makeText(this, "If photo was selected, attach it in WhatsApp if needed.", Toast.LENGTH_LONG).show()
        }
    }
}

private class SimpleTextWatcher(val onText: (String) -> Unit) : android.text.TextWatcher {
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { onText(s?.toString().orEmpty()) }
    override fun afterTextChanged(s: android.text.Editable?) {}
}
