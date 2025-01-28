import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

class Book {
    String title;
    String author;
    double price;
    int stock;
    boolean isDiscontinued;
    String imagePath;

    public Book(String title, String author, double price, int stock, boolean isDiscontinued, String imagePath) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.isDiscontinued = isDiscontinued;
        this.imagePath = imagePath;
    }

    @Override
    public String toString() {
        return title + "," + author + "," + price + "," + stock + "," + isDiscontinued + "," + imagePath;
    }

    public static Book fromString(String bookString) {
        String[] parts = bookString.split(",");
        return new Book(parts[0], parts[1], Double.parseDouble(parts[2]),
                Integer.parseInt(parts[3]), Boolean.parseBoolean(parts[4]), parts.length > 5 ? parts[5] : "");
    }
}

public class BookstoreInventoryManagement extends JFrame {
    private final List<Book> books = new ArrayList<>();
    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel imageLabel;
    private final JTextArea detailsArea;

    public BookstoreInventoryManagement() {
        loadBooksFromFile();

        setTitle("Bookstore Inventory Management");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                int width = getWidth();
                int height = getHeight();
                Color color1 = new Color(165, 117, 80); // Light brown
                Color color2 = new Color(92, 64, 51); // Dark brown
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, height, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, width, height);
            }
        };
        mainPanel.setLayout(new BorderLayout());
        add(mainPanel);

        // Table panel
        String[] columnNames = {"Title", "Author", "Price", "Stock", "Discontinued?", "Image"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column){
                Component c = super.prepareRenderer(renderer, row, column);
                int stock = Integer.parseInt(getValueAt(row, 3).toString());
                boolean isDiscontinued = getValueAt(row, 4).toString().equalsIgnoreCase("Yes");

                //0 STOCK == ORANGE
                //ISDISCONTINUED == RED
                //AVAILABLE == GRREN

                if (stock == 0) {
                    c.setBackground(Color.ORANGE);
                } else if (isDiscontinued){
                    c.setBackground(new Color(238, 114, 114));
                } else {
                    c.setBackground(new Color(136, 231, 136));
                }
                c.setForeground(Color.BLACK);
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(253, 238, 217));

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Book Image Preview panel
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setPreferredSize(new Dimension(300, 0));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Book Preview"));
        mainPanel.add(previewPanel, BorderLayout.EAST);

        // Image preview
        imageLabel = new JLabel("No Image", SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        previewPanel.add(imageLabel, BorderLayout.NORTH);

        // Book details
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        previewPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

         // Search panel
         JPanel searchPanel = new JPanel();
         searchPanel.setOpaque(false);
         JTextField searchField = new JTextField(20);
         searchField.setBorder(new EmptyBorder(4, 4, 4, 4));
         JButton searchButton = new JButton("Search");
         searchPanel.add(new JLabel("Search: "));
         searchPanel.add(searchField);
         searchPanel.add(searchButton);
         mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("Add Book");
        JButton updateBookButton = new JButton("Update/Delete Book");
        JButton viewDiscontinuedButton = new JButton("View All Discontinued Books");
        JButton displayAllButton = new JButton("Display All Books");
        JButton sortTitleButton = new JButton("Sort by Title");
        JButton sortPriceButton = new JButton("Sort by Price");
        //JButton searchButton = new JButton("Search Book");

        buttonPanel.add(addButton);
        buttonPanel.add(updateBookButton);
        buttonPanel.add(viewDiscontinuedButton);
        buttonPanel.add(displayAllButton);
        buttonPanel.add(sortTitleButton);
        buttonPanel.add(sortPriceButton);
        //buttonPanel.add(searchButton);
        

        addButton.addActionListener(e -> addBook());
        updateBookButton.addActionListener(e -> updateOrDeleteBook());
        searchButton.addActionListener(e -> searchBooks(searchField.getText()));
        viewDiscontinuedButton.addActionListener(e -> viewDiscontinuedBooks());
        displayAllButton.addActionListener(e -> displayBooks());
        sortTitleButton.addActionListener(e -> sortBooks("title"));
        sortPriceButton.addActionListener(e -> sortBooks("price"));
        //searchButton.addActionListener(e -> searchBook());

        // Table click listener
        table.getSelectionModel().addListSelectionListener(e -> displayBookDetails());

        displayBooks();
        setVisible(true);
    }

    private void loadBooksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("books.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                books.add(Book.fromString(line));
            }
        } catch (IOException e) {
            System.out.println("No existing database found. Starting fresh.");
        }
    }

    private void saveBooksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("notepad.txt"))) {
            for (Book book : books) {
                writer.write(book.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addBook() {
        JTextField titleField = new JTextField();
        JTextField authorField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JFileChooser fileChooser = new JFileChooser();

        String[] options = {"Yes", "No"};
        int discontinuedIndex = JOptionPane.showOptionDialog(null, new JComponent[]{
                new JLabel("Title:"), titleField,
                new JLabel("Author:"), authorField,
                new JLabel("Price:"), priceField,
                new JLabel("Stock:"), stockField,
                new JLabel("Select Image:"), fileChooser,
                new JLabel("Is the book discontinued?:"), 
        }, "Add Book", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[1]);

        if (discontinuedIndex != -1) {
            String title = titleField.getText();
            String author = authorField.getText();
            double price = Double.parseDouble(priceField.getText());
            int stock = Integer.parseInt(stockField.getText());
            boolean isDiscontinued = discontinuedIndex == 0;
            String imagePath = fileChooser.getSelectedFile() != null ? fileChooser.getSelectedFile().getPath() : "";

            books.add(new Book(title, author, price, stock, isDiscontinued, imagePath));
            saveBooksToFile();
            displayBooks();
        }
    }

    private void updateOrDeleteBook() {
        String title = JOptionPane.showInputDialog("Enter the title of the book to update or delete:");
        if (title == null || title.isEmpty()) return;

        for (Book book : books) {
            if (book.title.equalsIgnoreCase(title)) {
                String[] options = {"Update", "Delete"};
                int choice = JOptionPane.showOptionDialog(null, "What would you like to do?", "Update or Delete Book",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                if (choice == 0) {
                    JTextField titleField = new JTextField(book.title);
                    JTextField authorField = new JTextField(book.author);
                    JTextField priceField = new JTextField(String.valueOf(book.price));
                    JTextField stockField = new JTextField(String.valueOf(book.stock));
                    JFileChooser fileChooser = new JFileChooser();
                    String[] discontinuedOptions = {"Yes", "No"};
                    int discontinuedIndex = JOptionPane.showOptionDialog(null, new JComponent[]{
                            new JLabel("Title:"), titleField,
                            new JLabel("Author:"), authorField,
                            new JLabel("Price:"), priceField,
                            new JLabel("Stock:"), stockField,
                            new JLabel("Upload Image:"), fileChooser,
                            new JLabel("Is the book discontinued?:"),
                    }, "Update Book", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null,
                            discontinuedOptions, book.isDiscontinued ? discontinuedOptions[0] : discontinuedOptions[1]);

                    if (discontinuedIndex != -1) {
                        book.title = titleField.getText();
                        book.author = authorField.getText();
                        book.price = Double.parseDouble(priceField.getText());
                        book.stock = Integer.parseInt(stockField.getText());
                        book.isDiscontinued = discontinuedIndex == 0;
                        book.imagePath = fileChooser.getSelectedFile() != null ? fileChooser.getSelectedFile().getPath() : book.imagePath;
                        JOptionPane.showMessageDialog(this, "Book updated successfully.");
                    }
                } else if (choice == 1) {
                    books.remove(book);
                    JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                }
                saveBooksToFile();
                displayBooks();
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Book not found.");
    }

    private void viewDiscontinuedBooks() {
        DefaultTableModel discontinuedModel = new DefaultTableModel(new String[]{"Title", "Author", "Price", "Stock", "Discontinued", "Image"}, 0);
        for (Book book : books) {
            if (book.isDiscontinued) {
                discontinuedModel.addRow(new Object[]{book.title, book.author, book.price, book.stock, "Yes", book.imagePath});
            }
        }
        table.setModel(discontinuedModel);
    }

    private void displayBooks() {
        table.setModel(tableModel);
        tableModel.setRowCount(0); // Clear table
        for (Book book : books) {
            tableModel.addRow(new Object[]{book.title, book.author, book.price, book.stock, book.isDiscontinued ? "Yes" : "No", book.imagePath});
        }
    }

    private void searchBooks(String query) {
        if (query == null || query.isEmpty()) return;

        DefaultTableModel searchModel = new DefaultTableModel(new String[]{"Title", "Author", "Price", "Stock", "Discontinued", "Image"}, 0);
        for (Book book : books) {
            if ((book.title.toLowerCase().contains(query.toLowerCase()) ||
                book.author.toLowerCase().contains(query.toLowerCase())) && !book.isDiscontinued) {
                searchModel.addRow(new Object[]{book.title, book.author, book.price, book.stock, book.isDiscontinued ? "Yes" : "No", book.imagePath });
            }
        }
        table.setModel(searchModel);
    }

    private void displayBookDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            imageLabel.setIcon(null);
            imageLabel.setText("No Image");
            detailsArea.setText("");
            return;
        }

        String imagePath = table.getValueAt(selectedRow, 5).toString();
        String details = "Title:                  " + table.getValueAt(selectedRow, 0) + "\n"
                + "Author:              " + table.getValueAt(selectedRow, 1) + "\n"
                + "Price:                Php " + table.getValueAt(selectedRow, 2) + "\n"
                + "Stock:               " + table.getValueAt(selectedRow, 3) + "\n"
                + "Discontinued:   " + table.getValueAt(selectedRow, 4);
        detailsArea.setText(details);

        if (!imagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaledImage = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setText("");
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("No Image");
        }
    }

    private void sortBooks(String criterion) {
        Comparator<Book> comparator = criterion.equals("title") ?
                Comparator.comparing(book -> book.title.toLowerCase()) :
                Comparator.comparingDouble(book -> book.price);

        books.sort(comparator);
        saveBooksToFile();
        displayBooks();
    }

    public static void main(String[] args) {
        new BookstoreInventoryManagement();
    }
}