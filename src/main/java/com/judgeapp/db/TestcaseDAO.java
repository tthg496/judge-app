package com.judgeapp.db;

import java.sql.*;
import java.util.*;

public class TestcaseDAO {
    public static void addTestcase(int problemId, String input, String expectedOutput, boolean isSample) throws SQLException {
        String sql = "INSERT INTO testcases(problem_id, input, expected_output, is_sample) VALUES(?,?,?,?)";
        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
        ps.setInt(1, problemId);
        ps.setString(2, input);
        ps.setString(3, expectedOutput);
        ps.setInt(4, isSample ? 1 : 0);
        ps.executeUpdate();
    }

    public static List<String[]> getTestcases(int problemId) throws SQLException {
        List<String[]> list = new ArrayList<>();
        PreparedStatement ps = DatabaseManager.getConnection()
            .prepareStatement("SELECT * FROM testcases WHERE problem_id=?");
        ps.setInt(1, problemId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new String[]{
                String.valueOf(rs.getInt("id")),
                rs.getString("input"),
                rs.getString("expected_output"),
                rs.getInt("is_sample") == 1 ? "Sample" : "Hidden"
            });
        }
        return list;
    }

    public static void deleteTestcasesByProblem(int problemId) throws SQLException {
        PreparedStatement ps = DatabaseManager.getConnection()
            .prepareStatement("DELETE FROM testcases WHERE problem_id=?");
        ps.setInt(1, problemId);
        ps.executeUpdate();
    }
}