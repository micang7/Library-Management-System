package database;

import string.Str;

import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;

public class Database {
    private final ArrayList<DatabaseRecord> records = new ArrayList<>();
    private final String file;

    public Database(String file) {
        this.file = file;
    }

    public int getSize() {
        return records.size();
    }

    // first occurrence
    public DatabaseRecord getRecord(DatabaseRecord object, String... fields) {
        for (DatabaseRecord record : records) {
            if (record.isEqualBy(object, fields))
                return record;
        }
        return null;
    }

    public void addRecord(DatabaseRecord record) {
        records.add(record);
    }

    public boolean removeRecord(DatabaseRecord record) {
        DatabaseRecord found = getRecord(record, "id");
        if (found != null) {
            records.remove(found);
            return true;
        }
        return false;
    }

    public boolean updateRecord(DatabaseRecord object) {
        DatabaseRecord found = getRecord(object, "id");
        if (found != null) {
            records.remove(found);
            records.add(object);
            return true;
        }
        return false;
    }

    public boolean loadFromFile(DatabaseRecord obj_template, String... file_name) {
        try {
            File file = new File(file_name.length == 1 ? file_name[0] : this.file);
            Scanner myReader = new Scanner(file);
            while (myReader.hasNextLine()) {
                records.add(obj_template.createObjectFromFileLine(myReader.nextLine(), this));
            }
            myReader.close();
            return true;
        } catch (FileNotFoundException _) {
            return false;
        }
    }

    public boolean saveToFile(DatabaseRecord obj_template, String... file_name) {
        try {
            FileWriter myWriter = new FileWriter(file_name.length == 1 ? file_name[0] : this.file);
            for (DatabaseRecord record : records)
                myWriter.write(obj_template.convertObjectToFileLine(record));
            myWriter.close();
            return true;
        } catch (IOException _) {
            return false;
        }
    }

    public void clear() {
        records.clear();
    }

    public void sort(String[] sort_by_fields) {
        records.sort((record1, record2) -> {
            int i = 0, result;
            do {
                result = record1.compareByField(record2, sort_by_fields[i]);
            } while (result == 0 && ++i < sort_by_fields.length);
            return result;
        });
    }

    public int[] display(String[] column_names, DatabaseRecord obj_template, int start_index, int table_size, String[] sort_by_fields, String grouping_field, String... filters) {
        String title_row = "";
        String[] h_line_parts = new String[column_names.length];
        if (!records.isEmpty()) {
            String[] first_row = obj_template.convertObjectToTableRowCells(records.getFirst(), column_names);
            int i = 0;
            while (i < first_row.length) {
                title_row += "| " + Str.adjustToWidth(column_names[i].toUpperCase(), first_row[i].length(), ' ', true) + " ";
                h_line_parts[i] = "-".repeat(first_row[i].length() + 2);
                i++;
            }
            if (!grouping_field.isEmpty()) {
                title_row += "| " + column_names[i].toUpperCase() + " ";
                h_line_parts[i] = "-".repeat(column_names[i].length() + 2);
            }
        } else {
            for (int i = 0; i < column_names.length; i++) {
                title_row += "| " + column_names[i].toUpperCase() + " ";
                h_line_parts[i] = "-".repeat(column_names[i].length() + 2);
            }
        }

        System.out.println("." + String.join(".", h_line_parts) + ".");
        System.out.println(title_row + "|");

        sort(sort_by_fields);

        int index = 0, printed = 0;
        int frame_start = 0, frame_counter = 0, prev = start_index;
        while (printed < table_size && index < records.size()) {
            if (records.get(index).isEqualBy(obj_template, filters)) {
                frame_counter++;
                if (frame_counter == 1)
                    frame_start = index;
                if (frame_counter == table_size) {
                    if (printed == 0)
                        prev = frame_start;
                    frame_counter = 0;
                }
                DatabaseRecord first = records.get(index);
                int copies = 1;
                if (!grouping_field.isEmpty()) {
                    while (index+1 < records.size() && records.get(index + 1).isEqualBy(first, grouping_field)) {
                        index++;
                        copies++;
                    }
                }
                if (index >= start_index) {
                    System.out.println("|" + String.join("+", h_line_parts) + "|");
                    System.out.println("| " + String.join(" | ", obj_template.convertObjectToTableRowCells(first, column_names)) + " |"
                            + (grouping_field.isEmpty() ? "" : " " + Str.adjustToWidth(String.valueOf(copies), column_names[column_names.length-1].length(), ' ', false) + " |"));
                    printed++;
                }
            }
            index++;
        }
        System.out.println("'" + String.join("'", h_line_parts) + "'");
        if (printed == 0) System.out.println(" No records found.\n```````````````````\n");
        return new int[] {prev, printed, index};
    }
}