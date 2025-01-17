package app;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;


public class DatabaseConnector {
    public static Connection connectToDatabase(String url, String user, String password) throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public static void createTable(Connection connection, String tableName, LinkedHashMap<String, String> columns) throws SQLException {
        StringBuilder query = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(tableName).append(" (");
        for (String column : columns.keySet()) {
            query.append(column).append(" ").append(columns.get(column)).append(", ");
        }

        query.replace(query.length() - 2, query.length(), ") ");

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(query.toString());
        }

        System.out.println("Table " + tableName + " created");
    }

    //Returns id of the new record, or -1 if record was not created.
    public static int saveRecord(Connection connection, String tableName, String[] values) throws SQLException {
        int result;
        StringBuilder preparedQuery = new StringBuilder("INSERT INTO ").append(tableName).append("(");

        //get field names
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet resultSet = databaseMetaData.getColumns(null, null, tableName, "%");
        while (resultSet.next()) {
            if (!resultSet.getBoolean("IS_AUTOINCREMENT")) {
                preparedQuery.append(resultSet.getString("COLUMN_NAME")).append(", ");
            }
        }

        preparedQuery.replace(preparedQuery.length() - 2, preparedQuery.length(), ") ");
        preparedQuery.append("VALUES (");

        //get '?' for values
        String wildCards = String.join(", ", Collections.nCopies(values.length, "?"));
        preparedQuery.append(wildCards);
        preparedQuery.append(")");

        int rowsAffected;
        try (PreparedStatement statement = connection.prepareStatement(preparedQuery.toString(), Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < values.length; i++) {
                statement.setString(i + 1, values[i]);
            }

            rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                result = -1;
            }

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                result = generatedKeys.getInt(1);
                System.out.println("Record saved.");
            } else {
                result = -1;
            }
        }

        return result;
    }

    public static void updateRecord(Connection connection, String tableName, String[] values, int id) throws SQLException {
        StringBuilder updateQuery = new StringBuilder("UPDATE ").append(tableName).append(" SET ");

        //get field names
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet resultSet = databaseMetaData.getColumns(null, null, tableName, "%");

        int counter = 0;
        while (resultSet.next()) {
            if (!resultSet.getBoolean("IS_AUTOINCREMENT")) {
                updateQuery.append(resultSet.getString("COLUMN_NAME")).append(" = ").append("'");
                updateQuery.append(values[counter]).append("', ");
                counter++;
            }
        }

        updateQuery.replace(updateQuery.length() - 2, updateQuery.length(), " WHERE id = ");
        updateQuery.append(id);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(updateQuery.toString());

            String selectQuery = "SELECT * FROM " + tableName + " WHERE id = " + id;
            ResultSet resultSet1 = statement.executeQuery(selectQuery);

            while (resultSet1.next()) {
                System.out.println("Record updated. Id: " + resultSet1.getString("id"));
            }
        }
    }

    public static void deleteRecord(Connection connection, String tableName, int id) throws SQLException {
        StringBuilder deleteQuery = new StringBuilder("DELETE FROM ").append(tableName).append(" WHERE id = ");
        deleteQuery.append(id);

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(deleteQuery.toString());

            System.out.println("Record deleted.");
        }
    }
}
