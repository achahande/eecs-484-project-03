import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TreeSet;
import java.util.Vector;

import org.json.JSONObject;
import org.json.JSONArray;

public class GetData {

    static String prefix = "project3.";

    // You must use the following variable as the JDBC connection
    Connection oracleConnection = null;

    // You must refer to the following variables for the corresponding 
    // tables in your database
    String userTableName = null;
    String friendsTableName = null;
    String cityTableName = null;
    String currentCityTableName = null;
    String hometownCityTableName = null;

    // DO NOT modify this constructor
    public GetData(String u, Connection c) {
        super();
        String dataType = u;
        oracleConnection = c;
        userTableName = prefix + dataType + "_USERS";
        friendsTableName = prefix + dataType + "_FRIENDS";
        cityTableName = prefix + dataType + "_CITIES";
        currentCityTableName = prefix + dataType + "_USER_CURRENT_CITIES";
        hometownCityTableName = prefix + dataType + "_USER_HOMETOWN_CITIES";
    }

    @SuppressWarnings("unchecked")
    public JSONArray toJSON() throws SQLException {

        // This is the data structure to store all users' information
        JSONArray users_info = new JSONArray();
        
        try (Statement stmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            String userQuery = "SELECT * FROM " + userTableName;
            ResultSet users = stmt.executeQuery(userQuery);
            
            while(users.next()) {
                // Get user's information
                int user_id = users.getInt(1);
                JSONObject user = new JSONObject();
                user.put("user_id", user_id);
                user.put("first_name", users.getString(2));
                user.put("last_name", users.getString(3));
                user.put("year_of_birth", users.getInt(4));
                user.put("month_of_birth", users.getInt(5));
                user.put("day_of_birth", users.getInt(6));
                user.put("gender", users.getString(7));

                // Get user's freinds with larger user ID
                JSONArray friends = new JSONArray();
                try(Statement friendsStmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                    String friendQuery = 
                        "SELECT USER2_ID " + 
                        "FROM " + friendsTableName + " " + 
                        "WHERE USER1_ID = " + user_id;
                    ResultSet friendResults = friendsStmt.executeQuery(friendQuery);
                    while (friendResults.next()) {
                        friends.put(friendResults.getInt(1));
                    }
                }
                user.put("friends", friends);

                // Get user's current city
                JSONObject currentCity = new JSONObject();
                try(Statement currentCityStmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                    String currentCityQuery = 
                        "SELECT C.CITY_NAME, C.STATE_NAME, C.COUNTRY_NAME " + 
                        "FROM " + cityTableName + " C " + 
                        "JOIN " + currentCityTableName + " CC ON CC.CURRENT_CITY_ID = C.CITY_ID " +
                        "WHERE CC.USER_ID = " + user_id;
                    ResultSet currentCityResults = currentCityStmt.executeQuery(currentCityQuery);
                    if(currentCityResults.next()) {
                        currentCity.put("city", currentCityResults.getString(1));
                        currentCity.put("state", currentCityResults.getString(2));
                        currentCity.put("country", currentCityResults.getString(3));
                    }
                }
                user.put("current", currentCity);

                // Get user's hometown city
                JSONObject hometownCity = new JSONObject();
                try(Statement hometownCityStmt = oracleConnection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                    String hometownCityQuery = 
                        "SELECT C.CITY_NAME, C.STATE_NAME, C.COUNTRY_NAME " + 
                        "FROM " + cityTableName + " C " + 
                        "JOIN " + hometownCityTableName + " HC ON HC.HOMETOWN_CITY_ID = C.CITY_ID " + 
                        "WHERE HC.USER_ID = " + user_id;
                    ResultSet hometownCityResults = hometownCityStmt.executeQuery(hometownCityQuery);
                    if(hometownCityResults.next()) {
                        hometownCity.put("city", hometownCityResults.getString(1));
                        hometownCity.put("state", hometownCityResults.getString(2));
                        hometownCity.put("country", hometownCityResults.getString(3));
                    }
                }
                user.put("hometown", hometownCity);

                // Add completed user to JSON
                users_info.put(user);
            }
            
            
            stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }

        return users_info;
    }

    // This outputs to a file "output.json"
    // DO NOT MODIFY this function
    public void writeJSON(JSONArray users_info) {
        try {
            FileWriter file = new FileWriter(System.getProperty("user.dir") + "/output.json");
            file.write(users_info.toString());
            file.flush();
            file.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
