import database.Database;
import page.Page;
import user.Admin;
import user.User;
import book.Book;

import java.util.ArrayList;
import java.util.HashMap;
import java.time.LocalDate;
import java.util.Scanner;

public abstract class LibrarySystem {
    private static Admin admin;
    private static final String admin_file = "serialized_admin.ser";
    private static User user;
    private static final boolean show_passwords = false;

    private static final int user_min_age = 15;

    private static final String user_name_pattern = "^[^\\d]+$";
    private static final String user_date_pattern = "^\\d{4}\\-(?:0[1-9]|1[012])\\-(?:0[1-9]|[12][0-9]|3[01])$";
    private static final String user_pesel_pattern = "^\\d{11}$";
    private static final String user_email_pattern = "^[^\\s\\.]+(?:\\.[^\\s]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    private static final String user_password_pattern = "^\\S{4,}$";

    private static final String book_id_pattern = "^\\d+$";
    private static final String book_title_pattern = "^[^\\d]+$";
    private static final String book_author_pattern = "^(?:(?:[^\\s\\d]+\\.[\\ ]?)+[^\\s\\d]+){1}$|^(?:[^\\s\\d]+\\ [^\\s\\d]+){1}$";
    private static final String book_year_pattern = "^[0-9]{4}$";
    private static final String book_loan_period_pattern = "^\\d{1,31}$";

    private static final String users_data_file = "users_file.txt";
    private static final String books_data_file = "books_file.txt";

    private static Database user_database;
    private static Database book_database;
    private static final int table_size = 10;

    public static void runSystem() {
        admin = new Admin().deserialize(admin_file);
        String result1 = admin == null ? " Deserializing admin failed.\n" : "";

        user_database = new Database(users_data_file);
        String result2 = user_database.loadFromFile(new User()) ? "" : " Loading users data from file \"" + users_data_file + "\" failed.\n";

        book_database = new Database(books_data_file);
        String result3 = book_database.loadFromFile(new Book()) ? "" : " Loading books data from file \"" + books_data_file + "\" failed.\n";

        startPage(result1 + result2 + result3);
    }

    private static void startPage(String comment) {
        Page start_page = new Page(
                "WELCOME TO THE LIBRARY MANAGEMENT SYSTEM",
                new String[]{
                        "CREATE ACCOUNT~(1)",
                        "LOGIN AS USER~(2)",
                        "LOGIN AS ADMIN~(3)",
                        "EXIT~(4)"
                },
                comment,
                new String[]{"~[1-4]{1}~next_step"});

        HashMap<String, String> user_inputs = start_page.displayPageAndGetInputs();

        char next_step = user_inputs.get("next_step").charAt(0);
        switch (next_step) {
            case '1':
                createAccount("");
                break;
            case '2':
                loginAsUser("");
                break;
            case '3':
                loginAsAdmin("");
                break;
            case '4':
                finishSystem();
                break;
        }
    }

    private static void finishSystem() {
        Page.clearPage();

        book_database.sort(new String[] { "id" });

        if (!admin.serialize(admin_file))
            startPage("Serializing admin to file \"" + admin_file + "\" failed.\n");
        if (!user_database.saveToFile(new User()))
            startPage("Saving users data to file \"" + users_data_file + "\" failed.\n");
        if (!book_database.saveToFile(new Book()))
            startPage("Saving books data to file \"" + books_data_file + "\" failed.\n");

        System.out.println("\n Admin successfully serialized to file \"" + admin_file + "\".");
        System.out.println(" Users data successfully saved to file \"" + users_data_file + "\".");
        System.out.println(" Books data successfully saved to file \"" + books_data_file + "\".");

        System.out.println("\n Thank you for using the Library Management System!");
        System.exit(0);
    }

    // USER SECTION ------------------------------------

    private static void createAccount(String comment) {
        Page create_account_page = new Page(
                "CREATE ACCOUNT",
                new String[]{},
                comment,
                new String[]{
                        "Names (space separated)~" + user_name_pattern + "~names",
                        "Last name~" + user_name_pattern + "~last_name",
                        "Birth date (yyyy-mm-dd)~" + user_date_pattern + "~birth_date",
                        "PESEL~" + user_pesel_pattern + "~pesel",
                        "Email~" + user_email_pattern + "~email",
                        "Password (min 4 characters)~" + user_password_pattern + "~password",
                        "Confirm password~" + user_password_pattern + "$~confirm_password",
                        "\n >> CREATE ACCOUNT (1)  /  BACK (2)~^[12]$~next_step"
                });

        HashMap<String, String> user_inputs = create_account_page.displayPageAndGetInputs();

        if (user_inputs.get("next_step").compareTo("2") == 0) {
            startPage(" Creating account canceled.\n");
        } else {
            try {
                LocalDate.parse(user_inputs.get("birth_date"));
            } catch (Exception e) {
                createAccount(" Invalid date input. Date does not exist.\n\n");
            }
            LocalDate today = LocalDate.now();
            LocalDate input_date = LocalDate.parse(user_inputs.get("birth_date"));
            if (input_date.isAfter(today)) {
                createAccount(" Invalid date input. Date refers to the future.\n\n");
            } else if (input_date.isAfter(today.minusYears(user_min_age))) {
                startPage(" You are under " + user_min_age + " years old. Creating account canceled.\n");
            } else if (user_database.getRecord(new User().setPesel(Long.parseLong(user_inputs.get("pesel"))), "pesel") != null) {
                createAccount(" User with given PESEL already exists.\n\n");
            } else if (user_database.getRecord(new User().setEmail(user_inputs.get("email")), "email") != null) {
                createAccount(" This email is not available. Account already exists.\n\n");
            } else if (user_inputs.get("password").compareTo(user_inputs.get("confirm_password")) != 0) {
                createAccount(" Passwords do not match.\n\n");
            } else {
                user = new User(
                        user_inputs.get("names").split(" "),
                        user_inputs.get("last_name"),
                        LocalDate.parse(user_inputs.get("birth_date")),
                        Long.parseLong(user_inputs.get("pesel")),
                        user_inputs.get("email"),
                        user_inputs.get("password"),
                        new ArrayList<>()
                );

                user_database.addRecord(user);
                userHomePage("");
            }
        }
    }

    private static void loginAsUser(String comment) {
        Page user_login_page = new Page(
                "LOGIN AS USER",
                new String[]{},
                comment,
                new String[]{
                        "Email~" + user_email_pattern + "~email",
                        "Password~" + user_password_pattern + "~password",
                        "\n >> LOGIN (1)  /  BACK (2)~^[12]$~login"
                });

        HashMap<String, String> user_inputs = user_login_page.displayPageAndGetInputs();

        if (user_inputs.get("login").compareTo("2") == 0) {
            startPage(" Logging canceled.\n");
        } else {
            String email = user_inputs.get("email");
            String password = user_inputs.get("password");

            User found = User.verifyPassword(user_database, email, password);

            if (found == null)
                loginAsUser(" Wrong email or password!\n\n");
            else {
                user = found;
                userHomePage("");
            }
        }
    }

    private static void userHomePage(String comment) {
        Page user_home_page = new Page(
                "HI, " + user.getNames()[0].toUpperCase() + " " + user.getLastName().toUpperCase() + "!",
                new String[]{
                        "EDIT PROFILE~(1)",
                        "BORROW BOOK~(2)",
                        "RETURN BOOK~(3)",
                        "LOG OUT~(4)"
                },
                comment,
                new String[]{"~[1-4]{1}~next_step"});

        HashMap<String, String> user_inputs = user_home_page.displayPageAndGetInputs();

        char next_step = user_inputs.get("next_step").charAt(0);
        switch (next_step) {
            case '1':
                userProfilePage("", User.copy(user));
                break;
            case '2':
                userBorrowTable("", 0);
                break;
            case '3':
                userReturnTable("", 0);
                break;
            case '4':
                startPage(" Logged out.\n");
                break;
        }
    }

    private static void userProfilePage(String comment, User user_before_edit) {
        Page user_profile_page = new Page(
                "PROFILE",
                new String[]{
                        " ~edit",
                        "Names:       " + String.join(" ", user.getNames()) + "~(1)",
                        "Last Name:   " + user.getLastName() + "~(2)",
                        "Birth Date:  " + user.getBirthDate() + "~(3)",
                        "PESEL:       " + user.getPesel() + "~(4)",
                        "Email:       " + user.getEmail() + "~(5)",
                        "Password:    " + (show_passwords ? user.getPassword() : "*".repeat(user.getPassword().length())) + "~(6)",
                        " ~ ",
                        "SAVE~(7)",
                        "CANCEL~(8)",
                        "DELETE ACCOUNT~(9)"
                },
                comment,
                new String[]{"~[1-9]{1}~next_step"});

        HashMap<String, String> user_inputs = user_profile_page.displayPageAndGetInputs();

        char next_step = user_inputs.get("next_step").charAt(0);
        Page.clearPage();
        switch (next_step) {
            case '1':
                user.setNames(Page.getValidatedInput("\n Names (space separated)", user_name_pattern).split(" "));
                break;
            case '2':
                user.setLastName(Page.getValidatedInput(" Last name", user_name_pattern));
                break;
            case '3':
                String data;
                data = Page.getValidatedInput(" Birdth Date (yyyy-mm-dd)", user_date_pattern);
                try {
                    LocalDate.parse(data);
                    if (LocalDate.parse(data).isAfter(LocalDate.now()))
                        userProfilePage(" Invalid data input. Date refers to the future.\n", user_before_edit);
                    if (LocalDate.parse(data).isAfter(LocalDate.now().minusYears(user_min_age)))
                        userProfilePage(" Invalid data input. You cannot be under " + user_min_age + " years old.\n",
                                user_before_edit);
                    user.setBirthDate(LocalDate.parse(data));
                    break;
                } catch (Exception e) {
                    userProfilePage(" Invalid data input. Date does not exist.\n", user_before_edit);
                }
            case '4':
                user.setPesel(Long.parseLong(Page.getValidatedInput(" PESEL", user_pesel_pattern)));
                break;
            case '5':
                user.setEmail(Page.getValidatedInput(" Email", user_email_pattern));
                break;
            case '6':
                user.setPassword(Page.getValidatedInput(" Password (min 4 characters)", user_password_pattern));
                break;
            case '7':
                userHomePage(" Profile changes saved.\n");
                break;
            case '8':
                user = user_before_edit;
                user_database.updateRecord(user);
                userHomePage(" Profile changes canceled.\n");
                break;
            case '9':
                if (!user.getBooks().isEmpty())
                    userHomePage(" You cannot delete account having any books borrowed.\n");
                user_database.removeRecord(user);
                startPage(" Account deleted.\n");
                break;
        }
        userProfilePage("", user_before_edit);
    }

    private static void userBorrowTable(String comment, int start_index) {
        Page.clearPage();

        int[] prev_printed_last = book_database.display(
                new String[] { "serial id", "book", "loan period", "available" },
                new Book().setUserId(-1),
                start_index,
                table_size,
                new String[] {"user_id", "id"},
                "serial_id",
                "user_id"
        );

        System.out.print(comment);

        System.out.println("\n" +
                (start_index == prev_printed_last[0] ? "" : ">> PREV " + table_size + " RECORDS (W)\n") +
                (prev_printed_last[2] == book_database.getSize() ? "" : ">> NEXT " + table_size + " RECORDS (S)\n") +
                ">> BACK (Q)" +
                (prev_printed_last[1] == 0 ? "" : "\n\n>> BORROW BOOK BY GIVING ITS ID.\n")
        );

        String available_input = "Qq";
        if (!(start_index == prev_printed_last[0])) available_input += "Ww";
        if (!(prev_printed_last[2] == book_database.getSize())) available_input += "Ss";
        available_input = "[" + available_input + "]";
        if (!(prev_printed_last[1] == 0)) available_input += "|\\d+";
        available_input = "^" + available_input + "$";

        String next_step = Page.getValidatedInput("", available_input);
        switch (next_step.charAt(0)) {
            case 'W':
            case 'w':
                userBorrowTable("", prev_printed_last[0]);
                break;
            case 'S':
            case 's':
                userBorrowTable("", prev_printed_last[2] == book_database.getSize() ? start_index : prev_printed_last[2]);
                break;
            case 'Q':
            case 'q':
                userHomePage("");
                break;
            default:
                int book_id = Integer.parseInt(next_step);
                if (user.getBooks().contains(book_id))
                    userBorrowTable(" You have already borrowed book " + book_id + ".\n", start_index);
                if (Book.borrowBook(book_database, user_database, book_id, user))
                    userBorrowTable(" Book " + book_id + " borrowed successfully.\n", prev_printed_last[1] == 1 ? prev_printed_last[0] : start_index);
                else
                    userBorrowTable(" Book " + book_id + " is not available.\n", start_index);
                break;
        }
    }

    private static void userReturnTable(String comment, int start_index) {
        Page.clearPage();

        int[] prev_printed_last = book_database.display(
                new String[] { "serial id", "book", "borrow date", "return date", "days left" },
                new Book().setUserId(user.getId()),
                start_index,
                table_size,
                new String[] {"user_id", "serial_id"},
                "",
                "user_id"
        );

        System.out.print(comment);

        System.out.println("\n" +
                (start_index == prev_printed_last[0] ? "" : ">> PREV " + table_size + " RECORDS (W)\n") +
                (prev_printed_last[2] == book_database.getSize() ? "" : ">> NEXT " + table_size + " RECORDS (S)\n") +
                ">> BACK (Q)" +
                (prev_printed_last[1] == 0 ? "" : "\n\n>> RETURN BOOK BY GIVING ITS ID.\n")
        );

        String available_input = "Qq";
        if (!(start_index == prev_printed_last[0])) available_input += "Ww";
        if (!(prev_printed_last[2] == book_database.getSize())) available_input += "Ss";
        available_input = "[" + available_input + "]";
        if (!(prev_printed_last[1] == 0)) available_input += "|\\d+";
        available_input = "^" + available_input + "$";

        String next_step = Page.getValidatedInput("", available_input);
        switch (next_step.charAt(0)) {
            case 'W':
            case 'w':
                userReturnTable("", prev_printed_last[0]);
                break;
            case 'S':
            case 's':
                userReturnTable("", prev_printed_last[2] == book_database.getSize() ? start_index : prev_printed_last[2]);
                break;
            case 'Q':
            case 'q':
                userHomePage("");
                break;
            default:
                int book_serial_id = Integer.parseInt(next_step);
                if (Book.returnBook(book_database, user_database, book_serial_id, user))
                    userReturnTable(" Book " + book_serial_id + " returned successfully.\n", prev_printed_last[1] == 1 ? prev_printed_last[0] : start_index);
                else
                    userReturnTable(" Book " + book_serial_id + " is not borrowed.\n", start_index);
                break;
        }
    }

    // ADMIN SECTION -----------------------------------

    private static void loginAsAdmin(String comment) {
        Page admin_login_page = new Page(
                "LOGIN AS ADMIN",
                new String[]{},
                comment,
                new String[]{
                        "Password~" + user_password_pattern + "~password",
                        "\n >> LOGIN (1)  /  BACK (2)~^[12]$~login"
                });

        HashMap<String, String> user_inputs = admin_login_page.displayPageAndGetInputs();

        if (user_inputs.get("login").compareTo("2") == 0) {
            startPage(" Logging canceled.\n");
        } else {
            if (admin.verifyPassword(user_inputs.get("password"))) {
                adminHomePage("");
            } else
                loginAsAdmin(" Wrong password!\n\n");
        }
    }

    private static void adminHomePage(String comment) {
        Page admin_home_page = new Page(
                "HI, ADMIN!",
                new String[]{
                        "EDIT PASSWORD~(1)",
                        "BOOK DATABASE~(2)",
                        "USER DATABASE~(3)",
                        "LOG OUT~(4)"
                },
                comment,
                new String[]{"~[1-4]{1}~next_step"});

        HashMap<String, String> user_inputs = admin_home_page.displayPageAndGetInputs();

        char next_step = user_inputs.get("next_step").charAt(0);
        switch (next_step) {
            case '1':
                adminProfilePage(admin.getPassword());
                break;
            case '2':
                adminBookTable("", 0);
                break;
            case '3':
                adminUserTable(0);
                break;
            case '4':
                startPage(" Logged out.\n");
                break;
        }
    }

    private static void adminProfilePage(String password_before_edit) {
        Page admin_profile_page = new Page(
                "PASSWORD",
                new String[]{
                        " ~edit",
                        "Current password: " + (show_passwords ? admin.getPassword() : "*".repeat(admin.getPassword().length())) + "~(1)",
                        " ~ ",
                        "SAVE~(2)",
                        "CANCEL~(3)"
                },
                "",
                new String[]{"~[1-3]{1}~next_step"});

        HashMap<String, String> user_inputs = admin_profile_page.displayPageAndGetInputs();

        char next_step = user_inputs.get("next_step").charAt(0);
        switch (next_step) {
            case '1':
                Page.clearPage();
                admin.changePassword(Page.getValidatedInput("\n New Password (min 4 characters)", user_password_pattern));
                adminProfilePage(password_before_edit);
                break;
            case '2':
                adminHomePage(" Password changed successfully.\n");
                break;
            case '3':
                admin.changePassword(password_before_edit);
                adminHomePage(" Changes canceled.\n");
                break;
        }
    }

    private static void adminBookTable(String comment, int start_index) {
        Page.clearPage();

        int[] prev_printed_last = book_database.display(
                new String[] { "serial id", "id", "title", "author", "year", "loan period", "user", "borrow date" },
                new Book(),
                start_index,
                table_size,
                new String[] {"id"},
                ""
        );

        System.out.print(comment);

        System.out.println("\n" +
                (start_index == prev_printed_last[0] ? "" : ">> PREV " + table_size + " RECORDS (W)\n") +
                (prev_printed_last[2] == book_database.getSize() ? "" : ">> NEXT " + table_size + " RECORDS (S)\n") +
                "\n>> ADD BOOK (1)\n" +
                (prev_printed_last[1] == 0 ? "" : ">> REMOVE BOOK (2)\n\n>> CLEAR DATABASE (3)") +
                "\n>> LOAD FROM FILE (4)\n" +
                "\n>> BACK (Q)\n"
        );

        String available_input = "Qq14";
        if (!(start_index == prev_printed_last[0])) available_input += "Ww";
        if (!(prev_printed_last[2] == book_database.getSize())) available_input += "Ss";
        if (!(prev_printed_last[1] == 0)) available_input += "23";
        available_input = "^[" + available_input + "]$";

        String next_step = Page.getValidatedInput("", available_input);
        switch (next_step.charAt(0)) {
            case 'W':
            case 'w':
                adminBookTable("", prev_printed_last[0]);
                break;
            case 'S':
            case 's':
                adminBookTable("", prev_printed_last[2] == book_database.getSize() ? start_index : prev_printed_last[2]);
                break;
            case 'Q':
            case 'q':
                adminHomePage("");
                break;
            case '1':
                Page.clearPage();
                String title = "\"" + Page.getValidatedInput("\n Title", book_title_pattern) + "\"";
                String author = Page.getValidatedInput(" Author", book_author_pattern);
                int year = Integer.parseInt(Page.getValidatedInput(" Year", book_year_pattern));
                int loan_period = Integer.parseInt(Page.getValidatedInput(" Loan period", book_loan_period_pattern));
                System.out.println();
                String cancel1 = Page.getValidatedInput(">> ADD BOOK (1)  /  CANCEL (2)", "^[12]$");

                if (cancel1.compareTo("2") == 0)
                    adminBookTable(" Adding new book canceled.\n", start_index);

                Book existing = (Book) book_database.getRecord(new Book().setTitle(title).setAuthor(author).setYear(year).setLoanPeriod(loan_period), "title", "author", "year", "loan_period");

                book_database.addRecord(new Book(existing == null ? -1 : existing.getSerialId(), -1 ,title, author, year, loan_period, -1, null));
                adminBookTable(" New book added successfully.\n", start_index);
                break;
            case '2':
                Page.clearPage();
                int book_id = Integer.parseInt(Page.getValidatedInput("\n Book ID", book_id_pattern));
                System.out.println();
                String cancel2 = Page.getValidatedInput(">> REMOVE BOOK (1)  /  CANCEL (2)", "^[12]$");

                if (cancel2.compareTo("2") == 0)
                    adminBookTable(" Removing book canceled.\n", start_index);

                if (((Book) book_database.getRecord(new Book().setId(book_id), "id")).getUserId() != -1)
                    adminBookTable(" Book " + book_id + " is borrowed by one of the users.\n" +
                            ".\n", start_index);
                if (book_database.removeRecord(new Book().setId(book_id)))
                    adminBookTable(" Book removed successfully.\n", prev_printed_last[1] == 1 ? prev_printed_last[0] : start_index);
                adminBookTable(" Book " + book_id + " is not in database.\n", start_index);
                break;
            case '3':
                if (book_database.getRecord(new Book().setUserId(-1), "~user_id") != null)
                    adminBookTable(" You cannot clear whole database, because some books are borrowed.\n", start_index);
                book_database.clear();
                Book.resetSerialId();
                Book.resetId();
                adminBookTable(" Database cleared successfully.\n", 0);
                break;
            case '4':
                Page.clearPage();
                String file_name = Page.getValidatedInput("\n File name (*.txt)", ".+");
                if (!(file_name.endsWith(".txt"))) file_name += ".txt";
                if (book_database.loadFromFile(new Book(), file_name))
                    adminBookTable(" Book database loaded successfully.\n", 0);
                adminBookTable(" Book database loading failed.\n", 0);
                break;
        }
    }

    private static void adminUserTable(int start_index) {
        Page.clearPage();

        int[] prev_printed_last = user_database.display(
                new String[] { "id", "name", "email", "books" },
                new User(),
                start_index,
                table_size,
                new String[] {"id"},
                ""
        );

        System.out.println("\n" +
                (start_index == prev_printed_last[0] ? "" : ">> PREV " + table_size + " RECORDS (W)\n") +
                (prev_printed_last[2] == user_database.getSize() ? "" : ">> NEXT " + table_size + " RECORDS (S)\n") +
                ">> BACK (Q)" +
                (prev_printed_last[1] == 0 ? "" : "\n\n>> GET DETAIL INFO ABOUT USER BY GIVING HIS ID.\n")
        );

        String available_input = "Qq";
        if (!(start_index == prev_printed_last[0])) available_input += "Ww";
        if (!(prev_printed_last[2] == user_database.getSize())) available_input += "Ss";
        available_input = "[" + available_input + "]";
        if (!(prev_printed_last[1] == 0)) available_input += "|\\d+";
        available_input = "^" + available_input + "$";

        String next_step = Page.getValidatedInput("", available_input);
        switch (next_step.charAt(0)) {
            case 'W':
            case 'w':
                adminUserTable(prev_printed_last[0]);
                break;
            case 'S':
            case 's':
                adminUserTable(prev_printed_last[2] == book_database.getSize() ? start_index : prev_printed_last[2]);
                break;
            case 'Q':
            case 'q':
                adminHomePage("");
                break;
            default:
                Page.clearPage();
                System.out.print("\n" + user_database.getRecord(new User().setId(Integer.parseInt(next_step)), "id") + "\n\n Press any key to continue...\n ");
                new Scanner(System.in).nextLine();
                adminUserTable(start_index);
                break;
        }
    }
}
