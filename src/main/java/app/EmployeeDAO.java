package app;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EmployeeDAO {
    private final Connection connection;

    public EmployeeDAO(Connection connection) {
        this.connection = connection;
    }

    public Optional<Employee> addEmployee(Employee employee, String table) throws SQLException {
        String[] values = {employee.getName(), String.valueOf(employee.getAge()), employee.position, String.valueOf(employee.salary)};
        int id = DatabaseConnector.saveRecord(connection, table, values);
        if (id > 0) {
            employee.setId(id);
            return Optional.of(employee);
        } else {
            return Optional.empty();
        }
    }

    public void changeEmployee(Employee employee, String table) throws SQLException {
        String[] values = {employee.getName(), String.valueOf(employee.getAge()), employee.position, String.valueOf(employee.salary)};
        DatabaseConnector.updateRecord(connection, table, values, employee.getId());
    }

    public void deleteEmployee(Employee employee, String table) throws SQLException {
        DatabaseConnector.deleteRecord(connection, table, employee.getId());
    }

    public Optional<Employee> findEmployee(int id, String table) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM " + table + " WHERE id = ?")) {
            statement.setInt(1, id);

            boolean execute = statement.execute();

            if (!execute) {
                return Optional.empty();
            }

            ResultSet resultSet = statement.getResultSet();

            Employee employee = null;
            while (resultSet.next()) {
                if (employee == null) {
                    employee = new Employee();
                    employee.setId(resultSet.getInt("id"));
                    employee.setName(resultSet.getString("name"));
                    employee.setAge(resultSet.getInt("age"));
                    employee.setPosition(resultSet.getString("position"));
                    employee.setSalary(resultSet.getFloat(("salary")));
                }

                return Optional.of(employee);
            }
        }


        return Optional.empty();
    }
}
