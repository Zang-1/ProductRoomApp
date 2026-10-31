package edu.vlu.week07.productroom.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import edu.vlu.week07.productroom.data.local.AppDatabase
import edu.vlu.week07.productroom.data.local.ProductEntity
import edu.vlu.week07.productroom.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

private const val DEMO_MAIN_THREAD_CRASH = false

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // targetSdk 37 always draws edge-to-edge: keep the 16dp padding and add the system bars on top of it
        val basePadding = binding.main.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(basePadding + bars.left, basePadding + bars.top, basePadding + bars.right, basePadding + bars.bottom)
            insets
        }

        // Lab 07B Part 4 – Intentional bug: a raw DAO insert on the main thread, no lifecycleScope/viewModelScope.
        // Set DEMO_MAIN_THREAD_CRASH = true to reproduce:
        // java.lang.IllegalStateException: Cannot access database on the main thread ...
        if (DEMO_MAIN_THREAD_CRASH) {
            AppDatabase.getDatabase(this).productDao()
                .insertProductBlocking(ProductEntity(name = "Main thread test", price = 1.0, quantity = 1))
        }

        // Step 3b – Smoke test: Logcat "SMOKE_TEST" shows "DB opened, count = ..."
        lifecycleScope.launch {
            val count = AppDatabase.getDatabase(this@MainActivity).productDao().count()
            Log.d("SMOKE_TEST", "DB opened, count = $count")
        }

        adapter = ProductAdapter(
            onFavoriteClick = { p, fav -> viewModel.setFavorite(p, fav) },
            onDeleteClick = { p -> viewModel.deleteProduct(p) }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Observe LiveData -> UI tự cập nhật
        viewModel.allProducts.observe(this) { list ->
            adapter.submitList(list)
        }

        binding.btnAdd.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val price = binding.etPrice.text.toString().toDoubleOrNull() ?: 0.0
            val qty = binding.etQuantity.text.toString().toIntOrNull() ?: 0
            if (name.isBlank()) {
                Toast.makeText(this, "Tên sản phẩm không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.addProduct(name, price, qty)
            binding.etName.text?.clear()
            binding.etPrice.text?.clear()
            binding.etQuantity.text?.clear()
        }

        binding.btnClearAll.setOnClickListener { viewModel.clearAll() }
    }
}
