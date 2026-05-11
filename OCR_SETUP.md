# 📷 Hướng Dẫn Cài Đặt Tesseract OCR

Hiện tại, ứng dụng Judge App sử dụng **Tesseract OCR** để đọc text từ ảnh thay vì Gemini API.

## 🚀 Setup Tesseract (Windows)

### Bước 1: Tải Tesseract Installer

Tải từ: https://github.com/UB-Mannheim/tesseract/wiki

**Chọn file:**
```
tesseract-ocr-w64-setup-v5.x.x.exe (64-bit)
```

### Bước 2: Cài đặt Tesseract

1. Chạy installer
2. **Chọn ngôn ngữ:**
   - ✅ English
   - ✅ Vietnamese (Tiếng Việt)
3. Cài vào thư mục: `C:\Program Files\Tesseract-OCR`

### Bước 3: Xác nhận cài đặt thành công

Mở **PowerShell** và chạy:
```powershell
tesseract --version
```

Nếu thấy version số → Cài đặt thành công! ✅

---

## 📁 Setup Project

### Bước 1: Tạo thư mục `tessdata` trong project

```
judge-app/
  ├── tessdata/          ← Tạo thư mục này
  │   ├── vie.traineddata
  │   └── eng.traineddata
  ├── src/
  ├── pom.xml
  └── ...
```

### Bước 2: Copy file dữ liệu language

**Từ Tesseract installation:**
- `C:\Program Files\Tesseract-OCR\tessdata\vie.traineddata` → Copy vào `judge-app/tessdata/`
- `C:\Program Files\Tesseract-OCR\tessdata\eng.traineddata` → Copy vào `judge-app/tessdata/`

**Hoặc tải online từ:**
- https://github.com/UB-Mannheim/tesseract/tree/main/tessdata

### Bước 3: Build project

```bash
cd d:\1.University\semester-4\Java\judge-app
mvn clean install
```

---

## 🧪 Test OCR

### Cách 1: Qua GUI
1. Chạy MainApp
2. Tab "Thêm đề"
3. Nhấn "Chọn ảnh đề"
4. Chọn file ảnh (JPG, PNG)
5. Nhấn "AI đọc ảnh" (Tesseract OCR)
6. Xem kết quả trong ô Content

### Cách 2: Qua Java Code
```java
import com.judgeapp.ocr.OCRManager;

public class TestOCR {
    public static void main(String[] args) throws Exception {
        String text = OCRManager.readImageText("path/to/image.jpg");
        System.out.println(text);
    }
}
```

---

## ❓ Troubleshooting

### Lỗi: "Tesseract không tìm thấy"
**Cách sửa:**
1. Cài đặt lại Tesseract từ: https://github.com/UB-Mannheim/tesseract/wiki
2. Kiểm tra PATH: `tesseract --version`

### Lỗi: "tessdata không tìm thấy"
**Cách sửa:**
1. Tạo thư mục `tessdata` trong `judge-app/`
2. Copy file `.traineddata` vào thư mục này
3. Rebuild project: `mvn clean install`

### Lỗi: "Không nhận diện được text"
**Cách sửa:**
- ✅ Sử dụng ảnh chất lượng cao (DPI >= 150)
- ✅ Ảnh phải có text rõ ràng (không quá mờ)
- ✅ Định dạng: JPG, PNG, BMP (tránh WEBP, GIF)
- ✅ Thử cắt ảnh để chỉ có text cần thiết

### Lỗi: "Tesseract quá chậm"
**Cách tối ưu:**
- Giảm kích thước ảnh trước
- Cơt ảnh chỉ giữ phần text
- Sử dụng DPI tối ưu (150-300)

---

## 📊 So sánh: Tesseract vs Gemini API

| Tính năng | Tesseract | Gemini API |
|----------|-----------|-----------|
| **Chi phí** | Miễn phí ✅ | Trả tiền ❌ |
| **Tốc độ** | Nhanh ✅ | Chậm (network) ❌ |
| **Chất lượng** | Tốt (150-300 DPI) ✅ | Rất tốt ✅ |
| **Cài đặt** | Cần cài riêng ⚠️ | Chỉ cần API key ✅ |
| **Offline** | Có thể ✅ | Không ❌ |
| **Tiếng Việt** | Hỗ trợ ✅ | Tốt ✅ |

**Kết luận:** Tesseract tốt hơn cho dự án học tập! 🎓

---

## 🎯 Sử dụng OCR

1. **Chuẩn bị ảnh:**
   - Ảnh đề thi (JPG, PNG)
   - Chất lượng tốt (không bị mờ, bị cắt)
   - DPI: 150-300

2. **Nhấn "AI đọc ảnh":**
   - Tesseract sẽ đọc text
   - Điền vào ô Content tự động

3. **Kiểm tra kết quả:**
   - Sửa lỗi chính tả (nếu có)
   - Nhấn "Lưu đề" để hoàn thành

---

## 📚 Tài liệu thêm

- Tesseract Wiki: https://github.com/UB-Mannheim/tesseract/wiki
- Tess4j (Java wrapper): https://tess4j.sourceforge.net/
- Tesseract Data: https://github.com/UB-Mannheim/tesseract/tree/main/tessdata

Chúc bạn sử dụng OCR vui vẻ! 🚀
