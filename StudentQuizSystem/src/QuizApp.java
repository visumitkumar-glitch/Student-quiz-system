import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class QuizApp extends JFrame {
    private final Database database;
    private Database.Student student;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final JLabel welcome = new JLabel();
    private List<Question> questions;
    private int current, score;
    private final JLabel questionNumber = new JLabel();
    private final JTextArea prompt = new JTextArea();
    private final JRadioButton[] choices = {new JRadioButton(), new JRadioButton(), new JRadioButton(), new JRadioButton()};
    private final ButtonGroup choiceGroup = new ButtonGroup();

    public QuizApp() throws SQLException {
        super("Student Quiz System"); database = new Database();
        setDefaultCloseOperation(EXIT_ON_CLOSE); setSize(720, 520); setLocationRelativeTo(null); setContentPane(root);
        root.add(authPanel(), "AUTH"); root.add(homePanel(), "HOME"); root.add(quizPanel(), "QUIZ"); cards.show(root, "AUTH");
    }

    private JPanel panel() { JPanel p = new JPanel(); p.setBorder(new EmptyBorder(30, 55, 30, 55)); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); return p; }
    private void addField(JPanel p, String label, JComponent field) { p.add(new JLabel(label)); p.add(field); p.add(Box.createVerticalStrut(10)); }

    private JPanel authPanel() {
        JPanel outer = new JPanel(new GridLayout(1, 2, 25, 0)); outer.setBorder(new EmptyBorder(35, 45, 35, 45));
        JPanel login = panel(); login.add(title("Sign in")); JTextField email = new JTextField(); JPasswordField password = new JPasswordField();
        addField(login, "Email", email); addField(login, "Password", password); JButton in = new JButton("Sign in"); in.addActionListener(e -> signIn(email.getText(), password.getPassword())); login.add(in);
        JPanel register = panel(); register.add(title("New student")); JTextField name = new JTextField(); JTextField regEmail = new JTextField(); JPasswordField regPassword = new JPasswordField();
        addField(register, "Full name", name); addField(register, "Email", regEmail); addField(register, "Password", regPassword); JButton create = new JButton("Create account"); create.addActionListener(e -> register(name.getText(), regEmail.getText(), regPassword.getPassword())); register.add(create);
        outer.add(login); outer.add(register); return outer;
    }

    private JLabel title(String value) { JLabel l = new JLabel(value); l.setFont(l.getFont().deriveFont(Font.BOLD, 24f)); l.setAlignmentX(LEFT_ALIGNMENT); l.setBorder(new EmptyBorder(0,0,20,0)); return l; }

    private JPanel homePanel() {
        JPanel p = panel(); welcome.setFont(welcome.getFont().deriveFont(Font.BOLD, 26f)); p.add(welcome); p.add(Box.createVerticalStrut(18));
        JTextArea info = new JTextArea("Ready for a five-question quiz? Your score is saved automatically after you finish."); info.setLineWrap(true); info.setWrapStyleWord(true); info.setEditable(false); info.setOpaque(false); p.add(info); p.add(Box.createVerticalStrut(20));
        JButton start = new JButton("Start quiz"); start.addActionListener(e -> startQuiz()); p.add(start); p.add(Box.createVerticalStrut(10));
        JButton history = new JButton("View my results"); history.addActionListener(e -> showHistory()); p.add(history); p.add(Box.createVerticalStrut(10));
        JButton logout = new JButton("Log out"); logout.addActionListener(e -> cards.show(root, "AUTH")); p.add(logout); return p;
    }

    private JPanel quizPanel() {
        JPanel p = panel(); questionNumber.setFont(questionNumber.getFont().deriveFont(Font.BOLD, 16f)); p.add(questionNumber); p.add(Box.createVerticalStrut(10));
        prompt.setLineWrap(true); prompt.setWrapStyleWord(true); prompt.setEditable(false); prompt.setOpaque(false); prompt.setFont(prompt.getFont().deriveFont(19f)); p.add(prompt); p.add(Box.createVerticalStrut(18));
        for (int i = 0; i < choices.length; i++) { choiceGroup.add(choices[i]); p.add(choices[i]); p.add(Box.createVerticalStrut(8)); }
        p.add(Box.createVerticalGlue()); JButton next = new JButton("Next question"); next.addActionListener(e -> nextQuestion()); p.add(next); return p;
    }

    private void register(String name, String email, char[] rawPassword) {
        String password = new String(rawPassword); if (name.isBlank() || email.isBlank() || password.isBlank()) { error("Please complete every registration field."); return; }
        try { int id = database.register(name, email, password); student = new Database.Student(id, name.trim(), email.trim()); enterHome(); info("Your account was created. Welcome!"); }
        catch (SQLException ex) { error(ex.getMessage().contains("UNIQUE") ? "That email address is already registered." : "Could not create account: " + ex.getMessage()); }
    }

    private void signIn(String email, char[] rawPassword) {
        try { student = database.login(email, new String(rawPassword)); if (student == null) { error("Email or password is incorrect."); return; } enterHome(); }
        catch (SQLException ex) { error("Could not sign in: " + ex.getMessage()); }
    }

    private void enterHome() { welcome.setText("Welcome, " + student.name() + "!"); cards.show(root, "HOME"); }
    private void startQuiz() { try { questions = database.questions(); current = score = 0; showQuestion(); cards.show(root, "QUIZ"); } catch (SQLException ex) { error("Could not load questions: " + ex.getMessage()); } }
    private void showQuestion() { Question q = questions.get(current); questionNumber.setText("Question " + (current + 1) + " of " + questions.size()); prompt.setText(q.prompt()); choiceGroup.clearSelection(); for (int i = 0; i < 4; i++) choices[i].setText((char)('A' + i) + ". " + q.optionFor(String.valueOf((char)('A' + i)))); }
    private void nextQuestion() {
        int selected = -1; for (int i = 0; i < choices.length; i++) if (choices[i].isSelected()) selected = i;
        if (selected < 0) { error("Choose an answer before continuing."); return; }
        if (questions.get(current).correctOption().equals(String.valueOf((char)('A' + selected)))) score++;
        current++; if (current < questions.size()) showQuestion(); else finishQuiz();
    }
    private void finishQuiz() { try { database.saveAttempt(student.id(), score, questions.size()); info("Quiz complete! Your score is " + score + " out of " + questions.size() + "."); enterHome(); } catch (SQLException ex) { error("Score calculated, but could not save it: " + ex.getMessage()); } }
    private void showHistory() { try { List<String> attempts = database.history(student.id()); JTextArea text = new JTextArea(attempts.isEmpty() ? "No completed quizzes yet." : String.join("\n", attempts)); text.setEditable(false); JOptionPane.showMessageDialog(this, new JScrollPane(text), "My results", JOptionPane.INFORMATION_MESSAGE); } catch (SQLException ex) { error("Could not load results: " + ex.getMessage()); } }
    private void error(String message) { JOptionPane.showMessageDialog(this, message, "Student Quiz System", JOptionPane.ERROR_MESSAGE); }
    private void info(String message) { JOptionPane.showMessageDialog(this, message, "Student Quiz System", JOptionPane.INFORMATION_MESSAGE); }
    public static void main(String[] args) { SwingUtilities.invokeLater(() -> { try { new QuizApp().setVisible(true); } catch (Exception e) { JOptionPane.showMessageDialog(null, "Startup failed: " + e.getMessage()); } }); }
}
