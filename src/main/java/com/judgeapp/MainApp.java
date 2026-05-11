package com.judgeapp;

import com.judgeapp.ai.GeminiAPI;
import com.judgeapp.db.*;
import com.judgeapp.judge.Judge;
import com.judgeapp.ocr.OCRManager;
import com.google.gson.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class MainApp extends JFrame {

    // Theme
    static final Color BG = new Color(246, 247, 251);
    static final Color PANEL = Color.WHITE;
    static final Color CARD = new Color(250, 251, 253);
    static final Color BORDER = new Color(218, 225, 234);
    static final Color ACCENT = new Color(37, 99, 235);
    static final Color GREEN = new Color(13, 148, 136);
    static final Color RED = new Color(190, 18, 60);
    static final Color YELLOW = new Color(180, 83, 9);
    static final Color TEXT = new Color(17, 24, 39);
    static final Color MUTED = new Color(100, 116, 139);
    static final Font MONO = new Font("Consolas", Font.PLAIN, 14);
    static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 15);
    static final Font BODY_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    static final Font PAGE_FONT = new Font("Segoe UI", Font.BOLD, 22);

    private JTable problemTable;
    private DefaultTableModel problemModel;

    public MainApp() {
        setTitle("Judge App - Online Judge System");
        setSize(1240, 780);
        setMinimumSize(new Dimension(1120, 720));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(BG);

        JTabbedPane tabs = new JTabbedPane();
        styleTabs(tabs);
        tabs.addTab("  Danh sách đề  ", buildProblemListPanel());
        tabs.addTab("  Thêm đề  ", buildAddProblemPanel());
        tabs.addTab("  độ mạnh test case  ", buildStressPanel());

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(buildAppHeader(), BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
        setLocationRelativeTo(null);
        setVisible(true);
        refreshProblemTable();
    }

    // ============================================================
    // HELPERS UI
    // ============================================================
    private static class SurfacePanel extends JPanel {
        private final Color fill;
        private final Color stroke;
        private final int radius;

        SurfacePanel(LayoutManager layout, Color fill, Color stroke, int radius) {
            super(layout);
            this.fill = fill;
            this.stroke = stroke;
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(stroke);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;

        RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(1, 1, 1, 1);
        }
    }

    private Color mix(Color a, Color b, double ratio) {
        double keep = 1.0 - ratio;
        return new Color(
            (int) Math.round(a.getRed() * keep + b.getRed() * ratio),
            (int) Math.round(a.getGreen() * keep + b.getGreen() * ratio),
            (int) Math.round(a.getBlue() * keep + b.getBlue() * ratio)
        );
    }

    private JPanel buildAppHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setBackground(PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
            BorderFactory.createEmptyBorder(10, 22, 10, 22)
        ));

        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 2));
        titles.setOpaque(false);
        JLabel name = new JLabel("Judge App");
        name.setForeground(TEXT);
        name.setFont(new Font("Segoe UI", Font.BOLD, 22));
        JLabel subtitle = new JLabel("Online Judge System");
        subtitle.setForeground(MUTED);
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titles.add(name);
        titles.add(subtitle);

        JLabel badge = new JLabel("AI + DB", SwingConstants.CENTER);
        badge.setForeground(ACCENT);
        badge.setFont(new Font("Segoe UI", Font.BOLD, 13));
        badge.setPreferredSize(new Dimension(92, 34));
        badge.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(new Color(191, 219, 254), 8),
            BorderFactory.createEmptyBorder(7, 14, 7, 14)
        ));
        JPanel badgeWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        badgeWrap.setOpaque(false);
        badgeWrap.add(badge);

        header.add(titles, BorderLayout.WEST);
        header.add(badgeWrap, BorderLayout.EAST);
        return header;
    }

    private JPanel pageHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout(0, 3));
        header.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(TEXT);
        titleLabel.setFont(PAGE_FONT);
        header.add(titleLabel, BorderLayout.NORTH);
        if (subtitle != null && !subtitle.isBlank()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setForeground(MUTED);
            subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            header.add(subtitleLabel, BorderLayout.SOUTH);
        }
        return header;
    }

    private JPanel fieldBlock(String label, Component field) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(makeLabel(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private JButton makeBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setUI(new BasicButtonUI());
        btn.setBackground(bg);
        double luma = (0.299 * bg.getRed() + 0.587 * bg.getGreen() + 0.114 * bg.getBlue()) / 255.0;
        btn.setForeground(luma > 0.62 ? new Color(15, 23, 42) : Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(mix(bg, Color.BLACK, 0.18), 8),
            BorderFactory.createEmptyBorder(9, 16, 9, 16)
        ));
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(mix(bg, Color.BLACK, 0.07));
            }
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
            public void mousePressed(MouseEvent e) {
                btn.setBackground(mix(bg, Color.BLACK, 0.14));
            }
            public void mouseReleased(MouseEvent e) {
                btn.setBackground(btn.contains(e.getPoint()) ? mix(bg, Color.BLACK, 0.07) : bg);
            }
        });
        return btn;
    }

    private JTextArea makeCodeArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(MONO);
        ta.setBackground(new Color(15, 23, 42));
        ta.setForeground(new Color(226, 232, 240));
        ta.setCaretColor(Color.WHITE);
        ta.setSelectionColor(new Color(37, 99, 235));
        ta.setSelectedTextColor(Color.WHITE);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 11, 10, 11));
        return ta;
    }

    private JTextArea makeTextArea() {
        JTextArea ta = new JTextArea();
        ta.setFont(BODY_FONT);
        ta.setBackground(Color.WHITE);
        ta.setForeground(TEXT);
        ta.setCaretColor(TEXT);
        ta.setSelectionColor(new Color(219, 234, 254));
        ta.setSelectedTextColor(TEXT);
        ta.setBorder(BorderFactory.createEmptyBorder(10, 11, 10, 11));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private JTextField makeField(String placeholder) {
        JTextField tf = new JTextField();
        tf.setFont(BODY_FONT);
        tf.setBackground(Color.WHITE);
        tf.setForeground(MUTED);
        tf.setCaretColor(TEXT);
        tf.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER, 8),
            BorderFactory.createEmptyBorder(7, 11, 7, 11)
        ));
        // Placeholder behavior
        tf.setText(placeholder);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXT);
                }
            }
            public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isEmpty()) {
                    tf.setText(placeholder);
                    tf.setForeground(MUTED);
                }
            }
        });
        return tf;
    }

    private JLabel makeLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(TEXT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return lbl;
    }

    private JScrollPane darkScroll(Component c) {
        JScrollPane sp = new JScrollPane(c);
        sp.getViewport().setBackground(c.getBackground());
        sp.setBorder(new RoundedBorder(BORDER, 8));
        sp.getVerticalScrollBar().setUnitIncrement(16);
        return sp;
    }

    private JPanel card(String title, Component content) {
        JPanel p = new SurfacePanel(new BorderLayout(0, 10), PANEL, BORDER, 8);
        p.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        if (title != null) {
            JLabel lbl = new JLabel(title);
            lbl.setForeground(TEXT);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
            p.add(lbl, BorderLayout.NORTH);
        }
        p.add(content, BorderLayout.CENTER);
        return p;
    }

    private void styleTabs(JTabbedPane tabs) {
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(TITLE_FONT);
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 14, 10, 14));
        tabs.setOpaque(true);
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabInsets = new Insets(7, 14, 7, 14);
                selectedTabPadInsets = new Insets(0, 0, 0, 0);
                contentBorderInsets = new Insets(12, 0, 0, 0);
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected ? new Color(219, 234, 254) : PANEL);
                g2.fillRoundRect(x + 3, y + 3, w - 6, h - 6, 8, 8);
                if (!isSelected) {
                    g2.setColor(BORDER);
                    g2.drawRoundRect(x + 3, y + 3, w - 6, h - 6, 8, 8);
                }
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                    int x, int y, int w, int h, boolean isSelected) {
            }

            @Override
            protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                    int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            }
        });
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(Color.WHITE);
        cb.setForeground(TEXT);
        cb.setFont(BODY_FONT);
        cb.setBorder(new RoundedBorder(BORDER, 8));
    }

    private void styleTable(JTable table) {
        table.setBackground(PANEL);
        table.setForeground(TEXT);
        table.setFont(BODY_FONT);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(232, 237, 245));
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionBackground(new Color(37, 99, 235));
        table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setBackground(new Color(241, 245, 249));
        table.getTableHeader().setForeground(TEXT);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setReorderingAllowed(false);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? PANEL : CARD);
                    c.setForeground(TEXT);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        });
    }

    // ============================================================
    // TAB 1: DANH SÁCH ĐỀ
    // ============================================================
    private JPanel buildProblemListPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));

        panel.add(pageHeader("Danh sách đề thi", "Các đề bài đã lưu trong hệ thống"), BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Tên đề bài", "Time Limit", "Memory Limit"};
        problemModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        problemTable = new JTable(problemModel);
        styleTable(problemTable);
        problemTable.getColumnModel().getColumn(0).setMaxWidth(60);
        problemTable.getColumnModel().getColumn(2).setMaxWidth(120);
        problemTable.getColumnModel().getColumn(3).setMaxWidth(130);

        panel.add(card("Kho đề bài", darkScroll(problemTable)), BorderLayout.CENTER);

        // Double-click để xem chi tiết
        problemTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = problemTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        int problemId = Integer.parseInt(problemModel.getValueAt(row, 0).toString());
                        showProblemDetailsDialog(problemId);
                    }
                }
            }
        });

        JButton btnRefresh = makeBtn("Làm mới", ACCENT);
        JButton btnViewDetails = makeBtn("Xem chi tiết", new Color(99, 102, 241));
        JButton btnManageTc = makeBtn("Quản lý testcase", new Color(14, 165, 233));
        btnRefresh.addActionListener(e -> refreshProblemTable());
        btnViewDetails.addActionListener(e -> {
            int row = problemTable.getSelectedRow();
            if (row < 0) {
                showError("Chọn một đề trong danh sách trước!");
                return;
            }
            int problemId = Integer.parseInt(problemModel.getValueAt(row, 0).toString());
            showProblemDetailsDialog(problemId);
        });
        btnManageTc.addActionListener(e -> {
            int row = problemTable.getSelectedRow();
            if (row < 0) {
                showError("Chọn một đề trong danh sách trước!");
                return;
            }
            int problemId = Integer.parseInt(problemModel.getValueAt(row, 0).toString());
            String title = problemModel.getValueAt(row, 1).toString();
            showTestcaseManagerDialog(problemId, title);
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        south.setBackground(BG);
        south.add(btnViewDetails);
        south.add(btnManageTc);
        south.add(btnRefresh);
        panel.add(south, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshProblemTable() {
        try {
            problemModel.setRowCount(0);
            for (String[] row : ProblemDAO.getAllProblems())
                problemModel.addRow(row);
        } catch (Exception e) {
            showError("Lỗi load đề: " + e.getMessage());
        }
    }

    // ============================================================
    // TAB 2: THÊM ĐỀ MỚI
    // ============================================================
    private JPanel buildAddProblemPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));

        panel.add(pageHeader("Thêm đề bài mới", "Nhập nội dung, ảnh đề, và testcase (tùy chọn)"), BorderLayout.NORTH);

        JTextField tfTitle = makeField("Nhập tên đề...");
        JTextArea taContent = makeTextArea();
        taContent.setRows(8);
        JTextField tfTime = makeField("1.0");
        JTextField tfMem = makeField("256");
        JLabel lblStatus = new JLabel("  ");
        lblStatus.setFont(BODY_FONT);
        lblStatus.setForeground(MUTED);
        List<String[]> pendingTestcases = new ArrayList<>();

        JLabel lblImagePath = new JLabel("Chưa chọn ảnh");
        lblImagePath.setForeground(MUTED);
        lblImagePath.setFont(BODY_FONT);
        lblImagePath.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER, 8),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        lblImagePath.setOpaque(true);
        lblImagePath.setBackground(Color.WHITE);
        JButton btnPickImg = makeBtn("Chọn ảnh đề", new Color(59, 130, 246));
        JButton btnOCR = makeBtn("AI đọc ảnh", new Color(139, 92, 246));
        JPanel imgRow = new JPanel(new BorderLayout(6, 0));
        imgRow.setBackground(BG);
        imgRow.add(lblImagePath, BorderLayout.CENTER);
        JPanel imgBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        imgBtns.setBackground(BG);
        imgBtns.add(btnPickImg); imgBtns.add(btnOCR);
        imgRow.add(imgBtns, BorderLayout.EAST);

        JPanel limits = new JPanel(new GridLayout(1, 2, 12, 0));
        limits.setOpaque(false);
        tfTime.setPreferredSize(new Dimension(90, 36));
        tfMem.setPreferredSize(new Dimension(90, 36));
        JPanel timeBlock = new JPanel(new BorderLayout(0, 6));
        timeBlock.setOpaque(false);
        timeBlock.add(makeLabel("Time limit (s)"), BorderLayout.NORTH);
        timeBlock.add(tfTime, BorderLayout.CENTER);
        JPanel memBlock = new JPanel(new BorderLayout(0, 6));
        memBlock.setOpaque(false);
        memBlock.add(makeLabel("Memory (MB)"), BorderLayout.NORTH);
        memBlock.add(tfMem, BorderLayout.CENTER);
        limits.add(timeBlock);
        limits.add(memBlock);

        DefaultTableModel tcModel = new DefaultTableModel(new String[]{"Input", "Expected Output", "Type"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tcTable = new JTable(tcModel);
        styleTable(tcTable);
        tcTable.getColumnModel().getColumn(2).setMaxWidth(80);

        JTextArea taTcInput = makeCodeArea();
        taTcInput.setRows(4);
        taTcInput.setText("Nhập input testcase...");
        JTextArea taTcOutput = makeCodeArea();
        taTcOutput.setRows(4);
        taTcOutput.setText("Nhập expected output...");
        JCheckBox cbSample = new JCheckBox("Sample");
        cbSample.setBackground(BG);
        cbSample.setForeground(TEXT);
        JButton btnAddTc = makeBtn("Thêm testcase", new Color(16, 185, 129));
        JButton btnRemoveTc = makeBtn("Xóa testcase đã chọn", new Color(220, 38, 38));
        JButton btnClearTc = makeBtn("Xóa danh sách testcase", new Color(71, 85, 105));
        JButton btnAIGenTc = makeBtn("AI sinh testcase", new Color(168, 85, 247));

        JPanel problemForm = new JPanel(new GridBagLayout());
        problemForm.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.weightx = 1;
        g.insets = new Insets(0, 0, 8, 0);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.anchor = GridBagConstraints.NORTHWEST;

        g.gridy = 0;
        problemForm.add(makeLabel("Tên đề"), g);
        g.gridy = 1;
        problemForm.add(tfTitle, g);

        g.gridy = 2;
        g.insets = new Insets(10, 0, 8, 0);
        problemForm.add(makeLabel("Nội dung đề"), g);
        JScrollPane contentScroll = darkScroll(taContent);
        contentScroll.setPreferredSize(new Dimension(420, 260));
        contentScroll.setMinimumSize(new Dimension(320, 170));
        g.gridy = 3;
        g.weighty = 1;
        g.fill = GridBagConstraints.BOTH;
        g.insets = new Insets(0, 0, 12, 0);
        problemForm.add(contentScroll, g);

        g.gridy = 4;
        g.weighty = 0;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0, 0, 8, 0);
        problemForm.add(makeLabel("Ảnh đề (tùy chọn)"), g);
        g.gridy = 5;
        problemForm.add(imgRow, g);

        g.gridy = 6;
        g.insets = new Insets(12, 0, 0, 0);
        problemForm.add(limits, g);

        JPanel tcInputPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        tcInputPanel.setBackground(BG);
        tcInputPanel.setPreferredSize(new Dimension(560, 180));
        tcInputPanel.add(card("Input", darkScroll(taTcInput)));
        tcInputPanel.add(card("Expected Output", darkScroll(taTcOutput)));

        JPanel tcBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        tcBtnPanel.setBackground(BG);
        tcBtnPanel.add(cbSample);
        tcBtnPanel.add(btnAddTc);
        tcBtnPanel.add(btnRemoveTc);
        tcBtnPanel.add(btnAIGenTc);
        tcBtnPanel.add(btnClearTc);

        JPanel tcPanel = new JPanel(new BorderLayout(8, 8));
        tcPanel.setOpaque(false);
        JPanel tcTop = new JPanel(new BorderLayout(8, 8));
        tcTop.setOpaque(false);
        tcTop.add(tcInputPanel, BorderLayout.CENTER);
        tcTop.add(tcBtnPanel, BorderLayout.SOUTH);
        tcPanel.add(tcTop, BorderLayout.NORTH);
        JScrollPane tcScroll = darkScroll(tcTable);
        tcScroll.setPreferredSize(new Dimension(560, 170));
        tcPanel.add(card("Danh sách testcase (tùy chọn - có thể thêm sau)", tcScroll), BorderLayout.CENTER);

        JSplitPane editorSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            card("Thông tin đề bài", problemForm),
            card("Bộ testcase", tcPanel));
        editorSplit.setResizeWeight(0.42);
        editorSplit.setDividerLocation(0.42);
        editorSplit.setDividerSize(8);
        editorSplit.setBorder(null);
        editorSplit.setBackground(BG);
        panel.add(editorSplit, BorderLayout.CENTER);

        // Buttons bottom
        JButton btnAIGen = makeBtn("AI sinh Generator", new Color(139, 92, 246));
        JButton btnAIChecker = makeBtn("AI sinh Checker", new Color(234, 88, 12));
        JButton btnAICode = makeBtn("AI sinh code mẫu AC", new Color(16, 185, 129));
        JButton btnSave = makeBtn("Lưu đề", ACCENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        btnRow.setBackground(BG);
        btnRow.add(btnAIGen); btnRow.add(btnAIChecker);
        btnRow.add(btnAICode); btnRow.add(btnSave);

        JPanel south = new JPanel(new BorderLayout());
        south.setBackground(BG);
        south.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
        south.add(btnRow, BorderLayout.NORTH);
        south.add(lblStatus, BorderLayout.SOUTH);
        panel.add(south, BorderLayout.SOUTH);

        // === Image pick ===
        final String[] imagePath = {null};
        btnPickImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Image files", "jpg", "jpeg", "png", "bmp"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imagePath[0] = fc.getSelectedFile().getAbsolutePath();
                lblImagePath.setText(fc.getSelectedFile().getName());
                lblImagePath.setForeground(TEXT);
            }
        });

        // === AI đọc ảnh (OCR) ===
        btnOCR.addActionListener(e -> {
            if (imagePath[0] == null) { showError("Chọn ảnh trước!"); return; }
            lblStatus.setForeground(YELLOW);
            lblStatus.setText("⏳ Tesseract OCR đang đọc ảnh...");
            new Thread(() -> {
                try {
                    String text = OCRManager.readImageText(imagePath[0]);
                    SwingUtilities.invokeLater(() -> {
                        // Parse thông tin từ text OCR
                        String[] info = parseProblemInfoFromText(text);
                        String title = info[0];
                        String content = info[1];
                        String timeLimit = info[2];
                        String memLimit = info[3];
                        
                        // Tự điền vào form
                        if (!title.isEmpty()) tfTitle.setText(title);
                        if (!content.isEmpty()) taContent.setText(content);
                        if (!timeLimit.isEmpty()) tfTime.setText(timeLimit);
                        if (!memLimit.isEmpty()) tfMem.setText(memLimit);
                        
                        lblStatus.setForeground(GREEN);
                        lblStatus.setText("✅ OCR đọc & parse xong! Tên: " + title);
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(RED);
                        lblStatus.setText("❌ " + ex.getMessage());
                    });
                }
            }).start();
        });

        // === AI sinh Generator ===
        btnAIGen.addActionListener(e -> {
            String content = taContent.getText().trim();
            if (content.isEmpty()) { showError("Nhập nội dung đề trước!"); return; }
            lblStatus.setForeground(YELLOW);
            lblStatus.setText("⏳ AI đang viết code Generator...");
            new Thread(() -> {
                try {
                    String code = GeminiAPI.generateGeneratorCode(content);
                    SwingUtilities.invokeLater(() -> {
                        showCodeDialog("Code Generator (Java)", code);
                        lblStatus.setForeground(GREEN);
                        lblStatus.setText("✅ AI sinh Generator xong!");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(RED);
                        lblStatus.setText("❌ " + ex.getMessage());
                    });
                }
            }).start();
        });

        // === AI sinh Checker ===
        btnAIChecker.addActionListener(e -> {
            String content = taContent.getText().trim();
            if (content.isEmpty()) { showError("Nhập nội dung đề trước!"); return; }
            lblStatus.setForeground(YELLOW);
            lblStatus.setText("⏳ AI đang viết Checker...");
            new Thread(() -> {
                try {
                    String code = GeminiAPI.generateCheckerCode(content);
                    SwingUtilities.invokeLater(() -> {
                        showCodeDialog("Code Checker (Java)", code);
                        lblStatus.setForeground(GREEN);
                        lblStatus.setText("✅ AI sinh Checker xong!");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(RED);
                        lblStatus.setText("❌ " + ex.getMessage());
                    });
                }
            }).start();
        });

        // === AI sinh code mẫu ===
        btnAICode.addActionListener(e -> {
            String content = taContent.getText().trim();
            if (content.isEmpty()) { showError("Nhập nội dung đề trước!"); return; }
            String lang = (String) JOptionPane.showInputDialog(this,
                "Chọn ngôn ngữ:", "AI sinh code mẫu",
                JOptionPane.QUESTION_MESSAGE, null,
                new String[]{"Java", "C++", "Python"}, "Java");
            if (lang == null) return;
            lblStatus.setForeground(YELLOW);
            lblStatus.setText("⏳ AI đang viết code mẫu " + lang + "...");
            new Thread(() -> {
                try {
                    String code = GeminiAPI.generateSampleCode(content, lang);
                    SwingUtilities.invokeLater(() -> {
                        showCodeDialog("Code mẫu AC (" + lang + ")", code);
                        lblStatus.setForeground(GREEN);
                        lblStatus.setText("✅ AI sinh code mẫu xong!");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(RED);
                        lblStatus.setText("❌ " + ex.getMessage());
                    });
                }
            }).start();
        });

        // === Lưu đề ===
        btnSave.addActionListener(e -> {
            try {
                String title = tfTitle.getText().trim();
                String content = taContent.getText().trim();
                if (title.equals("Nhập tên đề...")) title = "";
                String timeText = tfTime.getText().trim();
                String memText = tfMem.getText().trim();
                if (timeText.equals("1.0")) timeText = "1.0";
                if (memText.equals("256")) memText = "256";
                double tl = Double.parseDouble(timeText);
                int ml = Integer.parseInt(memText);
                if (title.isEmpty()) { showError("Nhập tên đề!"); return; }
                if (content.isEmpty()) { showError("Nhập nội dung đề!"); return; }
                
                // Lưu đề (không bắt buộc testcase)
                int id = ProblemDAO.addProblem(title, content, tl, ml);
                
                // Lưu testcase nếu có
                if (!pendingTestcases.isEmpty()) {
                    for (String[] tc : pendingTestcases) {
                        TestcaseDAO.addTestcase(id, tc[0], tc[1], "Sample".equals(tc[2]));
                    }
                    lblStatus.setForeground(GREEN);
                    lblStatus.setText("✅ Lưu thành công! Problem ID = " + id + " | " + pendingTestcases.size() + " testcase");
                } else {
                    lblStatus.setForeground(GREEN);
                    lblStatus.setText("✅ Lưu đề thành công! Problem ID = " + id + " (Chưa có testcase - có thể thêm sau)");
                }
                
                // Reset form
                tfTitle.setText(""); taContent.setText(""); taTcInput.setText("Nhập input testcase..."); taTcOutput.setText("Nhập expected output...");
                imagePath[0] = null;
                lblImagePath.setText("Chưa chọn ảnh");
                lblImagePath.setForeground(MUTED);
                pendingTestcases.clear();
                tcModel.setRowCount(0);
                refreshProblemTable();
            } catch (Exception ex) {
                lblStatus.setForeground(RED);
                lblStatus.setText("❌ Lỗi: " + ex.getMessage());
            }
        });

        btnAddTc.addActionListener(e -> {
            String in = taTcInput.getText().trim();
            String out = taTcOutput.getText().trim();
            if (in.equals("Nhập input testcase...")) in = "";
            if (out.equals("Nhập expected output...")) out = "";
            if (in.isEmpty() || out.isEmpty()) {
                showError("Input/Expected output testcase không được để trống!");
                return;
            }
            String type = cbSample.isSelected() ? "Sample" : "Hidden";
            pendingTestcases.add(new String[]{in, out, type});
            tcModel.addRow(new String[]{in, out, type});
            taTcInput.setText("Nhập input testcase...");
            taTcOutput.setText("Nhập expected output...");
            cbSample.setSelected(false);
            lblStatus.setForeground(GREEN);
            lblStatus.setText("✅ Đã thêm testcase. Tổng hiện tại: " + pendingTestcases.size());
        });

        btnRemoveTc.addActionListener(e -> {
            int row = tcTable.getSelectedRow();
            if (row < 0) {
                showError("Chọn một testcase trong bảng để xóa!");
                return;
            }
            pendingTestcases.remove(row);
            tcModel.removeRow(row);
            lblStatus.setForeground(YELLOW);
            lblStatus.setText("🗑 Đã xóa testcase. Còn lại: " + pendingTestcases.size());
        });

        btnClearTc.addActionListener(e -> {
            pendingTestcases.clear();
            tcModel.setRowCount(0);
            lblStatus.setForeground(YELLOW);
            lblStatus.setText("🧹 Đã xóa toàn bộ testcase tạm.");
        });

        btnAIGenTc.addActionListener(e -> {
            String content = taContent.getText().trim();
            if (content.isEmpty()) { showError("Nhập nội dung đề trước!"); return; }
            String countText = JOptionPane.showInputDialog(this, "Số testcase muốn AI sinh (1-20):", "8");
            if (countText == null) return;
            int count;
            try {
                count = Integer.parseInt(countText.trim());
            } catch (Exception ex) {
                showError("Số lượng không hợp lệ!");
                return;
            }
            if (count < 1 || count > 20) { showError("Số lượng phải từ 1 đến 20"); return; }

            lblStatus.setForeground(YELLOW);
            lblStatus.setText("⏳ AI đang sinh testcase...");
            int finalCount = count;
            new Thread(() -> {
                try {
                    String prompt = """
                        Dựa trên đề bài sau:
                        %s
                        
                        Hãy sinh %d testcase chất lượng cao cho bài lập trình này.
                        Yêu cầu:
                        - Trả về JSON array thuần, không markdown, không giải thích.
                        - Mỗi phần tử có dạng:
                          {"input":"...", "expectedOutput":"...", "isSample":false}
                        - input/expectedOutput đúng format đề bài.
                        - Bao phủ cả edge cases.
                        """.formatted(content, finalCount);
                    String raw = GeminiAPI.ask(prompt).trim();
                    JsonArray arr = JsonParser.parseString(raw).getAsJsonArray();
                    SwingUtilities.invokeLater(() -> {
                        int added = 0;
                        for (JsonElement el : arr) {
                            JsonObject obj = el.getAsJsonObject();
                            String in = obj.get("input").getAsString();
                            String out = obj.get("expectedOutput").getAsString();
                            boolean isSample = obj.has("isSample") && obj.get("isSample").getAsBoolean();
                            String type = isSample ? "Sample" : "Hidden";
                            pendingTestcases.add(new String[]{in, out, type});
                            tcModel.addRow(new String[]{in, out, type});
                            added++;
                        }
                        lblStatus.setForeground(GREEN);
                        lblStatus.setText("✅ AI sinh " + added + " testcase thành công!");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(RED);
                        lblStatus.setText("❌ AI sinh testcase lỗi: " + ex.getMessage());
                    });
                }
            }).start();
        });

        return panel;
    }

    // ============================================================
    // TAB 3: KIỂM TRA ĐỘ MẠNH TESTCASE
    // ============================================================
    private JPanel buildStressPanel() {
        JPanel panel = new JPanel(new BorderLayout(14, 14));
        panel.setBackground(BG);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 22, 22, 22));

        panel.add(pageHeader("Kiểm tra test case", "Kiểm tra độ mạnh của testcase"), BorderLayout.NORTH);

        // 3 code editors
        JTextArea taAC = makeCodeArea();
        taAC.setText("// Paste code AC (đúng) vào đây");
        JTextArea taWA = makeCodeArea();
        taWA.setText("// Paste code WA (sai) vào đây");
        JTextArea taTLE = makeCodeArea();
        taTLE.setText("// Paste code TLE (chậm) vào đây");

        JTabbedPane codeTabs = new JTabbedPane();
        styleTabs(codeTabs);
        codeTabs.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        codeTabs.addTab("  Code AC  ", darkScroll(taAC));
        codeTabs.addTab("  Code WA  ", darkScroll(taWA));
        codeTabs.addTab("  Code TLE  ", darkScroll(taTLE));

        // Config
        JPanel configRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 2));
        configRow.setOpaque(false);
        JTextField tfProbId = makeField("Problem ID");
        tfProbId.setPreferredSize(new Dimension(110, 36));
        JTextField tfRuns = makeField("20");
        tfRuns.setPreferredSize(new Dimension(70, 36));
        JComboBox<String> cbLang = new JComboBox<>(new String[]{"Java", "C++", "Python"});
        styleCombo(cbLang);
        JButton btnRun = makeBtn("Chạy kiểm tra độ mạnh", GREEN);
        configRow.add(makeLabel("Problem ID:")); configRow.add(tfProbId);
        configRow.add(makeLabel("Số lần chạy:")); configRow.add(tfRuns);
        configRow.add(cbLang); configRow.add(btnRun);

        // Result
        JTextArea taResult = makeCodeArea();
        taResult.setEditable(false);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
            codeTabs, card("Kết quả kiểm tra độ mạnh", darkScroll(taResult)));
        split.setDividerLocation(350);
        split.setBackground(BG);
        split.setBorder(new RoundedBorder(BORDER, 8));
        split.setDividerSize(7);
        split.setResizeWeight(0.58);

        panel.add(split, BorderLayout.CENTER);
        panel.add(card(null, configRow), BorderLayout.SOUTH);

        // Kiểm tra độ mạnh testcase logic
        btnRun.addActionListener(e -> {
            try {
                String probIdStr = tfProbId.getText().trim();
                if (probIdStr.isEmpty()) { 
                    taResult.setText("❌ Lỗi: Phải nhập Problem ID!"); 
                    return; 
                }
                
                int probId = Integer.parseInt(probIdStr);
                int runs = Integer.parseInt(tfRuns.getText().trim());
                String lang = (String) cbLang.getSelectedItem();
                
                String[] prob = ProblemDAO.getProblem(probId);
                if (prob == null) { 
                    taResult.setText("❌ Lỗi: Không tìm thấy đề ID=" + probId + "!"); 
                    return; 
                }
                
                double tl = Double.parseDouble(prob[3]);
                List<String[]> tcs = TestcaseDAO.getTestcases(probId);
                if (tcs.isEmpty()) { 
                    taResult.setText("❌ Lỗi: Đề ID=" + probId + " chưa có testcase!"); 
                    return; 
                }
                
                String codeAC = taAC.getText().trim();
                if (codeAC.isEmpty() || codeAC.contains("// Paste")) {
                    taResult.setText("❌ Lỗi: Phải nhập Code AC vào tab 'Code AC'!"); 
                    return;
                }

                taResult.setText("⏳ Chuẩn bị kiểm tra độ mạnh " + Math.min(runs, tcs.size()) + " testcase...\n");
                taResult.append("   Problem: " + prob[1] + " (ID=" + probId + ")\n");
                taResult.append("   Time Limit: " + tl + "s\n");
                taResult.append("   Testcase có: " + tcs.size() + "\n");
                taResult.append("   Language: " + lang + "\n\n");
                
                new Thread(() -> {
                    StringBuilder sb = new StringBuilder();
                    int acPassed = 0, waDetected = 0, tleDetected = 0;
                    String codeWA = taWA.getText().contains("// Paste") ? null : taWA.getText().trim();
                    String codeTLE = taTLE.getText().contains("// Paste") ? null : taTLE.getText().trim();

                    for (int i = 0; i < Math.min(runs, tcs.size()); i++) {
                        String input = tcs.get(i)[1];
                        String expected = tcs.get(i)[2];

                        // AC code
                        String[] resAC = Judge.run(codeAC, input, tl, lang);
                        String acOut = resAC[0];
                        String acVerdict = Judge.check(expected, acOut);
                        if (acVerdict.equals("AC")) acPassed++;

                        sb.append(String.format("Test #%d: AC=%s", i+1, acVerdict));

                        // WA code
                        if (codeWA != null) {
                            String[] resWA = Judge.run(codeWA, input, tl, lang);
                            String waVerdict = isSystemVerdict(resWA[0]) ? resWA[0]
                                : Judge.check(expected, resWA[0]);
                            if (waVerdict.equals("WA")) waDetected++;
                            sb.append("  WA_code=").append(waVerdict);
                        }

                        // TLE code
                        if (codeTLE != null) {
                            String[] resTLE = Judge.run(codeTLE, input, tl, lang);
                            String tleVerdict = isSystemVerdict(resTLE[0]) ? resTLE[0] : Judge.check(expected, resTLE[0]);
                            if (tleVerdict.equals("TLE")) tleDetected++;
                            sb.append("  TLE_code=").append(tleVerdict);
                        }

                        sb.append("\n");
                    }

                    sb.append("\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
                    sb.append("✅ Code AC đúng: ").append(acPassed).append("/").append(Math.min(runs, tcs.size())).append("\n");
                    if (codeWA != null) sb.append("❌ WA bị phát hiện: ").append(waDetected).append(" lần\n");
                    if (codeTLE != null) sb.append("⏰ TLE bị phát hiện: ").append(tleDetected).append(" lần\n");

                    if (codeWA != null && waDetected == 0)
                        sb.append("\n⚠️  TESTCASE YẾU! Code WA vẫn pass hết → cần thêm testcase!\n");
                    if (codeTLE != null && tleDetected == 0)
                        sb.append("⚠️  TESTCASE YẾU! Code TLE vẫn pass hết → cần testcase lớn hơn!\n");
                    if ((codeWA == null || waDetected > 0) && (codeTLE == null || tleDetected > 0))
                        sb.append("\n🏆 TESTCASE TỐT! Đã phát hiện được code sai/chậm!\n");

                    SwingUtilities.invokeLater(() -> taResult.setText(taResult.getText() + sb.toString()));
                }).start();
            } catch (NumberFormatException ex) { 
                taResult.setText("❌ Lỗi: Problem ID và Số lần chạy phải là số!\n" + ex.getMessage()); 
            } catch (Exception ex) { 
                taResult.setText("❌ Lỗi: " + ex.getMessage()); 
                ex.printStackTrace();
            }
        });

        return panel;
    }

    // ============================================================
    // HELPERS
    // ============================================================
    private boolean isSystemVerdict(String output) {
        return output.equals("CE") || output.equals("TLE") || output.equals("ERR") || output.equals("RE");
    }

    private void showTestcaseManagerDialog(int problemId, String problemTitle) {
        JDialog dlg = new JDialog(this, "Quản lý testcase - " + problemTitle, true);
        dlg.setSize(920, 620);
        dlg.setLayout(new BorderLayout(10, 10));
        dlg.getContentPane().setBackground(BG);

        DefaultTableModel model = new DefaultTableModel(new String[]{"Input", "Expected Output", "Type"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(2).setMaxWidth(90);

        JTextArea taInput = makeCodeArea();
        taInput.setRows(5);
        JTextArea taOutput = makeCodeArea();
        taOutput.setRows(5);
        JCheckBox cbSample = new JCheckBox("Sample");
        cbSample.setBackground(BG);
        cbSample.setForeground(TEXT);
        JLabel lbl = new JLabel(" ");
        lbl.setForeground(MUTED);

        JButton btnAdd = makeBtn("Thêm", new Color(16, 185, 129));
        JButton btnDelete = makeBtn("Xóa đã chọn", new Color(220, 38, 38));
        JButton btnSave = makeBtn("Lưu thay đổi", ACCENT);

        JPanel ioPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        ioPanel.setBackground(BG);
        ioPanel.add(card("Input testcase", darkScroll(taInput)));
        ioPanel.add(card("Expected output", darkScroll(taOutput)));

        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actionBar.setBackground(BG);
        actionBar.add(cbSample);
        actionBar.add(btnAdd);
        actionBar.add(btnDelete);
        actionBar.add(btnSave);

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBackground(BG);
        top.add(ioPanel, BorderLayout.CENTER);
        top.add(actionBar, BorderLayout.SOUTH);

        dlg.add(top, BorderLayout.NORTH);
        dlg.add(card("Danh sách testcase", darkScroll(table)), BorderLayout.CENTER);
        dlg.add(lbl, BorderLayout.SOUTH);

        try {
            List<String[]> rows = TestcaseDAO.getTestcases(problemId);
            for (String[] tc : rows) model.addRow(new String[]{tc[1], tc[2], tc[3]});
            lbl.setText(" Đã tải " + rows.size() + " testcase.");
        } catch (Exception e) {
            lbl.setForeground(RED);
            lbl.setText(" Lỗi tải testcase: " + e.getMessage());
        }

        btnAdd.addActionListener(ev -> {
            String in = taInput.getText().trim();
            String out = taOutput.getText().trim();
            if (in.isEmpty() || out.isEmpty()) {
                showError("Input và Expected Output không được để trống!");
                return;
            }
            model.addRow(new String[]{in, out, cbSample.isSelected() ? "Sample" : "Hidden"});
            taInput.setText("");
            taOutput.setText("");
            cbSample.setSelected(false);
            lbl.setForeground(GREEN);
            lbl.setText(" Đã thêm testcase. Tổng: " + model.getRowCount());
        });

        btnDelete.addActionListener(ev -> {
            int row = table.getSelectedRow();
            if (row < 0) {
                showError("Chọn testcase cần xóa!");
                return;
            }
            model.removeRow(row);
            lbl.setForeground(YELLOW);
            lbl.setText(" Đã xóa testcase. Còn lại: " + model.getRowCount());
        });

        btnSave.addActionListener(ev -> {
            try {
                TestcaseDAO.deleteTestcasesByProblem(problemId);
                for (int i = 0; i < model.getRowCount(); i++) {
                    String in = model.getValueAt(i, 0).toString();
                    String out = model.getValueAt(i, 1).toString();
                    boolean isSample = model.getValueAt(i, 2).toString().equals("Sample");
                    TestcaseDAO.addTestcase(problemId, in, out, isSample);
                }
                lbl.setForeground(GREEN);
                lbl.setText(" ✅ Đã lưu testcase cho đề ID=" + problemId + ". Tổng: " + model.getRowCount());
            } catch (Exception e) {
                lbl.setForeground(RED);
                lbl.setText(" ❌ Lưu thất bại: " + e.getMessage());
            }
        });

        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showCodeDialog(String title, String code) {
        JTextArea ta = makeCodeArea();
        ta.setText(code);
        JDialog dlg = new JDialog(this, title, false);
        dlg.setSize(700, 500);
        dlg.add(darkScroll(ta));
        JButton btnCopy = makeBtn("Copy", ACCENT);
        btnCopy.addActionListener(ev -> {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(code), null);
            JOptionPane.showMessageDialog(dlg, "Đã copy!");
        });
        JPanel bot = new JPanel(); bot.setBackground(BG); bot.add(btnCopy);
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Parse thông tin đề từ text OCR
     * @return [title, content, timeLimit, memoryLimit]
     */
    private String[] parseProblemInfoFromText(String text) {
        String title = "";
        String content = text;
        String timeLimit = "1.0";
        String memLimit = "256";
        
        String[] lines = text.split("\n");
        if (lines.length > 0) {
            // Dòng đầu tiên = tên đề (loại bỏ keywords)
            title = lines[0]
                .replaceAll("(?i)(tên đề|title|đề|problem)[:：]\\s*", "")
                .trim();
            if (title.length() > 100) title = title.substring(0, 100); // Giới hạn độ dài
        }
        
        // Parse Time Limit (tìm số đi kèm với "time", "s", "giây", "second")
        java.util.regex.Pattern timePattern = java.util.regex.Pattern.compile("(?i)(time|giây|second)[:：\\s]*([0-9.]+)");
        java.util.regex.Matcher timeMatcher = timePattern.matcher(text);
        if (timeMatcher.find()) {
            timeLimit = timeMatcher.group(2);
        }
        
        // Parse Memory Limit (tìm số đi kèm với "memory", "MB", "m", "bộ nhớ")
        java.util.regex.Pattern memPattern = java.util.regex.Pattern.compile("(?i)(memory|bộ nhớ|mb)[:：\\s]*([0-9]+)");
        java.util.regex.Matcher memMatcher = memPattern.matcher(text);
        if (memMatcher.find()) {
            memLimit = memMatcher.group(2);
        }
        
        return new String[]{title, content, timeLimit, memLimit};
    }

    private void showProblemDetailsDialog(int problemId) {
        try {
            String[] problem = ProblemDAO.getProblem(problemId);
            if (problem == null) {
                showError("Không tìm thấy đề!");
                return;
            }
            
            String title = problem[1];
            String content = problem[2];
            String timeLimit = problem[3];
            String memLimit = problem[4];
            
            JDialog dlg = new JDialog(this, "Chi tiết đề - " + title, true);
            dlg.setSize(1200, 750);
            dlg.setLayout(new BorderLayout(8, 8));
            dlg.getContentPane().setBackground(BG);
            dlg.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            // Header
            JPanel header = new JPanel(new GridLayout(1, 4, 20, 0));
            header.setBackground(BG);
            header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            
            header.add(makeLabel("Tên đề:", title, 12));
            header.add(makeLabel("ID:", "" + problemId, 12));
            header.add(makeLabel("Time Limit:", timeLimit + " s", 12));
            header.add(makeLabel("Memory Limit:", memLimit + " MB", 12));
            
            dlg.add(header, BorderLayout.NORTH);
            
            // Content
            JTextArea taContent = makeCodeArea();
            taContent.setText(content);
            taContent.setEditable(false);
            taContent.setLineWrap(false);
            JPanel contentPanel = card("Nội dung đề", darkScroll(taContent));
            
            // Testcases
            DefaultTableModel tcModel = new DefaultTableModel(new String[]{"#", "Input", "Output", "Type"}, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            JTable tcTable = new JTable(tcModel);
            styleTable(tcTable);
            tcTable.getColumnModel().getColumn(0).setMaxWidth(40);
            tcTable.getColumnModel().getColumn(3).setMaxWidth(80);
            tcTable.setRowHeight(24);
            
            List<String[]> testcases = TestcaseDAO.getTestcases(problemId);
            int tcCount = 1;
            for (String[] tc : testcases) {
                tcModel.addRow(new String[]{String.valueOf(tcCount++), tc[1], tc[2], tc[3]});
            }
            
            JPanel tcPanel;
            if (testcases.isEmpty()) {
                tcPanel = new JPanel(new BorderLayout());
                tcPanel.setBackground(BG);
                JLabel lbl = new JLabel("(Chưa có testcase)");
                lbl.setForeground(MUTED);
                lbl.setFont(BODY_FONT);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                tcPanel.add(lbl, BorderLayout.CENTER);
            } else {
                tcPanel = card("Test Cases (" + testcases.size() + ")", darkScroll(tcTable));
            }
            
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, contentPanel, tcPanel);
            split.setDividerLocation(600);
            split.setBackground(BG);
            split.setDividerSize(4);
            split.setContinuousLayout(true);
            dlg.add(split, BorderLayout.CENTER);
            
            // Footer
            JButton btnClose = makeBtn("Đóng", ACCENT);
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            footer.setBackground(BG);
            footer.add(btnClose);
            dlg.add(footer, BorderLayout.SOUTH);
            
            btnClose.addActionListener(e -> dlg.dispose());
            
            dlg.setLocationRelativeTo(this);
            dlg.setVisible(true);
        } catch (Exception e) {
            showError("Lỗi tải chi tiết: " + e.getMessage());
        }
    }

    private JPanel makeLabel(String key, String value, int fontSize) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        p.setBackground(BG);
        JLabel lKey = new JLabel(key);
        lKey.setFont(new Font("Tahoma", Font.BOLD, fontSize));
        lKey.setForeground(MUTED);
        JLabel lVal = new JLabel(value);
        lVal.setFont(new Font("Tahoma", Font.PLAIN, fontSize));
        lVal.setForeground(TEXT);
        p.add(lKey);
        p.add(lVal);
        return p;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        try {
            DatabaseManager.initDatabase();
            SwingUtilities.invokeLater(MainApp::new);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Lỗi khởi động: " + e.getMessage());
        }
    }
}
