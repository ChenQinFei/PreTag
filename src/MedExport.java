import java.io.*;
import java.sql.*;

/**
 * 导出需要预先标记的医案
 *
 * @author Qinfei Chen
 * @create 2016-09-26 11:09
 */
public class MedExport {
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException {
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("E:\\医案.csv")),"GBK"));
        String connectionUrl = "jdbc:mysql://10.15.82.58:3306/sampledb?user=root&password=123&useUnicode=true&characterEncoding=UTF8";
        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(connectionUrl);
        Statement st = conn.createStatement();
        String sql = "SELECT * from medicalrecord WHERE recordId>=1 and recordId<=100 or recordId>=38323 and recordId<=38372 or recordId>=14297 and recordId<=14347";
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
//            if (rs.getString("cfIndex") == ""){
            bw.write(rs.getInt("recordId")+","+rs.getString("recordTitle").replace(",","，")+","+rs.getString("content").replace(",","，").replaceAll("\n|\r"," ")+","+rs.getString("cfIndex")+" ");
//            } else{
//                bw.write(rs.getInt("recordId")+","+rs.getString("recordTitle")+","+rs.getString("content")+","+rs.getString("cfIndex"));
//            }
            bw.write("\r\n");
        }
        bw.close();
        st.close();
        conn.close();
    }
}
