
import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienReaderConnectionException;
import com.alien.enterpriseRFID.reader.AlienReaderNotValidException;
import com.alien.enterpriseRFID.reader.AlienReaderTimeoutException;
import com.alien.enterpriseRFID.tags.Tag;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class Controller implements Initializable {

    public JFXTextField tfldSQLId;
    public ImageView imgSettings;
    public ImageView imgBase;
    public JFXTextField tfldSQLName;
    public JFXTextField tfldPort;
    public JFXTextField tfldUsername;
    public JFXPasswordField tfldPassword;
    public JFXCheckBox chbxAccBottle;
    public JFXCheckBox chbxSimple;
    public JFXButton btnSaveSettings;
    public TreeTableView tableRfids;
    public TreeTableColumn<DataRfid, String> colUHFId;
    public TreeTableColumn<DataRfid, String> colName;
    public Label lalbelLog;
    public Pane pnlBase;
    public Pane pnlSettings;
    public Pane pnlConnect;
    public JFXTextField tfldIP;
    public Label allCount;
    public JFXCheckBox chbxTest;

    private List<String> tagListArray;
    private FileChooser fileChooser;
    private JSONParser jsonParser;
    private JSONObject jsonObject;

    private String ipMain = "";
    private int portMain = 0;
    private String usernameMain = "";
    private String passwordMain = "";

    private AlienClass1Reader reader = new AlienClass1Reader();
    private boolean isStarting = false, isConnectingOpen = false, isSettingsOpen = false;

    private TreeItem<DataRfid> root = new TreeItem<DataRfid>(new DataRfid("Rfid", "Name"));

    private SQLConnectBase sqlConnectBase;

    private File fileExcel = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //pnlBase.toFront();
        tagListArray = new ArrayList<>();
        fileChooser = new FileChooser();

        colUHFId.setStyle("-fx-alignment: CENTER; -fx-padding: 10px");
        colName.setStyle("-fx-alignment: CENTER; -fx-padding: 10px");

        try {
            String path = File.createTempFile("temp-file", "tmp").getParent() + "\\data_user.json";
            File f_ex = new File(path);
            if (f_ex.exists()) {

                jsonParser = new JSONParser();
                Object object = jsonParser.parse(new FileReader(path));
                jsonObject = (JSONObject) object;

                String IP = (String) jsonObject.get("ip");
                String Port = (String) jsonObject.get("port");
                String username = (String) jsonObject.get("username");
                String password = (String) jsonObject.get("password");
                String check = (String) jsonObject.get("chbxAccBottle");
                String checkSample = (String) jsonObject.get("checkSample");

                tfldIP.setText(IP);
                tfldPort.setText(Port);
                tfldUsername.setText(username);
                tfldPassword.setText(password.toString());

                if (check.equals("true") && checkSample.equals("false")) {
                    chbxAccBottle.setSelected(true);
                    chbxSimple.setSelected(false);
                } else if (checkSample.equals("true") && check.equals("false")) {
                    chbxSimple.setSelected(true);
                    chbxAccBottle.setSelected(false);
                } else {
                    chbxAccBottle.setSelected(false);
                    chbxSimple.setSelected(false);
                }

                ipMain = IP;
                portMain = Integer.parseInt(Port);
                usernameMain = username;
                passwordMain = password;

            }
        } catch (IOException | ParseException e) {

        }

        tableRfids.setRoot(root);
        tableRfids.setShowRoot(false);
    }

    public void clckConnection(ActionEvent actionEvent) {
        if (!isConnectingOpen) {
            if (isSettingsOpen) {
                isSettingsOpen = false;
                pnlSettings.setVisible(false);
                pnlSettings.toBack();
            }
            pnlConnect.setVisible(true);
            pnlConnect.toFront();
            isConnectingOpen = true;
        } else {
            isConnectingOpen = false;
            pnlConnect.setVisible(false);
            pnlConnect.toBack();
        }
    }

    public void clckChoosFile(ActionEvent actionEvent) {
        fileExcel = fileChooser.showOpenDialog(null);
    }

    public void clckSendData(ActionEvent actionEvent) throws IOException {
        if (fileExcel != null) {
            FileInputStream file = new FileInputStream(new File(String.valueOf(fileExcel)));
            XSSFWorkbook workbook = new XSSFWorkbook(file);
            XSSFSheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.rowIterator();
            List<String> mainRfids = new ArrayList<>();
            if (!tfldSQLId.getText().toString().equals("") && !tfldSQLName.getText().toString().equals("")) {
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Cell cell = row.getCell(0);
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_NUMERIC:
                            break;


                        case Cell.CELL_TYPE_STRING:

                            try {
                                if (sqlConnectBase == null) {
                                    sqlConnectBase = new SQLConnectBase("root", "9704112");
                                    sqlConnectBase.connect();
                                }
                                if (!sqlConnectBase.isOpen()) {
                                    sqlConnectBase.connect();
                                }
                            } catch (SQLException e) {
                                lalbelLog.setVisible(true);
                                lalbelLog.setText("Cannot close connection BD!");
                                return;
                            }

                            if (!mainRfids.contains(tfldSQLId.getText().toString())) {
                                mainRfids.add(tfldSQLId.getText().toString());
                                try {
                                    String query = "INSERT INTO rfids_main VALUES('" + tfldSQLId.getText().toString() + "', '" + new String(tfldSQLName.getText().toString().getBytes(), "UTF-8") + "');";
                                    sqlConnectBase.InsertRfids_main(query);

                                } catch (SQLException e) {
                                    lalbelLog.setVisible(true);
                                    lalbelLog.setText("Cannot insert to BD! Check your data or excel file");
                                    return;
                                }
                            }
                            try {
                                String query = "INSERT INTO rfid_under_main VALUES('" + cell.getStringCellValue() + "', '" + tfldSQLId.getText().toString() + "');";
                                sqlConnectBase.InsertRfids_under_main(query);
                            } catch (SQLException e) {
                                lalbelLog.setVisible(true);
                                lalbelLog.setText("Cannot insert to BD! Check your data or excel file");
                                return;
                            }
                            try {
                                sqlConnectBase.close();
                            } catch (Exception e) {

                            }

                            break;

                    }
                }
            }

            workbook.close();

        }
        clearBase();
    }

    public void clckBaseStart(ActionEvent actionEvent) {
        clearBase();
    }

    public void clckSettings(ActionEvent actionEvent) {
        if (!isSettingsOpen) {
            if (isConnectingOpen) {
                isConnectingOpen = false;
                pnlConnect.setVisible(false);
                pnlConnect.toBack();
            }
            pnlSettings.setVisible(true);
            pnlSettings.toFront();
            isSettingsOpen = true;
        } else {
            isSettingsOpen = false;
            pnlSettings.setVisible(false);
            pnlSettings.toBack();
        }
    }

    public void clicAccBottle(ActionEvent actionEvent) {
        count[0] = 0;
        if (chbxAccBottle.isSelected() && chbxSimple.isSelected()) {
            chbxSimple.setSelected(false);
        }
    }

    public void clicSample(ActionEvent actionEvent) {
        count[0] = 0;
        if (chbxSimple.isSelected() && chbxAccBottle.isSelected()) {
            chbxAccBottle.setSelected(false);
        }
    }

    public void clckSaveSettings(ActionEvent actionEvent) {
        if (!tfldIP.getText().equals("") && !tfldPort.getText().equals("") && !tfldUsername.getText().equals("")) {
            try {
                jsonObject = new JSONObject();

                jsonObject.put("ip", tfldIP.getText());
                jsonObject.put("port", tfldPort.getText());
                jsonObject.put("username", tfldUsername.getText());
                jsonObject.put("password", tfldPassword.getText());
                jsonObject.put("chbxAccBottle", chbxAccBottle.isSelected() ? "true" : "false");
                jsonObject.put("checkSample", chbxSimple.isSelected() ? "true" : "false");

                String path = File.createTempFile("temp-file", "tmp").getParent() + "\\data_user.json";
                FileWriter file = new FileWriter(path);
                file.write(jsonObject.toJSONString());
                file.flush();
            } catch (Exception e) {

            }
        }

        clearBase();
    }

    private final int[] count = {0};

    public void btnStartRfidRead(ActionEvent actionEvent) throws AlienReaderTimeoutException, AlienReaderNotValidException, AlienReaderConnectionException, SQLException {

        root.getChildren().clear();

        allCount.setText("Count: ");

        if (!chbxTest.isSelected()) {

            if (!isStarting) {

                isStarting = true;
                tagListArray.clear();

                reader = new AlienClass1Reader(ipMain, portMain);
                reader.setUsername(usernameMain);
                reader.setPassword(passwordMain);

                try {
                    reader.open();
                    lalbelLog.setVisible(true);
                    lalbelLog.setText("Connect to Alien success");
                    lalbelLog.setTextFill(Color.GREEN);
                } catch (AlienReaderNotValidException | AlienReaderTimeoutException | AlienReaderConnectionException e) {
                    lalbelLog.setVisible(true);
                    lalbelLog.setText("Cannot connect to Alien!");
                    lalbelLog.setTextFill(Color.RED);
                    isStarting = false;
                    return;
                }


                try {
                    if (sqlConnectBase == null) {
                        sqlConnectBase = new SQLConnectBase("root", "9704112");
                        sqlConnectBase.connect();
                    } else if (!sqlConnectBase.isOpen()) {
                        sqlConnectBase.connect();
                    }
                } catch (SQLException e) {
                    lalbelLog.setVisible(true);
                    lalbelLog.setText("Cannot connect to DataBase MySQL!");
                    lalbelLog.setTextFill(Color.RED);
                    return;
                }
            }


            Service<Void> service = new Service<Void>() {
                @Override
                protected Task<Void> createTask() {
                    count[0] = 0;
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws ClassCastException, NullPointerException {
                            try {
                                do {
                                    Tag tagList[] = reader.getTagList();
                                    if (tagList == null) {

                                    } else {


                                        for (Tag tag : tagList) {
                                            String tagID = tag.getTagID().replace(" ", "");
                                            if (!tagListArray.contains(tagID)) {
                                                tagListArray.add(tagID);

                                                if (chbxAccBottle.isSelected()) {

                                                    boolean is_box = sqlConnectBase.isBox(tagID);
                                                    boolean is_bottle = sqlConnectBase.isBottle(tagID);

                                                    if (!is_bottle && !is_box) {

                                                        continue;
                                                    }

                                                    if (is_box) {

                                                        Platform.runLater(() -> {
                                                            try {
                                                                count[0]++;
                                                                TreeItem<DataRfid> findRfid = findRfid(root, tagID);

                                                                if (findRfid == null) {
                                                                    Map<String, String> dataRfid = sqlConnectBase.query(tagID);

                                                                    if (dataRfid != null) {
                                                                        findRfid = new TreeItem<DataRfid>(new DataRfid(tagID, dataRfid.get(tagID)));


                                                                        root.getChildren().addAll(findRfid);

                                                                        colUHFId.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                            @Override
                                                                            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                                return param.getValue().getValue().getRfidProperty();
                                                                            }
                                                                        });

                                                                        colName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                            @Override
                                                                            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                                return param.getValue().getValue().getNameProperty();
                                                                            }
                                                                        });
                                                                    }
                                                                }

                                                                allCount.setText("Count: " + count[0]);

                                                            } catch (NullPointerException | ClassCastException e) {

                                                            }
                                                        });


                                                    } else if (is_bottle) {
                                                        String main_id = sqlConnectBase.getMainInRfid(tagID);
                                                        if (main_id != null) {
                                                            Map<String, String> dataRfid = sqlConnectBase.query(main_id);

                                                            Platform.runLater(() -> {
                                                                count[0]++;
                                                                TreeItem<DataRfid> findRfid = findRfid(root, main_id);

                                                                if (findRfid == null) {
                                                                    findRfid = new TreeItem<DataRfid>(new DataRfid(main_id, dataRfid.get(main_id)));
                                                                    TreeItem<DataRfid> underRfid = new TreeItem<DataRfid>(new DataRfid(tagID, ""));

                                                                    findRfid.getChildren().addAll(underRfid);
                                                                    root.getChildren().addAll(findRfid);


                                                                    colUHFId.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                        @Override
                                                                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                            return param.getValue().getValue().getRfidProperty();
                                                                        }
                                                                    });

                                                                    colName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                        @Override
                                                                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                            return param.getValue().getValue().getNameProperty();
                                                                        }
                                                                    });

                                                                    allCount.setText("Count: " + count[0]);

                                                                } else {

                                                                    TreeItem<DataRfid> underRfid = new TreeItem<DataRfid>(new DataRfid(tagID, ""));
                                                                    findRfid.getChildren().addAll(underRfid);


                                                                    colUHFId.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                        @Override
                                                                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                            return param.getValue().getValue().getRfidProperty();
                                                                        }
                                                                    });

                                                                    colName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                        @Override
                                                                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                            return param.getValue().getValue().getNameProperty();
                                                                        }
                                                                    });

                                                                    allCount.setText("Count: " + count[0]);
                                                                }
                                                            });

                                                        }
                                                    }
                                                } else if (chbxSimple.isSelected()) {
                                                    Platform.runLater(() -> {
                                                        count[0]++;

                                                        root.getChildren().addAll(new TreeItem<>(new DataRfid(tagID, "")));

                                                        colUHFId.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                            @Override
                                                            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                return param.getValue().getValue().getRfidProperty();
                                                            }
                                                        });

                                                        allCount.setText("Count: " + count[0]);
                                                    });

                                                } else {

                                                    Platform.runLater(() -> {
                                                        try {
                                                            count[0]++;
                                                            Map<String, String> rfid_query = sqlConnectBase.query(tagID);
                                                            if (rfid_query != null) {
                                                                TreeItem<DataRfid> mainRfid = new TreeItem<DataRfid>(new DataRfid(tagID, rfid_query.get(tagID)));

                                                                List<String> underRfids = sqlConnectBase.getDataUnderRfid(tagID);

                                                                if (!underRfids.isEmpty()) {
                                                                    for (String rfid : underRfids) {

                                                                        mainRfid.getChildren().addAll(new TreeItem<DataRfid>(new DataRfid(rfid, "")));

                                                                    }

                                                                    root.getChildren().addAll(mainRfid);

                                                                    colUHFId.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                        @Override
                                                                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                            return param.getValue().getValue().getRfidProperty();
                                                                        }
                                                                    });

                                                                    colName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                                                                        @Override
                                                                        public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                                                            return param.getValue().getValue().getNameProperty();
                                                                        }
                                                                    });
                                                                }
                                                            }

                                                            allCount.setText("Count: " + count[0]);
                                                        } catch (NullPointerException e) {

                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                    Thread.sleep(100);
                                } while (isStarting);

                            } catch (Exception e) {

                            }

                            return null;
                        }
                    };
                }
            };

            if (!service.isRunning()) {
                service.start();
                lalbelLog.setVisible(true);
                lalbelLog.setText("Starting!");
                lalbelLog.setTextFill(Color.GREEN);

            } else {
                service.restart();
            }

        } else {
            if (sqlConnectBase == null) {
                sqlConnectBase = new SQLConnectBase("root", "9704112");
                sqlConnectBase.connect();
            } else {
                if (!sqlConnectBase.isOpen()) {
                    sqlConnectBase.connect();
                }
            }

            Map<String, String> main_rfids = sqlConnectBase.main_rfids();

            if (main_rfids != null) {

                final int count[] = {0};

                Platform.runLater(() -> {

                    for (String key : main_rfids.keySet()) {
                        TreeItem<DataRfid> mainID = new TreeItem<>(new DataRfid(key, main_rfids.get(key)));

                        count[0]++;

                        List<String> under_rfids = sqlConnectBase.getDataUnderRfid(key);

                        if (under_rfids != null) {
                            for (String underRFID : under_rfids) {
                                TreeItem<DataRfid> underID = new TreeItem<>(new DataRfid(underRFID, ""));

                                mainID.getChildren().add(underID);
                            }
                        }

                        root.getChildren().add(mainID);

                        colUHFId.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                            @Override
                            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                return param.getValue().getValue().getRfidProperty();
                            }
                        });

                        colName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
                            @Override
                            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
                                return param.getValue().getValue().getNameProperty();
                            }
                        });

                        allCount.setText("Count: " + count[0]);

                        lalbelLog.setText("Starting");
                        lalbelLog.setVisible(true);
                        lalbelLog.setTextFill(Color.GREEN);

                    }

                });

            }

        }


    }

    public void btnStopRfidRead(ActionEvent actionEvent) {
        try {
            if (isStarting) {
                this.isStarting = false;

                if (reader.isOpen()) {
                    reader.close();

                }


                try {
                    if (sqlConnectBase.isOpen()) {
                        sqlConnectBase.close();
                    }
                } catch (SQLException e) {
                    lalbelLog.setVisible(true);
                    lalbelLog.setText("Cannot close connection BD!");
                    lalbelLog.setTextFill(Color.RED);
                    return;
                }

                lalbelLog.setVisible(true);
                lalbelLog.setText("Stop!");
                lalbelLog.setTextFill(Color.RED);

                allCount.setText("Count: ");
            }
        } catch (Exception e) {

        }
    }

    public void clckClose(ActionEvent actionEvent) {
        Stage primaryStage = (Stage) pnlConnect.getScene().getWindow();
        primaryStage.close();
    }

    public void clickChbxTest(ActionEvent actionEvent) {
        count[0] = 0;
        if (!chbxTest.isSelected()) {
            chbxAccBottle.setSelected(false);
            chbxSimple.setSelected(false);
        }
    }

    class DataRfid extends RecursiveTreeObject<DataRfid> {
        SimpleStringProperty rfid;
        SimpleStringProperty name;

        public DataRfid(String rfid, String name) {
            this.rfid = new SimpleStringProperty(rfid);
            this.name = new SimpleStringProperty(name);
        }

        public SimpleStringProperty getNameProperty() {
            return name;
        }

        public SimpleStringProperty getRfidProperty() {
            return rfid;
        }
    }

    private TreeItem<DataRfid> findRfid(TreeItem<DataRfid> root, String tagID) {

        for (TreeItem<DataRfid> child : root.getChildren()) {
            if (child.getValue().rfid.getValue().equals(tagID))
                return child;
        }

        return null;
    }

    public void clearBase() {
        pnlConnect.setVisible(false);
        pnlConnect.toBack();
        isConnectingOpen = false;
        pnlSettings.setVisible(false);
        pnlSettings.toBack();
        isSettingsOpen = false;
        pnlBase.toFront();
    }
}
