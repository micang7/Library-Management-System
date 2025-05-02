package database;

public interface DatabaseRecord {
    boolean isEqualBy(DatabaseRecord object, String... fields);

    int compareByField(DatabaseRecord object, String field);

    DatabaseRecord createObjectFromFileLine(String file_line, Database... db);

    String convertObjectToFileLine(DatabaseRecord object);

    String[] convertObjectToTableRowCells(DatabaseRecord object, String... table_columns);
}