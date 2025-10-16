package com.rp.todo.ui;

import com.rp.todo.io.TaskStore;
import com.rp.todo.model.Task;
import com.rp.todo.model.Task.Priority;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.io.IOException;

public class ConsoleApp {
    private final Scanner sc = new Scanner(System.in);
    private final TaskStore store = new TaskStore("tasks.csv");
    private final List<Task> tasks = new ArrayList<>();
    private int nextId = 1;

    public ConsoleApp() {
        try {
            tasks.addAll(store.load());
            nextId = calcNextId(tasks);
        } catch (IOException e) {
            System.out.println("Note: starting fresh (no CSV yet).");
        }
    }

    public void run() {
        while (true) {
            System.out.println("\n=== To-Do List ===");
            System.out.println("1) Add task");
            System.out.println("2) List all");
            System.out.println("3) Filter by year/month");
            System.out.println("4) Mark done by ID");
            System.out.println("5) Edit by ID");
            System.out.println("6) Delete by ID");
            System.out.println("7) Save");
            System.out.println("8) Exit");
            System.out.print("Choose: ");
            String ch = sc.nextLine().trim();

            switch (ch) {
                case "1": add(); break;
                case "2": list(tasks); break;
                case "3": filterByMonth(); break;
                case "4": markDone(); break;
                case "5": edit(); break;
                case "6": deleteById(); break;
                case "7": save(); break;
                case "8": save(); System.out.println("Bye!"); return;
                default: System.out.println("Invalid choice.");
            }
        }
    }

    private void add() {
        System.out.print("Title: ");
        String title = sc.nextLine().trim();
        if (title.isEmpty()) { System.out.println("Title required."); return; }

        LocalDate due = readDateOptional("Due date (YYYY-MM-DD, blank for none): ");

        Priority pr = readPriority("Priority (LOW/MEDIUM/HIGH): ");

        tasks.add(new Task(nextId++, title, due, pr, false));
        System.out.println("Added.");
    }

    private void list(List<Task> list) {
        if (list.isEmpty()) { System.out.println("(No tasks)"); return; }
        list.sort(Task::compare);

        System.out.printf("%-4s %-1s %-35s %-12s %-8s%n", "ID", "✓", "Title", "Due", "Priority");
        System.out.println("-----------------------------------------------------------------------");

        for (Task t : list) {
            String tick = t.done ? "✔" : " ";
            String due = (t.dueDate == null) ? "-" : t.dueDate.toString();
            System.out.printf("%-4d %-1s %-35s %-12s %-8s%n", t.id, tick, truncate(t.title, 33), due, t.priority);
        }
    }

    private void filterByMonth() {
        try {
            System.out.print("Year (e.g., 2025): ");
            int y = Integer.parseInt(sc.nextLine().trim());
            System.out.print("Month (1-12): ");
            int m = Integer.parseInt(sc.nextLine().trim());
            if (m < 1 || m > 12) { System.out.println("Month must be 1..12"); return; }

            List<Task> filtered = new ArrayList<>();
            for (Task t : tasks) {
                if (t.dueDate != null && t.dueDate.getYear() == y && t.dueDate.getMonthValue() == m) {
                    filtered.add(t);
                }
            }
            list(filtered);
        } catch (NumberFormatException e) {
            System.out.println("Enter valid numbers for year/month.");
        }
    }

    private void markDone() {
        Task t = findByIdPrompt();
        if (t == null) return;
        t.done = true;
        System.out.println("Marked as done.");
    }

    private void edit() {
        Task t = findByIdPrompt();
        if (t == null) return;

        System.out.print("New title (leave blank to keep): ");
        String title = sc.nextLine().trim();
        if (!title.isEmpty()) t.title = title;

        LocalDate newDue = readDateOptional("New due date (YYYY-MM-DD, blank to clear/keep): ");
        t.dueDate = newDue;

        Priority pr = readPriorityOptional("New priority (LOW/MEDIUM/HIGH, blank to keep): ");
        if (pr != null) t.priority = pr;

        System.out.println("Updated.");
    }

    private void deleteById() {
        Task t = findByIdPrompt();
        if (t == null) return;
        tasks.remove(t);
        System.out.println("Deleted.");
    }

    private void save() {
        try {
            store.save(tasks);
            System.out.println("Saved to tasks.csv");
        } catch (IOException e) {
            System.out.println("Save failed: " + e.getMessage());
        }
    }

    // ---- helpers ----

    private Task findByIdPrompt() {
        System.out.print("Enter ID: ");
        String s = sc.nextLine().trim();
        try {
            int id = Integer.parseInt(s);
            for (Task t : tasks) if (t.id == id) return t;
            System.out.println("ID not found.");
            return null;
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return null;
        }
    }

    private LocalDate readDateOptional(String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return null;
        try { return LocalDate.parse(s); }
        catch (DateTimeParseException e) { System.out.println("Invalid date. Leaving empty."); return null; }
    }

    private Priority readPriority(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().toUpperCase();
            try { return Priority.valueOf(s); }
            catch (Exception e) { System.out.println("Use LOW, MEDIUM, or HIGH."); }
        }
    }

    private Priority readPriorityOptional(String prompt) {
        System.out.print(prompt);
        String s = sc.nextLine().trim();
        if (s.isEmpty()) return null;
        try { return Priority.valueOf(s.toUpperCase()); }
        catch (Exception e) { System.out.println("Invalid priority. Keeping old."); return null; }
    }

    private int calcNextId(List<Task> list) {
        int max = 0;
        for (Task t : list) if (t.id > max) max = t.id;
        return max + 1;
    }

    private String truncate(String s, int n) {
        if (s == null) return "";
        return s.length() <= n ? s : s.substring(0, n - 3) + "...";
    }
}
