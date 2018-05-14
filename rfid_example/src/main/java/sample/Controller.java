package sample;

import com.alien.enterpriseRFID.reader.AlienClass1Reader;
import com.alien.enterpriseRFID.reader.AlienReaderConnectionException;
import com.alien.enterpriseRFID.reader.AlienReaderNotValidException;
import com.alien.enterpriseRFID.reader.AlienReaderTimeoutException;
import com.alien.enterpriseRFID.tags.Tag;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public JFXTreeTableView tableView;
    public TreeTableColumn<DataRfid, String> colUHFId;
    public TreeTableColumn<DataRfid, String> colName;

    private TreeItem<DataRfid> root = new TreeItem<DataRfid>(new DataRfid("Rfid", "Name"));

    private AlienClass1Reader reader;



    @Override
    public void initialize(URL location, ResourceBundle resources) {

//        tableView.setRoot(root);
//        tableView.setShowRoot(false);
//
//        reader = new AlienClass1Reader("192.168.1.121", 23);
//        reader.setUsername("alien");
//        reader.setPassword("password");
//
//
//        try {
//            reader.open();
//        } catch (AlienReaderNotValidException e) {
//            e.printStackTrace();
//        } catch (AlienReaderTimeoutException e) {
//            e.printStackTrace();
//        } catch (AlienReaderConnectionException e) {
//            e.printStackTrace();
//        }
//
//        Service<Void> service = new Service<Void>() {
//            @Override
//            protected Task<Void> createTask() {
//                return new Task<Void>() {
//                    @Override
//                    protected Void call() throws Exception {
//
//                        do {
//                            Tag tagList[] = reader.getTagList();
//                            if(tagList == null) {
//                                System.out.println("Not tag");
//                            } else {
//                                for(Tag tag : tagList) {
//                                    Platform.runLater(() -> {
//                                        String tagID = tag.getTagID().replace(" ", "");
//                                        TreeItem<DataRfid> findRfid = new TreeItem<>(new DataRfid(tagID, ""));
//                                        TreeItem<DataRfid> underRfid = new TreeItem<>(new DataRfid(tagID, ""));
//
//                                        findRfid.getChildren().addAll(underRfid);
//                                        root.getChildren().addAll(findRfid);
//
//                                        colUHFId.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
//                                            public SimpleStringProperty call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
//                                                return param.getValue().getValue().getRfidProperty();
//                                            }
//                                        });
//
//                                        colName.setCellValueFactory(new Callback<TreeTableColumn.CellDataFeatures<DataRfid, String>, ObservableValue<String>>() {
//                                            @Override
//                                            public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<DataRfid, String> param) {
//                                                return param.getValue().getValue().getNameProperty();
//                                            }
//                                        });
//                                    });
//                                }
//                            }
//
//
//                        }while(reader.isOpen());
//
//                        return null;
//                    }
//                };
//            }
//        };
//
//        service.start();

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
}
