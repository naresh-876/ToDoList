package com.rp.todo.io;

import com.rp.todo.model.Task;
import com.rp.todo.model.Task.Priority;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// CSV storage: "id","title","dueDate","priority","done"
public class TaskStore {
    private final File file;

    public TaskStore(String fileName) {
        this.file = new File(fileName);
    }

    public void save(List<Task> tasks) throws IOException {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("\"id\",\"title\",\"dueDate\",\"priority\",\"done\"");
            for (Task t : tasks) {
                out.println(csv(Integer.toString(t.id)) + "," +
                            csv(t.title) + "," +
                            csv(t.dueDate == null ? "" : t.dueDate.toString()) + "," +
                            csv(t.priority.name()) + "," +
                            csv(Boolean.toString(t.done)));
            }
        }
    }

    public List<Task> load() throws IOException {
        List<Task> list = new ArrayList<>();
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean header = true;
            while ((line = br.readLine()) != null) {
                if (header) { header = false; continue; }
                line = line.trim();
                if (line.isEmpty()) continue;
                List<String> c = parse(line);
                if (c.size() != 5) continue;
                try {
                    int id = Integer.parseInt(c.get(0));
                    String title = c.get(1);
                    String dueStr = c.get(2);
                    LocalDate due = dueStr.isEmpty() ? null : LocalDate.parse(dueStr);
                    Priority pr = Priority.valueOf(c.get(3));
                    boolean done = Boolean.parseBoolean(c.get(4));
                    list.add(new Task(id, title, due, pr, done));
                } catch (Exception ignored) { }
            }
        }
        return list;
    }

    private String csv(String s) {
        if (s == null) s = "";
        return "\"" + s.replace("\"","\"\"") + "\"";
    }

    // very small CSV parser supporting quoted fields and commas
    private List<String> parse(String line) {
        ArrayList<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQ) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"'); i++;
                    } else inQ = false;
                } else cur.append(ch);
            } else {
                if (ch == '"') inQ = true;
                else if (ch == ',') { out.add(cur.toString()); cur.setLength(0); }
                else cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }
}
