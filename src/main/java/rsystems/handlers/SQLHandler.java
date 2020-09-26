package rsystems.handlers;

import java.sql.*;

public class SQLHandler {

    protected static Connection connection = null;
    private String DatabaseURL = "";

    public SQLHandler(String DatabaseURL) {
        this.DatabaseURL = DatabaseURL;
        connect();
    }

    public void connect() {
        try {
            connection = DriverManager.getConnection(DatabaseURL);
        } catch (SQLException throwables) {
            System.out.println(throwables.getMessage());
        }
    }

    public String getString(String table, String columnID, Integer rowID){
        String output = "";

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT " + columnID.toUpperCase() + " FROM " + table.toUpperCase() + " WHERE ID = " + rowID);

            while (rs.next()) {
                output = rs.getString(columnID.toUpperCase());
            }

        } catch(SQLException throwables){
            System.out.println(throwables.getMessage());
        } finally {
            // do nothing at the moment
        }

        return output;
    }

}
