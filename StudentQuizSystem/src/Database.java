import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public final class Database {
    private static final String URL = "jdbc:sqlite:data/student_quiz.db";

    public Database() throws SQLException {
        new File("data").mkdirs();
        initialise();
    }

    private Connection connect() throws SQLException { return DriverManager.getConnection(URL); }

    private void initialise() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS students (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, password TEXT NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS questions (id INTEGER PRIMARY KEY AUTOINCREMENT, prompt TEXT NOT NULL, a TEXT NOT NULL, b TEXT NOT NULL, c TEXT NOT NULL, d TEXT NOT NULL, correct TEXT NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS attempts (id INTEGER PRIMARY KEY AUTOINCREMENT, student_id INTEGER NOT NULL, score INTEGER NOT NULL, total INTEGER NOT NULL, taken_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP, FOREIGN KEY(student_id) REFERENCES students(id))");
            try (PreparedStatement insert = c.prepareStatement("INSERT OR IGNORE INTO questions (id,prompt,a,b,c,d,correct) VALUES (?,?,?,?,?,?,?)")) {
                String[][] seed = {
                    {"1", "Which language is primarily used for Android app development?", "Java", "HTML", "SQL", "CSS", "A"},
                    {"2", "What does CPU stand for?", "Central Program Unit", "Computer Processing Utility", "Central Processing Unit", "Core Power Unit", "C"},
                    {"3", "Which SQL statement reads data from a table?", "SELECT", "INSERT", "UPDATE", "DELETE", "A"},
                    {"4", "What is the result of 7 × 8?", "54", "56", "64", "48", "B"},
                    {"5", "Which is a Java keyword?", "function", "class", "define", "print", "B"}
                };
                for (String[] q : seed) { for (int i = 0; i < q.length; i++) insert.setString(i + 1, q[i]); insert.addBatch(); }
                insert.executeBatch();
            }
        }
    }

    public int register(String name, String email, String password) throws SQLException {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("INSERT INTO students(name,email,password) VALUES(?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, name.trim()); p.setString(2, email.trim().toLowerCase()); p.setString(3, hash(password));
            p.executeUpdate(); try (ResultSet keys = p.getGeneratedKeys()) { keys.next(); return keys.getInt(1); }
        }
    }

    public Student login(String email, String password) throws SQLException {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT id,name,email FROM students WHERE email=? AND password=?")) {
            p.setString(1, email.trim().toLowerCase()); p.setString(2, hash(password));
            try (ResultSet r = p.executeQuery()) { return r.next() ? new Student(r.getInt("id"), r.getString("name"), r.getString("email")) : null; }
        }
    }

    public List<Question> questions() throws SQLException {
        List<Question> result = new ArrayList<>();
        try (Connection c = connect(); Statement p = c.createStatement(); ResultSet r = p.executeQuery("SELECT * FROM questions ORDER BY id")) {
            while (r.next()) result.add(new Question(r.getInt("id"), r.getString("prompt"), r.getString("a"), r.getString("b"), r.getString("c"), r.getString("d"), r.getString("correct")));
        }
        return result;
    }

    public void saveAttempt(int studentId, int score, int total) throws SQLException {
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("INSERT INTO attempts(student_id,score,total) VALUES(?,?,?)")) {
            p.setInt(1, studentId); p.setInt(2, score); p.setInt(3, total); p.executeUpdate();
        }
    }

    public List<String> history(int studentId) throws SQLException {
        List<String> rows = new ArrayList<>();
        try (Connection c = connect(); PreparedStatement p = c.prepareStatement("SELECT score,total,taken_at FROM attempts WHERE student_id=? ORDER BY id DESC")) {
            p.setInt(1, studentId); try (ResultSet r = p.executeQuery()) { while (r.next()) rows.add(r.getString("taken_at") + "  —  " + r.getInt("score") + "/" + r.getInt("total")); }
        }
        return rows;
    }

    private static String hash(String password) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder value = new StringBuilder();
            for (byte b : bytes) value.append(String.format("%02x", b));
            return value.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    public record Student(int id, String name, String email) {}
}
