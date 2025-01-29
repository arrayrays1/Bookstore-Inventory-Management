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
    // Book class that holds information about each book
    String title;
    String author;
    double price;
    int stock;
    boolean isDiscontinued;
    String imagePath;

    // Constructor for initializing a book object
    public Book(String title, String author, double price, int stock, boolean isDiscontinued, String imagePath) {
        this.title = title;
        this.author = author;
        this.price = price;
        this.stock = stock;
        this.isDiscontinued = isDiscontinued;
        this.imagePath = imagePath;
    }

    // Converts the book object to a string format for file saving
    @Override
    public String toString() {
        return title + "," + author + "," + price + "," + stock + "," + isDiscontinued + "," + imagePath;
    }

    // Creates a book object from a string format (e.g., from a file)
    public static Book fromString(String bookString) {
        String[] parts = bookString.split(",");
        return new Book(parts[0], parts[1], Double.parseDouble(parts[2]),
                Integer.parseInt(parts[3]), Boolean.parseBoolean(parts[4]), parts.length > 5 ? parts[5] : "");
    }
}

public class BookstoreInventoryManagement extends JFrame {
    private final List<Book> books = new ArrayList<>();  // List of books in the inventory
    private final DefaultTableModel tableModel;  // Table model for the book table
    private final JTable table;  // JTable to display books
    private final JLabel imageLabel;  // Label to display the book image preview
    private final JTextArea detailsArea;  // TextArea to display book details

    public BookstoreInventoryManagement() {
        loadBooksFromFile();  // Load existing books from the file

        setTitle("Bookstore Inventory Management");
        setSize(1100, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Gradient background for the main panel
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

        // Table panel setup with column names
        String[] columnNames = {"Title", "Author", "Price", "Stock", "Discontinued", "Image"};
        tableModel = new DefaultTableModel(columnNames, 0);  // Table model for book data
        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                int stock = Integer.parseInt(getValueAt(row, 3).toString());
                boolean isDiscontinued = getValueAt(row, 4).toString().equalsIgnoreCase("Yes");

                // Change background color based on stock and discontinued status
                if (stock == 0) {
                    c.setBackground(Color.ORANGE);  // Stock is zero, highlight in orange
                } else if (isDiscontinued) {
                    c.setBackground(new Color(238, 114, 114));  // Discontinued book, highlight in red
                } else {
                    c.setBackground(new Color(136, 231, 136));  // Available book, highlight in green
                }
                c.setForeground(Color.BLACK);  // Set text color to black
                return c;
            }
        };
        table.setFillsViewportHeight(true);
        table.setBackground(new Color(253, 238, 217));

        JScrollPane scrollPane = new JScrollPane(table);  // Scroll pane for the table
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Book Image Preview panel setup
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setPreferredSize(new Dimension(300, 0));
        previewPanel.setBorder(BorderFactory.createTitledBorder("Book Preview"));
        mainPanel.add(previewPanel, BorderLayout.EAST);

        // Image preview label
        imageLabel = new JLabel("No Image", SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(300, 300));
        previewPanel.add(imageLabel, BorderLayout.NORTH);

        // Book details text area
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        previewPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);

        // Search panel for searching books
        JPanel searchPanel = new JPanel();
        searchPanel.setOpaque(false);
        JTextField searchField = new JTextField(20);
        searchField.setBorder(new EmptyBorder(4, 4, 4, 4));
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Buttons for adding, updating, and sorting books
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        JButton addButton = new JButton("Add Book");
        JButton updateBookButton = new JButton("Update/Delete Book");
        JButton viewOutOfStockButton = new JButton("Track Out of Stock");
        JButton displayAllButton = new JButton("Display All Books");
        JButton sortTitleButton = new JButton("Sort by Title");
        JButton sortPriceButton = new JButton("Sort by Price");

        buttonPanel.add(addButton);
        buttonPanel.add(updateBookButton);
        buttonPanel.add(viewOutOfStockButton); 
        buttonPanel.add(displayAllButton);
        buttonPanel.add(sortTitleButton);
        buttonPanel.add(sortPriceButton);

        // Action listeners for buttons
        addButton.addActionListener(e -> addBook());
        updateBookButton.addActionListener(e -> updateOrDeleteBook());
        searchButton.addActionListener(e -> searchBooks(searchField.getText()));
        viewOutOfStockButton.addActionListener(e -> viewOutOfStockBooks());        displayAllButton.addActionListener(e -> displayBooks());
        sortTitleButton.addActionListener(e -> sortBooks("title"));
        sortPriceButton.addActionListener(e -> sortBooks("price"));

        // Table row selection listener to display book details
        table.getSelectionModel().addListSelectionListener(e -> displayBookDetails());

        // Display all books at the start
        displayBooks();
        setVisible(true);
    }

    // Loads books from the "books.txt" file
    private void loadBooksFromFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader("books.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                books.add(Book.fromString(line));  // Parse and add each book to the list
            }
        } catch (IOException e) {
            System.out.println("No existing database found. Starting fresh.");
        }
    }

    // Saves books to the "books.txt" file
    private void saveBooksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("books.txt"))) {
            for (Book book : books) {
                writer.write(book.toString());  // Write each book's details to file
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Add a new book to the inventory
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
            saveBooksToFile();  // Save the updated list of books to the file
            displayBooks();  // Update the display
        }
    }

    // Update or delete an existing book
    private void updateOrDeleteBook() {
        String title = JOptionPane.showInputDialog("Enter the title of the book to update or delete:");
        if (title == null || title.isEmpty()) return;

        for (Book book : books) {
            if (book.title.equalsIgnoreCase(title)) {
                String[] options = {"Update", "Delete"};
                int choice = JOptionPane.showOptionDialog(null, "What would you like to do?", "Update or Delete Book",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                if (choice == 0) {
                    // Update book details using input fields
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
                        // Update book details based on user input
                        book.title = titleField.getText();
                        book.author = authorField.getText();
                        book.price = Double.parseDouble(priceField.getText());
                        book.stock = Integer.parseInt(stockField.getText());
                        book.isDiscontinued = discontinuedIndex == 0;
                        book.imagePath = fileChooser.getSelectedFile() != null ? fileChooser.getSelectedFile().getPath() : book.imagePath;
                        JOptionPane.showMessageDialog(this, "Book updated successfully.");
                    }
                } else if (choice == 1) {
                    // Delete the selected book from the inventory
                    books.remove(book);
                    JOptionPane.showMessageDialog(this, "Book deleted successfully.");
                }
                saveBooksToFile();  // Save the updated list of books to the file
                displayBooks();  // Update the display
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Book not found.");
    }
    // This method filters and displays only the books that are out of stock.
    private void viewOutOfStockBooks() {
        // Create a new table model for out of stock books.
        DefaultTableModel outOfStockModel = new DefaultTableModel(new String[]{"Title", "Author", "Price", "Stock", "Discontinued", "Image"}, 0);
        // Loop through all books to check if the stock is zero.
        for (Book book : books) {
            if (book.stock == 0) {
                // Add the out of stock book to the model.
                outOfStockModel.addRow(new Object[]{book.title, book.author, book.price, book.stock, book.isDiscontinued ? "Yes" : "No", book.imagePath});
            }
        }
        // Set the table's model to show out of stock books.
        table.setModel(outOfStockModel);
    }

    // This method displays all books in the inventory.
    private void displayBooks() {
        // Set the table model for the inventory.
        table.setModel(tableModel);
        tableModel.setRowCount(0); // Clear the table before adding new rows.
        // Loop through all books and display them.
        for (Book book : books) {
            tableModel.addRow(new Object[]{book.title, book.author, book.price, book.stock, book.isDiscontinued ? "Yes" : "No", book.imagePath});
        }
    }

    // This method allows for searching books by title or author.
    private void searchBooks(String query) {
        // Check if the search query is valid.
        if (query == null || query.isEmpty()) return;

        // Create a new table model for the search results.
        DefaultTableModel searchModel = new DefaultTableModel(new String[]{"Title", "Author", "Price", "Stock", "Discontinued", "Image"}, 0);
        // Loop through all books and add matching books to the search model.
        for (Book book : books) {
            if ((book.title.toLowerCase().contains(query.toLowerCase()) ||
                book.author.toLowerCase().contains(query.toLowerCase())) && !book.isDiscontinued) {
                searchModel.addRow(new Object[]{book.title, book.author, book.price, book.stock, book.isDiscontinued ? "Yes" : "No", book.imagePath });
            }
        }
        // Set the table's model to display the search results.
        table.setModel(searchModel);
    }

    // This method displays detailed information for the selected book.
    private void displayBookDetails() {
        // Get the index of the selected row in the table.
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            // If no book is selected, clear the details display.
            imageLabel.setIcon(null);
            imageLabel.setText("No Image");
            detailsArea.setText("");
            return;
        }

        // Get the image path and details for the selected book.
        String imagePath = table.getValueAt(selectedRow, 5).toString();
        String details = "Title:                  " + table.getValueAt(selectedRow, 0) + "\n"
                + "Author:              " + table.getValueAt(selectedRow, 1) + "\n"
                + "Price:                Php " + table.getValueAt(selectedRow, 2) + "\n"
                + "Stock:               " + table.getValueAt(selectedRow, 3) + "\n"
                + "Discontinued:   " + table.getValueAt(selectedRow, 4);
        // Set the details area text.
        detailsArea.setText(details);

        // Display the image of the book if it exists.
        if (!imagePath.isEmpty()) {
            ImageIcon icon = new ImageIcon(imagePath);
            Image scaledImage = icon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
            imageLabel.setText("");
        } else {
            // If no image, set a placeholder text.
            imageLabel.setIcon(null);
            imageLabel.setText("No Image");
        }
    }

    // This method sorts the books based on a given criterion (either by title or by price).
    private void sortBooks(String criterion) {
        // Create a comparator based on the sorting criterion.
        Comparator<Book> comparator = criterion.equals("title") ?
                Comparator.comparing(book -> book.title.toLowerCase()) :
                Comparator.comparingDouble(book -> book.price);

        // Sort the books using the selected comparator.
        books.sort(comparator);
        // Save the updated book list to file.
        saveBooksToFile();
        // Refresh the display to show the sorted list.
        displayBooks();
    }

    // The main method initializes the bookstore inventory management application.
    public static void main(String[] args) {
        // Create a new instance of the application.
        new BookstoreInventoryManagement();
    }
}