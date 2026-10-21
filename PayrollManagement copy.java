package JDBCtasks;

import java.sql.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Scanner;

public class PayrollManagement {

    static final String URL =
            "jdbc:mysql://localhost:3306/PayrollDB";
    static final String USER = "root";
    static final String PASSWORD = "krishna@123";

    static Scanner sc = new Scanner(System.in);

    static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    static void addEmployee() {
        String employeeSql = "INSERT INTO employees " +
                "(employee_name, department, email, joining_date) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection con = getConnection()) {
            con.setAutoCommit(false);

            try {
                System.out.print("Enter employee name: ");
                String name = sc.nextLine();

                System.out.print("Enter department: ");
                String department = sc.nextLine();

                System.out.print("Enter email: ");
                String email = sc.nextLine();

                System.out.print("Enter joining date (YYYY-MM-DD): ");
                Date joiningDate = Date.valueOf(sc.nextLine());

                System.out.print("Enter basic salary: ");
                BigDecimal basic = new BigDecimal(sc.nextLine());

                System.out.print("Enter allowances: ");
                BigDecimal allowances = new BigDecimal(sc.nextLine());

                System.out.print("Enter tax rate (%): ");
                BigDecimal taxRate = new BigDecimal(sc.nextLine());

                if (basic.signum() < 0 || allowances.signum() < 0 ||
                        taxRate.signum() < 0 ||
                        taxRate.compareTo(new BigDecimal("100")) > 0) {
                    System.out.println("Invalid salary or tax values.");
                    con.rollback();
                    return;
                }

                int employeeId;

                try (PreparedStatement ps = con.prepareStatement(
                        employeeSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, department);
                    ps.setString(3, email);
                    ps.setDate(4, joiningDate);
                    ps.executeUpdate();

                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        rs.next();
                        employeeId = rs.getInt(1);
                    }
                }

                String salarySql = "INSERT INTO salaries " +
                        "(employee_id, basic_salary, allowances, tax_rate) " +
                        "VALUES (?, ?, ?, ?)";

                try (PreparedStatement ps =
                             con.prepareStatement(salarySql)) {
                    ps.setInt(1, employeeId);
                    ps.setBigDecimal(2, basic);
                    ps.setBigDecimal(3, allowances);
                    ps.setBigDecimal(4, taxRate);
                    ps.executeUpdate();
                }

                con.commit();
                System.out.println("Employee and salary added. ID: "
                        + employeeId);

            } catch (Exception e) {
                con.rollback();
                System.out.println("Error: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    static void viewEmployees() {
        String sql = "SELECT e.employee_id, e.employee_name, " +
                "e.department, e.email, s.basic_salary, s.allowances, " +
                "s.tax_rate FROM employees e LEFT JOIN salaries s " +
                "ON e.employee_id=s.employee_id ORDER BY e.employee_id";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        "ID: " + rs.getInt("employee_id") +
                        " | Name: " + rs.getString("employee_name") +
                        " | Department: " + rs.getString("department") +
                        " | Email: " + rs.getString("email") +
                        " | Basic: " + rs.getBigDecimal("basic_salary") +
                        " | Allowances: " + rs.getBigDecimal("allowances") +
                        " | Tax %: " + rs.getBigDecimal("tax_rate"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void updateSalary() {
        try (Connection con = getConnection()) {
            System.out.print("Enter employee ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter new basic salary: ");
            BigDecimal basic = new BigDecimal(sc.nextLine());

            System.out.print("Enter new allowances: ");
            BigDecimal allowances = new BigDecimal(sc.nextLine());

            System.out.print("Enter new tax rate (%): ");
            BigDecimal taxRate = new BigDecimal(sc.nextLine());

            if (basic.signum() < 0 || allowances.signum() < 0 ||
                    taxRate.signum() < 0 ||
                    taxRate.compareTo(new BigDecimal("100")) > 0) {
                System.out.println("Invalid salary or tax values.");
                return;
            }

            String sql = "UPDATE salaries SET basic_salary=?, " +
                    "allowances=?, tax_rate=? WHERE employee_id=?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setBigDecimal(1, basic);
                ps.setBigDecimal(2, allowances);
                ps.setBigDecimal(3, taxRate);
                ps.setInt(4, id);

                System.out.println(ps.executeUpdate() == 1
                        ? "Salary updated."
                        : "Salary record not found.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void generatePayroll() {
        try (Connection con = getConnection()) {
            System.out.print("Enter employee ID: ");
            int id = Integer.parseInt(sc.nextLine());

            System.out.print("Enter payroll month (YYYY-MM): ");
            LocalDate month = LocalDate.parse(sc.nextLine() + "-01");

            System.out.print("Enter other deductions: ");
            BigDecimal deductions = new BigDecimal(sc.nextLine());

            if (deductions.signum() < 0) {
                System.out.println("Deductions cannot be negative.");
                return;
            }

            con.setAutoCommit(false);

            try {
                String find = "SELECT s.basic_salary, s.allowances, " +
                        "s.tax_rate FROM salaries s JOIN employees e " +
                        "ON e.employee_id=s.employee_id " +
                        "WHERE e.employee_id=? FOR UPDATE";

                BigDecimal basic;
                BigDecimal allowances;
                BigDecimal taxRate;

                try (PreparedStatement ps = con.prepareStatement(find)) {
                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            System.out.println(
                                    "Employee or salary record not found.");
                            con.rollback();
                            return;
                        }

                        basic = rs.getBigDecimal("basic_salary");
                        allowances = rs.getBigDecimal("allowances");
                        taxRate = rs.getBigDecimal("tax_rate");
                    }
                }

                BigDecimal gross = money(basic.add(allowances));
                BigDecimal tax = money(gross.multiply(taxRate)
                        .divide(new BigDecimal("100"), 4,
                                RoundingMode.HALF_UP));
                BigDecimal net = money(gross.subtract(tax)
                        .subtract(deductions));

                if (net.signum() < 0) {
                    System.out.println(
                            "Deductions cannot exceed salary after tax.");
                    con.rollback();
                    return;
                }

                String insert = "INSERT INTO payroll " +
                        "(employee_id, payroll_month, gross_salary, " +
                        "tax_amount, other_deductions, net_salary) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";

                try (PreparedStatement ps =
                             con.prepareStatement(insert)) {
                    ps.setInt(1, id);
                    ps.setDate(2, Date.valueOf(month));
                    ps.setBigDecimal(3, gross);
                    ps.setBigDecimal(4, tax);
                    ps.setBigDecimal(5, deductions);
                    ps.setBigDecimal(6, net);
                    ps.executeUpdate();
                }

                con.commit();

                System.out.println("Employee ID: " + id);
                System.out.println("Month: " + month);
                System.out.println("Basic Salary: " + basic);
                System.out.println("Allowances: " + allowances);
                System.out.println("Gross Salary: " + gross);
                System.out.println("Tax: " + tax);
                System.out.println("Other Deductions: " + deductions);
                System.out.println("Net Salary: " + net);

            } catch (Exception e) {
                con.rollback();
                System.out.println("Payroll failed: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void generatePayrollBatch() {
        try (Connection con = getConnection()) {
            System.out.print("Enter payroll month (YYYY-MM): ");
            LocalDate month = LocalDate.parse(sc.nextLine() + "-01");

            con.setAutoCommit(false);

            String select = "SELECT e.employee_id, s.basic_salary, " +
                    "s.allowances, s.tax_rate FROM employees e " +
                    "JOIN salaries s ON e.employee_id=s.employee_id";

            String insert = "INSERT INTO payroll " +
                    "(employee_id, payroll_month, gross_salary, " +
                    "tax_amount, other_deductions, net_salary) " +
                    "VALUES (?, ?, ?, ?, 0, ?)";

            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery(select);
                 PreparedStatement ps = con.prepareStatement(insert)) {

                int count = 0;

                while (rs.next()) {
                    int id = rs.getInt("employee_id");
                    BigDecimal basic = rs.getBigDecimal("basic_salary");
                    BigDecimal allowances = rs.getBigDecimal("allowances");
                    BigDecimal rate = rs.getBigDecimal("tax_rate");

                    BigDecimal gross = money(basic.add(allowances));
                    BigDecimal tax = money(gross.multiply(rate)
                            .divide(new BigDecimal("100"), 4,
                                    RoundingMode.HALF_UP));
                    BigDecimal net = money(gross.subtract(tax));

                    ps.setInt(1, id);
                    ps.setDate(2, Date.valueOf(month));
                    ps.setBigDecimal(3, gross);
                    ps.setBigDecimal(4, tax);
                    ps.setBigDecimal(5, net);
                    ps.addBatch();
                    count++;
                }

                if (count == 0) {
                    System.out.println("No employees with salary records.");
                    con.rollback();
                    return;
                }

                ps.executeBatch();
                con.commit();
                System.out.println(
                        "Batch payroll generated for " + count + " employees.");

            } catch (Exception e) {
                con.rollback();
                System.out.println(
                        "Batch payroll failed: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewPayroll() {
        String sql = "SELECT p.payroll_id, e.employee_name, " +
                "p.payroll_month, p.gross_salary, p.tax_amount, " +
                "p.other_deductions, p.net_salary FROM payroll p " +
                "JOIN employees e ON p.employee_id=e.employee_id " +
                "ORDER BY p.payroll_month DESC, p.payroll_id";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                System.out.println(
                        "Payroll ID: " + rs.getInt("payroll_id") +
                        " | Employee: " + rs.getString("employee_name") +
                        " | Month: " + rs.getDate("payroll_month") +
                        " | Gross: " + rs.getBigDecimal("gross_salary") +
                        " | Tax: " + rs.getBigDecimal("tax_amount") +
                        " | Deductions: " +
                        rs.getBigDecimal("other_deductions") +
                        " | Net: " + rs.getBigDecimal("net_salary"));
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void viewPayslip() {
        String sql = "SELECT p.*, e.employee_name, e.department, " +
                "e.email FROM payroll p JOIN employees e " +
                "ON p.employee_id=e.employee_id WHERE p.employee_id=? " +
                "AND p.payroll_month=?";

        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Enter employee ID: ");
            ps.setInt(1, Integer.parseInt(sc.nextLine()));

            System.out.print("Enter payroll month (YYYY-MM): ");
            LocalDate month = LocalDate.parse(sc.nextLine() + "-01");
            ps.setDate(2, Date.valueOf(month));

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("Payslip not found.");
                    return;
                }

                System.out.println("\n EMPLOYEE PAYSLIP ");
                System.out.println("Employee: " +
                        rs.getString("employee_name"));
                System.out.println("Department: " +
                        rs.getString("department"));
                System.out.println("Email: " + rs.getString("email"));
                System.out.println("Month: " +
                        rs.getDate("payroll_month"));
                System.out.println("Gross Salary: " +
                        rs.getBigDecimal("gross_salary"));
                System.out.println("Tax: " +
                        rs.getBigDecimal("tax_amount"));
                System.out.println("Other Deductions: " +
                        rs.getBigDecimal("other_deductions"));
                System.out.println("Net Salary: " +
                        rs.getBigDecimal("net_salary"));
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void salarySummary() {
        String sql = "SELECT COUNT(*) AS employee_count, " +
                "COALESCE(SUM(gross_salary), 0) AS total_gross, " +
                "COALESCE(SUM(tax_amount), 0) AS total_tax, " +
                "COALESCE(SUM(net_salary), 0) AS total_net " +
                "FROM payroll";

        try (Connection con = getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                System.out.println("Payroll records: " +
                        rs.getInt("employee_count"));
                System.out.println("Total gross salary: " +
                        rs.getBigDecimal("total_gross"));
                System.out.println("Total tax: " +
                        rs.getBigDecimal("total_tax"));
                System.out.println("Total net salary: " +
                        rs.getBigDecimal("total_net"));
            }
        } catch (SQLException e) {
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
            System.out.println("\n PAYROLL MANAGEMENT SYSTEM ");
            System.out.println("1. Add Employee and Salary");
            System.out.println("2. View Employees and Salaries");
            System.out.println("3. Update Salary");
            System.out.println("4. Generate Individual Payroll");
            System.out.println("5. Generate Payroll for All Employees");
            System.out.println("6. View Payroll Records");
            System.out.println("7. View Payslip");
            System.out.println("8. Salary Summary");
            System.out.println("9. Exit");
            System.out.print("Enter your choice: ");

            try {
                int choice = Integer.parseInt(sc.nextLine());

                switch (choice) {
                    case 1: addEmployee(); break;
                    case 2: viewEmployees(); break;
                    case 3: updateSalary(); break;
                    case 4: generatePayroll(); break;
                    case 5: generatePayrollBatch(); break;
                    case 6: viewPayroll(); break;
                    case 7: viewPayslip(); break;
                    case 8: salarySummary(); break;
                    case 9:
                        sc.close();
                        System.out.println("Thank you!");
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

