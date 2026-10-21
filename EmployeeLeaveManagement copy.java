package JDBCtasks;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

public class EmployeeLeaveManagement {

    static final String URL =
            "jdbc:mysql://localhost:3306/EmployeeLeaveDB";
    static final String USER = "root";
    static final String PASSWORD = "krishna@123";

    static Scanner sc = new Scanner(System.in);

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static void addEmployee() {
        String sql = "INSERT INTO employees " +
                "(employee_name, department, email, joining_date) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql,
                     Statement.RETURN_GENERATED_KEYS)) {

            System.out.print("Enter employee name: ");
            ps.setString(1, sc.nextLine());

            System.out.print("Enter department: ");
            ps.setString(2, sc.nextLine());

            System.out.print("Enter email: ");
            ps.setString(3, sc.nextLine());

            System.out.print("Enter joining date (YYYY-MM-DD): ");
            ps.setDate(4, Date.valueOf(sc.nextLine()));

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    try (PreparedStatement balance = con.prepareStatement(
                            "INSERT INTO leave_balance " +
                            "(employee_id, total_leaves, leaves_taken) " +
                            "VALUES (?, 20, 0)")) {
                        balance.setInt(1, id);
                        balance.executeUpdate();
                    }
                    System.out.println("Employee added. ID: " + id);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewEmployees() {
        String sql = "SELECT * FROM employees ORDER BY employee_id";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        rs.getInt("employee_id") + " | " +
                        rs.getString("employee_name") + " | " +
                        rs.getString("department") + " | " +
                        rs.getString("email") + " | " +
                        rs.getDate("joining_date"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void updateEmployee() {
        try (Connection con = getConnection()) {
            System.out.print("Enter employee ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter new name: ");
            String name = sc.nextLine();

            System.out.print("Enter new department: ");
            String department = sc.nextLine();

            System.out.print("Enter new email: ");
            String email = sc.nextLine();

            String sql = "UPDATE employees SET employee_name=?, " +
                    "department=?, email=? WHERE employee_id=?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, name);
                ps.setString(2, department);
                ps.setString(3, email);
                ps.setInt(4, id);

                int rows = ps.executeUpdate();
                System.out.println(rows > 0
                        ? "Employee updated."
                        : "Employee not found.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void deleteEmployee() {
        System.out.print("Enter employee ID to delete: ");
        int id = Integer.parseInt(sc.nextLine());

        String sql = "DELETE FROM employees WHERE employee_id=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();

            System.out.println(rows > 0
                    ? "Employee deleted."
                    : "Employee not found.");
        } catch (SQLException e) {
            System.out.println(
                    "Cannot delete employee. Related leave records may exist.");
        }
    }

    static void applyLeave() {
        try (Connection con = getConnection()) {
            System.out.print("Enter employee ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter start date (YYYY-MM-DD): ");
            LocalDate start = LocalDate.parse(sc.nextLine());

            System.out.print("Enter end date (YYYY-MM-DD): ");
            LocalDate end = LocalDate.parse(sc.nextLine());

            if (end.isBefore(start)) {
                System.out.println("End date cannot be before start date.");
                return;
            }

            System.out.print("Enter leave reason: ");
            String reason = sc.nextLine();

            String sql = "INSERT INTO leave_requests " +
                    "(employee_id, start_date, end_date, reason) " +
                    "VALUES (?, ?, ?, ?)";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.setDate(2, Date.valueOf(start));
                ps.setDate(3, Date.valueOf(end));
                ps.setString(4, reason);

                ps.executeUpdate();
                System.out.println("Leave application submitted.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewLeaveRequests() {
        String sql = "SELECT l.leave_id, e.employee_name, " +
                "l.start_date, l.end_date, l.reason, l.status " +
                "FROM leave_requests l JOIN employees e " +
                "ON l.employee_id=e.employee_id ORDER BY l.leave_id";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        "Leave ID: " + rs.getInt("leave_id") +
                        " | Employee: " + rs.getString("employee_name") +
                        " | Start: " + rs.getDate("start_date") +
                        " | End: " + rs.getDate("end_date") +
                        " | Reason: " + rs.getString("reason") +
                        " | Status: " + rs.getString("status"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void approveLeave() {
        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                System.out.print("Enter leave ID to approve: ");
                int leaveId = Integer.parseInt(sc.nextLine());

                int employeeId;
                LocalDate start;
                LocalDate end;

                String find = "SELECT employee_id, start_date, end_date " +
                        "FROM leave_requests WHERE leave_id=? " +
                        "AND status='PENDING' FOR UPDATE";

                try (PreparedStatement ps = con.prepareStatement(find)) {
                    ps.setInt(1, leaveId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println(
                                    "Pending leave request not found.");
                            con.rollback();
                            return;
                        }

                        employeeId = rs.getInt("employee_id");
                        start = rs.getDate("start_date").toLocalDate();
                        end = rs.getDate("end_date").toLocalDate();
                    }
                }

                int days = (int) ChronoUnit.DAYS.between(start, end) + 1;

                String balanceSql = "SELECT total_leaves, leaves_taken " +
                        "FROM leave_balance WHERE employee_id=? FOR UPDATE";

                try (PreparedStatement ps =
                             con.prepareStatement(balanceSql)) {
                    ps.setInt(1, employeeId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Leave balance record not found.");
                        }

                        int available = rs.getInt("total_leaves")
                                - rs.getInt("leaves_taken");

                        if (days > available) {
                            System.out.println(
                                    "Insufficient leave balance. Available: "
                                    + available);
                            con.rollback();
                            return;
                        }
                    }
                }

                String updateBalance =
                        "UPDATE leave_balance SET leaves_taken = " +
                        "leaves_taken + ? WHERE employee_id=?";

                try (PreparedStatement ps =
                             con.prepareStatement(updateBalance)) {
                    ps.setInt(1, days);
                    ps.setInt(2, employeeId);
                    ps.executeUpdate();
                }

                String updateRequest =
                        "UPDATE leave_requests SET status='APPROVED' " +
                        "WHERE leave_id=? AND status='PENDING'";

                try (PreparedStatement ps =
                             con.prepareStatement(updateRequest)) {
                    ps.setInt(1, leaveId);

                    if (ps.executeUpdate() != 1) {
                        throw new SQLException(
                                "Request status changed. Approval cancelled.");
                    }
                }

                con.commit();
                System.out.println("Leave approved. Days deducted: " + days);

            } catch (Exception e) {
                con.rollback();
                System.out.println("Approval failed: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static void rejectLeave() {
        String sql = "UPDATE leave_requests SET status='REJECTED' " +
                "WHERE leave_id=? AND status='PENDING'";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter leave ID to reject: ");
            int id = Integer.parseInt(sc.nextLine());

            ps.setInt(1, id);
            System.out.println(ps.executeUpdate() == 1
                    ? "Leave rejected."
                    : "Pending leave request not found.");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void checkLeaveBalance() {
        String sql = "SELECT e.employee_id, e.employee_name, " +
                "b.total_leaves, b.leaves_taken, " +
                "(b.total_leaves-b.leaves_taken) AS available " +
                "FROM employees e JOIN leave_balance b " +
                "ON e.employee_id=b.employee_id WHERE e.employee_id=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter employee ID: ");
            ps.setInt(1, Integer.parseInt(sc.nextLine()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Employee: " +
                            rs.getString("employee_name"));
                    System.out.println("Total leaves: " +
                            rs.getInt("total_leaves"));
                    System.out.println("Leaves taken: " +
                            rs.getInt("leaves_taken"));
                    System.out.println("Available leaves: " +
                            rs.getInt("available"));
                } else {
                    System.out.println("Employee not found.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewLeaveHistory() {
        String sql = "SELECT leave_id, start_date, end_date, " +
                "reason, status FROM leave_requests " +
                "WHERE employee_id=? ORDER BY leave_id";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter employee ID: ");
            ps.setInt(1, Integer.parseInt(sc.nextLine()));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println(
                            "Leave ID: " + rs.getInt("leave_id") +
                            " | Start: " + rs.getDate("start_date") +
                            " | End: " + rs.getDate("end_date") +
                            " | Reason: " + rs.getString("reason") +
                            " | Status: " + rs.getString("status"));
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Add MySQL Connector/J to Eclipse.");
            return;
        }

        while (true) {
            System.out.println("\n EMPLOYEE LEAVE MANAGEMENT ");
            System.out.println("1. Add Employee");
            System.out.println("2. View Employees");
            System.out.println("3. Update Employee");
            System.out.println("4. Delete Employee");
            System.out.println("5. Apply for Leave");
            System.out.println("6. View Leave Requests");
            System.out.println("7. Approve Leave");
            System.out.println("8. Reject Leave");
            System.out.println("9. Check Leave Balance");
            System.out.println("10. View Leave History");
            System.out.println("11. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1: addEmployee(); break;
                    case 2: viewEmployees(); break;
                    case 3: updateEmployee(); break;
                    case 4: deleteEmployee(); break;
                    case 5: applyLeave(); break;
                    case 6: viewLeaveRequests(); break;
                    case 7: approveLeave(); break;
                    case 8: rejectLeave(); break;
                    case 9: checkLeaveBalance(); break;
                    case 10: viewLeaveHistory(); break;
                    case 11:
                        System.out.println("Thank you!");
                        sc.close();
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }
    }
}

