package user;

import database.*;
import string.Str;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class User extends Admin implements DatabaseRecord {
    private static int last_id = 0;

    private String[] names;
    private String last_name;
    private LocalDate birth_date;
    private long pesel;
    private String email;

    private ArrayList<Integer> borrowed_books = new ArrayList<>();
    private int id;

    private static final HashMap<String, Integer> field_max_lengths = new HashMap<>() {
        {
            put("name", 4);
            put("email", 5);
            put("password", 8);
            put("books", 0);
            put("id", 0);
        }
    };

    public User() {
    }

    public User(String[] names, String last_name, LocalDate birth_date, long pesel, String email, String password,
                ArrayList<Integer> borrowed_books, int... id) {
        this.names = names;
        this.last_name = last_name;
        this.birth_date = birth_date;
        this.pesel = pesel;
        this.email = email;
        this.password = password;
        this.borrowed_books = borrowed_books;
        this.id = id.length == 0 ? last_id : id[0];
        last_id++;

        if (initialsName(this).length() > field_max_lengths.get("name")) {
            field_max_lengths.put("name", initialsName(this).length());
        }
        if (this.email.length() > field_max_lengths.get("email")) {
            field_max_lengths.put("email", this.email.length());
        }
        if (this.password.length() > field_max_lengths.get("password")) {
            field_max_lengths.put("password", this.password.length());
        }
        if (String.valueOf(this.borrowed_books.size()).length() > field_max_lengths.get("books")) {
            field_max_lengths.put("books", String.valueOf(this.borrowed_books.size()).length());
        }
        if (String.valueOf(this.id).length() > field_max_lengths.get("id")) {
            field_max_lengths.put("id", String.valueOf(this.id).length());
        }
    }

    public static User copy(User user) {
        return new User(
                user.getNames(),
                user.getLastName(),
                user.getBirthDate(),
                user.getPesel(),
                user.getEmail(),
                user.getPassword(),
                user.getBooks(),
                user.getId()
        );
    }

    // getters
    public String[] getNames() {
        return this.names;
    }

    public String getLastName() {
        return this.last_name;
    }

    public LocalDate getBirthDate() {
        return this.birth_date;
    }

    public long getPesel() {
        return this.pesel;
    }

    public String getEmail() {
        return this.email;
    }

    public ArrayList<Integer> getBooks() {
        return this.borrowed_books;
    }

    public int getId() {
        return this.id;
    }

    // setters
    public void setNames(String[] names) {
        this.names = names;
    }

    public void setLastName(String last_name) {
        this.last_name = last_name;
    }

    public void setBirthDate(LocalDate birth_date) {
        this.birth_date = birth_date;
    }

    public User setPesel(long pesel) {
        this.pesel = pesel;
        return this;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public void addBorrowedBook(int book_id) {
        this.borrowed_books.add(book_id);
    }

    public void removeBorrowedBook(int book_id) {
        this.borrowed_books.remove(Integer.valueOf(book_id));
    }

    public User setId(int id) {
        this.id = id;
        return this;
    }

    // methods
    public boolean isEqualBy(DatabaseRecord object, String... fields) {
        if (fields.length == 1 && fields[0].compareTo("id") == 0)
            return this.id == ((User) object).getId();
        if (fields.length == 1 && fields[0].compareTo("email") == 0)
            return this.email.compareTo(((User) object).getEmail()) == 0;
        if (fields.length == 1 && fields[0].compareTo("pesel") == 0)
            return this.pesel == ((User) object).getPesel();
        if (fields.length == 2 && fields[0].compareTo("email") == 0 && fields[1].compareTo("password") == 0)
            return this.email.compareTo(((User) object).getEmail()) == 0 && this.password.compareTo(((User) object).getPassword()) == 0;
        return true;
    }

    public int compareByField(DatabaseRecord object, String field) {
        if (field.compareTo("id") == 0)
            return this.id - ((User) object).getId();
        if (field.compareTo("name") == 0)
            return Str.compare(String.join(" ", this.names), String.join(" ", ((User) object).getNames()));
        if (field.compareTo("last_name") == 0)
            return Str.compare(this.last_name, ((User) object).getLastName());
        if (field.compareTo("birth_date") == 0)
            return this.birth_date.compareTo(((User) object).getBirthDate());
        if (field.compareTo("pesel") == 0)
            return Long.compare(this.pesel, ((User) object).getPesel());
        if (field.compareTo("email") == 0)
            return Str.compare(this.email, ((User) object).getEmail());
        if (field.compareTo("password") == 0)
            return Str.compare(this.password, ((User) object).getPassword());
        if (field.compareTo("books") == 0)
            return this.borrowed_books.size() - ((User) object).getBooks().size();
        return 0;
    }

    public DatabaseRecord createObjectFromFileLine(String file_line, Database... db) {
        String[] splited = file_line.split(";");
        return new User(
                splited[0].split(":"),
                splited[1],
                LocalDate.parse(splited[2]),
                Long.parseLong(splited[3]),
                splited[4],
                splited[5],
                new ArrayList<Integer>(splited[6].compareTo("") == 0 ? List.of() : Arrays.stream(splited[6].split(":")).map(Integer::parseInt).collect(Collectors.toList())),
                Integer.parseInt(splited[7])
        );
    }

    public String convertObjectToFileLine(DatabaseRecord object) {
        User user = (User) object;
        return String.join(":", user.getNames()) + ";" +
                user.getLastName() + ";" +
                user.getBirthDate() + ";" +
                user.getPesel() + ";" +
                user.getEmail() + ";" +
                user.getPassword() + ";" +
                user.getBooks().stream().map(String::valueOf).collect(Collectors.joining(":")) + ";" +
                user.getId() + "\n";
    }

    private String initialsName(User user) {
        String initials = "";
        for (String name : user.getNames())
            initials += name.charAt(0) + ".";
        return initials + user.getLastName();
    }

    public String[] convertObjectToTableRowCells(DatabaseRecord object, String... table_columns) {
        User user = (User) object;
        if (table_columns.length == 4)
            return new String[] {
                    Str.adjustToWidth(Str.adjustToWidth(String.valueOf(user.getId()), field_max_lengths.get("id"), '0', false), 2, ' ', false),
                    Str.adjustToWidth(initialsName(user), field_max_lengths.get("name"), ' ', true),
                    Str.adjustToWidth(user.getEmail(), field_max_lengths.get("email"), ' ', true),
                    Str.adjustToWidth(Str.adjustToWidth(String.valueOf(user.getBooks().size()), field_max_lengths.get("books"), '0', false), 5, ' ', false)};
        if (table_columns.length == 8)
            return new String[] {
                Str.adjustToWidth(Str.adjustToWidth(String.valueOf(user.getId()), field_max_lengths.get("id"), '0', false), 2, ' ', false),
                Str.adjustToWidth(initialsName(user), field_max_lengths.get("name"), ' ', true),
                user.getBirthDate().toString(),
                String.valueOf(user.getPesel()),
                Str.adjustToWidth(user.getEmail(), field_max_lengths.get("email"), ' ', true),
                Str.adjustToWidth(user.getPassword(), field_max_lengths.get("password"), ' ', true),
                Str.adjustToWidth(Str.adjustToWidth(String.valueOf(user.getBooks().size()), field_max_lengths.get("books"), '0', false), 5, ' ', false)};

        return new String[] {};
    }

    public static User verifyPassword(Database db, String email, String password) {
        DatabaseRecord found = db.getRecord(new User().setEmail(email).setPassword(password), "email", "password");
        return (User) found;
    }

    public String toString() {
        return " ID:         " + id +
                "\n Names:      " + String.join(" ", names) +
                "\n Last Name:  " + last_name +
                "\n Birth Date: " + birth_date +
                "\n PESEL:      " + pesel +
                "\n Email:      " + email +
                "\n Books:      " + (borrowed_books.isEmpty() ? "-" : borrowed_books.stream().map(String::valueOf).collect(Collectors.joining(" "))) +
                "\n Password:   " + "*".repeat(password.length());
    }
}
