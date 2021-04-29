package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;

@Slf4j
@Service
public class LoginService {

    private static final String Driver = "com.mysql.cj.jdbc.Driver";
    private static final String Url = "jdbc:mysql://localhost/sqlinjection?useUnicode=true&serverTimezone=Asia/Seoul&allowMultiQueries=true";
    private static final String user = "root";
    private static final String password = "";

    //SQL Injection query
    @Transactional
    public boolean findUserByIdAndPwError(String id, String pw) throws Exception {
        Class.forName(Driver);
        ResultSet rs = null;
        Statement stmt = null;
        String sql = "select * from user where id='" + id + "' and pw='" + pw + "'";
        log.info(sql);

        try {
            Connection con = DriverManager.getConnection(Url, user, password);
            stmt = con.createStatement();

            rs = stmt.executeQuery(sql);
            if(rs.next()) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //defence SQL Injection by parameter binding
    @Transactional
    public boolean findUserByIdAndPw(String id, String pw) throws Exception {
        Class.forName(Driver);
        ResultSet rs = null;
        PreparedStatement pstmt = null;
        String sql = "select * from user where id=? and pw=?";

        try {
            Connection con = DriverManager.getConnection(Url, user, password);
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, pw);
            String sqlChanged = pstmt.toString();
            log.info(sqlChanged);

            rs = pstmt.executeQuery();
            if(rs.next()) return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
