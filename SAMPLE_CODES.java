// ============================================================
// CODE SAMPLES FOR TESTING - Copy vào tab "độ mạnh test case"
// ============================================================

// ========== PROBLEM 1: SUM OF TWO NUMBERS ==========

// --- Code AC (Correct) ---
// Copy vào ô "Code AC"
import java.util.Scanner;
public class SumAC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a + b);
    }
}

// --- Code WA (Wrong Answer) ---
// Copy vào ô "Code WA"
import java.util.Scanner;
public class SumWA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        System.out.println(a - b); // BUG: should be a + b
    }
}

// --- Code TLE (Time Limit Exceeded) ---
// Copy vào ô "Code TLE"
import java.util.Scanner;
public class SumTLE {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int a = sc.nextInt();
        int b = sc.nextInt();
        // Simulate slow operation
        for (long i = 0; i < 1_000_000_000L; i++) {
            Math.sqrt(i);
        }
        System.out.println(a + b);
    }
}

// ========== PROBLEM 2: FIBONACCI NUMBERS ==========

// --- Code AC (Correct) ---
// Copy vào ô "Code AC"
import java.util.Scanner;
public class FibAC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        if (n <= 2) {
            System.out.println(1);
            return;
        }
        long a = 1, b = 1;
        for (int i = 3; i <= n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        System.out.println(b);
    }
}

// --- Code WA (Wrong Answer) ---
// Copy vào ô "Code WA"
import java.util.Scanner;
public class FibWA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) {
            long temp = a + b;
            a = b;
            b = temp;
        }
        System.out.println(a); // BUG: off by one
    }
}

// --- Code TLE (Time Limit Exceeded - Exponential) ---
// Copy vào ô "Code TLE"
import java.util.Scanner;
public class FibTLE {
    static long fib(int n) {
        if (n <= 1) return 1;
        return fib(n - 1) + fib(n - 2); // SLOW: O(2^n)
    }
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        System.out.println(fib(n));
    }
}

// ========== PROBLEM 3: SORT ARRAY ==========

// --- Code AC (Correct) ---
// Copy vào ô "Code AC"
import java.util.Scanner;
import java.util.Arrays;
public class SortAC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        Arrays.sort(arr);
        for (int i = 0; i < n; i++) {
            if (i > 0) System.out.print(" ");
            System.out.print(arr[i]);
        }
        System.out.println();
    }
}

// --- Code WA (Wrong - Reverse Sort) ---
// Copy vào ô "Code WA"
import java.util.Scanner;
import java.util.Arrays;
public class SortWA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int[] arr = new int[n];
        for (int i = 0; i < n; i++) {
            arr[i] = sc.nextInt();
        }
        Arrays.sort(arr);
        // BUG: Print in reverse
        for (int i = n - 1; i >= 0; i--) {
            if (i < n - 1) System.out.print(" ");
            System.out.print(arr[i]);
        }
        System.out.println();
    }
}

// ========== PROBLEM 4: CHECK PALINDROME ==========

// --- Code AC (Correct) ---
// Copy vào ô "Code AC"
import java.util.Scanner;
public class PalindromeAC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine().trim();
        String rev = new StringBuilder(s).reverse().toString();
        if (s.equals(rev)) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }
    }
}

// --- Code WA (Wrong - Case Sensitive) ---
// Copy vào ô "Code WA"
import java.util.Scanner;
public class PalindromeWA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String s = sc.nextLine().trim();
        String rev = new StringBuilder(s).reverse().toString();
        // BUG: Should use equalsIgnoreCase for case-insensitive
        if (s.equals(rev)) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }
    }
}

// ========== PROBLEM 5: COUNT PRIMES ==========

// --- Code AC (Correct - Sieve of Eratosthenes) ---
// Copy vào ô "Code AC"
import java.util.Scanner;
public class PrimesAC {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        if (n <= 2) {
            System.out.println(0);
            return;
        }
        
        boolean[] isPrime = new boolean[n];
        for (int i = 2; i < n; i++) {
            isPrime[i] = true;
        }
        
        for (int i = 2; i * i < n; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j < n; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        
        int count = 0;
        for (int i = 2; i < n; i++) {
            if (isPrime[i]) count++;
        }
        System.out.println(count);
    }
}

// --- Code WA (Wrong - Off by one) ---
// Copy vào ô "Code WA"
import java.util.Scanner;
public class PrimesWA {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        
        boolean[] isPrime = new boolean[n + 1];
        for (int i = 2; i <= n; i++) { // BUG: should be < n
            isPrime[i] = true;
        }
        
        for (int i = 2; i * i <= n; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j <= n; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        
        int count = 0;
        for (int i = 2; i <= n; i++) {
            if (isPrime[i]) count++;
        }
        System.out.println(count);
    }
}

// --- Code TLE (Slow - Trial Division) ---
// Copy vào ô "Code TLE"
import java.util.Scanner;
public class PrimesTLE {
    static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; i <= n; i++) { // SLOW: trial division
            if (n % i == 0) return false;
        }
        return true;
    }
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int n = sc.nextInt();
        int count = 0;
        for (int i = 2; i < n; i++) {
            if (isPrime(i)) count++;
        }
        System.out.println(count);
    }
}

// ============================================================
// HƯỚNG DẪN SỬ DỤNG:
// 1. Mở tab "độ mạnh test case"
// 2. Nhập Problem ID (1-5)
// 3. Copy code từ trên vào 3 ô Code AC/WA/TLE
// 4. Nhập số lần chạy (20 là tốt)
// 5. Nhấn "Chạy kiểm tra độ mạnh"
// 6. Xem kết quả:
//    - AC code should pass all testcases
//    - WA code should fail some testcases
//    - TLE code should timeout (nếu testcase mạnh)
// ============================================================
