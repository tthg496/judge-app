package com.judgeapp.db;

import java.sql.*;
import java.util.*;

public class ProblemDAO {
    public static int addProblem(String title, String content, double timeLimit, int memoryLimit) throws SQLException {
        return addProblem(title, content, timeLimit, memoryLimit, null, null, null, null);
    }

    public static int addProblem(String title, String content, double timeLimit, int memoryLimit,
            String generatorCode, String checkerCode, String sampleACCode, String sampleACLanguage) throws SQLException {
        String sql = """
            INSERT INTO problems(
                title, content, time_limit, memory_limit,
                generator_code, checker_code, sample_ac_code, sample_ac_language
            ) VALUES(?,?,?,?,?,?,?,?)
        """;
        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, title);
        ps.setString(2, content);
        ps.setDouble(3, timeLimit);
        ps.setInt(4, memoryLimit);
        ps.setString(5, generatorCode);
        ps.setString(6, checkerCode);
        ps.setString(7, sampleACCode);
        ps.setString(8, sampleACLanguage);
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        return rs.next() ? rs.getInt(1) : -1;
    }

    public static List<String[]> getAllProblems() throws SQLException {
        List<String[]> list = new ArrayList<>();
        ResultSet rs = DatabaseManager.getConnection()
            .createStatement().executeQuery("SELECT id, title, time_limit, memory_limit FROM problems");
        while (rs.next()) {
            list.add(new String[]{
                String.valueOf(rs.getInt("id")),
                rs.getString("title"),
                rs.getDouble("time_limit") + "s",
                rs.getInt("memory_limit") + "MB"
            });
        }
        return list;
    }

    public static String[] getProblem(int id) throws SQLException {
        PreparedStatement ps = DatabaseManager.getConnection()
            .prepareStatement("SELECT * FROM problems WHERE id=?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new String[]{
                String.valueOf(rs.getInt("id")),
                rs.getString("title"),
                rs.getString("content"),
                String.valueOf(rs.getDouble("time_limit")),
                String.valueOf(rs.getInt("memory_limit")),
                rs.getString("generator_code"),
                rs.getString("checker_code"),
                rs.getString("sample_ac_code"),
                rs.getString("sample_ac_language")
            };
        }
        return null;
    }

    public static void updateProblem(int id, String title, String content, double timeLimit, int memoryLimit) throws SQLException {
        String sql = "UPDATE problems SET title=?, content=?, time_limit=?, memory_limit=? WHERE id=?";
        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
        ps.setString(1, title);
        ps.setString(2, content);
        ps.setDouble(3, timeLimit);
        ps.setInt(4, memoryLimit);
        ps.setInt(5, id);
        ps.executeUpdate();
    }

    public static void updateArtifacts(int id, String generatorCode, String checkerCode,
            String sampleACCode, String sampleACLanguage) throws SQLException {
        String sql = """
            UPDATE problems
            SET generator_code=?, checker_code=?, sample_ac_code=?, sample_ac_language=?
            WHERE id=?
        """;
        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql);
        ps.setString(1, generatorCode);
        ps.setString(2, checkerCode);
        ps.setString(3, sampleACCode);
        ps.setString(4, sampleACLanguage);
        ps.setInt(5, id);
        ps.executeUpdate();
    }

    public static void deleteProblem(int id) throws SQLException {
        PreparedStatement ps = DatabaseManager.getConnection()
            .prepareStatement("DELETE FROM problems WHERE id=?");
        ps.setInt(1, id);
        ps.executeUpdate();
    }
}
