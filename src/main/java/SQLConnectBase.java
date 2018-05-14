import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SQLConnectBase {
    private Connection connection;
    private Statement statement;
    private String URL;
    private String USERNAME;
    private String PASS;

    public SQLConnectBase(String USERNAME, String PASS) {
        this.URL = "jdbc:mysql://localhost:3306/rfids?useUnicode=true&characterEncoding=utf8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
        this.USERNAME = USERNAME;
        this.PASS = PASS;
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(this.URL, this.USERNAME, this.PASS);
            statement = connection.createStatement();
        } catch (SQLException e) {

        }
    }

    public Map<String, String> query(String rfid) {
        Map<String, String> result = new HashMap<>();
        try {
            String query = "select * from rfids_main where rfid_id = '" + rfid + "'";
            ResultSet resultSet  = statement.executeQuery(query);
            if(resultSet.next()) {
                result.put(resultSet.getString("rfid_id"), resultSet.getString("main"));
                return result;
            }
        } catch(SQLException e) {
            System.out.println("");
        }
        return null;
    }

    public boolean isBox(String rfid) {
        try {
            String query = "select * from rfids_main where rfid_id = '" + rfid + "'";
            ResultSet resultSet = statement.executeQuery(query);
            if (resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {

        }

        return false;
    }

    public boolean isBottle(String rfid) {
        String query = "select * from rfid_under_main where rfid_id = '" + rfid + "'";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()) {
                return true;
            }
        } catch (SQLException e) {

        }
        return false;
    }

    public ResultSet getDataRfid(String rfid) {
        String query = "select * from rfid_under_main where rfid_main_id ='" + rfid + "'";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            return resultSet;
        } catch (SQLException e) {

        }

        return null;
    }

    public void close() {
        try {
            if(!statement.isClosed())
                statement.close();
            if(!connection.isClosed())
                connection.close();
        } catch (SQLException e) {

        }
    }

    public String getMainInRfid(String rfid) {
        String query = "select * from rfid_under_main where rfid_id = '" + rfid + "'";
        try {
            ResultSet resultSet = statement.executeQuery(query);
            if(resultSet.next()) {
                return resultSet.getString("rfid_main_id");
            }
        } catch (SQLException e) {

        }

        return null;
    }

    public List<String> getDataUnderRfid(String rfid) {
        String query = "select * from rfid_under_main where rfid_main_id = '" + rfid + "'";
        try {
            List<String> result = new ArrayList<>();
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                result.add(resultSet.getString("rfid_id"));
            }

            if(!result.isEmpty()) {
                return result;
            }
        } catch (SQLException e) {

        }

        return null;
    }

    public void InsertRfids_main(String query) throws SQLException {
        statement.execute(query);
    }

    public void InsertRfids_under_main(String query) throws SQLException {
        statement.execute(query);
    }

    public boolean isOpen() throws SQLException {
        return !connection.isClosed();
    }

    public Map<String, String> main_rfids() throws SQLException {

        Map<String, String> result = new HashMap<>();

        String query = "select * from rfids_main";
        ResultSet resultSet = statement.executeQuery(query);

        while(resultSet.next()) {
            result.put(resultSet.getString("rfid_id"), resultSet.getString("main"));
        }

        resultSet.close();

        if(result.isEmpty()) {
            return null;
        } else {
            return result;
        }

    }
}
