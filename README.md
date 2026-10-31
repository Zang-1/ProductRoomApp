# ProductRoomApp – Lab 07A: Room Entity & DAO Construction

**Course:** Mobile Application Programming
**Student:** Kiều Bảo Giang – 2474802010095
**Target CLOs:** CLO 2 (Apply & Code), CLO 3 (Database Architecture)

Android app (Kotlin, XML Views) that stores products in a local SQLite database through **Jetpack Room**.
Data is saved on the device, so it is still there after the app is closed or killed.

## Learning objectives covered

- Room ORM + KSP configured in Kotlin DSL (`build.gradle.kts`).
- Kotlin data class mapped to an SQLite table with `@Entity`, `@PrimaryKey`, `@ColumnInfo`.
- Data Access Object with `@Dao`, `@Insert`, `@Delete`, `@Query`.
- Asynchronous queries with Kotlin Coroutines (`suspend`) and reactive `LiveData`.

## Step 0 – Gradle & KSP configuration

`app/build.gradle.kts` applies the KSP plugin next to the Android plugin and adds the Room dependencies
(versions are declared in `gradle/libs.versions.toml`):

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp) // com.google.devtools.ksp
}

dependencies {
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.lifecycle.livedata.ktx)
}
```

| Library | Version in this project | Version in the handout |
|---|---|---|
| Room (`room-runtime`, `room-ktx`, `room-compiler`) | 2.8.5 | 2.6.1 |
| KSP | 2.3.12 | – |
| `lifecycle-livedata-ktx` | 2.8.7 | 2.8.0 |
| Android Gradle Plugin | 9.4.1 | – |

Newer versions are used because the project is built with AGP 9.4.1 / compileSdk 37 (current Android Studio).
AGP 9 compiles Kotlin by itself, so the separate `kotlin.android` plugin from the handout is not needed.

## Step 1 – Entity: `data/local/ProductEntity.kt`

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "product_name") val name: String,
    @ColumnInfo(name = "product_price") val price: Double,
    @ColumnInfo(name = "stock_quantity") val quantity: Int,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean = false
)
```

Table created by Room (from `app/schemas/.../1.json`):

```sql
CREATE TABLE products (
    id             INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    product_name   TEXT    NOT NULL,
    product_price  REAL    NOT NULL,
    stock_quantity INTEGER NOT NULL,
    is_favorite    INTEGER NOT NULL
)
```

`is_favorite` is an extra column (not in the spec) used by the star button in the UI.

## Step 2 – DAO: `data/local/ProductDao.kt`

| Operation | Annotation | Type |
|---|---|---|
| `insertProduct(product: ProductEntity)` | `@Insert(onConflict = OnConflictStrategy.REPLACE)` | `suspend` |
| `deleteProduct(product: ProductEntity)` | `@Delete` | `suspend` |
| `getAllProducts()` | `@Query("SELECT * FROM products ORDER BY id DESC")` | `LiveData<List<ProductEntity>>` |
| `getProductById(productId: Int)` | `@Query("SELECT * FROM products WHERE id = :productId")` | `suspend`, returns `ProductEntity?` |

Extra queries used by the UI: `setFavorite`, `count`, `deleteAll`.

## Project structure

```
app/src/main/java/edu/vlu/week07/productroom/
├── data/
│   ├── local/
│   │   ├── ProductEntity.kt      # Step 1
│   │   ├── ProductDao.kt         # Step 2
│   │   └── AppDatabase.kt        # thread-safe singleton, file product_database.db
│   └── repository/
│       └── ProductRepository.kt  # runs DAO calls on Dispatchers.IO
└── ui/
    ├── ProductViewModel.kt       # viewModelScope + LiveData
    ├── ProductAdapter.kt         # ListAdapter + DiffUtil
    └── MainActivity.kt           # ViewBinding, observes LiveData
```

## Run

1. Open the folder in Android Studio and wait for Gradle Sync.
2. **Build → Make Project**: Room generates `ProductDao_Impl` and `AppDatabase_Impl` in `app/build/generated/ksp/debug/`.
3. Run on an emulator or a device (min SDK 24). Logcat tag `SMOKE_TEST` shows `DB opened, count = ...`.
4. Add, favorite and delete products; close the app and open it again: the products are still there.

If the app crashes with `Room cannot verify the data integrity`, an older build with a different table is installed:
uninstall the app (or clear its data) and run again.
