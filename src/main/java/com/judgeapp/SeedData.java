package com.judgeapp;

import com.judgeapp.db.*;
import java.sql.*;

public class SeedData {
    public static void main(String[] args) {
        try {
            seedDatabase();
            System.out.println("✅ Seed data inserted successfully!");
        } catch (SQLException e) {
            System.err.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void seedDatabase() throws SQLException {
        // Problem 1: Sum of Two Numbers (Simple - IOI)
        insertProblem(
            "Sum of Two Numbers",
            "Cho hai số nguyên a và b. Hãy tính tổng của chúng.\n\nInput:\nHai dòng, mỗi dòng một số nguyên.\n\nOutput:\nMột số nguyên là tổng của a và b.",
            1.0,  // time limit
            256   // memory limit
        );
        int probId1 = getLastInsertId();
        
        // Add testcases for Problem 1
        insertTestcase(probId1, "5\n3", "8", "sample");
        insertTestcase(probId1, "10\n20", "30", "sample");
        insertTestcase(probId1, "100\n200", "300", "hidden");
        insertTestcase(probId1, "-5\n3", "-2", "hidden");
        insertTestcase(probId1, "1000000\n2000000", "3000000", "hidden");

        // Problem 2: Fibonacci (Medium - IOI)
        insertProblem(
            "Fibonacci Number",
            "Cho số tự nhiên n. Hãy tính số Fibonacci thứ n.\n\nFibonacci: F(1)=1, F(2)=1, F(n)=F(n-1)+F(n-2) với n>2\n\nInput:\nMột số tự nhiên n (1 <= n <= 40)\n\nOutput:\nSố Fibonacci thứ n",
            2.0,
            256
        );
        int probId2 = getLastInsertId();
        
        insertTestcase(probId2, "1", "1", "sample");
        insertTestcase(probId2, "5", "5", "sample");
        insertTestcase(probId2, "10", "55", "hidden");
        insertTestcase(probId2, "20", "6765", "hidden");
        insertTestcase(probId2, "30", "832040", "hidden");

        // Problem 3: Array Sorting (Medium - ICPC)
        insertProblem(
            "Sort Array",
            "Cho mảng n phần tử. Hãy sắp xếp mảng theo thứ tự tăng dần.\n\nInput:\nDòng đầu: số n\nDòng tiếp: n số nguyên\n\nOutput:\nMảng đã sắp xếp, các phần tử cách nhau bởi dấu cách",
            1.5,
            256
        );
        int probId3 = getLastInsertId();
        
        insertTestcase(probId3, "5\n5 2 8 1 9", "1 2 5 8 9", "sample");
        insertTestcase(probId3, "3\n3 2 1", "1 2 3", "sample");
        insertTestcase(probId3, "1\n5", "5", "hidden");
        insertTestcase(probId3, "6\n6 5 4 3 2 1", "1 2 3 4 5 6", "hidden");

        // Problem 4: Palindrome Check (Easy)
        insertProblem(
            "Check Palindrome",
            "Kiểm tra xem một chuỗi có phải là palindrome hay không.\n\nPalindrome là chuỗi đọc xuôi và đọc ngược giống nhau.\n\nInput:\nMột chuỗi\n\nOutput:\nYES nếu là palindrome, NO nếu không",
            1.0,
            256
        );
        int probId4 = getLastInsertId();
        
        insertTestcase(probId4, "racecar", "YES", "sample");
        insertTestcase(probId4, "hello", "NO", "sample");
        insertTestcase(probId4, "a", "YES", "hidden");
        insertTestcase(probId4, "aba", "YES", "hidden");
        insertTestcase(probId4, "abc", "NO", "hidden");

        // Problem 5: Prime Number (Medium)
        insertProblem(
            "Count Primes",
            "Cho số n. Hãy đếm có bao nhiêu số nguyên tố nhỏ hơn n.\n\nInput:\nMột số n (n <= 1000000)\n\nOutput:\nSố lượng số nguyên tố nhỏ hơn n",
            3.0,
            256
        );
        int probId5 = getLastInsertId();
        
        insertTestcase(probId5, "10", "4", "sample");
        insertTestcase(probId5, "2", "0", "sample");
        insertTestcase(probId5, "100", "25", "hidden");
        insertTestcase(probId5, "1000", "168", "hidden");

        System.out.println("✅ Successfully inserted 5 problems with testcases!");
        System.out.println("\n📋 Sample Code for Testing:\n");
        printSampleCodes();
    }

    private static void insertProblem(String title, String content, double timeLimit, int memoryLimit) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO problems (title, content, time_limit, memory_limit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, title);
            stmt.setString(2, content);
            stmt.setDouble(3, timeLimit);
            stmt.setInt(4, memoryLimit);
            stmt.executeUpdate();
            System.out.println("✅ Inserted problem: " + title);
        }
    }

    private static void insertTestcase(int problemId, String input, String expectedOutput, String type) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        String sql = "INSERT INTO testcases (problem_id, input, expected_output, is_sample) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, problemId);
            stmt.setString(2, input);
            stmt.setString(3, expectedOutput);
            stmt.setBoolean(4, "sample".equals(type));
            stmt.executeUpdate();
        }
    }

    private static int getLastInsertId() throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        String sql = "SELECT @@IDENTITY as id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("id");
            }
            return -1;
        }
    }

    private static void printSampleCodes() {
        System.out.println("=== Code AC (Sum of Two Numbers) ===");
        System.out.println("import java.util.Scanner;\n" +
            "public class SumAC {\n" +
            "    public static void main(String[] args) {\n" +
            "        Scanner sc = new Scanner(System.in);\n" +
            "        int a = sc.nextInt();\n" +
            "        int b = sc.nextInt();\n" +
            "        System.out.println(a + b);\n" +
            "    }\n" +
            "}");

        System.out.println("\n=== Code WA (Sum of Two Numbers - Wrong) ===");
        System.out.println("import java.util.Scanner;\n" +
            "public class SumWA {\n" +
            "    public static void main(String[] args) {\n" +
            "        Scanner sc = new Scanner(System.in);\n" +
            "        int a = sc.nextInt();\n" +
            "        int b = sc.nextInt();\n" +
            "        System.out.println(a - b); // Wrong: should be a + b\n" +
            "    }\n" +
            "}");

        System.out.println("\n=== Code TLE (Fibonacci - Too Slow) ===");
        System.out.println("import java.util.Scanner;\n" +
            "public class FibTLE {\n" +
            "    static long fib(int n) {\n" +
            "        if (n <= 2) return 1;\n" +
            "        return fib(n-1) + fib(n-2); // Exponential - too slow!\n" +
            "    }\n" +
            "    public static void main(String[] args) {\n" +
            "        Scanner sc = new Scanner(System.in);\n" +
            "        int n = sc.nextInt();\n" +
            "        System.out.println(fib(n));\n" +
            "    }\n" +
            "}");

        System.out.println("\n=== Code AC (Fibonacci - Fast) ===");
        System.out.println("import java.util.Scanner;\n" +
            "public class FibAC {\n" +
            "    public static void main(String[] args) {\n" +
            "        Scanner sc = new Scanner(System.in);\n" +
            "        int n = sc.nextInt();\n" +
            "        long a = 1, b = 1;\n" +
            "        for (int i = 3; i <= n; i++) {\n" +
            "            long temp = a + b;\n" +
            "            a = b;\n" +
            "            b = temp;\n" +
            "        }\n" +
            "        System.out.println(n <= 2 ? 1 : b);\n" +
            "    }\n" +
            "}");
    }
}
