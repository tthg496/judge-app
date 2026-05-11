# Seed Data - Hướng Dẫn

Để test các chức năng của ứng dụng, bạn cần insert dữ liệu mẫu vào database.

## Cách 1: Sử dụng SQL Script (Khuyên dùng)

### B1: Mở SQL Server Management Studio (SSMS)

### B2: Kết nối tới SQL Server
- Server: `localhost` (hoặc địa chỉ của SQL Server)
- Authentication: SQL Server Authentication
- Login: `sa`
- Password: `123456`

### B3: Chọn database `JudgeApp`

### B4: Mở file `seed_data.sql` và chạy

Hoặc copy-paste nội dung của `seed_data.sql` vào query window và execute.

---

## Cách 2: Sử dụng Java Program

### B1: Mở Terminal trong project

### B2: Compile toàn bộ project
```bash
mvn clean compile
```

### B3: Chạy SeedData
```bash
mvn exec:java -Dexec.mainClass="com.judgeapp.SeedData"
```

---

## Dữ liệu được insert

### 5 Problems (Đề):
1. **Sum of Two Numbers** - Cộng hai số (Dễ)
   - 5 testcases (2 sample, 3 hidden)

2. **Fibonacci Number** - Tính số Fibonacci (Trung bình)
   - 5 testcases (2 sample, 3 hidden)

3. **Sort Array** - Sắp xếp mảng (Trung bình)
   - 4 testcases (2 sample, 2 hidden)

4. **Check Palindrome** - Kiểm tra chuỗi palindrome (Dễ)
   - 5 testcases (2 sample, 3 hidden)

5. **Count Primes** - Đếm số nguyên tố (Trung bình)
   - 4 testcases (2 sample, 2 hidden)

**Tổng cộng: 5 đề, 23 testcases**

---

## Code Mẫu để Test (Để vào tab "độ mạnh test case")

### Code AC (Sum) - Đúng:
```java
import java.util.Scanner;
public class SumAC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a + b);
    }
}
```

### Code WA (Sum) - Sai:
```java
import java.util.Scanner;
public class SumWA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a - b); // Sai: nên cộng không trừ
    }
}
```

### Code AC (Fibonacci) - Đúng:
```java
import java.util.Scanner;
public class FibAC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        long a = 1, b = 1;
        for (int i = 3; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        System.out.println(n <= 2 ? 1 : b);
    }
}
```

### Code TLE (Fibonacci) - Chậm:
```java
import java.util.Scanner;
public class FibTLE {
    static long fib(int n) {
        if (n <= 2) return 1;
        return fib(n-1) + fib(n-2); // Quá chậm!
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        System.out.println(fib(n));
    }
}
```

---

## Các chức năng bạn có thể test:

### ✅ Tab "Danh sách đề"
- Xem danh sách 5 đề đã thêm
- Xem chi tiết từng đề
- Xem testcases của từng đề

### ✅ Tab "Thêm đề"
- Thêm đề mới (text hoặc upload ảnh)
- AI phân tích đề và sinh testcase (nếu API Gemini đã config)

### ✅ Tab "độ mạnh test case"
- Nhập Problem ID (1-5)
- Paste code AC, WA, TLE
- Chạy kiểm tra xem testcase có mạnh không
  - Nếu code AC pass hết → testcase ổn
  - Nếu code WA bị catch → testcase mạnh
  - Nếu code TLE bị catch → testcase rất mạnh

---

## Note:
- Nếu database chưa có bảng, ứng dụng sẽ tự tạo khi khởi động
- Bạn có thể chạy `seed_data.sql` nhiều lần, nó sẽ thêm dữ liệu mới (không xóa cũ)
- Để xóa toàn bộ dữ liệu: `DELETE FROM testcases; DELETE FROM problems;`
