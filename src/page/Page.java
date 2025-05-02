package page;

import java.util.Scanner;
import java.util.HashMap;

public class Page {
    private static final Scanner scanner = new Scanner(System.in);

    private final int width;
    private final String title;
    private final String[] options;
    private final String comment;
    private final String[] inputs;

    public Page(String title, String[] options, String comment, String[] inputs, int... width) {
        this.title = title;
        this.options = options;
        this.comment = comment;
        this.inputs = inputs;
        this.width = width.length == 0 ? 44 : width[0];
    }

    public static void clearPage() {
        System.out.println("\n".repeat(100));
    }

    public static String getValidatedInput(String title, String pattern) {
        String user_input;
        int i = 0;
        do {
            if (i == 1)
                System.out.println(" Invalid input...");
            System.out.print(" " + title + ": ");
            user_input = scanner.nextLine();
            i++;
        } while (!(user_input.matches(pattern)));
        return user_input;
    }

    public HashMap<String, String> displayPageAndGetInputs() {
        Page.clearPage();

        System.out.println("." + "-".repeat(width) + ".");

        int title_space = (width - title.length()) / 2;
        System.out.println(
                "|" + " ".repeat(title_space) + title + " ".repeat(title_space + (title.length() % 2 == 1 ? 1 : 0)) + "|");

        if (options.length > 0) {
            System.out.println("|" + "=".repeat(width) + "|");
            System.out.println("|" + " ".repeat(width) + "|");

            for (String option : options) {
                String option_title = option.split("~")[0];
                String option_key = option.split("~")[1];
                int option_space = width - option_title.length() - option_key.length() - 9;
                System.out.println("|  " + (option_title.isBlank() ? "  " : ">>") + " " + option_title + " ".repeat(option_space)
                        + option_key + "    |");
            }

            System.out.println("|" + " ".repeat(width) + "|");
        }

        System.out.println("'" + "-".repeat(width) + "'");

        System.out.print(comment);

        HashMap<String, String> user_inputs = new HashMap<>();

        for (String input : inputs) {
            String input_title = input.split("~")[0];
            String input_pattern = input.split("~")[1];
            String input_key = input.split("~")[2];
            user_inputs.put(input_key, getValidatedInput(input_title, input_pattern));
        }
        return user_inputs;
    }
}