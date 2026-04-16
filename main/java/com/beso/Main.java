package com.beso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import java.sql.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@RestController
public class Main {

    private static final String APP_NAME = "Beso El Nady Restaurant";
    private static final String DARK_NAVY = "#0f172a";

    // بيانات قاعدة البيانات الأونلاين (Supabase)
    private static final String DB_URL = "jdbc:postgresql://db.dpjnhnslqfjohyffifsb.supabase.co:5432/postgres";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "اكتب_باسورد_السوبابيس_هنا"; // <--- حط الباسورد بتاعك هنا

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        System.out.println(APP_NAME + " is running on Cloud! 🚀");
    }

    @GetMapping("/saveOrder")
    public String saveOrder(@RequestParam double total) {
        // الاستعلام الجديد المتوافق مع PostgreSQL
        String sql = "INSERT INTO Orders (\"Order - Date\", \"Total Price\") VALUES (CURRENT_DATE, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setDouble(1, total);
            pstmt.executeUpdate();
            return "Order Saved Successfully to Supabase! ✅";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error saving order: " + e.getMessage();
        }
    }
}