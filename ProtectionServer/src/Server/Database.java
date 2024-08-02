package Server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JOptionPane;

class Database {
    private Statement statement;
    private ResultSet result;

    public Database() {
        try {
            // Load SQL Server JDBC driver
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            // Establish the connection
            Connection conn = DriverManager.getConnection(
                    "jdbc:sqlserver://orion-a-12.database.windows.net:1433;"
                    + "database=LicenseDatabase;"
                    + "user=Pulsar@orion-a-12;"
                    + "password=*Nebula53;"
                    + "encrypt=true;"
                    + "trustServerCertificate=false;"
                    + "hostNameInCertificate=*.database.windows.net;"
                    + "loginTimeout=30;"
            );
            statement = conn.createStatement();
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "JDBC Driver not found", "Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to database", "Connection Failed",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public Object[][] search(String skey) {
        Object[][] res = null;
        int i = 0, count = 0;
        try {
            result = statement.executeQuery(
                    "SELECT ID, LICENSE, FNAME, LNAME, REGDATE FROM LicenseTable WHERE ID LIKE '%" + skey
                            + "%' OR LICENSE LIKE '%" + skey + "%' OR FNAME LIKE '%" + skey + "%' OR LNAME LIKE '%"
                            + skey + "%' OR REGDATE LIKE '%" + skey + "%';"
            );
            result.last();
            count = result.getRow();
            result.beforeFirst();
            res = new Object[count][5];
            while (result.next()) {
                res[i][0] = result.getInt(1);
                res[i][1] = result.getString(2);
                res[i][2] = result.getString(3);
                res[i][3] = result.getString(4);
                res[i][4] = result.getString(5);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            res = new Object[1][5];
            res[0][0] = "No results";
            res[0][1] = "No results";
            res[0][2] = "No results";
            res[0][3] = "No results";
            res[0][4] = "No results";
        }
        return res;
    }

    public Object[][] fetchAll() {
        int max = getNumberOfRows();
        int i = 0;
        Object[][] data = new Object[max][5];
        try {
            result = statement.executeQuery("SELECT ID, LICENSE, FNAME, LNAME, REGDATE FROM LicenseTable;");
            while (result.next()) {
                data[i][0] = result.getInt(1); // id
                data[i][1] = result.getString(2); // license
                data[i][2] = result.getString(3).equals("empty") ? "" : result.getString(3);
                data[i][3] = result.getString(4).equals("empty") ? "" : result.getString(4);
                data[i][4] = result.getString(5).equals("empty") ? "" : result.getString(5);
                i++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public boolean insertKey(String key, String md5) {
        try {
            statement.execute("INSERT INTO LicenseTable (LICENSE, MD5) VALUES ('" + key + "', '" + md5 + "');");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "License Key is Already Present", "Duplicate Entry Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public int getNumberOfRows() {
        int num = -1;
        try {
            result = statement.executeQuery("SELECT COUNT(*) FROM LicenseTable;");
            if (result.next()) {
                num = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return num;
    }

    public String getKeyOfIndex(String index) {
        String key = null;
        try {
            result = statement.executeQuery("SELECT LICENSE FROM LicenseTable WHERE ID = '" + index + "';");
            if (result.next()) {
                key = result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    public boolean removeRow(String key) {
        try {
            statement.execute("DELETE FROM LicenseTable WHERE ID = '" + key + "';");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int[] getAllIndex() {
        int[] index = null;
        try {
            result = statement.executeQuery("SELECT ID FROM LicenseTable;");
            result.last();
            int count = result.getRow();
            result.beforeFirst();
            index = new int[count];
            int i = 0;
            while (result.next()) {
                index[i++] = result.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return index;
    }

    public boolean isKeyPresent(String md5) {
        boolean isTrue = false;
        try {
            result = statement.executeQuery("SELECT * FROM LicenseTable WHERE MD5 = '" + md5 + "';");
            isTrue = result.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return isTrue;
    }

    public String getKey(String md5) {
        String key = "Empty";
        if (isKeyPresent(md5)) {
            try {
                result = statement.executeQuery("SELECT LICENSE FROM LicenseTable WHERE MD5 = '" + md5 + "';");
                if (result.next()) {
                    key = result.getString(1);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return key;
    }

    public boolean isMacAssociated(String md5) {
        String mac = "";
        try {
            result = statement.executeQuery("SELECT MAC FROM LicenseTable WHERE MD5 = '" + md5 + "';");
            if (result.next()) {
                mac = result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return !mac.equals("empty");
    }

    public boolean isSameMac(String mac, String md5) {
        String m = "";
        try {
            result = statement.executeQuery("SELECT MAC FROM LicenseTable WHERE MD5 = '" + md5 + "';");
            if (result.next()) {
                m = result.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return m.equals(mac);
    }

    public boolean updateCredentials(String license, String mac, String fname, String lname, String regdate) {
        boolean isTrue = false;
        try {
            statement.execute(
                    "UPDATE LicenseTable SET MAC = '" + mac + "', FNAME = '" + fname + "', LNAME = '" + lname
                            + "', REGDATE = '" + regdate + "' WHERE LICENSE = '" + license + "';"
            );
            isTrue = true;
        } catch (SQLException e) {
            e.printStackTrace();
            isTrue = false;
        }
        return isTrue;
    }

    public boolean updateCred(String license, String fname, String lname) {
        boolean isTrue = false;
        try {
            statement.execute(
                    "UPDATE LicenseTable SET FNAME = '" + fname + "', LNAME = '" + lname + "' WHERE LICENSE = '"
                            + license + "';"
            );
            isTrue = true;
        } catch (SQLException e) {
            e.printStackTrace();
            isTrue = false;
        }
        return isTrue;
    }
}
