# CodeClaim — Mod nhập code nhận thưởng cho Fabric 1.21.1

> Dành cho server Cobbleverse. Không cần resource pack, không cần mod phía client.

## Tính năng

| Tính năng | Chi tiết |
|-----------|----------|
| Lệnh nhập code | `/claim <code>` — **mọi người chơi** đều dùng được |
| Mỗi người 1 lần | Mỗi UUID chỉ nhập được một code 1 lần |
| Giới hạn tổng | `max_uses` giới hạn tổng số người có thể nhập |
| Không giới hạn | Đặt `max_uses: -1` để không giới hạn số người |
| Lệnh thưởng | Chạy bất kỳ lệnh server nào, dùng `%player%` cho tên người chơi |
| Reload | `/claimadmin reload` — chỉ OP mới dùng |
| Xem thống kê | `/claimadmin info <code>` — xem đã dùng bao nhiêu lượt |
| Lưu trữ | File JSON tự động (`data/codeclaim_data.json`) |
| Không cần DB | Không cần MySQL/MongoDB như bản gốc |

---

## Cách build

### Yêu cầu
- Java 21 (JDK)
- Internet để tải Gradle và Fabric jars

### Các bước

```bash
# 1. Clone/copy thư mục này
cd codeclaim-mod

# 2. Tải Gradle wrapper
gradle wrapper --gradle-version 8.8
# Hoặc tự tải https://gradle.org/releases/ và dùng lệnh gradle trực tiếp

# 3. Build mod
./gradlew build
# Windows: gradlew.bat build

# 4. File .jar xuất ra tại:
#    build/libs/codeclaim-1.0.0.jar
```

---

## Cài đặt lên server

1. Copy `codeclaim-1.0.0.jar` vào thư mục `mods/` của server
2. Đảm bảo đã có `fabric-api` trong `mods/`
3. Khởi động server
4. File config tự động tạo tại `config/codeclaim.json`
5. Chỉnh sửa config, dùng `/claimadmin reload` để áp dụng

---

## Cấu hình (config/codeclaim.json)

```json
{
  "messages": {
    "invalid_code":    "&cCode '&4{code}&c' không tồn tại!",
    "already_claimed": "&cBạn đã nhập code '&4{code}&c' rồi!",
    "max_uses":        "&cCode '&4{code}&c' đã hết lượt sử dụng!",
    "success":         "&aĐã nhận thưởng từ code '&2{code}&a' thành công!",
    "reloaded":        "&aCodeClaim đã reload config!"
  },
  "codes": {
    "VIP2024": {
      "max_uses": 100,
      "commands": [
        "give %player% minecraft:diamond 5",
        "say %player% vừa nhận code VIP!"
      ]
    },
    "NEWPLAYER": {
      "max_uses": -1,
      "commands": [
        "give %player% minecraft:bread 16"
      ]
    },
    "GRAND_PRIZE": {
      "max_uses": 1,
      "commands": [
        "give %player% minecraft:netherite_ingot 1"
      ]
    }
  }
}
```

### Giải thích

- `max_uses: -1` → không giới hạn số người nhập
- `max_uses: 1` → chỉ 1 người đầu tiên nhập được (dùng cho grand prize)
- `%player%` trong `commands` → tự động thay bằng tên người chơi
- Màu sắc tin nhắn: dùng `&a` thay cho `§a`

---

## Lệnh

| Lệnh | Ai dùng | Chức năng |
|------|---------|-----------|
| `/claim <code>` | Mọi người chơi | Nhập code nhận thưởng |
| `/claimadmin reload` | OP level 2 | Reload config + data |
| `/claimadmin info <code>` | OP level 2 | Xem thống kê code |

---

## Dữ liệu lưu trữ

File `data/codeclaim_data.json` lưu tự động, không cần setup database.
Cấu trúc:

```json
{
  "playerRedeemed": {
    "uuid-người-chơi": ["VIP2024", "NEWPLAYER"]
  },
  "codeUsageCount": {
    "vip2024": 47,
    "newplayer": 132,
    "grand_prize": 1
  }
}
```

---

## So sánh với mod Redeemed gốc

| Tính năng | Redeemed (gốc) | CodeClaim (mod này) |
|-----------|---------------|---------------------|
| Permission | OP only | Mọi người chơi |
| Database | SQLite/MySQL/MongoDB | File JSON đơn giản |
| Resource pack | Không cần | Không cần |
| Kotlin | Có | Không (Java thuần) |
| Reload | Có | Có |
| Giới hạn dùng | Có | Có |
| Mỗi người 1 lần | Có | Có |
