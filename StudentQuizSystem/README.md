# Student Quiz System

A Java Swing desktop application backed by a local SQLite database. Students can register, log in, take a five-question quiz, and view their result history.

## Run it

1. Install a JDK (Java 17 or newer).
2. Double-click `run.bat`, or run it from Command Prompt.
3. Register a new student account, log in, and select **Start quiz**.

The app creates `data/student_quiz.db` automatically the first time it runs. The database contains `students`, `questions`, and `attempts` tables, and it is pre-populated with five sample questions. Passwords are stored as SHA-256 hashes for this educational example; use a salted adaptive password hash (such as BCrypt or Argon2) before deploying a real system.

> The included launcher uses the SQLite JDBC driver already installed in this Codex environment. On another computer, download the Xerial SQLite JDBC JAR (for example `sqlite-jdbc-3.47.1.0.jar`) and set the `SQLITE_JDBC_JAR` environment variable to its full path before launching.

## Switch to MySQL (optional)

1. Install MySQL Server and MySQL Workbench.
2. In Workbench, create a database: `CREATE DATABASE student_quiz;`
3. Download MySQL Connector/J and place its JAR in a `lib` folder beside the project.
4. Replace the driver and URL in `src/Database.java`:
   - Driver: `com.mysql.cj.jdbc.Driver`
   - URL: `jdbc:mysql://localhost:3306/student_quiz?useSSL=false&serverTimezone=UTC`
   - Add your MySQL username and password to `DriverManager.getConnection(...)`.
5. Change SQLite-only SQL in `initialise()` (`AUTOINCREMENT`, `INSERT OR IGNORE`) to MySQL equivalents (`AUTO_INCREMENT`, `INSERT IGNORE`).
6. Run the app with the MySQL connector JAR on the classpath instead of the SQLite JAR.

For a shared classroom deployment, use MySQL or PostgreSQL on a secured server and do not store database passwords in source code.
