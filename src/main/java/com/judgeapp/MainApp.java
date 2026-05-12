package com.judgeapp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.*;

import com.judgeapp.ai.GeminiAPI;
import com.judgeapp.db.*;
import com.judgeapp.judge.Judge;
import com.judgeapp.ocr.OCRManager;
import com.google.gson.*;

// PDF support
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

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
        tabs.addTab("  Độ mạnh test case  ", buildStressPanel());

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
        JButton btnDelete = makeBtn("Xóa đề", RED);
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
        btnDelete.addActionListener(e -> {
            int row = problemTable.getSelectedRow();
            if (row < 0) {
                showError("Chọn một đề trong danh sách trước!");
                return;
            }
            int problemId = Integer.parseInt(problemModel.getValueAt(row, 0).toString());
            String title = problemModel.getValueAt(row, 1).toString();
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn chắc chắn muốn xóa đề: " + title + " và tất cả test case của nó?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    TestcaseDAO.deleteTestcasesByProblem(problemId);
                    ProblemDAO.deleteProblem(problemId);
                    showSuccess("Xóa đề thành công!");
                    refreshProblemTable();
                } catch (Exception ex) {
                    showError("Lỗi xóa đề: " + ex.getMessage());
                }
            }
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        south.setBackground(BG);
        south.add(btnViewDetails);
        south.add(btnManageTc);
        south.add(btnDelete);
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
        final String[] generatorCode = {null};
        final String[] checkerCode = {null};
        final String[] sampleACCode = {null};
        final String[] sampleACLanguage = {"Java"};

        JLabel lblImagePath = new JLabel("Chưa chọn ảnh/file");
        lblImagePath.setForeground(MUTED);
        lblImagePath.setFont(BODY_FONT);
        lblImagePath.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(BORDER, 8),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        lblImagePath.setOpaque(true);
        lblImagePath.setBackground(Color.WHITE);
        JButton btnPickImg = makeBtn("Chọn ảnh/file", new Color(59, 130, 246));
        JButton btnOCR = makeBtn("AI đọc ảnh/file", new Color(139, 92, 246));
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

        // === Image/File pick ===
        final String[] imagePath = {null};
        btnPickImg.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Tất cả file hỗ trợ (ảnh, text, PDF)", "jpg", "jpeg", "png", "bmp", "gif", "txt", "md", "pdf"));
            fc.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "File ảnh (JPG, PNG, BMP, GIF)", "jpg", "jpeg", "png", "bmp", "gif"));
            fc.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "File text (TXT, MD)", "txt", "md"));
            fc.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "File PDF", "pdf"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                imagePath[0] = fc.getSelectedFile().getAbsolutePath();
                lblImagePath.setText(fc.getSelectedFile().getName());
                lblImagePath.setForeground(TEXT);
            }
        });

        // === Đọc nội dung file (OCR cho ảnh, đọc text cho txt/md, extract cho PDF) ===
        btnOCR.addActionListener(e -> {
            if (imagePath[0] == null) { showError("Chọn file trước!"); return; }
            
            String filePath = imagePath[0].toLowerCase();
            boolean isImage = filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") 
                           || filePath.endsWith(".png") || filePath.endsWith(".bmp")
                           || filePath.endsWith(".gif");
            boolean isTextFile = filePath.endsWith(".txt") || filePath.endsWith(".md");
            boolean isPDF = filePath.endsWith(".pdf");
            
            if (!isImage && !isTextFile && !isPDF) {
                showError("Định dạng file không hỗ trợ! Chỉ hỗ trợ: ảnh (jpg, png, bmp, gif), text (txt, md), PDF");
                return;
            }
            
            String statusMsg = isImage ? "⏳ OCR đang đọc ảnh..." 
                             : isPDF ? "⏳ Đang đọc file PDF..." 
                             : "⏳ Đang đọc file text...";
            lblStatus.setForeground(YELLOW);
            lblStatus.setText(statusMsg);
            
            new Thread(() -> {
                try {
                    String text;
                    String fileType;
                    
                    if (isImage) {
                        text = OCRManager.readImageText(imagePath[0]);
                        fileType = "OCR";
                    } else if (isPDF) {
                        // Đọc PDF bằng PDFBox
                        File pdfFile = new File(imagePath[0]);
                        try (PDDocument document = Loader.loadPDF(pdfFile)) {
                            PDFTextStripper stripper = new PDFTextStripper();
                            text = stripper.getText(document);
                        }
                        fileType = "PDF";
                    } else {
                        text = new String(Files.readAllBytes(new File(imagePath[0]).toPath()), "UTF-8");
                        fileType = "Text";
                    }
                    
                    SwingUtilities.invokeLater(() -> {
                        // Parse thông tin từ text
                        String[] info = parseProblemInfoFromText(text);
                        String title = info[0];
                        String content = info[1];
                        String timeLimit = info[2];
                        String memLimit = info[3];
                        
                        // Tự điền vào form
                        if (!title.isEmpty()) {
                            tfTitle.setText(title);
                            tfTitle.setForeground(TEXT);
                        }
                        if (!content.isEmpty()) taContent.setText(content);
                        if (!timeLimit.isEmpty()) {
                            tfTime.setText(timeLimit);
                            tfTime.setForeground(TEXT);
                        }
                        if (!memLimit.isEmpty()) {
                            tfMem.setText(memLimit);
                            tfMem.setForeground(TEXT);
                        }
                        
                        lblStatus.setForeground(GREEN);
                        lblStatus.setText("✅ " + fileType + " đọc xong! Tên đề: " + (title.isEmpty() ? "(chưa xác định)" : title));
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        lblStatus.setForeground(RED);
                        lblStatus.setText("❌ Lỗi đọc file: " + ex.getMessage());
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
                    generatorCode[0] = code;
                    SwingUtilities.invokeLater(() -> {
                        showCodeDialog("Code Generator (Java)", code, edited -> generatorCode[0] = edited);
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
                    checkerCode[0] = code;
                    SwingUtilities.invokeLater(() -> {
                        showCodeDialog("Code Checker (Java)", code, edited -> checkerCode[0] = edited);
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
                    sampleACCode[0] = code;
                    sampleACLanguage[0] = lang;
                    SwingUtilities.invokeLater(() -> {
                        showCodeDialog("Code mẫu AC (" + lang + ")", code, edited -> sampleACCode[0] = edited);
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
                int id = ProblemDAO.addProblem(title, content, tl, ml,
                    generatorCode[0], checkerCode[0], sampleACCode[0], sampleACLanguage[0]);
                
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
                lblImagePath.setText("Chưa chọn ảnh/file");
                lblImagePath.setForeground(MUTED);
                pendingTestcases.clear();
                generatorCode[0] = null;
                checkerCode[0] = null;
                sampleACCode[0] = null;
                sampleACLanguage[0] = "Java";
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
                    if (generatorCode[0] != null && sampleACCode[0] != null) {
                        List<String[]> generated = generateTestcasesFromGenerator(
                            generatorCode[0], sampleACCode[0], sampleACLanguage[0], finalCount, 5.0);
                        SwingUtilities.invokeLater(() -> {
                            for (String[] tc : generated) {
                                pendingTestcases.add(tc);
                                tcModel.addRow(new String[]{tc[0], tc[1], tc[2]});
                            }
                            lblStatus.setForeground(GREEN);
                            lblStatus.setText("Da sinh " + generated.size() + " testcase bang Generator + Code AC.");
                        });
                        return;
                    }

                    String prompt = """
                        Dựa trên đề bài sau:
                        %s
                        
                        Hãy sinh %d testcase chất lượng cao cho bài lập trình này.
                        Yêu cầu:
                        - Chỉ trả về JSON array thuần (bắt đầu bằng [ kết thúc bằng ]), không markdown, không backticks, không giải thích.
                        - Mỗi phần tử có dạng: {"input":"...", "expectedOutput":"...", "isSample":false}
                        - input/expectedOutput phải đúng format đề bài.
                        - Bao phủ cả edge cases, base case.
                        - Dấu ngoặc kép trong input/output phải escape: \\" thay vì "
                        VD: [{"input":"1\\n2","expectedOutput":"3","isSample":false}]
                        """.formatted(content, finalCount);
                    String raw = GeminiAPI.ask(prompt).trim();
                    
                    // Xóa markdown backticks nếu có
                    raw = raw.replaceAll("```json\\s*", "").replaceAll("```\\s*", "").trim();
                    
                    // Tìm JSON array trong response
                    int startIdx = raw.indexOf('[');
                    int endIdx = raw.lastIndexOf(']');
                    if (startIdx < 0 || endIdx < 0) {
                        throw new Exception("Không tìm thấy JSON array trong response: " + raw.substring(0, Math.min(200, raw.length())));
                    }
                    raw = raw.substring(startIdx, endIdx + 1);
                    
                    JsonArray arr = JsonParser.parseString(raw).getAsJsonArray();
                    SwingUtilities.invokeLater(() -> {
                        int added = 0;
                        for (JsonElement el : arr) {
                            try {
                                JsonObject obj = el.getAsJsonObject();
                                String in = obj.get("input").getAsString();
                                String out = obj.get("expectedOutput").getAsString();
                                boolean isSample = obj.has("isSample") && obj.get("isSample").getAsBoolean();
                                String type = isSample ? "Sample" : "Hidden";
                                
                                // Thêm vào pendingTestcases (sẽ lưu vào DB khi save problem)
                                pendingTestcases.add(new String[]{in, out, type});
                                tcModel.addRow(new String[]{in, out, type});
                                added++;
                            } catch (Exception innerEx) {
                                System.err.println("Lỗi parse testcase: " + el + " | " + innerEx.getMessage());
                            }
                        }
                        lblStatus.setForeground(GREEN);
                        lblStatus.setText("✅ AI sinh " + added + " testcase thành công! (Tổng: " + pendingTestcases.size() + ")");
                    });
                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        ex.printStackTrace();
                        String msg = ex.getMessage();
                        if (msg == null) msg = ex.getClass().getSimpleName();
                        lblStatus.setForeground(RED);
                        lblStatus.setText("❌ AI sinh testcase lỗi: " + msg);
                    });
                    System.err.println("DEBUG: " + ex);
                    ex.printStackTrace();
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
        final int[] resultVersion = {0};
        codeTabs.addChangeListener(e -> {
            resultVersion[0]++;
            taResult.setText("");
        });
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                resultVersion[0]++;
                taResult.setText("");
            }
        });

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
                String selectedLang = (String) cbLang.getSelectedItem();
                
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
                
                String checkerForProblem = getProblemArtifact(prob, 6);
                String savedAC = getProblemArtifact(prob, 7);
                String savedACLanguage = getProblemArtifact(prob, 8);

                String codeAC = normalizedSubmittedCode(taAC.getText());
                String codeWA = normalizedSubmittedCode(taWA.getText());
                String codeTLE = normalizedSubmittedCode(taTLE.getText());
                String acLanguage = selectedLang;
                if (codeAC == null && savedAC != null) {
                    codeAC = savedAC;
                    acLanguage = savedACLanguage == null ? "Java" : savedACLanguage;
                    taAC.setText(savedAC);
                    cbLang.setSelectedItem(acLanguage);
                }
                if (codeAC == null && codeWA == null && codeTLE == null) {
                    taResult.setText("Lỗi: Hãy nhập ít nhất một code mẫu AC, WA hoặc TLE!");
                    return;
                }

                taResult.setText("⏳ Chuẩn bị kiểm tra độ mạnh " + Math.min(runs, tcs.size()) + " testcase...\n");
                final String runACLanguage = acLanguage;
                final String runLanguage = selectedLang;
                final String runCodeAC = codeAC;
                final String runCodeWA = codeWA;
                final String runCodeTLE = codeTLE;
                final String runChecker = checkerForProblem;
                taResult.append("   Problem: " + prob[1] + " (ID=" + probId + ")\n");
                taResult.append("   Time Limit: " + tl + "s\n");
                taResult.append("   Testcase có: " + tcs.size() + "\n");
                taResult.append("   Language: " + runLanguage + "\n");
                taResult.append("   Checker: " + (runChecker == null ? "default compare" : "custom checker") + "\n\n");
                int runVersion = resultVersion[0];
                
                new Thread(() -> {
                    StringBuilder sb = new StringBuilder();
                    int total = Math.min(runs, tcs.size());
                    int acPassed = 0;
                    int waDetected = 0, waPassed = 0, waOther = 0;
                    int tleDetected = 0, tlePassed = 0, tleOther = 0;

                    for (int i = 0; i < total; i++) {
                        String input = tcs.get(i)[1];
                        String expected = tcs.get(i)[2];

                        sb.append(String.format("Test #%d:", i + 1));

                        // AC code
                        if (runCodeAC != null) {
                            String[] resAC = Judge.run(runCodeAC, input, tl, runACLanguage);
                            String acVerdict = judgeVerdict(expected, resAC[0], input, runChecker, tl);
                            if (acVerdict.equals("AC")) acPassed++;

                            sb.append(" Code AC=").append(acVerdict);
                            if (!acVerdict.equals("AC")) {
                                appendFailureDetails(sb, input, expected, resAC);
                            }
                        }

                        // WA code
                        if (runCodeWA != null) {
                            String[] resWA = Judge.run(runCodeWA, input, tl, runLanguage);
                            String waVerdict = judgeVerdict(expected, resWA[0], input, runChecker, tl);
                            if (waVerdict.equals("WA")) {
                                waDetected++;
                                sb.append("  Code WA=BAT_DUOC_SAI(WA)");
                            } else if (waVerdict.equals("AC")) {
                                waPassed++;
                                sb.append("  Code WA=SAI_NHUNG_LOT_QUA(AC)");
                            } else {
                                waOther++;
                                sb.append("  Code WA=LOI_MAU(").append(waVerdict).append(")");
                            }
                        }

                        // TLE code
                        if (runCodeTLE != null) {
                            String[] resTLE = Judge.run(runCodeTLE, input, tl, runLanguage);
                            String tleVerdict = judgeVerdict(expected, resTLE[0], input, runChecker, tl);
                            if (tleVerdict.equals("TLE")) {
                                tleDetected++;
                                sb.append("  Code TLE=BAT_DUOC_CHAM(TLE)");
                            } else if (tleVerdict.equals("AC")) {
                                tlePassed++;
                                sb.append("  Code TLE=CHUA_BI_CHAM(AC)");
                            } else {
                                tleOther++;
                                sb.append("  Code TLE=LOI_KHAC(").append(tleVerdict).append(")");
                            }
                        }

                        sb.append("\n");
                    }

                    appendStressSummary(sb, total,
                        runCodeAC != null, acPassed,
                        runCodeWA != null, waDetected, waPassed, waOther,
                        runCodeTLE != null, tleDetected, tlePassed, tleOther);

                    SwingUtilities.invokeLater(() -> {
                        if (runVersion == resultVersion[0]) {
                            taResult.setText(taResult.getText() + sb.toString());
                        }
                    });
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

    private String normalizedSubmittedCode(String rawCode) {
        if (rawCode == null) return null;
        String code = rawCode.trim();
        if (code.isEmpty() || code.contains("// Paste")) return null;
        return code;
    }

    private List<String[]> generateTestcasesFromGenerator(String generatorCode, String acCode,
            String acLanguage, int count, double timeLimit) throws Exception {
        List<String[]> generated = new ArrayList<>();
        String language = (acLanguage == null || acLanguage.isBlank()) ? "Java" : acLanguage;

        for (int i = 0; i < count; i++) {
            String[] genResult = Judge.run(generatorCode, "", timeLimit, "Java");
            if (isSystemVerdict(genResult[0])) {
                throw new Exception("Generator " + genResult[0] + ": "
                    + (genResult.length > 2 ? genResult[2] : ""));
            }

            String input = genResult[0].trim();
            String[] acResult = Judge.run(acCode, input, timeLimit, language);
            if (isSystemVerdict(acResult[0])) {
                throw new Exception("Code AC " + acResult[0] + " khi sinh expected: "
                    + (acResult.length > 2 ? acResult[2] : ""));
            }

            generated.add(new String[]{input, acResult[0].trim(), "Hidden"});
        }
        return generated;
    }

    private String judgeVerdict(String expected, String actual, String input, String checkerCode, double timeLimit) {
        return isSystemVerdict(actual) ? actual : Judge.check(expected, actual, input, checkerCode, timeLimit);
    }

    private String getProblemArtifact(String[] problem, int index) {
        if (problem == null || problem.length <= index) return null;
        String value = problem[index];
        return value == null || value.isBlank() ? null : value;
    }

    private void appendFailureDetails(StringBuilder sb, String input, String expected, String[] result) {
        sb.append("\n    input=\"").append(escapeVisible(input)).append("\"");
        sb.append(" expected=\"").append(escapeVisible(expected)).append("\"");
        sb.append(" actual=\"").append(escapeVisible(result[0])).append("\"");
        if (result.length > 2 && result[2] != null && !result[2].isBlank()) {
            sb.append("\n    details=\"").append(escapeVisible(result[2])).append("\"");
        }
    }

    private String escapeVisible(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }

    private void appendStressSummary(StringBuilder sb, int total,
            boolean hasAC, int acPassed,
            boolean hasWA, int waDetected, int waPassed, int waOther,
            boolean hasTLE, int tleDetected, int tlePassed, int tleOther) {
        sb.append("\n---------------------------------\n");
        if (hasAC) {
            sb.append("Code AC pass: ").append(acPassed).append("/").append(total).append("\n");
        }
        if (hasWA) {
            sb.append("Code WA sai bi bat: ").append(waDetected).append("/").append(total).append("\n");
            if (waPassed > 0) sb.append("Code WA sai nhung lot qua: ").append(waPassed).append("/").append(total).append("\n");
            if (waOther > 0) sb.append("Code WA mau bi loi khac: ").append(waOther).append("/").append(total).append("\n");
        }
        if (hasTLE) {
            sb.append("Code TLE cham bi bat: ").append(tleDetected).append("/").append(total).append("\n");
            if (tlePassed > 0) sb.append("Code TLE chua bi cham: ").append(tlePassed).append("/").append(total).append("\n");
            if (tleOther > 0) sb.append("Code TLE mau loi kieu khac: ").append(tleOther).append("/").append(total).append("\n");
        }

        sb.append("\nDANH GIA DOC LAP:\n");
        if (hasAC) {
            if (acPassed == total) {
                sb.append("- AC: OK, code dung pass het testcase.\n");
            } else {
                sb.append("- AC: CHUA ON, code dung khong pass het. Kiem tra expected output, input testcase hoac code AC.\n");
            }
        }
        if (hasWA) {
            if (waDetected > 0) {
                sb.append("- WA: OK, da co testcase bat duoc code sai.\n");
            } else if (waPassed == total) {
                sb.append("- WA: TESTCASE YEU, code sai van pass het.\n");
            } else {
                sb.append("- WA: CHUA DANH GIA DUOC, code WA mau bi CE/RE/TLE hoac loi khac.\n");
            }
        }
        if (hasTLE) {
            if (tleDetected > 0) {
                sb.append("- TLE: OK, da co testcase bat duoc code cham.\n");
            } else if (tlePassed == total) {
                sb.append("- TLE: TESTCASE CHUA DU LON, code cham van chay AC.\n");
            } else {
                sb.append("- TLE: CHUA DANH GIA DUOC, code TLE mau loi kieu khac.\n");
            }
        }
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
        showCodeDialog(title, code, null);
    }

    private void showCodeDialog(String title, String code, java.util.function.Consumer<String> onUse) {
        JTextArea ta = makeCodeArea();
        ta.setText(code);
        JDialog dlg = new JDialog(this, title, false);
        dlg.setSize(700, 500);
        dlg.add(darkScroll(ta));
        JButton btnCopy = makeBtn("Copy", ACCENT);
        btnCopy.addActionListener(ev -> {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new java.awt.datatransfer.StringSelection(ta.getText()), null);
            JOptionPane.showMessageDialog(dlg, "Đã copy!");
        });
        JPanel bot = new JPanel(); bot.setBackground(BG); bot.add(btnCopy);
        if (onUse != null) {
            JButton btnUse = makeBtn("Luu vao de", GREEN);
            btnUse.addActionListener(ev -> {
                onUse.accept(ta.getText());
                JOptionPane.showMessageDialog(dlg, "Da luu code nay vao de tam. Bam 'Luu de' de ghi vao CSDL.");
                dlg.dispose();
            });
            bot.add(btnUse);
        }
        dlg.add(bot, BorderLayout.SOUTH);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
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
