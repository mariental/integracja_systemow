import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Main extends JFrame {

    private static String oldValue = null;

    private static ArrayList<Integer> duplicatesRowsNumbers = new ArrayList<>();
    private static ArrayList<Integer> editedRowsNumbers = new ArrayList<>();

    public static DefaultTableModel readDataFromTxt(File file, DefaultTableModel model, JTable table) {
        try {
            Scanner fileScanner = new Scanner(file).useDelimiter("\n");
            int lineNumber = 1;
            while(fileScanner.hasNextLine()){
                String line = fileScanner.nextLine();
                Scanner lineScanner = new Scanner(line).useDelimiter(";");
                while(lineScanner.hasNext()){
                    String[] rowToSave = new String[16];
                    rowToSave[0] = (Integer.toString(lineNumber));
                    for(int i=1; i < 16; i++){
                        String token = lineScanner.next();
                        if(token.isEmpty()){
                            rowToSave[i] = "brak danych";
                        } else {
                            rowToSave[i] = (token);
                        }
                    }
                    if(checkDuplicates(model, rowToSave)){
                        duplicatesRowsNumbers.add(model.getRowCount());
                    }
                    model.addRow(rowToSave);
                }
                lineNumber++;
            }
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }
        return model;
    }

    public static void saveFileToTxt(DefaultTableModel model, Frame frame) {
        try {
            FileWriter fileWriter = new FileWriter("src/main/resources/katalog.txt");
            BufferedWriter bufferWriter = new BufferedWriter(fileWriter);
            for(int i=0; i < model.getRowCount(); i++){
                if(!duplicatesRowsNumbers.contains(i)){
                    for(int j=1; j < model.getColumnCount(); j++){
                        String cell = model.getValueAt(i, j).toString();
                        if (cell.equals("brak danych")) {
                            cell = "";
                        }
                        bufferWriter.write(cell);
                        bufferWriter.write(";");
                    }
                }
                bufferWriter.newLine();
            }
            bufferWriter.close();
            fileWriter.close();
            JOptionPane.showMessageDialog(frame, "Plik został pomyślnie zapisany");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Błąd podczas zapisu pliku");
        }
    }

    public static DefaultTableModel saveRow(Node node, DefaultTableModel model){
        NodeList childNodes = node.getChildNodes();
        String[] row = new String[16];
        Element mainElement = (Element)node;
        row[0] = mainElement.getAttribute("id");
        int counter = 1;
        for(int i=0; i<childNodes.getLength(); i++) {
            if(counter == 5 || counter == 11) {
                counter++;
            }
            Node childNode = childNodes.item(i);
            if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element)childNode;
                if(element.getNodeName().equals("screen")) {
                    if(element.hasAttribute("touch")) {
                        if(element.getAttribute("touch").equals("no")) {
                            row[5] = "nie";
                        } else {
                            row[5] = "tak";
                        }
                    }
                    else{
                        row[5] = "brak danych";
                    }
                }
                else if(element.getNodeName().equals("disc")) {
                    if (element.hasAttribute("type")) {
                        row[11] = element.getAttribute("type");
                    }
                    else {
                        row[11]  ="brak danych";
                    }
                }
                if (childNode.hasChildNodes() && childNode.getChildNodes().getLength() > 1) {
                    NodeList childNodes2 = childNode.getChildNodes();
                    for(int j=0; j<childNodes2.getLength(); j++){
                        Node nextChildNode = childNodes2.item(j);
                        if(nextChildNode.getNodeType() == Node.ELEMENT_NODE) {
                            if(!nextChildNode.getTextContent().trim().isEmpty()) {
                                row[counter] = nextChildNode.getTextContent();
                            } else {
                                row[counter] = "brak danych";
                            }
                            counter++;
                        }
                    }
                }
                else {
                    if(!childNode.getTextContent().trim().isEmpty()) {
                        row[counter] = childNode.getTextContent();
                    } else {
                        row[counter] = "brak danych";
                    }
                    counter++;
                }
            }
        }
        if(checkDuplicates(model, row)){
            duplicatesRowsNumbers.add(model.getRowCount());
        }
        model.addRow(row);
        return model;
    }

    public static DefaultTableModel readDataFromXml(File file, DefaultTableModel model) {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(file);
            doc.getDocumentElement().normalize();
            NodeList laptopNodes = doc.getElementsByTagName("laptop");
            for(int i=0; i<laptopNodes.getLength(); i++){
                Node childNode = laptopNodes.item(i);
                if(childNode.getNodeType() == Node.ELEMENT_NODE) {
                    model = saveRow(childNode, model);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }
        return model;
    }

    public static void saveFileToXml(DefaultTableModel model, Frame frame) {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.newDocument();
            Element rootElement = doc.createElement("laptops");
            Attr attr = doc.createAttribute("moddate");
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            String formattedDate = myDateObj.format(myFormatObj);
            attr.setValue(formattedDate);
            rootElement.setAttributeNode(attr);
            doc.appendChild(rootElement);
            for(int i=0; i < model.getRowCount(); i++){
                if(!duplicatesRowsNumbers.contains(i)){
                    Element laptop = doc.createElement("laptop");
                    rootElement.appendChild(laptop);
                    int counter = 0;
                    for(int j=0; j < model.getColumnCount(); j++){
                        String cell = model.getValueAt(i, j).toString();
                        Element element = null;
                        if (cell.equals("brak danych")) {
                            cell = "";
                        }
                        if(j == 0) {
                            Attr id = doc.createAttribute("id");
                            id.setValue(cell);
                            laptop.setAttributeNode(id);
                        }
                        if(j == 1 || j == 9 || j == 14 || j == 15 ) {
                            String[] names = {"manufacturer", "ram", "os", "disc_reader"};
                            element = doc.createElement(names[counter]);
                            element.appendChild(doc.createTextNode(cell));
                            counter++;
                        }
                        if(j == 5) {
                            element = doc.createElement("screen");
                            Attr screen = doc.createAttribute("touch");
                            if(cell.equals("nie")) {
                                screen.setValue("no");
                            } else {
                                screen.setValue("yes");
                            }
                            element.setAttributeNode(screen);
                            String[] names = {"size", "resolution", "type"};
                            for(int k = 0; k < 3; k++) {
                                Element element2 = doc.createElement(names[k]);
                                element2.appendChild(doc.createTextNode(model.getValueAt(i, k+2).toString()));
                                element.appendChild(element2);
                            }
                        }
                        if(j == 11) {
                            element = doc.createElement("disc");
                            if(!cell.equals("")){
                                Attr type = doc.createAttribute("type");
                                type.setValue(cell);
                                element.setAttributeNode(type);
                            }
                            Element element2 = doc.createElement("storage");
                            element2.appendChild(doc.createTextNode(model.getValueAt(i, 10).toString()));
                            element.appendChild(element2);
                        }
                        if(j == 12){
                            element = doc.createElement("graphic_card");
                            String[] names = {"name", "memory"};
                            for(int k = 0; k < 2; k++) {
                                Element element2 = doc.createElement(names[k]);
                                element2.appendChild(doc.createTextNode(model.getValueAt(i, k+12).toString()));
                                element.appendChild(element2);
                            }
                        }
                        if(j == 6) {
                            element = doc.createElement("processor");
                            String[] names = {"name", "physical_cores", "clock_speed"};
                            for(int k = 0; k < 3; k++) {
                                Element element2 = doc.createElement(names[k]);
                                element2.appendChild(doc.createTextNode(model.getValueAt(i, k+6).toString()));
                                element.appendChild(element2);
                            }
                        }
                        if(element != null) {
                            laptop.appendChild(element);
                        }
                    }
                }
            }
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File("src/main/resources/katalog.xml"));
            transformer.transform(source, result);
            JOptionPane.showMessageDialog(frame, "Plik został pomyślnie zapisany");
        } catch (ParserConfigurationException e) {
            JOptionPane.showMessageDialog(frame, "Błąd podczas zapisu pliku");
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }

    }

    public static Connection connectToDb() {
        Connection connection;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/integracja", "root", "Junikorn69@");
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static DefaultTableModel readDataFromDb(DefaultTableModel model, Connection connection) {
        Statement statement;
        ResultSet resultSet;
        ResultSetMetaData resultSetMetaData;
        int columnCounter;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("SELECT * FROM laptops");
            resultSetMetaData = resultSet.getMetaData();
            columnCounter = resultSetMetaData.getColumnCount();
            while (resultSet.next()) {
                String[] row = new String[16];
                for(int i=1; i<=columnCounter; i++) {
                    if(resultSetMetaData.getColumnType(i) == 12) {
                        if(resultSet.getString(i) == null) {
                            row[i-1] = ("brak danych");
                        } else {
                            row[i-1] = (resultSet.getString(i));
                        }
                    } else {
                        int data = resultSet.getInt(i);
                        if (resultSetMetaData.getColumnName(i).equals("isTouchable")) {
                            if(data == 0){
                                row[i-1] = ("nie");
                            } else if (data == 1) {
                                row[i-1] = ("tak");
                            } else {
                                row[i-1] = ("brak danych");
                            }
                        } else if (data == 0){
                            row[i-1] = ("brak danych");
                        } else if(resultSetMetaData.getColumnName(i).equals("matrixSize")) {
                            row[i-1] = (data + "\"");
                        } else if (resultSetMetaData.getColumnName(i).equals("ram") | resultSetMetaData.getColumnName(i).equals("discStorage") |resultSetMetaData.getColumnName(i).equals("graphicCardMemory")) {
                            row[i-1] = (data + "GB");
                        } else {
                          row[i-1] = (String.valueOf(data));
                        }
                    }
                }
                if(checkDuplicates(model, row)){
                    duplicatesRowsNumbers.add(model.getRowCount());
                }
                model.addRow(row);
            }
            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return model;
    }

    public static void saveDataToDb(DefaultTableModel model, Connection connection) {
        for(int i=0; i < model.getRowCount(); i++){
            if(!duplicatesRowsNumbers.contains(i)){
                StringBuilder queryBuilder = new StringBuilder("INSERT INTO `laptops`(`producer`, `matrixSize`, `resolution`, `matrixType`, `isTouchable`, `processorName`, `physicalCores`, `clockSpeed`, `ram`, `discStorage`, `discType`, `graphicCardName`, `graphicCardMemory`, `operationSystem`, `discReader`) VALUES(");
                for(int j=1; j < model.getColumnCount(); j++){
                    String value = model.getValueAt(i, j).toString();
                    int[] intValues = {2, 7, 8, 9, 10, 13};
                    int finalJ = j;
                    if (value.equals("brak danych")) {
                        queryBuilder.append("NULL");
                    } else if (Arrays.stream(intValues).anyMatch(k -> k == finalJ)) {
                        value = value.replaceAll("[^\\d.]", "");
                        queryBuilder.append(value);
                    } else if (j == 5) {
                        if(value.equals("nie")){
                            queryBuilder.append(0);
                        } else {
                            queryBuilder.append(1);
                        }
                    } else {
                        queryBuilder.append("\""+ model.getValueAt(i, j) + "\"");
                    }
                    if(j != model.getColumnCount()-1){
                        queryBuilder.append(", ");
                    }
                }
                queryBuilder.append(")");
                String query = queryBuilder.toString();
                try {
                    PreparedStatement statement = connection.prepareStatement(query);
                    statement.execute();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static boolean checkDuplicates(DefaultTableModel model, String[] row) {
        int duplicatesCounter = 0;
        for(int i=0; i < model.getRowCount(); i++){
            for(int j=1; j < model.getColumnCount(); j++) {
                if(model.getValueAt(i, j).equals(row[j])){
                    duplicatesCounter++;
                }
            }
            if(duplicatesCounter == 15) {
                return true;
            }
            duplicatesCounter = 0;
        }
        return false;
    }

    public static void main(String[] args) {
        String[] headers = {"Lp.", "Nazwa producenta", "Przekątna ekranu" ,"Rozdzielczość ekranu",
                "Rodzaj powierzchni ekranu", "Czy ekran jest dotykowy?", "Nazwa procesora",
                "Liczba rdzeni fizycznych", "Prędkość taktowania MHz", "Wielkość pamięci RAM",
                "Pojemność dysku", "Rodzaj dysku", "Nazwa układu graficznego", "Pamięć układu",
                "Nazwa systemu operacyjnego", "Rodzaj napędu fizycznego"};
        DefaultTableModel model = new DefaultTableModel(headers, 0);
        final File[] file = new File[1];
        JFrame frame = new JFrame("Integracja systemów - Katarzyna Kurek");
        JTable table = new JTable();
        JLabel infoText = new JLabel();
        JButton importButtonTxt = new JButton("Importuj z pliku txt");
        JButton exportButtonTxt = new JButton("Eksportuj do pliku txt");
        JButton importButtonXml = new JButton("Importuj z pliku xml");
        JButton exportButtonXml = new JButton("Eksportuj do pliku xml");
        JButton importFromDatabase = new JButton("Importuj z bazy danych");
        JButton exportToDatabase = new JButton("Eksportuj do bazy danych");
        JButton clearTable = new JButton("Wyczyść tabelę");
        Connection conn = connectToDb();
        table.setDefaultRenderer(Object.class, new RowRenderer(true, duplicatesRowsNumbers, editedRowsNumbers));

        importButtonTxt.addActionListener(e -> {
            file[0] = new File("src/main/resources/katalog.txt");
            if(file[0].exists()){
                table.setModel(readDataFromTxt(file[0],model, table));
                if(model.getRowCount() == 0){
                    infoText.setText("Brak danych do wczytania");
                } else {
                    infoText.setText("Dane wczytane z pliku txt - nowe rekordy: " + (model.getRowCount()-duplicatesRowsNumbers.size()) + ", duplikaty: " + duplicatesRowsNumbers.size());
                }
            } else {
                infoText.setText("Brak pliku");
            }
        });
        importButtonXml.addActionListener(e -> {
            file[0] = new File("src/main/resources/katalog.xml");
            if(file[0].exists()) {
                table.setModel(readDataFromXml(file[0],model));
                if(model.getRowCount() == 0){
                    infoText.setText("Brak danych do wczytania");
                } else {
                    infoText.setText("Dane wczytane z pliku xml - nowe rekordy: " + (model.getRowCount()-duplicatesRowsNumbers.size()) + ", duplikaty: " + duplicatesRowsNumbers.size());
                }
            } else {
                infoText.setText("Brak pliku");
            }
        });
        exportButtonTxt.addActionListener(e -> {
            saveFileToTxt(model, frame);
        });
        exportButtonXml.addActionListener(e -> {
            saveFileToXml(model, frame);
        });
        importFromDatabase.addActionListener(e -> {
            table.setModel(readDataFromDb(model, conn));
            if(model.getRowCount() == 0){
                infoText.setText("Brak danych do wczytania");
            } else {
                infoText.setText("Dane wczytane z bazy danych - nowe rekordy: " + (model.getRowCount()-duplicatesRowsNumbers.size()) + ", duplikaty: " + duplicatesRowsNumbers.size());
            }
        });
        exportToDatabase.addActionListener(e -> {
            saveDataToDb(model, conn);
        });
        clearTable.addActionListener(e -> {
            duplicatesRowsNumbers.clear();
            editedRowsNumbers.clear();
            model.setRowCount(0);
            infoText.setText("");
        });
        table.setCellSelectionEnabled(true);
        table.addPropertyChangeListener("tableCellEditor", e -> {
            if(table.isEditing()){
                oldValue = model.getValueAt(table.getSelectedRow(), table.getSelectedColumn()).toString();
            }
            if(!table.isEditing() & oldValue != null){
                String newValue = model.getValueAt(table.getEditingRow(), table.getEditingColumn()).toString().trim();
                if(!Objects.equals(oldValue, newValue)) {
                    editedRowsNumbers.add(table.getEditingRow());
                    table.getColumnModel().getColumn(table.getEditingColumn()).setCellRenderer(new CellRenderer(table.getEditingRow(), duplicatesRowsNumbers, editedRowsNumbers));
                    model.fireTableDataChanged();
                }
            }
        });
        frame.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(table);
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        frame.setSize(1600,800);
        panel.add(importButtonTxt);
        panel.add(exportButtonTxt);
        panel.add(importButtonXml);
        panel.add(exportButtonXml);
        panel.add(importFromDatabase);
        panel.add(exportToDatabase);
        panel.add(clearTable);
        panel.add(infoText);
        frame.add(panel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}
