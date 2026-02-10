import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.Timer;

public class Sudoku extends JFrame {
    // Game state
    private int[][] solution = new int[9][9];
    private int[][] playerGrid = new int[9][9];
    private boolean[][] isInitial = new boolean[9][9];
    private JTextField[][] cells = new JTextField[9][9];

    // UI Components
    private JLabel timeLabel, errorLabel;
    private int secondsPlayed = 0;
    private int errorCount = 0;
    private Timer gameTimer;

    // Colors & Fonts
    private final Color BG_COLOR = new Color(240, 242, 245);
    private final Color ACCENT_COLOR = new Color(79, 70, 229); // Indigo
    private final Color INITIAL_TEXT = new Color(31, 41, 55);
    private final Color INPUT_TEXT = new Color(37, 99, 235);
    private final Font MAIN_FONT = new Font("SansSerif", Font.BOLD, 22);

    public Sudoku() {
        setTitle("Sudoku Master");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(550, 700);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BG_COLOR);

        setupTopPanel();
        setupBoard();
        setupBottomPanel();

        startNewGame();
        setLocationRelativeTo(null);
    }

    private void setupTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        getContentPane().setBackground(Color.ORANGE);
        topPanel.setOpaque(false);

        JButton newGameBtn = createStyledButton("New Game", new Color(34, 197, 94));
        newGameBtn.addActionListener(e -> startNewGame());

        JButton hintBtn = createStyledButton("Get Hint", ACCENT_COLOR);
        hintBtn.addActionListener(e -> giveHint());

        topPanel.add(newGameBtn);
        topPanel.add(hintBtn);
        add(topPanel, BorderLayout.NORTH);
    }

    private void setupBoard() {
        JPanel boardWrapper = new JPanel(new GridBagLayout());
        boardWrapper.setOpaque(false);

        JPanel board = new JPanel(new GridLayout(9, 9));
        board.setBackground(Color.WHITE);
        board.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));

        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                JTextField tf = new JTextField();
                tf.setHorizontalAlignment(JTextField.CENTER);
                tf.setFont(MAIN_FONT);
                tf.setPreferredSize(new Dimension(50, 50));

                // Styling the borders for 3x3 effect
                int top = (r % 3 == 0) ? 2 : 1;
                int left = (c % 3 == 0) ? 2 : 1;
                int bottom = (r == 8) ? 2 : 1;
                int right = (c == 8) ? 2 : 1;
                tf.setBorder(new MatteBorder(top, left, bottom, right, Color.GRAY));

                final int row = r;
                final int col = c;

                tf.addKeyListener(new KeyAdapter() {
                    public void keyReleased(KeyEvent e) {
                        handleInput(row, col);
                    }
                });

                cells[r][c] = tf;
                board.add(tf);
            }
        }
        boardWrapper.add(board);
        add(boardWrapper, BorderLayout.CENTER);
    }

    private void setupBottomPanel() {
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.setPreferredSize(new Dimension(550, 60));
        bottomPanel.setBackground(Color.WHITE);

        timeLabel = new JLabel("⏱ Time: 00:00", SwingConstants.CENTER);
        errorLabel = new JLabel("❌ Mistakes: 0", SwingConstants.CENTER);

        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        errorLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        bottomPanel.add(timeLabel);
        bottomPanel.add(errorLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void handleInput(int r, int c) {
        if (isInitial[r][c]) return;

        String text = cells[r][c].getText().trim();
        if (text.isEmpty()) {
            playerGrid[r][c] = 0;
            return;
        }

        if (text.length() > 1 || !Character.isDigit(text.charAt(0)) || text.equals("0")) {
            cells[r][c].setText("");
            return;
        }

        int val = Integer.parseInt(text);
        if (isValid(playerGrid, r, c, val)) {
            playerGrid[r][c] = val;
            cells[r][c].setForeground(INPUT_TEXT);
            checkWin();
        } else {
            errorCount++;
            errorLabel.setText("❌ Mistakes: " + errorCount);
            cells[r][c].setText("");
            playerGrid[r][c] = 0;
            // Brief red flash
            cells[r][c].setBackground(new Color(254, 226, 226));
            Timer t = new Timer(300, e -> cells[r][c].setBackground(Color.WHITE));
            t.setRepeats(false);
            t.start();
        }
    }

    private void startNewGame() {
        errorCount = 0;
        secondsPlayed = 0;
        errorLabel.setText("❌ Mistakes: 0");

        fillGrid(solution);
        Random rand = new Random();

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j].setBackground(Color.WHITE);
                if (rand.nextInt(100) < 35) {
                    playerGrid[i][j] = solution[i][j];
                    isInitial[i][j] = true;
                    cells[i][j].setText(String.valueOf(solution[i][j]));
                    cells[i][j].setEditable(false);
                    cells[i][j].setForeground(INITIAL_TEXT);
                    cells[i][j].setBackground(new Color(243, 244, 246));
                } else {
                    playerGrid[i][j] = 0;
                    isInitial[i][j] = false;
                    cells[i][j].setText("");
                    cells[i][j].setEditable(true);
                    cells[i][j].setForeground(INPUT_TEXT);
                }
            }
        }

        if (gameTimer != null) gameTimer.stop();
        gameTimer = new Timer(1000, e -> {
            secondsPlayed++;
            timeLabel.setText(String.format("⏱ Time: %02d:%02d", secondsPlayed/60, secondsPlayed%60));
        });
        gameTimer.start();
    }

    private void giveHint() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (playerGrid[i][j] == 0) {
                    playerGrid[i][j] = solution[i][j];
                    cells[i][j].setText(String.valueOf(solution[i][j]));
                    cells[i][j].setForeground(new Color(5, 150, 105));
                    checkWin();
                    return;
                }
            }
        }
    }

    // Logic Helper: Check if number placement is valid
    private boolean isValid(int[][] grid, int r, int c, int num) {
        for (int i = 0; i < 9; i++) {
            if (grid[r][i] == num || grid[i][c] == num) return false;
        }
        int br = (r/3)*3, bc = (c/3)*3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (grid[br+i][bc+j] == num) return false;
            }
        }
        return true;
    }

    // Logic Helper: Generate Sudoku Grid
    private boolean fillGrid(int[][] grid) {
        // Clear grid
        for(int[] row : grid) Arrays.fill(row, 0);
        return solve(grid);
    }

    private boolean solve(int[][] grid) {
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 9; c++) {
                if (grid[r][c] == 0) {
                    Integer[] ns = {1,2,3,4,5,6,7,8,9};
                    Collections.shuffle(Arrays.asList(ns));
                    for (int n : ns) {
                        if (isValid(grid, r, c, n)) {
                            grid[r][c] = n;
                            if (solve(grid)) return true;
                            grid[r][c] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    private void checkWin() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (playerGrid[i][j] == 0) return;
            }
        }
        gameTimer.stop();
        JOptionPane.showMessageDialog(this, "🎉 You Solved it!\nMistakes: " + errorCount);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.RED);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Sudoku().setVisible(true));
    }
}