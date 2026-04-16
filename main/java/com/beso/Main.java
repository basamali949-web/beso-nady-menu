package com.beso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.*;
import java.sql.*;
import java.util.*;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@RestController
public class Main {

    private static final String APP_NAME = "Beso El Nady Restaurant";
    private static final String DARK_NAVY = "#0f172a";
    private static final String DB_PATH = "C:/Users/Administrator/OneDrive/Documents/Database21.accdb";

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @GetMapping("/saveOrder")
    public String saveOrder(@RequestParam double total, @RequestParam String desc) {
        String url = "jdbc:ucanaccess://" + DB_PATH;
        String sql = "INSERT INTO Orders ([Order - Date], [Total Price], [Order Description], [Table Number]) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
            pstmt.setDouble(2, total);
            pstmt.setString(3, desc);
            pstmt.setString(4, "T-01");
            pstmt.executeUpdate();
            return "Saved";
        } catch (Exception e) { return "Error"; }
    }

    @GetMapping("/")
    public String showBesoApp() {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'>");

        // أهم سطر عشان يشتغل على الموبايل صح
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no'>");

        html.append("<style>");
        html.append("body { background:#1e293b; color:white; direction:rtl; font-family:sans-serif; margin:0; }");
        html.append(".header { background:").append(DARK_NAVY).append("; padding:15px; text-align:center; position:sticky; top:0; z-index:1000; box-shadow:0 4px 10px rgba(0,0,0,0.3); }");
        html.append(".search-bar { width:90%; padding:12px; margin:15px auto; display:block; border-radius:25px; border:none; background:#334155; color:white; outline:none; }");

        // تقسيم الشاشة للموبايل والكمبيوتر
        html.append(".main-grid { display:flex; flex-direction:column; padding:10px; gap:20px; }");
        html.append("@media (min-width: 768px) { .main-grid { flex-direction:row; padding:20px; } }");

        html.append(".menu-side { flex:2.5; }");
        html.append(".bill-side { flex:1; background:white; color:#333; padding:20px; border-radius:20px; box-shadow:0 10px 30px rgba(0,0,0,0.5); height:fit-content; position:sticky; top:80px; }");

        html.append(".cat-title { color:#38bdf8; margin:20px 0 10px 0; border-right:4px solid #38bdf8; padding-right:10px; font-size:1.2em; }");
        html.append(".items-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(140px, 1fr)); gap:15px; }");
        html.append(".card { background:#334155; border-radius:15px; text-align:center; padding:10px; border:1px solid #475569; }");
        html.append(".emoji-logo { font-size:45px; margin-bottom:5px; }");
        html.append(".add-btn { width:100%; padding:8px; background:#38bdf8; color:white; border:none; border-radius:8px; cursor:pointer; font-weight:bold; }");

        html.append(".welcome { position:fixed; inset:0; background:").append(DARK_NAVY).append("; z-index:5000; display:flex; flex-direction:column; align-items:center; justify-content:center; text-align:center; }");
        html.append("</style></head><body>");

        // شاشة الترحيب
        html.append("<div id='welcome' class='welcome'>");
        html.append("<div style='font-size:100px;'>🍔</div><h1 style='color:#38bdf8;'>").append(APP_NAME).append("</h1>");
        html.append("<button onclick='document.getElementById(\"welcome\").style.display=\"none\"' style='padding:15px 50px; background:#38bdf8; color:white; border:none; border-radius:30px; cursor:pointer; font-size:20px; font-weight:bold;'>ابدأ الأوردر 🚀</button></div>");

        html.append("<div class='header'><h2>").append(APP_NAME).append(" 👨‍🍳</h2></div>");
        html.append("<input type='text' class='search-bar' id='search' placeholder='🔍 ابحث عن طلبك...' onkeyup='doSearch()'>");

        html.append("<div class='main-grid'><div class='menu-side'>");

        try (Connection conn = DriverManager.getConnection("jdbc:ucanaccess://" + DB_PATH);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Menu ORDER BY Category")) {
            String lastCat = "";
            while(rs.next()){
                String c = rs.getString("Category");
                if(!c.equals(lastCat)){
                    if(!lastCat.isEmpty()) html.append("</div>");
                    html.append("<h2 class='cat-title'>🍴 ").append(c).append("</h2><div class='items-grid'>");
                    lastCat = c;
                }
                html.append("<div class='card item-box' data-name='").append(rs.getString("Item Name")).append("'>");
                html.append("<div class='emoji-logo'>").append(rs.getString("ImageURL")).append("</div>");
                html.append("<h3 style='font-size:1em;'>").append(rs.getString("Item Name")).append("</h3>");
                html.append("<p style='color:#38bdf8; margin:5px 0;'>").append(rs.getDouble("Price")).append(" EGP</p>");
                html.append("<button class='add-btn' onclick=\"add('").append(rs.getString("Item Name")).append("', ").append(rs.getDouble("Price")).append(")\">إضافة +</button></div>");
            }
            html.append("</div>");
        } catch(Exception e) {}

        html.append("</div><div class='bill-side'>");
        html.append("<h2 style='text-align:center; margin:0;'>🧾 الفاتورة</h2>");
        html.append("<div id='billItems' style='margin:15px 0; border-top:1px solid #eee; padding-top:10px;'></div>");
        html.append("<div style='display:flex; justify-content:space-between; font-size:20px; font-weight:bold;'><span>الإجمالي:</span><span><span id='total'>0</span> EGP</span></div>");
        html.append("<button style='width:100%; margin-top:20px; padding:15px; background:").append(DARK_NAVY).append("; color:white; border-radius:12px; cursor:pointer; font-size:18px;' onclick='finishOrder()'>طباعة الفاتورة 🖨️</button>");
        html.append("</div></div>");

        html.append("<script>");
        html.append("let bill = {}; let total = 0;");
        html.append("function add(n, p) { if(bill[n]) { bill[n].qty++; } else { bill[n] = {price:p, qty:1}; } total += p; updateUI(); }");
        html.append("function updateUI() { let div = document.getElementById('billItems'); div.innerHTML = ''; for(let n in bill) { div.innerHTML += '<div style=\"display:flex; justify-content:space-between; padding:8px 0; border-bottom:1px dashed #eee;\"><span>'+bill[n].qty+'x '+n+'</span><b>'+(bill[n].qty*bill[n].price)+' EGP</b></div>'; } document.getElementById('total').innerText = total; }");
        html.append("function doSearch() { let q = document.getElementById('search').value.toLowerCase(); document.querySelectorAll('.item-box').forEach(b => { b.style.display = b.getAttribute('data-name').toLowerCase().includes(q) ? 'block' : 'none'; }); }");

        html.append("function finishOrder() {");
        html.append("  if(total === 0) return alert('ضيف أصناف الأول!');");
        html.append("  let desc = ''; for(let n in bill) desc += bill[n].qty + 'x ' + n + ', ';");
        html.append("  fetch('/saveOrder?total='+total+'&desc='+encodeURIComponent(desc)).then(() => {");
        html.append("    let w = window.open('', '', 'width=400,height=600');");
        html.append("    w.document.write('<html><body style=\"direction:rtl; text-align:center; font-family:sans-serif; padding:20px;\">');");
        html.append("    w.document.write('<div style=\"border:1px solid #000; padding:15px; border-radius:10px;\">');");
        html.append("    w.document.write('<h2>🌟 ").append(APP_NAME).append(" 🌟</h2><hr>');");
        html.append("    w.document.write('<p>'+new Date().toLocaleString()+'</p>');");
        html.append("    w.document.write(document.getElementById('billItems').innerHTML);");
        html.append("    w.document.write('<h3>الإجمالي: '+total+' EGP</h3>');");
        html.append("    w.document.write('<p>شكراً لزيارتكم ❤️</p></div></body></html>');");
        html.append("    w.document.close(); setTimeout(() => { w.print(); w.close(); bill={}; total=0; updateUI(); }, 500);");
        html.append("  });");
        html.append("}");
        html.append("</script></body></html>");
        return html.toString();
    }
}