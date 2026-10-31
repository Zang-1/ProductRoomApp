# AI_USAGE – Lab 07 (Room Database)

Sinh viên: Kiều Bảo Giang – MSSV 2474802010095

- **Công cụ:** Claude (Claude Code)
- **AI đã làm:**
  - Tạo khung project `ProductRoomApp` (package `edu.vlu.week07.productroom`) và cấu hình Gradle: plugin KSP, Room, Lifecycle, ViewBinding.
    Phiên bản thư viện được nâng lên cho khớp Android Studio đang dùng (AGP 9.4.1, Room 2.8.5, KSP 2.3.12, compileSdk 37) thay vì bản trong tài liệu (AGP 8.2.0, Room 2.6.1).
  - Gõ lại code Step 1–7 theo tài liệu: `ProductEntity`, `ProductDao`, `AppDatabase`, `ProductRepository`, `ProductViewModel`, `ProductAdapter`, `MainActivity`, 2 layout.
  - Viết phần giải thích trong `DEBUG_REPORT.md`.
- **Em tự làm và kiểm tra:**
  - Build, kiểm tra 2 file `ProductDao_Impl` và `AppDatabase_Impl` được sinh ra (Step 3b), Logcat `SMOKE_TEST` hiện `DB opened, count = 0`.
  - Chạy app trên máy thật, thêm / yêu thích / xóa sản phẩm; dùng Database Inspector chạy `SELECT * FROM products WHERE is_favorite = 1;` và chụp màn hình.
  - Bật "Don't keep activities", thêm sản phẩm, về Home rồi mở lại: dữ liệu vẫn còn.
  - Đọc và giải thích được từng file: luồng View → ViewModel → Repository → DAO → SQLite, vì sao dùng `Dispatchers.IO`, `@Volatile` + `synchronized` trong singleton, `LiveData` + `ListAdapter`/`DiffUtil`.
