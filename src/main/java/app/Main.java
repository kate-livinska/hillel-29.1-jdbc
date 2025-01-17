package app;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        String userName = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        String dbName = System.getenv("DB_NAME");
        String url = "jdbc:mysql://localhost:52000/" + dbName + "?allowPublicKeyRetrieval=true&useSSL=false";

        String table = "employees";
        String backupTable = "employees_backup";

        Class.forName("com.mysql.cj.jdbc.Driver");

        try (Connection connection = DatabaseConnector.connectToDatabase(url, userName, password)) {

            Employee testEmployee = new Employee("TEST", 42, "TEST", 6500.0F);
            EmployeeDAO employeeDAO = new EmployeeDAO(connection);

            Optional<Employee> employee = employeeDAO.addEmployee(testEmployee, table);
            Employee savedEmployee = employee.get();

            savedEmployee.setSalary(10000.0F);
            employeeDAO.changeEmployee(savedEmployee, table);

            LinkedHashMap<String, String> columns = new LinkedHashMap<>();
            columns.put("id", "INT PRIMARY KEY AUTO_INCREMENT");
            columns.put("name", "VARCHAR(255)");
            columns.put("age", "INT");
            columns.put("position", "VARCHAR(255)");
            columns.put("salary", "FLOAT");

            DatabaseConnector.createTable(connection, backupTable, columns);

            Optional<Employee> backupEmployee = employeeDAO.addEmployee(testEmployee, backupTable);
            Employee savedBackupEmployee = backupEmployee.get();
            System.out.println(savedBackupEmployee.toString());

            employeeDAO.deleteEmployee(savedBackupEmployee, backupTable);

            Optional<Employee> optionalFoundEmployee = employeeDAO.findEmployee(2, backupTable);
            Employee foundEmployee = optionalFoundEmployee.get();
            System.out.println(foundEmployee.toString());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
