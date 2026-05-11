package com.judgeapp.db;

import java.sql.*;
import java.util.*;

public class ProblemDAO {
    public static int addProblem(String title, String content, double timeLimit, int memoryLimit) throws SQLException {
        String sql = "INSERT INTO problems(title, content, time_limit, memory_limit) VALUES(?,?,?,?)";
        PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, title);
        ps.setString(2, content);
        ps.setDouble(3, timeLimit);
        ps.setInt(4, memoryLimit);
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
                String.valueOf(rs.getInt("memory_limit"))
            };
        }
        return null;
    }
}