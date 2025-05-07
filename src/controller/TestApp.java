package controller;

import java.sql.Date;
import java.util.List;
import java.util.Scanner;

import model.Transaction;
import model.User;
import service.TransactionService;
import service.UserService;

public class TestApp {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserService userService = new UserService();
        TransactionService transactionService = new TransactionService();

        User loggedInUser = null;

        while (true) {
            System.out.println("\n--- Personal Finance Dashboard ---");
            if (loggedInUser == null) {
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("0. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Password: ");
                        String password = scanner.nextLine();
                        System.out.println(userService.register(new User(name, email, password)));
                        break;

                    case 2:
                        System.out.print("Email: ");
                        String loginEmail = scanner.nextLine();
                        System.out.print("Password: ");
                        String loginPass = scanner.nextLine();
                        loggedInUser = userService.login(loginEmail, loginPass);
                        if (loggedInUser != null) {
                            System.out.println("✅ Login successful");
                        } 
                        else {
                            System.out.println("❌ Login failed. Please check your credentials.");
                        }

                        break;

                    case 0:
                        System.out.println("Goodbye!");
                        scanner.close();
                        return;
                }

            } else {
                System.out.println("\nWelcome, " + loggedInUser.getName());
                System.out.println("1. Add Transaction");
                System.out.println("2. View All Transactions");
                System.out.println("3. View Transactions by Category");
                System.out.println("4. Update Transaction");
                System.out.println("5. Delete Transaction");
                System.out.println("0. Logout");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Type (income/expense): ");
                        String type = scanner.nextLine();

                        System.out.print("Amount: ");
                        double amount = scanner.nextDouble();
       
                        System.out.print("Category: ");
                        String category = scanner.nextLine();

                        System.out.print("Description: ");
                        String desc = scanner.nextLine();

                        System.out.print("Date (yyyy-mm-dd): ");
                        String dateStr = scanner.nextLine();
                        Date date = Date.valueOf(dateStr);

                        Transaction t = new Transaction(
                            0, // id will be auto
                            loggedInUser.getId(),
                            type,
                            amount,
                            category,
                            desc,
                            date
                        );

                        String result = transactionService.addTransaction(t);
                        System.out.println(result);
                        break;

                    case 2:
                        List<Transaction> allTx = transactionService.getTransactionsByUser(loggedInUser.getId());
                        allTx.forEach(System.out::println);
                        break;

                    case 3:
                        System.out.print("Enter category: ");
                        String cat = scanner.nextLine();
                        List<Transaction> byCat = transactionService.getTransactionsByCategory(loggedInUser.getId(), cat);
                        byCat.forEach(System.out::println);
                        break;

                    case 4:
                        System.out.print("Transaction ID to update: ");
                        int updateId = scanner.nextInt();
                        scanner.nextLine();

                        System.out.print("New Type (income/expense): ");
                        String newType = scanner.nextLine();

                        System.out.print("New Amount: ");
                        double newAmount = scanner.nextDouble();
                        scanner.nextLine();

                        System.out.print("New Category: ");
                        String newCat = scanner.nextLine();

                        System.out.print("New Description: ");
                        String newDesc = scanner.nextLine();

                        System.out.print("New Date (yyyy-mm-dd): ");
                        Date newDate = Date.valueOf(scanner.nextLine());

                        Transaction updatedTx = new Transaction(updateId, loggedInUser.getId(), newType, newAmount, newCat, newDesc, newDate);
                        System.out.println(transactionService.updateTransaction(updatedTx));
                        break;

                    case 5:
                        System.out.print("Transaction ID to delete: ");
                        int deleteId = scanner.nextInt();
                        scanner.nextLine();
                        System.out.println(transactionService.deleteTransaction(deleteId, loggedInUser.getId()));
                        break;

                    case 0:
                        loggedInUser = null;
                        System.out.println("Logged out.");
                        break;
                }
            }
        }
    }
}
