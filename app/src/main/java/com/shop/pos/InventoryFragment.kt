package com.shop.pos

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class InventoryFragment : Fragment(), InventoryItemListener {

    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var inventoryAdapter: InventoryAdapter
    private lateinit var fabAddItem: FloatingActionButton
    private lateinit var buttonImportCsv: Button
    private lateinit var inventoryRepository: InventoryRepository

    private var inventoryItems = mutableListOf<InventoryItem>()
    private var selectedImageUri: Uri? = null
    private lateinit var dialogImageViewPreview: ImageView

    // --- Image picker launcher ---
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val contentResolver = requireActivity().contentResolver
                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(uri, takeFlags)
                selectedImageUri = uri
                dialogImageViewPreview.load(uri)
            }
        }
    }

    // --- CSV file picker launcher ---
    private val csvPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri ->
                readCsvFile(uri)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dao = (requireActivity().application as PosApplication).database.inventoryDao()
        inventoryRepository = InventoryRepository(dao)

        inventoryRecyclerView = view.findViewById(R.id.recyclerViewInventory)
        fabAddItem = view.findViewById(R.id.fabAddItem)
        buttonImportCsv = view.findViewById(R.id.buttonImportCsv)

        inventoryAdapter = InventoryAdapter(inventoryItems, this)
        inventoryRecyclerView.adapter = inventoryAdapter
        inventoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fabAddItem.setOnClickListener {
            showAddItemDialog()
        }

        buttonImportCsv.setOnClickListener {
            openCsvFilePicker()
        }
    }

    override fun onResume() {
        super.onResume()
        loadInventoryItems()
    }

    // --- CSV File Picker ---
    private fun openCsvFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/*"   // some devices don't recognize text/csv
        }
        csvPickerLauncher.launch(intent)
    }

    // --- CSV File Reader ---
    private fun readCsvFile(uri: Uri) {
        val itemsToImport = mutableListOf<InventoryItem>()
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // skip header
            reader.readLine()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(",")
                if (tokens.size >= 5) {
                    val item = InventoryItem(
                        name = tokens[0].trim(),
                        code = tokens[1].trim().ifEmpty { null },
                        stockQuantity = tokens[2].trim().toIntOrNull() ?: 0,
                        price = tokens[3].trim().toDoubleOrNull() ?: 0.0,
                        costPrice = tokens[4].trim().toDoubleOrNull() ?: 0.0
                    )
                    itemsToImport.add(item)
                }
            }
            reader.close()

            if (itemsToImport.isNotEmpty()) {
                lifecycleScope.launch {
                    itemsToImport.forEach { inventoryRepository.addInventoryItem(it) }
                    loadInventoryItems()
                    Toast.makeText(requireContext(), "${itemsToImport.size} items imported successfully!", Toast.LENGTH_SHORT).show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Error reading CSV file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- Load all items from DB ---
    private fun loadInventoryItems() {
        lifecycleScope.launch {
            val itemsFromDb = inventoryRepository.getInventoryItems()
            inventoryItems.clear()
            inventoryItems.addAll(itemsFromDb)
            activity?.runOnUiThread {
                inventoryAdapter.notifyDataSetChanged()
            }
        }
    }

    // --- Add/Edit Dialog ---
    private fun showAddItemDialog(position: Int = -1) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_item, null)

        dialogImageViewPreview = dialogView.findViewById(R.id.imageViewPreview)
        val buttonChooseImage = dialogView.findViewById<Button>(R.id.buttonChooseImage)
        val editTextItemName = dialogView.findViewById<EditText>(R.id.editTextItemName)
        val editTextItemCode = dialogView.findViewById<EditText>(R.id.editTextItemCode)
        val editTextQuantity = dialogView.findViewById<EditText>(R.id.editTextQuantity)
        val editTextPrice = dialogView.findViewById<EditText>(R.id.editTextPrice)
        val editTextCostPrice = dialogView.findViewById<EditText>(R.id.editTextCostPrice)
        // ++ Find new views for wholesale
        val editTextWholesaleQty = dialogView.findViewById<EditText>(R.id.editTextWholesaleQty)
        val editTextWholesalePrice = dialogView.findViewById<EditText>(R.id.editTextWholesalePrice)
        val switchForSale = dialogView.findViewById<SwitchMaterial>(R.id.switchForSale)

        selectedImageUri = null

        val isEditing = position != -1
        val dialogTitle = if (isEditing) "ပစ္စည်း အချက်အလက် ပြင်ဆင်ရန်" else "ပစ္စည်းအသစ် ထည့်သွင်းပါ"

        if (isEditing) {
            val item = inventoryItems[position]
            editTextItemName.setText(item.name)
            editTextItemCode.setText(item.code ?: "")
            editTextQuantity.setText(item.stockQuantity.toString())
            editTextPrice.setText(item.price.toString())
            editTextCostPrice.setText(item.costPrice.toString())
            // ++ Load wholesale data if it exists
            item.wholesaleQuantity?.let { editTextWholesaleQty.setText(it.toString()) }
            item.wholesalePrice?.let { editTextWholesalePrice.setText(it.toString()) }
            switchForSale.isChecked = item.isForSale

            if (!item.imageUri.isNullOrEmpty()) {
                selectedImageUri = Uri.parse(item.imageUri)
                dialogImageViewPreview.load(selectedImageUri)
            } else {
                dialogImageViewPreview.setImageResource(R.drawable.ic_add_photo)
            }
        } else {
            dialogImageViewPreview.setImageResource(R.drawable.ic_add_photo)
        }

        buttonChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton("သိမ်းမည်") { dialog, _ ->
                val name = editTextItemName.text.toString()
                val code = editTextItemCode.text.toString().ifEmpty { null }
                val quantityStr = editTextQuantity.text.toString()
                val priceStr = editTextPrice.text.toString()
                val costPriceStr = editTextCostPrice.text.toString()
                // ++ Get wholesale data strings
                val wholesaleQtyStr = editTextWholesaleQty.text.toString()
                val wholesalePriceStr = editTextWholesalePrice.text.toString()
                val isForSale = switchForSale.isChecked

                if (name.isNotEmpty() && quantityStr.isNotEmpty() && priceStr.isNotEmpty() && costPriceStr.isNotEmpty()) {
                    val price = priceStr.toDouble()
                    val costPrice = costPriceStr.toDouble()

                    lifecycleScope.launch {
                        if (isEditing) {
                            val oldItem = inventoryItems[position]
                            val updatedItem = oldItem.copy(
                                name = name,
                                code = code,
                                imageUri = selectedImageUri?.toString(),
                                stockQuantity = quantityStr.toInt(),
                                price = price,
                                costPrice = costPrice,
                                // ++ Add wholesale data to updated item
                                wholesaleQuantity = wholesaleQtyStr.toIntOrNull(),
                                wholesalePrice = wholesalePriceStr.toDoubleOrNull(),
                                isForSale = isForSale
                            )
                            inventoryRepository.updateInventoryItem(updatedItem)
                        } else {
                            val newItem = InventoryItem(
                                name = name,
                                code = code,
                                imageUri = selectedImageUri?.toString(),
                                stockQuantity = quantityStr.toInt(),
                                price = price,
                                costPrice = costPrice,
                                // ++ Add wholesale data to new item
                                wholesaleQuantity = wholesaleQtyStr.toIntOrNull(),
                                wholesalePrice = wholesalePriceStr.toDoubleOrNull(),
                                isForSale = isForSale
                            )
                            inventoryRepository.addInventoryItem(newItem)
                        }
                        loadInventoryItems()
                    }
                    dialog.dismiss()
                } else {
                    Toast.makeText(requireContext(), "အချက်အလက် အပြည့်အစုံ ဖြည့်ပါ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }

    // --- Edit ---
    override fun onEditItem(position: Int) {
        showAddItemDialog(position)
    }

    // --- Delete ---
    override fun onDeleteItem(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("ပစ္စည်း ဖျက်ရန်")
            .setMessage("ဒီပစ္စည်းကို စာရင်းထဲက ဖျက်မှာ သေချာလား?")
            .setPositiveButton("ဖျက်မည်") { dialog, _ ->
                lifecycleScope.launch {
                    val itemToDelete = inventoryItems[position]
                    inventoryRepository.deleteInventoryItem(itemToDelete)
                    loadInventoryItems()
                }
                dialog.dismiss()
            }
            .setNegativeButton("မလုပ်တော့ပါ") { dialog, _ ->
                dialog.cancel()
            }
            .create()
            .show()
    }
}