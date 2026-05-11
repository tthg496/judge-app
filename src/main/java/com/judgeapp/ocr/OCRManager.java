package com.judgeapp.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.File;

public class OCRManager {
    private static final Tesseract tess = new Tesseract();

    static {
        // Tìm tessdata folder
        String tessdataPath = findTessdataPath();
        
        try {
            tess.setDatapath(tessdataPath);
            tess.setLanguage("vie+eng"); // Hỗ trợ Tiếng Việt + Tiếng Anh
            tess.setTessVariable("user_defined_dpi", "300");
            System.out.println("✅ Tesseract initialized với tessdata: " + tessdataPath);
        } catch (Exception e) {
            System.err.println("⚠️ Cảnh báo: Tesseract chưa sẵn sàng - " + e.getMessage());
        }
    }

    /**
     * Tìm thư mục tessdata
     */
    private static String findTessdataPath() {
        // Cách 1: Tìm từ working directory hiện tại
        File tessdataRelative = new File("tessdata");
        if (tessdataRelative.exists() && tessdataRelative.isDirectory()) {
            return tessdataRelative.getAbsolutePath();
        }
        
        // Cách 2: Tìm từ project root
        String projectRoot = System.getProperty("user.dir");
        File tessdataInRoot = new File(projectRoot, "tessdata");
        if (tessdataInRoot.exists() && tessdataInRoot.isDirectory()) {
            return tessdataInRoot.getAbsolutePath();
        }
        
        // Cách 3: Dùng C:\Program Files\Tesseract-OCR\tessdata (nếu cài system-wide)
        File systemTessdata = new File("C:\\Program Files\\Tesseract-OCR\\tessdata");
        if (systemTessdata.exists() && systemTessdata.isDirectory()) {
            return systemTessdata.getAbsolutePath();
        }
        
        // Nếu không tìm thấy, throw error
        throw new RuntimeException(
            "❌ Không tìm thấy thư mục tessdata!\n" +
            "Vui lòng tạo thư mục: " + tessdataRelative.getAbsolutePath() + "\n" +
            "Và copy các file .traineddata vào đó"
        );
    }

    /**
     * Đọc text từ ảnh sử dụng Tesseract OCR
     * @param imagePath Đường dẫn tuyệt đối đến file ảnh
     * @return Text nhận diện được từ ảnh
     * @throws Exception Nếu ảnh không hợp lệ hoặc không tìm thấy Tesseract
     */
    public static String readImageText(String imagePath) throws Exception {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                throw new Exception("File ảnh không tồn tại: " + imagePath);
            }
            
            System.out.println("📷 Đang đọc ảnh: " + imageFile.getName());
            String result = tess.doOCR(imageFile);
            
            if (result == null || result.trim().isEmpty()) {
                throw new Exception("Không thể nhận diện text từ ảnh. Vui lòng kiểm tra chất lượng ảnh.");
            }
            
            System.out.println("✅ OCR thành công! Nhận diện được " + result.split("\n").length + " dòng text");
            return result.trim();
            
        } catch (TesseractException e) {
            throw new Exception("❌ Lỗi Tesseract OCR: " + e.getMessage() + 
                "\n\nSự cố có thể là:\n" +
                "1. Tesseract chưa được cài đặt (tải từ https://github.com/UB-Mannheim/tesseract/wiki)\n" +
                "2. Thư mục 'tessdata' chưa được tạo trong project\n" +
                "3. Ảnh không rõ hoặc định dạng không hỗ trợ (dùng JPG, PNG)", e);
        } catch (Exception e) {
            throw new Exception("Lỗi khi đọc ảnh: " + e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra xem Tesseract có được cài đặt không
     */
    public static boolean isTesseractAvailable() {
        try {
            File tessdata = new File("tessdata");
            if (!tessdata.exists()) {
                System.err.println("⚠️ Thư mục 'tessdata' không tồn tại!");
                return false;
            }
            File viDataFile = new File("tessdata/vie.traineddata");
            if (!viDataFile.exists()) {
                System.err.println("⚠️ File 'tessdata/vie.traineddata' không tồn tại! OCR chỉ sẽ dùng Tiếng Anh");
            }
            return true;
        } catch (Exception e) {
            System.err.println("❌ Tesseract không sẵn sàng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Tải tessdata language pack (Tiếng Việt)
     * Chạy lần đầu để chuẩn bị
     */
    public static void setupTessdata() {
        System.out.println("🔧 Thiết lập Tesseract...");
        
        File tessdata = new File("tessdata");
        if (!tessdata.exists()) {
            tessdata.mkdirs();
            System.out.println("📁 Tạo thư mục 'tessdata'");
        }
        
        File viData = new File("tessdata/vie.traineddata");
        File engData = new File("tessdata/eng.traineddata");
        
        if (!viData.exists()) {
            System.out.println("⚠️ Tải 'vie.traineddata' từ: https://github.com/UB-Mannheim/tesseract/releases");
            System.out.println("   Đặt file vào thư mục: " + tessdata.getAbsolutePath());
        }
        
        if (!engData.exists()) {
            System.out.println("⚠️ Tải 'eng.traineddata' từ: https://github.com/UB-Mannheim/tesseract/releases");
            System.out.println("   Đặt file vào thư mục: " + tessdata.getAbsolutePath());
        }
    }
}
