package book;

import database.*;
import string.Str;
import user.User;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class Book implements DatabaseRecord {
    private static int last_serial_id = -1;
    private static int last_id = 0;

    private int serial_id;
    private int id;

    private String title;
    private String author;
    private int year;
    private int loan_period;

    private int user_id;
    private LocalDate borrow_date;

    final private static HashMap<String, Integer> field_max_lengths = new HashMap<>() {
        {
            put("title", 5);
            put("author", 6);
            put("loan_period", 11);
            put("serial_id", 0);    //9
            put("id", 0);           //2
            put("user_id", 0);      //7
        }
    };

    public Book() {
    }

    public Book(int serial_id, int id, String title, String author, int year, int loan_period, int user_id, LocalDate borrow_date) {
        if (serial_id == -1) this.serial_id = ++last_serial_id;
        else {
            this.serial_id = serial_id;
            last_serial_id = serial_id;
        }
        this.id = id == -1 ? last_id : id;
        last_id++;
        this.title = title;
        this.author = author;
        this.year = year;
        this.loan_period = loan_period;
        this.user_id = user_id;
        this.borrow_date = borrow_date == null ? LocalDate.now() : borrow_date;

        if (String.valueOf(this.serial_id).length() > field_max_lengths.get("serial_id")) {
            field_max_lengths.put("serial_id", String.valueOf(this.serial_id).length());
        }
        if (this.title.length() > field_max_lengths.get("title")) {
            field_max_lengths.put("title", this.title.length());
        }
        if (initialName(this).length() > field_max_lengths.get("author")) {
            field_max_lengths.put("author", initialName(this).length());
        }
        if (String.valueOf(this.loan_period).length() > field_max_lengths.get("loan_period")) {
            field_max_lengths.put("loan_period", String.valueOf(this.loan_period).length());
        }
        if (String.valueOf(this.user_id).length() > field_max_lengths.get("user_id")) {
            field_max_lengths.put("user_id", String.valueOf(this.user_id).length());
        }
        if (String.valueOf(this.id).length() > field_max_lengths.get("id")) {
            field_max_lengths.put("id", String.valueOf(this.id).length());
        }
    }

    // getters
    public int getSerialId() {
        return this.serial_id;
    }

    public int getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthor() {
        return this.author;
    }

    public int getYear() {
        return this.year;
    }

    public int getLoanPeriod() {
        return this.loan_period;
    }

    public int getUserId() {
        return this.user_id;
    }

    public LocalDate getBorrowDate() {
        return this.borrow_date;
    }

    // setters
    public Book setSerialId(int serial_id) {
        this.serial_id = serial_id;
        return this;
    }

    public Book setId(int id) {
        this.id = id;
        return this;
    }

    public Book setTitle(String title) {
        this.title = title;
        return this;
    }

    public Book setAuthor(String author) {
        this.author = author;
        return this;
    }

    public Book setYear(int year) {
        this.year = year;
        return this;
    }

    public Book setLoanPeriod(int loan_period) {
        this.loan_period = loan_period;
        return this;
    }

    public Book setUserId(int user_id) {
        this.user_id = user_id;
        return this;
    }

    public void setBorrowDate(LocalDate borrow_date) {
        this.borrow_date = borrow_date;
    }

    public static void resetSerialId() {
        last_serial_id = 0;
    }

    public static void resetId() {
        last_id = 0;
    }

    // methods
    public boolean isEqualBy(DatabaseRecord object, String... fields) {
        if (fields.length == 1 && fields[0].compareTo("id") == 0)
            return this.id == ((Book) object).getId();
        if (fields.length == 1 && fields[0].compareTo("serial_id") == 0)
            return this.serial_id == ((Book) object).getSerialId();
        if (fields.length == 1 && fields[0].compareTo("user_id") == 0)
            return this.user_id == ((Book) object).user_id;
        if (fields.length == 1 && fields[0].compareTo("~user_id") == 0)
            return !(this.user_id == ((Book) object).user_id);
        if (fields.length == 2 && String.join(" ", fields).compareTo("serial_id user_id") == 0)
            return this.serial_id == ((Book) object).getSerialId() && this.user_id == ((Book) object).user_id;
        if (fields.length == 4 && String.join(" ", fields).compareTo("title author year loan_period") == 0) {
            String comparedName;
            if (((Book) object).getAuthor().contains("."))
                comparedName = initialName(this);
            else
                comparedName = this.author;
            return this.title.compareTo(((Book) object).getTitle()) == 0 &&
                    comparedName.compareTo(((Book) object).getAuthor()) == 0 &&
                    this.year == ((Book) object).getYear() &&
                    this.loan_period == ((Book) object).getLoanPeriod();
        }
        return true;
    }

    public int compareByField(DatabaseRecord object, String field) {
        if (field.compareTo("id") == 0) {
            return this.id - ((Book) object).getId();
        } else if (field.compareTo("serial_id") == 0) {
            return this.serial_id - ((Book) object).getSerialId();
        } else if (field.compareTo("title") == 0) {
            return Str.compare(this.title, ((Book) object).getTitle());
        } else if (field.compareTo("author") == 0) {
            return Str.compare(this.author, ((Book) object).getAuthor());
        } else if (field.compareTo("year") == 0) {
            return this.year - ((Book) object).getYear();
        } else if (field.compareTo("loan_period") == 0) {
            return this.loan_period - ((Book) object).getLoanPeriod();
        } else if (field.compareTo("user_id") == 0) {
            return this.user_id - ((Book) object).getUserId();
        } else if (field.compareTo("borrow_date") == 0) {
            return this.borrow_date.compareTo(((Book) object).getBorrowDate());
        } else {
            return 0;
        }
    }

    public DatabaseRecord createObjectFromFileLine(String file_line, Database... db) {
        String[] splited = file_line.split(";");
        if (splited.length == 8)
            return new Book(
                Integer.parseInt(splited[0]),
                Integer.parseInt(splited[1]),
                splited[2],
                splited[3],
                Integer.parseInt(splited[4]),
                Integer.parseInt(splited[5]),
                Integer.parseInt(splited[6]),
                LocalDate.parse(splited[7]));

        Book existing = db.length == 0 ? null : (Book) db[0].getRecord(new Book().setTitle(splited[0]).setAuthor(splited[1]).setYear(Integer.parseInt(splited[2])).setLoanPeriod(Integer.parseInt(splited[3])), "title", "author", "year", "loan_period");

        return new Book(
                existing == null ? -1 : existing.getSerialId(),
                -1,
                splited[0],
                splited[1],
                Integer.parseInt(splited[2]),
                Integer.parseInt(splited[3]),
                -1,
                null);
    }

    public String convertObjectToFileLine(DatabaseRecord object) {
        Book book = (Book) object;
        return book.serial_id + ";" +
                book.id + ";" +
                book.title + ";" +
                book.author + ";" +
                book.year + ";" +
                book.loan_period + ";" +
                book.user_id + ";" +
                book.borrow_date.toString() + "\n";
    }

    private String initialName(Book book) {
        String[] names = book.getAuthor().split(" ");
        String initials = "";
        for (int i = 0; i < names.length - 1; i++)
            initials += names[i].charAt(0) + ".";
        return initials + names[names.length - 1];
    }

    public String[] convertObjectToTableRowCells(DatabaseRecord object, String... table_columns) {
        Book book = (Book) object;

        // when user borrows a book
        if (table_columns.length == 4)
            return  new String[] {
                    Str.adjustToWidth(Str.adjustToWidth(String.valueOf(book.getSerialId()), field_max_lengths.get("serial_id"), '0', false), 9, ' ', false),
                    Str.adjustToWidth(book.getTitle(), field_max_lengths.get("title"), ' ', true) + " " +
                    Str.adjustToWidth(initialName(book), field_max_lengths.get("author"), ' ', true) + " " +
                    book.getYear(),
                    Str.adjustToWidth(Integer.toString(book.getLoanPeriod()), field_max_lengths.get("loan_period"), ' ', false)};

        // when user returns a book
        if (table_columns.length == 5)
            return  new String[] {
                    Str.adjustToWidth(Str.adjustToWidth(String.valueOf(book.getSerialId()), field_max_lengths.get("serial_id"), '0', false), 9, ' ', false),
                    Str.adjustToWidth(book.getTitle(), field_max_lengths.get("title"), ' ', true) + " " +
                    Str.adjustToWidth(initialName(book), field_max_lengths.get("author"), ' ', true) + " " +
                    book.getYear(),
                    " " + book.getBorrowDate(),
                    " " + book.getBorrowDate().plusDays(book.getLoanPeriod()),
                    Str.adjustToWidth(String.valueOf(ChronoUnit.DAYS.between(LocalDate.now(), book.getBorrowDate().plusDays(book.getLoanPeriod()))), 9, ' ', false)};

        // when admin checks book db
        if (table_columns.length == 8)
            return  new String[] {
                    Str.adjustToWidth(Str.adjustToWidth(String.valueOf(book.getSerialId()), field_max_lengths.get("serial_id"), '0', false), 9, ' ', false),
                    Str.adjustToWidth(Str.adjustToWidth(String.valueOf(book.getId()), field_max_lengths.get("id"), '0', false), 2, ' ', false),
                    Str.adjustToWidth(book.getTitle(), field_max_lengths.get("title"), ' ', true),
                    Str.adjustToWidth(initialName(book), field_max_lengths.get("author"), ' ', true),
                    String.valueOf(book.getYear()),
                    Str.adjustToWidth(Integer.toString(book.getLoanPeriod()), field_max_lengths.get("loan_period"), ' ', false),
                    Str.adjustToWidth(Str.adjustToWidth(Integer.toString(book.getUserId()), field_max_lengths.get("user_id"), '0', false), 7, ' ', false),
                    " " + book.getBorrowDate().toString()};

        return new String[] {};
    }

    public static boolean borrowBook(Database book_database, Database user_database, int book_serial_id, User user) {
        Book found = (Book) book_database.getRecord(new Book().setSerialId(book_serial_id), "serial_id");

        if (found == null) return false;
        if (found.getUserId() != -1) return false;

        found.setUserId(user.getId());
        found.setBorrowDate(LocalDate.now());
        user.addBorrowedBook(book_serial_id);
        return book_database.updateRecord(found) && user_database.updateRecord(user);
    }

    public static boolean returnBook(Database book_database, Database user_database, int book_serial_id, User user) {
        Book found = (Book) book_database.getRecord(new Book().setSerialId(book_serial_id).setUserId(user.getId()), "serial_id", "user_id");
        if (found == null) return false;
        found.setUserId(-1);
        user.removeBorrowedBook(book_serial_id);
        return book_database.updateRecord(found) && user_database.updateRecord(user);
    }

    public String toString() {
        return " Serial ID:   " + serial_id +
                "\n ID:          " + id +
                "\n Title:       " + "\"" + title + "\"" +
                "\n Author:      " + author +
                "\n Year:        " + year +
                "\n Loan period: " + loan_period +
                "\n User ID:     " + user_id +
                "\n Borrow date: " + borrow_date + "\n";
    }
}