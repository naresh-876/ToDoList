package com.rp.todo.model;

import java.time.LocalDate;
import java.util.Objects;

// POJO for a task row
public class Task {
    public enum Priority { LOW, MEDIUM, HIGH }

    public int id;                 // unique ID in memory
    public String title;           // short description
    public LocalDate dueDate;      // may be null
    public Priority priority;      // LOW/MEDIUM/HIGH
    public boolean done;           // completion flag

    public Task(int id, String title, LocalDate dueDate, Priority priority, boolean done) {
        this.id = id;
        this.title = title;
        this.dueDate = dueDate;
        this.priority = priority;
        this.done = done;
    }

    // Sort: incomplete first, then by due date (nulls last), then HIGH>MEDIUM>LOW, then id
    public static int compare(Task a, Task b) {
        if (a.done != b.done) return a.done ? 1 : -1;
        if (!Objects.equals(a.dueDate, b.dueDate)) {
            if (a.dueDate == null) return 1;
            if (b.dueDate == null) return -1;
            int byDate = a.dueDate.compareTo(b.dueDate);
            if (byDate != 0) return byDate;
        }
        int pA = prioRank(a.priority), pB = prioRank(b.priority);
        if (pA != pB) return Integer.compare(pB, pA); // higher first
        return Integer.compare(a.id, b.id);
    }

    private static int prioRank(Priority p) {
        switch (p) {
            case HIGH: return 3;
            case MEDIUM: return 2;
            default: return 1;
        }
    }
}
