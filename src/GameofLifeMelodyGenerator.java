import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.sound.midi.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class GameofLifeMelodyGenerator {

    private static final int GRID_ROWS = 8;
    private static final int GRID_COLUMNS = 14;
    private static final int CELL_SIZE = 50;
    private static final int BUTTON_WIDTH = 150;
    private static final int BUTTON_HEIGHT = 45;
    private static final int DELAY = 100;
    private static final int[] notesForRow = {55,57, 59, 60, 62, 64, 65, 67, 69, 71, 72, 74, 76, 77};
    private boolean[][] grid;
    private boolean[][] nextGrid;
    private String[] columnLabels = {"G3","A3", "B3", "C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5", "D5","E5","F5"};
    private JPanel[][] cellPanels;
    private JFrame frame;
    private JPanel controlPanel;
    private JPanel gridPanel;
    private JButton randomCombinationButton;
    private JButton nextGenerationButton;
    private JButton cleanButton;
    private JButton playRowOrderButton;
    private JButton playColumnOrderButton;
    private boolean isPaused;
    private Synthesizer synthesizer;
    private MidiChannel midiChannel;
    private List<Integer> notesToPlay;
    private List<Integer> selectedCellNotes;

    public GameofLifeMelodyGenerator() {

        grid = new boolean[GRID_ROWS][GRID_COLUMNS];
        nextGrid = new boolean[GRID_ROWS][GRID_COLUMNS];
        cellPanels = new JPanel[GRID_ROWS][GRID_COLUMNS];
        frame = new JFrame("Game of Life Melody Generator");
        ImageIcon icon = new ImageIcon("/Users/sabrina/IdeaProjects/ciao/src/music+notes+icon.png");
        frame.setIconImage(icon.getImage());
        gridPanel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLUMNS));
        controlPanel = new JPanel();
        randomCombinationButton = new JButton("Random Combination");
        nextGenerationButton = new JButton("Next Generation");
        cleanButton = new JButton("Clean");
        playRowOrderButton = new JButton("Play in Row Order");
        playColumnOrderButton = new JButton("Play in Column Order");
        isPaused = true;
        initializeGUI();
        initializeGrid();
        initializeMidi();
    }


    private void initializeGUI() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Grid Panel
        gridPanel.setLayout(new GridLayout(GRID_ROWS, GRID_COLUMNS));
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                JPanel cellPanel = new JPanel();
                cellPanel.setPreferredSize(new Dimension(CELL_SIZE * 2, CELL_SIZE));
                cellPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                cellPanel.addMouseListener(new CellMouseListener(row, col));
                gridPanel.add(cellPanel);
                cellPanels[row][col] = cellPanel;
            }
        }

        // Column Labels Panel
        JPanel columnLabelsPanel = new JPanel(new GridLayout(1, GRID_COLUMNS));
        for (int col = 0; col < GRID_COLUMNS; col++) {
            JLabel label = new JLabel(columnLabels[col], SwingConstants.CENTER);
            columnLabelsPanel.add(label);
        }

        // Control Panel
        randomCombinationButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        nextGenerationButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        playRowOrderButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        playColumnOrderButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));
        cleanButton.setPreferredSize(new Dimension(BUTTON_WIDTH, BUTTON_HEIGHT));

        controlPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

        controlPanel.add(randomCombinationButton);
        controlPanel.add(nextGenerationButton);
        controlPanel.add(playRowOrderButton);
        controlPanel.add(playColumnOrderButton);
        controlPanel.add(cleanButton);

        randomCombinationButton.addActionListener(new RandomCombinationButtonListener());
        nextGenerationButton.addActionListener(new NextGenerationButtonListener());
        cleanButton.addActionListener(new CleanButtonListener());
        playRowOrderButton.addActionListener(new PlayRowOrderButtonListener());
        playColumnOrderButton.addActionListener(new PlayColumnOrderButtonListener());

        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(columnLabelsPanel, BorderLayout.NORTH);
        mainPanel.add(gridPanel, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);

        // Frame Layout
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }



    private void initializeGrid() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                grid[row][col] = false;
                nextGrid[row][col] = false;
                cellPanels[row][col].setBackground(Color.WHITE);
            }
        }
    }


    private void initializeMidi() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            midiChannel = synthesizer.getChannels()[0];
            notesToPlay = new ArrayList<>();
            selectedCellNotes = new ArrayList<>();
        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }


    private void playMidiNote(int note) {

        midiChannel.noteOn(note, 100);
    }


    private void stopMidiNote() {

        midiChannel.allNotesOff();
    }


    private void updateGrid() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                int neighbors = countNeighbors(row, col);
                if (grid[row][col]) {
                    nextGrid[row][col] = (neighbors == 2 || neighbors == 3);
                } else {
                    nextGrid[row][col] = (neighbors == 3);
                }
            }
        }
    }


    private int countNeighbors(int row, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int neighborRow = (row + i + GRID_ROWS) % GRID_ROWS;
                int neighborCol = (col + j + GRID_COLUMNS) % GRID_COLUMNS;
                if (grid[neighborRow][neighborCol]) {
                    count++;
                }
            }
        }
        if (grid[row][col]) {
            count--;
        }
        return count;
    }


    private void swapGrids() {
        boolean[][] temp = grid;
        grid = nextGrid;
        nextGrid = temp;
    }


    private void updateGUI() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                if (grid[row][col]) {
                    cellPanels[row][col].setBackground(Color.BLACK);
                } else {
                    cellPanels[row][col].setBackground(Color.WHITE);
                }
            }
        }
    }


    private class CellMouseListener extends MouseAdapter {

        private int row;
        private int col;

        public CellMouseListener(int row, int col) {
            this.row = row;
            this.col = col;
        }


        @Override
        public void mouseClicked(MouseEvent e) {
            if (isPaused) {
                grid[row][col] = !grid[row][col];
                if (grid[row][col]) {
                    cellPanels[row][col].setBackground(Color.BLACK);
                    selectedCellNotes.add(getNoteForCell(row, col));
                } else {
                    cellPanels[row][col].setBackground(Color.WHITE);
                    selectedCellNotes.remove(Integer.valueOf(getNoteForCell(row, col)));
                }
            }
        }


        private int getNoteForCell(int row, int col) {
            int columnIndex = col % notesForRow.length;
            return notesForRow[columnIndex];
        }
    }


        private class NextGenerationButtonListener implements ActionListener {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isPaused) {
                    generateNotes();
                    updateGrid();
                    updateGUI();
                    swapGrids();
                    updateGUI(); // Add this line to update the GUI after generating the next generation
                }
            }
        }


    private void generateNotes() {
        selectedCellNotes.clear();

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                if (grid[row][col]) {
                    int note = getNoteForCell(row, col);
                    selectedCellNotes.add(note);
                }
            }
        }
    }


    private class RandomCombinationButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPaused) {
                generateRandomCombination();
                updateGUI();
            }
        }


        private void generateRandomCombination() {
            for (int row = 0; row < GRID_ROWS; row++) {
                for (int col = 0; col < GRID_COLUMNS; col++) {
                    grid[row][col] = Math.random() < 0.5;
                    if (grid[row][col]) {
                        cellPanels[row][col].setBackground(Color.BLACK);
                    } else {
                        cellPanels[row][col].setBackground(Color.WHITE);
                    }
                }
            }
        }

    }


    private class CleanButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPaused) {
                cleanGrid();
                updateGUI();
            }
        }
    }


    private void cleanGrid() {
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLUMNS; col++) {
                grid[row][col] = false;
                cellPanels[row][col].setBackground(Color.WHITE);
            }
        }
        selectedCellNotes.clear();
    }


    private class PlayRowOrderButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPaused) {
                generateNotes();
                playNotesInRowOrder();
            }
        }
    }


    private void playNotesInRowOrder() {
        Thread playThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int row = 0; row < GRID_ROWS; row++) {
                    for (int col = 0; col < GRID_COLUMNS; col++) {
                        if (grid[row][col]) {
                            int note = getNoteForCell(row, col);
                            playMidiNote(note);
                            try {
                                Thread.sleep(300); // Delay between notes (adjust as needed)
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                stopMidiNote();
            }
        });
        playThread.start();
    }


    private class PlayColumnOrderButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (isPaused) {
                generateNotes();
                playNotesInColumnOrder();
            }
        }
    }


    private void playNotesInColumnOrder() {
        Thread playThread = new Thread(new Runnable() {
            @Override
            public void run() {
                for (int col = 0; col < GRID_COLUMNS; col++) {
                    for (int row = 0; row < GRID_ROWS; row++) {
                        if (grid[row][col]) {
                            int note = getNoteForCell(row, col);
                            playMidiNote(note);
                            try {
                                Thread.sleep(300); // Delay between notes (adjust as needed)
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                stopMidiNote();
            }
        });
        playThread.start();
    }


    private int getNoteForCell(int row, int col) {
        int columnIndex = col % notesForRow.length;
        return notesForRow[columnIndex];
    }


    public void run() {
        while (true) {
            if (!isPaused) {
                updateGrid();
                updateGUI();
                swapGrids();
            }
            try {
                Thread.sleep(DELAY);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        GameofLifeMelodyGenerator game = new GameofLifeMelodyGenerator();
        game.run();
    }
}
