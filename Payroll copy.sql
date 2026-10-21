
CREATE DATABASE IF NOT EXISTS PayrollDB;
USE PayrollDB;

CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE,
    joining_date DATE
);

CREATE TABLE salaries (
    salary_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL UNIQUE,
    basic_salary DECIMAL(10,2) NOT NULL,
    allowances DECIMAL(10,2) NOT NULL DEFAULT 0,
    tax_rate DECIMAL(5,2) NOT NULL DEFAULT 10.00,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    CHECK (basic_salary >= 0),
    CHECK (allowances >= 0),
    CHECK (tax_rate >= 0 AND tax_rate <= 100)
);

CREATE TABLE payroll (
    payroll_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    payroll_month DATE NOT NULL,
    gross_salary DECIMAL(10,2) NOT NULL,
    tax_amount DECIMAL(10,2) NOT NULL,
    other_deductions DECIMAL(10,2) NOT NULL DEFAULT 0,
    net_salary DECIMAL(10,2) NOT NULL,
    generated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    UNIQUE (employee_id, payroll_month)
);

INSERT INTO employees
(employee_name, department, email, joining_date)
VALUES
('krishna', 'IT', 'rahul@gmail.com', '2025-01-10'),
('narendra', 'HR', 'priya@gmail.com', '2025-02-15'),
('Arun', 'Finance', 'arun@gmail.com', '2025-03-01');

INSERT INTO salaries
(employee_id, basic_salary, allowances, tax_rate)
VALUES
(1, 40000.00, 5000.00, 10.00),
(2, 35000.00, 6000.00, 10.00),
(3, 50000.00, 8000.00, 12.00);

SELECT * FROM employees;
SELECT * FROM salaries;
SELECT * FROM payroll;
