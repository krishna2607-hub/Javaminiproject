package javaproject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

enum ExpenseCategory {
    FOOD,
    TRAVEL,
    SHOPPING,
    EDUCATION,
    BILLS,
    ENTERTAINMENT,
    OTHER
}

enum PaymentMode {
    CASH,
    UPI,
    CARD,
    NET_BANKING
}

class Expense {

    private String referenceId;
    private String description;
    private ExpenseCategory category;
    private PaymentMode paymentMode;
    private double amount;
    private LocalDateTime createdAt;

    public Expense(String referenceId,
                   String description,
                   ExpenseCategory category,
                   PaymentMode paymentMode,
                   double amount) {

        this.referenceId = referenceId;
        this.description = description;
        this.category = category;
        this.paymentMode = paymentMode;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getDescription() {
        return description;
    }

    public ExpenseCategory getCategory() {
        return category;
    }

    public PaymentMode getPaymentMode() {
        return paymentMode;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

class ExpenseManager {

    private ArrayList<Expense> expenseList = new ArrayList<>();

    private HashMap<ExpenseCategory, Double> categorySummary =
            new HashMap<>();

    private int nextNumber = 1;

    private double monthlyBudget = 30000;

    private DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public void addExpense(String description,
                            ExpenseCategory category,
                            PaymentMode paymentMode,
                            double amount) {

        String referenceId = "EXP" + nextNumber++;

        Expense expense = new Expense(
                referenceId,
                description,
                category,
                paymentMode,
                amount
        );

        expenseList.add(expense);

        categorySummary.put(
                category,
                categorySummary.getOrDefault(category, 0.0) + amount
        );

        System.out.println(
                "\nExpense recorded with ID: " + referenceId
        );
    }

    public void showAllExpenses() {

        if (expenseList.isEmpty()) {
            System.out.println("\nNo expenses recorded.");
            return;
        }

        System.out.println("\n EXPENSE HISTORY ");

        for (Expense expense : expenseList) {

            System.out.println(
                    expense.getReferenceId() +
                    " | " +
                    expense.getDescription() +
                    " | " +
                    expense.getCategory() +
                    " | ₹" +
                    expense.getAmount() +
                    " | " +
                    expense.getPaymentMode() +
                    " | " +
                    expense.getCreatedAt().format(formatter)
            );
        }
    }

    public void showTotalSpending() {

        double total = expenseList.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        System.out.println(
                "\nTotal Spending: ₹" + total
        );

        System.out.println(
                "Remaining Budget: ₹" +
                (monthlyBudget - total)
        );
    }

    public void showHighestExpense() {

        Optional<Expense> highest =
                expenseList.stream()
                        .max(
                                Comparator.comparingDouble(
                                        Expense::getAmount
                                )
                        );

        if (highest.isPresent()) {

            Expense expense = highest.get();

            System.out.println("\n HIGHEST EXPENSE ");

            System.out.println(
                    "Reference : " +
                    expense.getReferenceId()
            );

            System.out.println(
                    "Description : " +
                    expense.getDescription()
            );

            System.out.println(
                    "Category : " +
                    expense.getCategory()
            );

            System.out.println(
                    "Amount : ₹" +
                    expense.getAmount()
            );
        }
    }

    public void showCategorySummary() {

        System.out.println("\n CATEGORY SUMMARY ");

        categorySummary.forEach(
                (category, amount) ->
                        System.out.println(
                                category + " : ₹" + amount
                        )
        );
    }

    public void searchCategory(ExpenseCategory category) {

        List<Expense> matchingExpenses =
                expenseList.stream()
                        .filter(
                                expense ->
                                        expense.getCategory()
                                                == category
                        )
                        .collect(Collectors.toList());

        System.out.println(
                "\nExpenses under " + category + ":"
        );

        if (matchingExpenses.isEmpty()) {

            System.out.println("No expenses found.");

        } else {

            matchingExpenses.forEach(
                    expense ->
                            System.out.println(
                                    expense.getReferenceId() +
                                    " - " +
                                    expense.getDescription() +
                                    " - ₹" +
                                    expense.getAmount()
                            )
            );
        }
    }

    public void removeExpense(String referenceId) {

        Expense selectedExpense = null;

        for (Expense expense : expenseList) {

            if (expense.getReferenceId()
                    .equalsIgnoreCase(referenceId)) {

                selectedExpense = expense;
                break;
            }
        }

        if (selectedExpense == null) {

            System.out.println(
                    "\nExpense ID not found."
            );

            return;
        }

        expenseList.remove(selectedExpense);

        ExpenseCategory category =
                selectedExpense.getCategory();

        double updatedAmount =
                categorySummary.get(category)
                        - selectedExpense.getAmount();

        categorySummary.put(category, updatedAmount);

        System.out.println(
                "\nExpense " +
                referenceId +
                " removed successfully."
        );
    }

    public void showBudgetStatus() {

        double total = expenseList.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        double remaining =
                monthlyBudget - total;

        System.out.println("\n BUDGET STATUS ");

        System.out.println(
                "Monthly Budget : ₹" +
                monthlyBudget
        );

        System.out.println(
                "Used Amount : ₹" +
                total
        );

        System.out.println(
                "Remaining : ₹" +
                remaining
        );

        if (remaining < 0) {

            System.out.println(
                    "Warning: Budget exceeded!"
            );

        } else {

            System.out.println(
                    "Budget is under control."
            );
        }
    }
}

public class Personalexpensetracker {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        ExpenseManager manager =
                new ExpenseManager();

        while (true) {

           

            System.out.println(
                    "       PERSONAL EXPENSE TRACKER"
            );

           

            System.out.println("1. Add Expense");
            System.out.println("2. View Expense History");
            System.out.println("3. Total Spending");
            System.out.println("4. Highest Expense");
            System.out.println("5. Category Summary");
            System.out.println("6. Search Category");
            System.out.println("7. Remove Expense");
            System.out.println("8. Budget Status");
            System.out.println("9. Exit");

            System.out.print("\nChoose an option: ");

            int choice = scanner.nextInt();

            switch (choice) {

                case 1:

                    scanner.nextLine();

                    System.out.print(
                            "Enter description: "
                    );

                    String description =
                            scanner.nextLine();

                    System.out.println(
                            "\nCategories:"
                    );

                    for (ExpenseCategory category :
                            ExpenseCategory.values()) {

                        System.out.println(
                                "- " + category
                        );
                    }

                    System.out.print(
                            "Enter category: "
                    );

                    ExpenseCategory category =
                            ExpenseCategory.valueOf(
                                    scanner.next()
                                            .toUpperCase()
                            );

                    System.out.println(
                            "\nPayment Modes:"
                    );

                    for (PaymentMode mode :
                            PaymentMode.values()) {

                        System.out.println(
                                "- " + mode
                        );
                    }

                    System.out.print(
                            "Enter payment mode: "
                    );

                    PaymentMode paymentMode =
                            PaymentMode.valueOf(
                                    scanner.next()
                                            .toUpperCase()
                            );

                    System.out.print(
                            "Enter amount: "
                    );

                    double amount =
                            scanner.nextDouble();

                    manager.addExpense(
                            description,
                            category,
                            paymentMode,
                            amount
                    );

                    break;

                case 2:

                    manager.showAllExpenses();

                    break;

                case 3:

                    manager.showTotalSpending();

                    break;

                case 4:

                    manager.showHighestExpense();

                    break;

                case 5:

                    manager.showCategorySummary();

                    break;

                case 6:

                    System.out.print(
                            "Enter category: "
                    );

                    ExpenseCategory searchCategory =
                            ExpenseCategory.valueOf(
                                    scanner.next()
                                            .toUpperCase()
                            );

                    manager.searchCategory(
                            searchCategory
                    );

                    break;

                case 7:

                    System.out.print(
                            "Enter Expense ID: "
                    );

                    String expenseId =
                            scanner.next();

                    manager.removeExpense(
                            expenseId
                    );

                    break;

                case 8:

                    manager.showBudgetStatus();

                    break;

                case 9:

                    System.out.println(
                            "\nThank you for using " +
                            "Personal Expense Tracker!"
                    );

                    scanner.close();

                    return;

                default:

                    System.out.println(
                            "\nInvalid option."
                    );
            }
        }
    }
}
