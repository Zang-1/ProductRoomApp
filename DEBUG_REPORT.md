# DEBUG_REPORT – Notes database (Lab 07, mục 5)

Sinh viên: Kiều Bảo Giang – MSSV 2474802010095

Code Notes của bạn cùng nhóm có 2 lỗi cố ý: một lỗi làm **build thất bại**, một lỗi làm **app crash khi chạy**.

## Lỗi 1 – Thiếu `@PrimaryKey` trong Entity (lỗi build)

**Code lỗi**

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    val id: Long = 0L,   // thiếu @PrimaryKey
    val content: String
)
```

**Thông báo lỗi (KSP / Room compiler)**

```
error: An entity must have at least 1 field annotated with @PrimaryKey
```

**Nguyên nhân:** Room tạo bảng SQLite từ `@Entity` lúc biên dịch. Mỗi bảng Room phải có khóa chính để xác định từng dòng
(`@Update` / `@Delete` tìm dòng theo khóa chính). Không có `@PrimaryKey` thì Room compiler từ chối sinh code `NoteDao_Impl` / `AppDatabase_Impl`, nên build dừng.

**Cách sửa**

```kotlin
@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val content: String
)
```

`autoGenerate = true` + giá trị mặc định `0L`: khi insert, SQLite tự cấp id tăng dần.

## Lỗi 2 – Truy cập database trên main thread (lỗi runtime)

**Code lỗi**

```kotlin
class NoteRepository(private val dao: NoteDao) {
    fun add(note: NoteEntity) {
        dao.insert(note)   // gọi DB trên main thread
    }
}
```

**Thông báo lỗi**

```
java.lang.IllegalStateException: Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
```

**Nguyên nhân:** Đọc/ghi file SQLite có thể mất nhiều thời gian. Nếu chạy trên main thread thì giao diện bị đứng (ANR), nên Room kiểm tra
và ném `IllegalStateException`. (Vì `insert` là hàm `suspend` nên đoạn code trên thực tế còn không biên dịch được khi gọi từ hàm thường;
nếu bỏ `suspend` ở DAO cho nó biên dịch thì app sẽ crash với lỗi trên.)

**Cách sửa:** để `add` là hàm `suspend`, chuyển việc ghi DB sang `Dispatchers.IO`, và gọi từ coroutine (`viewModelScope.launch`) ở ViewModel –
giống `ProductRepository` trong bài lab.

```kotlin
class NoteRepository(
    private val dao: NoteDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend fun add(note: NoteEntity) = withContext(ioDispatcher) {
        dao.insert(note)
    }
}

// Trong ViewModel
fun addNote(content: String) {
    viewModelScope.launch { repository.add(NoteEntity(content = content)) }
}
```

Hàm đọc `observeAll(): LiveData<List<NoteEntity>>` không cần sửa: Room tự chạy truy vấn của `LiveData` trên luồng nền.

## Không nên sửa bằng `allowMainThreadQueries()`

`Room.databaseBuilder(...).allowMainThreadQueries()` làm hết lỗi crash nhưng chỉ che giấu vấn đề: app vẫn đứng giao diện khi DB chậm.
Chỉ dùng trong test, không dùng trong app thật.
