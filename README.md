# ONLINE SHOP TYCOON — Android Kotlin + libGDX

Game simulasi bisnis online shop 3D bergaya cartoon (vertical slice yang bisa dimainkan).
Ditulis dalam **Kotlin** memakai **libGDX 1.13.1** sebagai renderer 3D.

> Status: Fondasi + core loop playable. Kode sudah lolos kompilasi Kotlin terhadap
> libGDX 1.13.1. Belum di-build menjadi APK di mesin ini karena tidak ada Android SDK.

---

## 1. Cara mendapat APK (tanpa PC / tanpa Android Studio)

**Sudah jadi:** repository `https://github.com/Mikdad-Force/Online-Shop-Tycoon`
dan workflow build-nya sudah sukses. APK debug juga sudah disalin ke HP di:
`/storage/emulated/0/Download/Online-Shop-Tycoon.apk`

> Catatan: build sebelumnya force close karena `libgdx.so` tidak ikut terpaket.
> Sekarang native library sudah disertakan untuk armeabi-v7a/arm64-v8a/x86/x86_64
> lewat `src/main/jniLibs/`. **Uninstall versi lama dulu** sebelum install yang baru.

> (ABI: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`.)

Proyek ini punya GitHub Actions workflow (`.github/workflows/build-apk.yml`)
yang meng-compile APK di cloud secara gratis setiap kali ada push ke `main`.

1. Buat akun / login GitHub di browser HP (https://github.com).
2. Buat repository baru (misalnya `online-shop-tycoon`), pilih **Public**.
3. Upload semua file & folder proyek ini ke repository tersebut
   (GitHub web → **Add file → Upload files**, lalu pilih seluruh isi folder).
4. Buka tab **Actions** di repository → pilih workflow **Build Android APK** →
   klik **Run workflow** (atau cukup `push`/upload memicu otomatis).
5. Tunggu ±3–6 menit. Klik run yang selesai → bagian **Artifacts** →
   unduh `online-shop-tycoon-debug` (file `.zip`).
6. Ekstrak zip di HP → dapat `app-debug.apk` → install (izinkan *Install from unknown sources*).

Kalau Anda punya token GitHub dan ingin saya yang melakukan push dari sini,
beri tahu saja.

---

## 2. Cara main

| Aksi | Kontrol |
|------|---------|
| Jalan | Joystick kiri / W A S D |
| Lihat | Geser layar (drag) / mouse |
| Lari | Shift / tombol sprint |
| Jongkok | Ctrl |
| Lompat | Spasi |
| Interaksi | Tombol **INTERAKSI** / E |
| Putar barang | R |
| Ganti kamera (1st/3rd) | Tombol **VIEW** / V |
| Smartphone | Tombol **HP** / P |
| Komputer | Tombol **PC** / C |
| Simpan manual | Tombol **SAVE** |

**Loop utama:**
1. Beli stok lewat **HP → Supplier** (stok awal sudah ada).
2. Arahkan kamera ke rak → **INTERAKSI** untuk mengambil produk.
3. Arahkan ke meja packing → letakkan produk.
4. Arahkan ke area packing → tekan berulang untuk tahap: buka kardus → masukkan produk →
   bubble wrap → lakban → timbang → tempel resi.
5. Arahkan ke area pickup → serahkan paket ke kurir (kurir datang, bawa paket, pergi).
6. Pesanan selesai → kas + reputasi bertambah. Di **PC** bisa cek order, stok, keuangan, iklan.

---

## 3. Sistem yang sudah diimplementasikan

- **Arsitektur:** EventBus (event-driven), StateMachine, GameContext (service locator),
  modular system (tidak ada "god class").
- **Player 3D:** first-person, jalan/lari/jongkok/lompat, touch look + keyboard.
- **Interaksi:** berbasis arah kamera (ray) + radius, prompt otomatis, mudah ditambah objek baru.
- **Dunia 3D cartoon:** toko, rak gudang, meja packing, komputer, area pickup, jalan,
  truk kurir, NPC staff — dibuat prosedural (low-poly) tanpa asset eksternal.
- **Produk & stok:** Supplier marketplace, pembelian, stok rak yang mengikuti jumlah barang.
- **Order system:** pesanan masuk berkala, deadline, pembatalan, reputasi, naik stage (7 stage).
- **Packing system:** 6 tahapan berurutan dengan feedback notifikasi.
- **Kurir & kendaraan:** state machine datang → parkir → jalan → muat → pergi.
- **Ekonomi:** kas, biaya harian, pinjaman, iklan, reputasi.
- **Siang-malam:** siklus waktu, warna langit dinamis, arah matahari, hujan tiap 5 hari.
- **UI fungsional:** HUD, smartphone (marketplace/supplier/bank/chat), komputer
  (order/inventory/finance/staff/marketing), main menu.
- **Save system:** autosave 60 detik, manual save, slot, backup, versioning.
- **Audio & haptic (Phase 12):** 12 efek suara + BGM 7 detik loop (dibuat prosedural lewat
  `tools/gen_audio.py`), SFX terikat ke EventBus, getar saat interaksi/packing/order selesai,
  suara langkah saat berjalan.
- **Cuaca & FX (Phase 12):** hujan 3D mengikuti siklus hari (tiap 5 hari), partikel garis jatuh.
- **Variasi gameplay (Phase 17):** flash sale (hadiah 2x), pesanan darurat (deadline 75 detik,
  3x hadiah), komplain pelanggan, maintenance gudang, bulk deal supplier — muncul acak
  dengan banner di HUD.
- **Settings (Phase 18):** tab SETTING di HP — music ON/OFF, volume music & SFX, haptic,
  sensitivitas kamera, kualitas grafis; tersimpan di `settings.txt`.
- **Karakter chibi & animasi (Phase 11):** model chibi prosedural (kepala besar, badan kecil)
  dengan animasi IDLE/WALK/RUN/CARRY/PACK/CELEBRATE/TIRED/TALK. Kamera bisa dibalik ke
  orang ketiga (tombol **VIEW** / V).
- **AI karyawan (Phase 8):** Picker mengambil barang & menyiapkannya di meja, Stocker
  mengisi ulang stok, Packer memajukan tahap packing, Manager menaikkan reputasi.
  Bisa direkrut/diberhentikan di PC → STAFF; gaji harian mengikuti jumlah karyawan.
- **Upgrade & ekspansi (Phase 10):** 6 upgrade (rak, mesin packing, forklift, armada kurir,
  auto restock, gudang baru). Gudang baru menambah kapasitas + rak visual baru.
- **Animasi gudang (Phase 5):** conveyor berjalan, forklift mengangkut palet.
- **NPC pengunjung (Phase 7):** datang, bicara di counter, memberi reputasi, lalu pergi.
- **Finance (Phase 9):** laporan kas, potensi pendapatan, gaji, pajak estimasi, progres stage.
- **Fisika produk (Phase 4):** rotasi barang di tangan dengan tombol **R**.

---

## 3b. Referensi seni / visual

Target tampilan: **3D cartoon low-poly, cerah, hangat, rapi** — bukan realistik berlebih,
bukan neon. Prinsip yang dipakai:

- **Supermarket Simulator** (Nokta Games): rak berisi produk berwarna-warni, kardus,
  pencahayaan toko yang merata, UI diegetik (komputer/tablet di dunia game).
  Referensi: https://www.styledex.games/game/supermarket-simulator
  dan https://teemo.dev/game-design/supermarket-simulator/systems/development-and-presentation/
- **Cartoon Low Poly Supermarket City Scene** ( Sketchfab, antonmoek ):
  https://sketchfab.com/3d-models/cartoon-low-poly-supermarket-city-scene-63b3382ab7df4493a0738c50ad85556c
- **Two Point Hospital / House Flipper**: warna cerah high-key, bentuk sederhana,
  bayangan lembut, proporsi agak chibi.
- **Panduan warna dinding & lantai Supermarket Simulator** ( Steam ):
  https://steamcommunity.com/sharedfiles/filedetails/?id=3344387661

Implementasi di game: material warna solid, ambient + directional light yang **tidak**
di-setel terang-terang (menghindari wash-out putih), bayangan kontak palsu di bawah
objek/karakter, langit dinamis siang-malam, dan UI dengan panel solid (bukan transparan).
Aset dibuat prosedural dari primitive 3D agar tetap ringan di HP.

---

## 4. Peta kode

```
src/main/kotlin/com/ost/game/
├── core/      EventBus, StateMachine, GameConfig, GameContext, Settings
├── data/      Models (ProductDef, Order, Snapshot, Catalog), GameState
├── systems/   Inventory, Order, Packing, Courier, DayNight, Save, Audio,
│              LiveEvent, Upgrade, Employee
├── world/     World, Player, Interactable, ChibiCharacter, WeatherFX,
│              WarehouseFX, VisitorNPC
├── ui/        UIStyles, HUD, PhoneUI, ComputerUI, MainMenu
├── screens/   GameScreen (orkestrasi)
├── OSTGame.kt           entry point libGDX
└── AndroidLauncher.kt   Activity Android
```

---

## 5. Status roadmap

Semua phase 1–14 + 17–18 sudah punya implementasi konkret di dalam game:

| Phase | Status |
|-------|--------|
| 1 Foundation & Player | ✅ |
| 2 Interaction & Physics | ✅ |
| 3 Warehouse & Inventory | ✅ |
| 4 Supplier & Product | ✅ |
| 5 Marketplace & Order + animasi gudang | ✅ |
| 6 Picking & Packing (6 tahap) | ✅ |
| 7 Courier & Shipping + NPC pengunjung | ✅ |
| 8 NPC & Employee AI | ✅ (task nyata) |
| 9 Finance & Business | ✅ |
| 10 Upgrade & Expansion | ✅ |
| 11 Character Animation | ✅ (chibi prosedural, bukan FBX rig) |
| 12 Environment, Audio, FX | ✅ |
| 13 UI/UX & Polish | ✅ |
| 14 Optimization & Testing | ✅ kompilasi + CI; testing device oleh pemain |
| 17 Gameplay Variation | ✅ |
| 18 Game Settings | ✅ |

**Yang masih bisa dikembangkan lebih lanjut (di luar cakupan vertical slice):**
- Model 3D eksternal (FBX/GLTF) + Animator Controller rig penuh ( morph target ekspresi )
  — saat ini karakter dibuat dari primitive 3D dengan animasi transform.
- Pathfinding NavMesh untuk NPC (sekarang memakai pergerakan langsung ke target).
- Multi-platform marketplace sungguhan & backend server.
- Voice acting NPC dan tekstur PBR (sekarang audio prosedural & material warna polos).

Fitur baru tetap ditambahkan sebagai modul dan dihubungkan lewat `EventBus`.
