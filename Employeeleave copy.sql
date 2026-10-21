
CREATE DATABASE IF NOT EXISTS EmployeeLeaveDB;
USE EmployeeLeaveDB;

CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    joining_date DATE
);

CREATE TABLE leave_balance (
    balance_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL UNIQUE,
    total_leaves INT NOT NULL DEFAULT 20,
    leaves_taken INT NOT NULL DEFAULT 0,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

CREATE TABLE leave_requests (
    leave_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(255) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED')
        NOT NULL DEFAULT 'PENDING',
    applied_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CHECK (end_date >= start_date)
);

INSERT INTO employees
(employee_name, department, email, joining_date)
VALUES
('krishna', 'IT', 'rahul@yahoo.com', '2025-01-10'),
('narendra', 'HR', 'priya@yahoo.com', '2025-02-15'),
('Arun', 'Finance', 'arun@yahoo.com', '2025-03-01');

INSERT INTO leave_balance (employee_id, total_leaves, leaves_taken)
SELECT employee_id, 20, 0 FROM employees;

SELECT * FROM employees;
SELECT * FROM leave_balance;
SELECT * FROM leave_requests;
