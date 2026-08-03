---
name: android-code-review
description: >
  Android PR Code Review skill. Performs a comprehensive code review on the current Git
  repository's PR or specified code changes. Covers all review dimensions including architecture,
  Kotlin code quality, Android platform best practices, performance, security, and testability —
  along with a standardized output format. Can also be used as a standalone reference for
  Android review standards.
triggers:
  - /android-code-review
  - review this MR
  - do a code review
---

# Android Code Review

## Usage

```
/android-code-review                          # Review diff between current branch and develop
/android-code-review --branch feature/login   # Review a specific branch
/android-code-review --base develop           # Use a different base branch (default: develop)
/android-code-review --file path/to/File.kt   # Review a single file
/android-code-review --focus security         # Focus on a specific dimension (security/performance/arch)
/android-code-review --severity major         # Only show issues at or above the specified severity
/android-code-review --output ./review.md     # Save report to a file
```

## Arguments

| Argument | Description | Example |
|----------|-------------|---------|
| `--branch <name>` | Specify the branch to review | `--branch feature/payment` |
| `--base <branch>` | Base branch for comparison (default: `develop`) | `--base main` |
| `--file <path>` | Review a single file | `--file app/src/.../LoginViewModel.kt` |
| `--focus <dimension>` | Focus on a specific review dimension | `--focus performance` |
| `--severity <level>` | Only show issues at or above this level | `--severity major` |
| `--output <path>` | Save the report to a file | `--output ./review.md` |

## Execution Steps

### Step 1 — Fetch Code Changes
Retrieve the code to review based on the provided arguments:

```bash
# Default: current branch vs develop (or --base value if specified)
git log ${BASE:-develop}...HEAD --oneline
git diff ${BASE:-develop}...HEAD --stat
git diff ${BASE:-develop}...HEAD

# If --branch is specified
git diff ${BASE:-develop}...<branch-name>

# If --file is specified, read the file directly
```

> Note: this reviews **committed** changes only. Uncommitted staged/unstaged changes are not included. Use `--base` to change the base branch (default: `develop`).

### Step 2 — Analyze
Using the dimensions and checklists defined below, analyze each changed file for:
- Architecture & design correctness
- Kotlin code quality
- Android platform best practices
- Performance issues
- Security vulnerabilities
- Testability
- Readability & coding standards
- Compose & UI state ownership
- Navigation 3 conventions
- Data & persistence (Room schema/versioning)

For large PRs (> 500 lines), prioritize core business logic files.

**UI evidence check**: Determine whether the diff touches UI — added or modified `@Composable` functions, or added/changed files under `res/drawable*`. If it does, check whether the diff adds a `@Preview` for each new composable. Record the result — it feeds the `📸 UI Evidence` section of the report. Reviewers on this project ask for a screenshot or recording on nearly every UI MR, so surface it proactively rather than waiting for them to.

**String resource check**: Scan the diff for any changes to `*strings*.xml` or `res/values/*.xml` files. Collect every newly added `<string name="...">` entry (diff lines starting with a single `+` but **not** `+++` diff header lines). Lines starting with `-` are removals and must be excluded. Record the string key and the file it appears in — this list will be used in the `🌐 New Strings — Weblate Sync Required` section of the report.

### Step 3 — Generate Review Report
Output the report following the Standard Output Format defined below.

### Step 4 — Save Report (Optional)
If `--output <path>` was specified, use the Write tool to save the full report markdown to that path.

## Notes
- If git commands are unavailable, prompt the user to paste the code directly
- Always deliver feedback in a **constructive and respectful tone**

---

## Tech Stack Conventions
```
Language:         Kotlin (no new Java files)
Min SDK:          API 24 (Android 8.0)
Target SDK:       API 36
Architecture:     MVVM + Clean Architecture
UI Framework:     Jetpack Compose (new screens), View system (legacy screens)
DI:               Hilt
Async:            Kotlin Coroutines + Flow
Local Storage:    Room, DataStore, SQLCipher (encrypted DB)
Image Loading:    Coil
Unit Testing:     JUnit5 + MockK + Turbine
UI Testing:       Compose Testing
Build:            Convention plugins (build-logic/convention/), Version Catalogs (gradle/catalogs/)
```

---

## Review Dimensions & Checklists

> **ViewModel files**: When reviewing `*ViewModel.kt` files, also apply the conventions in [viewmodel-conventions.md](../viewmodel/viewmodel-conventions.md).

> **UseCase files**: When reviewing `*UseCase.kt` files, also apply the conventions in [usecase-conventions.md](../usecase/usecase-conventions.md).

> **Mapper files**: When reviewing `*Mapper.kt` files, also apply the conventions in [mapper-conventions.md](../mapper/mapper-conventions.md).

### 1. Architecture & Design

**Checklist:**
- [ ] Follows MVVM layering: UI → ViewModel → UseCase → Repository → Facade → Gateway
- [ ] Dependency direction is correct (Repository must not depend on ViewModel)
- [ ] Each UseCase has a single responsibility
- [ ] No cross-layer direct calls (e.g., UI layer accessing Repository directly)
- [ ] New modules are discussed before implementation
- [ ] Interfaces are appropriately abstracted (not over- or under-engineered)
- [ ] Hilt annotations used correctly (`@HiltViewModel`, `@AndroidEntryPoint`, `@Module @InstallIn`)
- [ ] Gateway/Facade interfaces used to abstract SDK access (not calling SDK directly from Repository)
- [ ] New repository implementations live in the data layer/package in a `data` module, not in the `app` module
- [ ] Repositories never inject use cases — this inverts Clean Architecture's dependency flow
- [ ] Repositories stay focused on data access; business/orchestration logic belongs in use cases
- [ ] `@HiltViewModel` ViewModels use `hiltViewModel()` in Compose — never `viewModel()` (causes `NoSuchMethodException` crash at runtime)
- [ ] Gateways are data-layer only — never injected into presentation. Add a repository method and a use case instead
- [ ] Domain layer takes no screen-context parameters — screen-specific fallbacks stay in presentation
- [ ] Use cases inject other use cases in the constructor — never accept one as a function parameter or lambda
- [ ] A global monitor (node changes, account events) is filtered down to the items actually on screen, not left firing app-wide
- [ ] **Reuse before adding** — an existing core-ui/shared component or extension is used where one exists, and logic repeated across screens is lifted into a domain use case

#### Modular Dependency checklist
- [ ] **`:feature:`** modules can depend on `:shared`, `:core`, and their own `:*-snowflakes`.
- [ ] **`:shared:`** modules can depend on `:core` and their own `:*-snowflakes`.
- [ ] **`:core:`** modules can only depend on other `:core` modules.
- [ ] **`Snowflake`** modules (ending in `-snowflakes` or `-snowflake-components`) can only depend on `:core`.


**Common Issues:**
```kotlin
// ❌ ViewModel holding Context — causes memory leaks
class LoginViewModel(private val context: Context) : ViewModel()

// ✅ Use @ApplicationContext annotation is fine in ViewModel if needed
class LoginViewModel(@ApplicationContext private val context: Context) : ViewModel()

// ✅ Use Application if needed
class LoginViewModel(private val app: Application) : ViewModel()

// ❌ Repository injected directly into Fragment
class LoginFragment {
    @Inject lateinit var userRepository: UserRepository
}

// ✅ Access data only through ViewModel
class LoginFragment {
    private val viewModel: LoginViewModel by viewModels()
}

// ❌ SDK called directly from Repository — bypasses gateway abstraction
class DefaultAccountRepository @Inject constructor(
    private val megaApi: MegaApiAndroid  // ❌ SDK dependency
)

// ✅ Abstract SDK behind a Gateway interface
class DefaultAccountRepository @Inject constructor(
    private val megaApiGateway: MegaApiGateway  // ✅ Gateway interface
)

// ❌ Hilt module without scope annotation — new instance on every injection
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideDatabase(): AppDatabase = ...  // ❌ missing @Singleton
}

// ✅ Scoped correctly
@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Singleton
    @Provides
    fun provideDatabase(): AppDatabase = ...
}

// ❌ Repository in app module — belongs in data layer
// app/src/main/kotlin/.../DefaultUserRepository.kt

// ✅ Repository in data module
// data/account/src/main/kotlin/.../DefaultAccountRepository.kt

// ❌ Use case injected into repository — inverts dependency flow
class DefaultUserRepository @Inject constructor(
    private val loginUseCase: LoginUseCase  // ❌
)

// ✅ Repository depends only on data sources, gateways, mappers
class DefaultUserRepository @Inject constructor(
    private val userApiGateway: UserApiGateway,
    private val userMapper: UserMapper
)

// ❌ viewModel() with @HiltViewModel — crashes with NoSuchMethodException (no no-arg constructor)
@HiltViewModel
class LoginViewModel @Inject constructor(private val useCase: LoginUseCase) : ViewModel()

@Composable
fun LoginScreen(viewModel: LoginViewModel = viewModel()) // ❌ CRASH

// ✅ Use hiltViewModel() so Hilt provides constructor dependencies
@Composable
fun LoginScreen(viewModel: LoginViewModel = hiltViewModel()) // ✅

// ❌ Business logic in repository — orchestration belongs in use case
class DefaultUserRepository {
    suspend fun getActiveUser(): User {
        val user = api.fetchUser()
        if (user.isExpired) refreshToken()  // ❌ business logic
        return user
    }
}

// ✅ Repository: data access only. Use case: orchestration
class DefaultUserRepository {
    suspend fun getUser(): User = api.fetchUser()
}
class GetActiveUserUseCase {
    suspend operator fun invoke(): User {
        val user = repository.getUser()
        if (user.isExpired) refreshTokenUseCase()
        return user
    }
}

// ❌ Gateway injected into presentation — skips the repository/use case layers
class SettingsViewModel @Inject constructor(
    private val megaLocalStorageGateway: MegaLocalStorageGateway
)

// ✅ Expose the operation as a repository method, then a use case
class SettingsViewModel @Inject constructor(
    private val setNonContactNameUseCase: SetNonContactNameUseCase
)

// ❌ Use case passed in as a parameter — the dependency is invisible and untestable
class ChatLocalLogoutUseCase {
    suspend operator fun invoke(disableChatApi: (suspend () -> Unit)?) { ... }
}

// ✅ Inject the dependency; let a flag decide whether it runs
class ChatLocalLogoutUseCase @Inject constructor(
    private val disableChatApiUseCase: DisableChatApiUseCase,
) {
    suspend operator fun invoke(disableChatApi: Boolean) {
        loginRepository.chatLocalLogout()
        if (disableChatApi) disableChatApiUseCase()
    }
}

// ❌ Reacts to every node change in the app — fires constantly, refreshes the whole screen
monitorNodeUpdatesUseCase()
    .onEach { refreshItems() }

// ✅ Narrow the global stream to the items currently displayed
monitorNodeUpdatesUseCase()
    .map { updates -> updates.filter { it.id in currentItemIds } }
    .filter { it.isNotEmpty() }
    .onEach { refreshItems(it) }
```

---

### 2. Kotlin Code Quality

**Checklist:**
- [ ] `val` preferred over `var` wherever possible
- [ ] No unsafe `!!` non-null assertions
- [ ] Scope functions (`let`, `run`, `apply`, `also`, `with`) used appropriately
- [ ] Data-holding classes use `data class`
- [ ] `sealed class` used for state/result modeling
- [ ] Extension functions are placed logically and not overused
- [ ] Naming follows conventions (camelCase, semantically clear)
- [ ] Enum entries use PascalCase — **never** `ALL_CAPS` (e.g., `PayWall` not `PAY_WALL`)
- [ ] No duplicated code (DRY principle)
- [ ] Functions are reasonably sized (recommended ≤ 40 lines)
- [ ] Complex logic has explanatory comments
- [ ] Time values use `kotlin.time.Duration` (`.seconds`, `.inWholeMilliseconds`) — not hand-rolled `* 1000` arithmetic
- [ ] Magic numbers are named constants, and the name or a comment records where the value came from (design spec, SDK contract)
- [ ] Parallel lists are combined with `zip` — never indexed against each other (`b[i]` crashes when sizes diverge)
- [ ] Flow operators (`onStart`, `takeWhile`, `filter`) are preferred over a manual `flow { }` builder when the source is already a Flow

**Common Issues:**
```kotlin
// ❌ Force unwrap — potential crash
val name = user!!.name

// ✅ Safe handling
val name = user?.name ?: "Unknown"

// ❌ Mutable var for a value that never changes
var userId: String = "123"

// ✅
val userId: String = "123"

// ✅ Sealed class for UI state
sealed class UiState {
    object Loading : UiState()
    data class Success(val data: UserInfo) : UiState()
    data class Error(val message: String) : UiState()
}

// ❌ Manual millisecond arithmetic — easy to get the factor wrong, unclear at the call site
val expireTimeSeconds = expiryDateMillis / 1000
val expiryMillis = exportedData.expirationTime * 1000

// ✅ kotlin.time.Duration makes the unit explicit and the conversion safe
val expireTimeSeconds = expiryDateMillis.milliseconds.inWholeSeconds
val expiryMillis = exportedData.expirationTime.seconds.inWholeMilliseconds

// ❌ Unexplained magic number — a reviewer has to ask where 3000 came from
delay(3000)

// ✅ Named, with the source of the value recorded
// Agreed with design: success snackbar stays visible for 3s after upload starts.
private val UPLOAD_SNACKBAR_DURATION = 3.seconds
delay(UPLOAD_SNACKBAR_DURATION)

// ❌ Indexing one list by another's index — crashes if the sizes ever diverge
sections.mapIndexed { index, section -> section to sectionStartOffsets[index] }

// ✅ zip pairs them safely and truncates instead of throwing
sections.zip(sectionStartOffsets)
```

---

### 3. Android Platform Best Practices

**Checklist:**
- [ ] No memory leak risk (Fragment holding View references beyond lifecycle)
- [ ] StateFlow collected within the correct lifecycle scope
- [ ] No long-running operations on the main thread
- [ ] Permissions follow the principle of least privilege
- [ ] Configuration changes (e.g., screen rotation) are handled properly
- [ ] Composables avoid creating side effects outside `LaunchedEffect` / `SideEffect`
- [ ] `LaunchedEffect` / `SideEffect` / `DisposableEffect` are used correctly
- [ ] Use `Timber` for logging, do not use Android Logger
- [ ] Navigation entry metadata uses `buildMetadata { }` DSL instead of direct creation functions
- [ ] No new `LiveData` — use `StateFlow`. `postValue` can drop emissions when called in quick succession
- [ ] A `Service` holds its collection `Job` and cancels it before relaunching — `onStartCommand` fires repeatedly and otherwise leaks collectors
- [ ] Feature-flag values read on a synchronous path are cached at startup, not fetched lazily at the call site
- [ ] Toggles adjacent to scrollable content are debounced so accidental touches while scrolling don't flicker the UI

**Common Issues:**
```kotlin
// ❌ Collects Flow without lifecycle awareness — runs in background
class MyFragment : Fragment() {
    override fun onViewCreated(...) {
        lifecycleScope.launch {
            viewModel.uiState.collect { ... }
        }
    }
}

// ✅ Use repeatOnLifecycle to stop collection when backgrounded
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect { ... }
    }
}

// ❌ Side effect called directly in Composable — runs on every recomposition
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    viewModel.loadUser()
}

// ✅ Use LaunchedEffect for one-time side effects
@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    LaunchedEffect(Unit) {
        viewModel.loadUser()
    }
}

// ❌ Direct metadata creation — does not compose with other metadata extensions
entry<InfoPsaBottomSheet>(
    metadata = bottomSheetMetadata(
        dismissOnBack = false,
        dismissOnOutsideClick = false
    )
) { ... }

// ✅ Use buildMetadata DSL — composable and extensible
entry<StandardPsaBottomSheet>(
    metadata = buildMetadata {
        withBottomSheet(
            dismissOnBack = false,
            dismissOnOutsideClick = false
        )
    },
) { ... }

// ❌ New collector on every onStartCommand — the old one keeps running
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    lifecycleScope.launch { audioPlayQueueBuilder().collect { ... } }
    return START_NOT_STICKY
}

// ✅ Hold the job and cancel the previous collection first
private var queueJob: Job? = null

override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    queueJob?.cancel()
    queueJob = lifecycleScope.launch {
        runCatching { audioPlayQueueBuilder().collect { ... } }
            .onFailure { Timber.e(it, "Failed to build play queue") }
    }
    return START_NOT_STICKY
}
```

---

### 4. Coroutines & Async

**Checklist:**
- [ ] Correct Dispatcher used (dispatchers are injected by DI with correct annotation, main thread for UI updates)
- [ ] Exceptions are properly handled (`runCatching`)
- [ ] No coroutine leaks (all coroutines are cancellable / scoped)
- [ ] `flowOn` used in the appropriate place for thread switching
- [ ] `GlobalScope` is not used
- [ ] `suspend` functions follow single responsibility
- [ ] Dispatchers should be injected by annotation (like `@IoDispatcher`, `@MainDispatcher` or `@DefaultDispatcher`), instead of specifying concrete type  
- [ ] **Every call into a suspend use case or cold Flow handles errors at the call site** — `runCatching` for suspend calls, `.catch { }` for flows. Do not assume the use case handles them internally; check it
- [ ] Cleanup/release sequences wrap **each** call separately, so one failure doesn't skip the remaining releases
- [ ] `viewModelScope` is not given `mainDispatcher` — it already dispatches on `Dispatchers.Main.immediate`
- [ ] `applicationScope` is used for mutations that must survive leaving the screen; `viewModelScope` for read-only or safely cancellable work
- [ ] Cleanup that must not be lost on cancellation runs under `NonCancellable`
- [ ] No `runBlocking` on the app startup path — it is a direct ANR source. Model initialiser criticality with separate interfaces, not a boolean flag

**Common Issues:**
```kotlin
// ❌ UI update inside IO dispatcher. 
viewModelScope.launch(ioDispatcher) {
    val data = repository.fetchData()
    uiState.value = data  // ❌ Must update on main thread
}

// ✅ Switch context properly
viewModelScope.launch {
    val data = withContext(ioDispatcher) { repository.fetchData() }
    uiState.value = data
}

// ❌ Unhandled exception — causes crash
viewModelScope.launch {
    repository.fetchUser()
}

// ✅ Handle errors explicitly
viewModelScope.launch {
    runCatching { repository.fetchUser() }
        .onSuccess { uiState.value = UiState.Success(it) }
        .onFailure { uiState.value = UiState.Error(it.message ?: "Unknown error") }
}

// ❌ One try/catch around the whole teardown — a throw on the first release skips the rest
try {
    player.release()
    mediaSession.release()
    audioFocusManager.abandon()
} catch (e: Exception) {
    Timber.e(e)
}

// ✅ Each release is independent, so a failure can't strand the others
runCatching { player.release() }.onFailure { Timber.e(it, "player.release failed") }
runCatching { mediaSession.release() }.onFailure { Timber.e(it, "mediaSession.release failed") }
runCatching { audioFocusManager.abandon() }.onFailure { Timber.e(it, "abandon focus failed") }

// ❌ Redundant dispatcher — viewModelScope already runs on Main.immediate
viewModelScope.launch(mainDispatcher) { ... }

// ✅
viewModelScope.launch { ... }

// ❌ Cleanup lost when the caller's scope is cancelled — the file leaks on disk
suspend fun removePage(id: String) {
    session.update { it.withoutPage(id) }
    pageFile(id).delete()
}

// ✅ Once the state change is committed, the cleanup must finish
suspend fun removePage(id: String) {
    session.update { it.withoutPage(id) }
    withContext(ioDispatcher + NonCancellable) { pageFile(id).delete() }
}
```

---

### 5. Performance

**Checklist:**
- [ ] Compose: unnecessary recompositions avoided (`remember`, `derivedStateOf` used correctly)
- [ ] Lists use `LazyColumn` (never full lists inside `ScrollView`)
- [ ] Images loaded with explicit size constraints (prevent OOM)
- [ ] No database or network calls on the main thread
- [ ] ViewModel does not cache unnecessarily large datasets in memory
- [ ] Intermediate `Bitmap`s are recycled in a `finally` so an exception mid-pipeline doesn't leak full-resolution buffers
- [ ] A `Bitmap` that may still be drawn by the compositor is **never** recycled — drop the reference and let GC reclaim it (recycling it throws `Canvas: trying to use a recycled bitmap`)
- [ ] In-memory thumbnail/preview collections are bounded — they must not grow with the number of items
- [ ] Newly added drawable assets are compressed (check the byte size of `.webp`/`.png` files in the diff)

**Common Issues:**
```kotlin
// ❌ Expensive operation runs on every recomposition
@Composable
fun UserList(users: List<User>) {
    val sortedUsers = users.sortedBy { it.name }
}

// ✅ Cache with remember, keyed on input
@Composable
fun UserList(users: List<User>) {
    val sortedUsers = remember(users) { users.sortedBy { it.name } }
}

// ❌ A throw in warp() leaks every full-res intermediate
fun capture(source: Bitmap): Bitmap {
    val rotated = source.rotate(angle)
    val warped = rotated.warp(corners)
    rotated.recycle()
    return warped
}

// ✅ Track intermediates and recycle them in finally
fun capture(source: Bitmap): Bitmap {
    val intermediates = mutableSetOf<Bitmap>()
    try {
        val rotated = source.rotate(angle).also { if (it !== source) intermediates += it }
        return rotated.warp(corners)
    } finally {
        intermediates.forEach { it.recycle() }
    }
}

// ❌ Recycling a thumbnail the compositor may still be drawing — timing-dependent crash
while (deckThumbnails.size > DECK_THUMBNAIL_WINDOW) {
    deckThumbnails.removeAt(0).recycle()
}

// ✅ Drop the reference; GC reclaims it once the frame is done with it
while (deckThumbnails.size > DECK_THUMBNAIL_WINDOW) {
    deckThumbnails.removeAt(0)
}
```

---

### 6. Security

**Checklist:**
- [ ] Sensitive data (tokens, passwords) not stored in plaintext SharedPreferences
- [ ] No sensitive data logged (e.g., `Log.d("token", token)`)
- [ ] User inputs are validated and sanitized
- [ ] Sensitive operations require authentication
- [ ] Database encrypted with SQLCipher
- [ ] Sensitive fields in entities/mappers encrypted using `EncryptData` before persistence
- [ ] No API keys or secrets committed to the repository
- [ ] **No MEGA-internal identifiers in committed files** — this repository mirrors publicly. Slack channel/user-group/bot IDs, Jira `customfield_*` and transition IDs, Confluence page IDs and TestRail project IDs all belong in a gitignored local constants file, with a committed `.example` template
- [ ] Destructive file operations never delete the source before the copy is verified — prefer avoiding the copy entirely (e.g. hand back a `content://` URI)

**Common Issues:**
```kotlin
// ❌ Token stored in plaintext
sharedPreferences.edit().putString("token", authToken).apply()

// ✅ Use EncryptedSharedPreferences
val encryptedPrefs = EncryptedSharedPreferences.create(
    context, "secure_prefs",
    MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
encryptedPrefs.edit().putString("token", authToken).apply()

// ❌ Logging sensitive data
Log.d("Auth", "User token: $token")

// ✅ Debug-only and redacted
if (BuildConfig.DEBUG) Log.d("Auth", "Token received successfully")

// ❌ Sensitive field stored unencrypted in Room entity
@Entity
data class BackupEntity(val backupId: String)

// ✅ Encrypt sensitive fields using EncryptData before persisting
suspend operator fun invoke(backup: Backup): BackupEntity? {
    return BackupEntity(
        encryptedBackupId = encryptData(backup.backupId.toString()) ?: return null
    )
}

// ❌ Source deleted regardless of whether the copy succeeded — unrecoverable data loss
sourceFile.copyTo(destination)
sourceFile.delete()

// ✅ Only delete once the copy is confirmed — or skip the copy altogether
runCatching { sourceFile.copyTo(destination) }
    .onSuccess { sourceFile.delete() }
    .onFailure { Timber.e(it, "Copy failed — keeping source") }
```

---

### 7. Testability

> **ViewModel tests**: When reviewing ViewModel test files (`*ViewModelTest.kt`), also apply the conventions in [viewmodel-test-conventions.md](../viewmodel/viewmodel-test-conventions.md).

> **UseCase tests**: When reviewing UseCase test files (`*UseCaseTest.kt`), also apply the conventions in [usecase-test-conventions.md](../usecase/usecase-test-conventions.md).

> **Mapper tests**: When reviewing Mapper test files (`*MapperTest.kt`), also apply the conventions in [mapper-test-conventions.md](../mapper/mapper-test-conventions.md).

**Checklist:**
- [ ] ViewModel, UseCase and Repository implementation business logics have corresponding unit tests
- [ ] **Test method naming:** Follow the patterns below (see Test Method Naming)
- [ ] New logic has corresponding tests — no missing tests for new behavior
- [ ] Compose View has UI tests
- [ ] ViewModels can be tested without Android framework dependencies
- [ ] Test coverage meets team requirements (recommended ≥ 80% for core logic)
- [ ] Test class annotated with `@ExtendWith(CoroutineMainDispatcherExtension::class)` and `@TestInstance(TestInstance.Lifecycle.PER_CLASS)`
- [ ] Flow emissions tested using Turbine (`underTest.state.test { ... }`)

#### UI Evidence (Compose / drawable changes)
- [ ] The MR description carries a **screenshot or screen recording** of the change — reviewers ask for this on nearly every UI MR, and it is the cheapest way to get a fast approval
- [ ] New composables have a `@Preview`
- [ ] New UI has snapshot tests — they verify every state renders before the screen is wired up, and later act as a CI safeguard
- [ ] New user-facing interactions emit an analytics event (or the MR says explicitly which follow-up ticket adds it)

#### Test Method Naming
- **Format**: Use one of the following patterns for test method names:
  - `` `test that <method> <action>` `` — e.g. `` `test that init does not call loginToFolderUseCase` ``()
  - `` `test that <method> <action> when <cause>` `` — e.g. `` `test that init emits Loaded when login succeeds` ``()

**Common Issues:**
```kotlin
// ❌ Test name does not follow "test that" patterns — inconsistent with project convention
@Test
fun `loading state is shown`() { ... }

// ✅ Use "test that <method> <action>" or "test that <method> <action> when <cause>"
@Test
fun `test that init emits Loaded when login succeeds`() { ... }
@Test
fun `test that init does not call loginToFolderUseCase`() { ... }
```

---

### 8. Code Style & Formatting

**Checklist:**
- [ ] Use explicit imports at the top of the file — never inline fully qualified class names in code
- [ ] Package structure follows conventions (e.g., `mega.privacy.android.feature.{feature}.{layer}`)
- [ ] Resources follow naming conventions (`ic_` icons, `bg_` backgrounds, `item_` list layouts)
- [ ] No hardcoded strings (should be in `shared_strings.xml`)
- [ ] Naming and comments are clear; intent is obvious (no unclear naming or comments)
- [ ] **No narration comments** — flag any comment that restates the next line, labels a section inside a function, explains a standard Kotlin/Android idiom, or describes what changed (`// Extracted from X`, `// Now uses Y`). This is the single most frequently repeated review comment on the project
- [ ] **Prefer extraction over commentary for routine code** — where a block is only unclear because it is unnamed, a well-named private function beats a comment. Genuinely complex logic still warrants an explanatory comment (see §2); the target here is ordinary code padded with step-by-step narration, which reviewers consistently ask to have removed
- [ ] Comment density matches the surrounding file — editing a sparsely commented file must not introduce new comments
- [ ] No leftover debug code or TODOs in production code
- [ ] **Extra or unused code after implementation switch** — When the change refactors or replaces an approach, check for leftover files, types, or dependencies that are no longer used and suggest removing or slimming them
- [ ] Naming conventions followed:
  - ViewModels: `{Feature}ViewModel` (e.g., `GlobalStateViewModel`)
  - Use Cases: `{Action}UseCase` (e.g., `LoginUseCase`)
  - Repositories: `Default{Feature}Repository` (e.g., `DefaultAccountRepository`)
  - Mappers: `{Source}Mapper` (e.g., `TransferMapper`)
  - Test files: `{ClassName}Test.kt`
- [ ] Indentation uses 4 spaces (not tabs) consistently, including Compose modifier chains and multi-line parameters
- [ ] KDoc present on public API classes, functions, and interfaces
- [ ] Build files use module name as filename (e.g., `feature/home/home.gradle.kts`)
- [ ] Convention plugins used for build configuration (`mega.android.library`, `mega.android.hilt`, etc.)
- [ ] New strings added to XML resource files (e.g., `shared_strings.xml`) have been synced to Weblate

**Common Issues:**
```kotlin
// ❌ Inline fully qualified name
val handler = android.os.Handler(android.os.Looper.getMainLooper())

// ✅ Explicit import at top
import android.os.Handler
import android.os.Looper
val handler = Handler(Looper.getMainLooper())

// ❌ Narration — the comments restate the code and mark sections inside the function
suspend operator fun invoke(folder: Node): Flow<List<Node>> {
    // Check whether the user has sync enabled
    val syncEnabled = isSyncEnabledUseCase()
    // Check whether the folder is already synced
    val alreadySynced = isFolderSyncedUseCase(folder.id)
    // Only restrict the node when both are true
    return if (syncEnabled && alreadySynced) restricted(folder) else all(folder)
}

// ✅ The condition is named, so the comments have nothing left to say
suspend operator fun invoke(folder: Node): Flow<List<Node>> =
    if (isFolderAlreadySynced(folder)) restricted(folder) else all(folder)

private suspend fun isFolderAlreadySynced(folder: Node): Boolean =
    isSyncEnabledUseCase() && isFolderSyncedUseCase(folder.id)
```

---

### 9. Compose & UI State

**Checklist:**
- [ ] Reusable components are **stateless** — state is hoisted to the screen or ViewModel and passed in
- [ ] No `ViewModel` is created inside a shared/reusable component; the screen owns it and passes state down
- [ ] Timers, countdowns and tickers are exposed as a `Flow` with an injectable time source, not a `LaunchedEffect` loop buried in a leaf component
- [ ] Validation and callback-orchestration logic lives in the ViewModel, not in the composable
- [ ] `MutableStateFlow.update { }` lambdas are **pure** — no suspend calls, no side effects. Under contention the block re-runs, so anything inside it can execute more than once
- [ ] Conversely, a read-modify-write **is** performed inside the `update { }` lambda, so the read and the write stay atomic
- [ ] ViewModel-owned lazy state uses `by lazy(LazyThreadSafetyMode.NONE)`
- [ ] UI state subtype names are consistent within a feature (`Data` vs `Loaded` — pick one)
- [ ] No `V2`/`New` suffixes on classes — rename the outgoing one `Legacy` instead

**Common Issues:**
```kotlin
// ❌ The component owns a ticker — untestable, and the state can't be shared or previewed
@Composable
fun OfferBannerCountdown(validUntil: Long) {
    var remaining by remember { mutableStateOf(Duration.ZERO) }
    LaunchedEffect(validUntil) {
        while (true) {
            remaining = (validUntil * 1000L - System.currentTimeMillis()).milliseconds
            delay(COUNTDOWN_TICK)
        }
    }
    Text(remaining.format())
}

// ✅ Expose the countdown as a Flow with a time seam; the control stays stateless
fun offerCountdownFlow(
    validUntil: Long,
    currentTimeMillis: () -> Long = System::currentTimeMillis,
): Flow<Duration> = flow {
    while (true) {
        val remainingMillis = validUntil * 1000L - currentTimeMillis()
        emit(remainingMillis.coerceAtLeast(0L).milliseconds)
        if (remainingMillis <= 0L) break
        delay(COUNTDOWN_TICK)
    }
}

@Composable
fun OfferBannerCountdown(remaining: Duration) {
    Text(remaining.format())
}

// ❌ Side effect inside update { } — the request can fire several times under contention
state.update { it.copy(deleteAccountEvent = triggered(runCatching { requestAccountDeletion() }.isSuccess)) }

// ✅ Do the work first, then apply a pure state transformation
val isSuccess = runCatching { requestAccountDeletion() }.isSuccess
state.update { it.copy(deleteAccountEvent = triggered(isSuccess)) }

// ❌ Read outside the lambda — another writer can slip in between the read and the write
val removed = session.value.pages.first { it.id == id }
session.update { it.withoutPage(id) }
delete(removed)

// ✅ Capture the value inside the lambda so the read-modify-write is atomic
var removed: Page? = null
session.update { current ->
    removed = current.pages.firstOrNull { it.id == id }
    current.withoutPage(id)
}
```

---

### 10. Navigation (Navigation 3)

**Checklist:**
- [ ] `NavKey`s do not appear in screen composables — they belong to destination and navigation code only. Screens take `() -> Unit` callbacks
- [ ] No `NavigationHandler` is passed or provided (via `CompositionLocal`) down to child composables — use `NavigationEventQueue` instead
- [ ] Per-instance navigation behaviour uses a **distinct NavKey type**. `entry<>` metadata is bound statically per NavKey type and evaluated at registration, so a field on the key cannot vary it at navigation time
- [ ] Large payloads (e.g. thousands of node handles) are not carried in a `NavKey` Parcelable — that risks `TransactionTooLargeException`. Use the existing RouteLauncher + holder pattern
- [ ] Legacy navigation bridging goes through `AppNavigator` / `MegaNavigator` and the feature-flag gate, not ad-hoc `startActivity` calls
- [ ] Snackbars: `SnackbarEventQueue` when the emitter is not Compose or the activity may finish; the local snackbar host state otherwise

**Common Issues:**
```kotlin
// ❌ NavKey constructed inside the screen — couples the UI to the navigation graph
AudioPlayerScreen(
    navigateToTransfers = { navigationHandler.navigate(TransfersNavKey()) },
)

// ✅ The screen exposes an intent callback; the destination decides where it goes
// In the screen:
AudioPlayerScreen(onTransfersClick = onTransfersClick)
// In the destination:
entry<AudioPlayerNavKey> {
    AudioPlayerScreen(onTransfersClick = { navigationHandler.navigate(TransfersNavKey()) })
}

// ❌ A field intended to vary the entry metadata — metadata is resolved at registration,
//    so forceDarkTheme is read once for the type and never per navigation
@Serializable
data class OptionsBottomSheetNavKey(val nodeId: Long, val forceDarkTheme: Boolean) : NavKey

// ✅ A distinct NavKey type per metadata variant; the shared content composable is reused
@Serializable data class OptionsBottomSheetNavKey(val nodeId: Long) : NavKey
@Serializable data class DarkOptionsBottomSheetNavKey(val nodeId: Long) : NavKey
```

---

### 11. Data & Persistence

**Checklist:**
- [ ] Any change to a Room entity, DAO schema or migration **bumps the database version** — without it, developers and users already on version N crash on the changed schema
- [ ] The exported schema for the new version is committed under `data/schemas/<database>/<N>.json`, and the previous version's JSON is left untouched (a modified older schema means the version bump was missed)
- [ ] Room column names are snake_case; timestamps use an `_at` suffix (`pinned_at`, `created_at`)
- [ ] Timestamp defaults live on the entity (`val pinnedAt: Long = System.currentTimeMillis()`) rather than being repeated at every call site
- [ ] Sensitive fields are encrypted with `EncryptData` before persistence (see §6)

**Common Issues:**
```kotlin
// ❌ Entity changed, version left alone, and the old schema file edited in place
@Database(entities = [...], version = 8)  // ❌ still 8 after adding a column
// data/schemas/…/8.json modified

// ✅ Bump the version and commit a new schema file
@Database(entities = [...], version = 9)
// data/schemas/…/8.json unchanged, 9.json added

// ❌ Caller-supplied timestamp, camelCase column
@Entity
data class HomePinnedItemEntity(
    @ColumnInfo(name = "pinnedAt") val pinnedAt: Long,
)

// ✅ snake_case with the _at convention and a default on the entity
@Entity
data class HomePinnedItemEntity(
    @ColumnInfo(name = "pinned_at") val pinnedAt: Long = System.currentTimeMillis(),
)
```

---

## Severity Level Definitions

| Level | Badge | Description | Blocks Merge? |
|-------|-------|-------------|---------------|
| Critical | 🔴 | Crash risk, data breach, severe performance issue | **Yes** |
| Major | 🟠 | Architecture violation, memory leak, logic error | **Yes** |
| Minor | 🟡 | Readability, naming conventions, small optimizations | No |
| Suggestion | 🔵 | Optional improvement, not required in this PR | No |

---

## Standard Output Format

All review reports must strictly follow this format:

````markdown
# PR Code Review Report

## Summary
- **Branch**: userid/JiraID-branch → develop
- **Files Changed**: X
- **Review Date**: YYYY-MM-DD HH:mm
- **Overall**: [One-sentence overall assessment]

## 🌐 New Strings — Weblate Sync Required
> ⚠️ New string keys were detected in this PR. Please ensure they are added to Weblate before merging.
> `string_key_one`, `string_key_two`

[Omit this section entirely if no new strings were detected in the diff.]

## 📸 UI Evidence
> ⚠️ This PR changes UI. Before requesting review, attach a screenshot or screen recording to the MR description.
> - Composables missing a `@Preview`: `ComposableOne`, `ComposableTwo`
> - New drawables to check for size: `res/drawable-xxhdpi/example.webp` (XXX KB)

[Omit this section entirely if the diff touches no composables or drawables.]

## Issue Overview
| Severity | Count |
|----------|-------|
| 🔴 Critical | X |
| 🟠 Major | X |
| 🟡 Minor | X |
| 🔵 Suggestion | X |

---

## Detailed Findings

### `path/to/FileName.kt`

#### 🔴 [Critical] Issue Title
**Location**: Line XX
**Problem**: Clear description of what the issue is and why it's risky.
**Suggestion**:
```kotlin
// ❌ Current code
...

// ✅ Suggested fix
...
```

#### 🟡 [Minor] Issue Title
**Location**: Line XX
**Problem**: ...
**Suggestion**: ...

---

## Highlights 👍
> Good practices worth acknowledging (at least one per review)
- `FileName.kt`: ...

## Conclusion
> [Approved ✅ / Request Changes 🔄 / Needs Discussion 💬]
>
> Brief explanation of the conclusion, and a list of blocking issues that must be fixed before merge (if any).
````

---

## Review Mindset & Etiquette

1. **Critique the code, not the person** — say "this function" not "you wrote"
2. **Explain the why** — every issue should include a reason, not just "this is wrong"
3. **Always provide a solution** — point out problems and suggest concrete fixes
4. **Acknowledge the good** — find at least one thing done well in every review
5. **Distinguish must-fix from nice-to-have** — Minor and Suggestion items are not blocking
