package lk.ijse.prabhash.orm.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTextField;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lk.ijse.prabhash.orm.bo.BOFactory;
import lk.ijse.prabhash.orm.bo.custom.RegistrationBO;
import lk.ijse.prabhash.orm.dto.RoomDTO;
import lk.ijse.prabhash.orm.dto.StudentDTO;
import lk.ijse.prabhash.orm.view.tm.CartTM;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class RegistrationFormController {
    public TableColumn colRoomTypeId;
    public TableColumn colType;
    public TableColumn colKeyMoney;
    public TableColumn colPaymentStatus;
    public TableColumn colDelete;
    public JFXButton btnAddToCart;
    public JFXComboBox<String> cmbStudentId;
    public JFXComboBox<String> cmbRoomTypeId;
    public JFXButton btnRegister;
    public Label lblReservation;
    public JFXTextField txtAddress;
    public JFXTextField txtContactNo;
    public JFXTextField txtName;
    public JFXTextField txtType;
    public JFXTextField txtKeyMoney;
    public JFXTextField txtQty;
    public JFXComboBox<String> cmbPaymentStatus;
    private final RegistrationBO registrationBO = (RegistrationBO) BOFactory.getBOFactory().getBO(BOFactory.BOTypes.REGISTRATION);
    public TableView<CartTM> tblCart;

    public void initialize(){
        colRoomTypeId.setCellValueFactory(new PropertyValueFactory<>("room_type_id"));
        colKeyMoney.setCellValueFactory(new PropertyValueFactory<>("key_money"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        TableColumn<CartTM, Button> lastCol = (TableColumn<CartTM, Button>) tblCart.getColumns().get(4);
        lastCol.setCellValueFactory(param -> {
            Button btnDelete = new Button("Delete");

            btnDelete.setOnAction(event -> {
                tblCart.getItems().remove(param.getValue());
                tblCart.getSelectionModel().clearSelection();
                enableOrDisableRegisterButton();
            });

            return new ReadOnlyObjectWrapper<>(btnDelete);
        });

        setStudentId();
        setRoomId();
        generateNewId();
        


        cmbStudentId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            enableOrDisableRegisterButton();
            if (newValue != null) {
                try {
                    List<StudentDTO> list = registrationBO.getStudentDetailUsingId(newValue);
                    for (StudentDTO dto : list
                    ) {
                        txtName.setText(dto.getName());
                        txtAddress.setText(dto.getAddress());
                        txtContactNo.setText(dto.getContact_no());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else {
                txtName.clear();
                txtAddress.clear();
                txtContactNo.clear();
            }
        });

        cmbRoomTypeId.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            btnAddToCart.setDisable(newValue==null);
           if (newValue != null){
               try {
                   List<RoomDTO> list = registrationBO.getRoomDetailUsingId(newValue);
                   for (RoomDTO dto:list
                   ) {
                       txtType.setText(dto.getType());
                       Optional<CartTM> tm = tblCart.getItems().stream().filter(detail -> detail.getRoom_type_id().equals(newValue)).findFirst();

                       txtQty.setText(String.valueOf(tm.isPresent() ? dto.getQty()- 1 : dto.getQty()));
                       txtKeyMoney.setText(String.valueOf(dto.getKey_money()));
                   }

               } catch (Exception e) {
                   e.printStackTrace();
               }
           }else {
               txtType.clear();
               txtKeyMoney.clear();
               txtQty.clear();

           }

        });

        cmbPaymentStatus.getItems().addAll("Paid","Paid Later");

    }

    private void setRoomId() {
        try {
            List<RoomDTO> allRooms = registrationBO.getAllRooms();
            for (RoomDTO dto:allRooms
                 ) {
             cmbRoomTypeId.getItems().add(dto.getRoom_type_id());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setStudentId() {
        try {
            List<StudentDTO> allStudents = registrationBO.getAllStudents();
            for (StudentDTO dto:allStudents
                 ) {
                cmbStudentId.getItems().add(dto.getStudent_id());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void btnAddToCartOnAction(ActionEvent actionEvent) {
       String roomTypeId = cmbRoomTypeId.getValue();
       String type = txtType.getText();
       Double keyMoney = Double.valueOf(txtKeyMoney.getText());
       String status = cmbPaymentStatus.getValue();

       if(btnAddToCart.getText().equals("Save")){

       }else {
           tblCart.getItems().add(new CartTM(roomTypeId,type,keyMoney,status));
       }
        cmbPaymentStatus.getSelectionModel().clearSelection();
        cmbRoomTypeId.getSelectionModel().clearSelection();
        btnRegister.requestFocus();
        enableOrDisableRegisterButton();


    }
    private void enableOrDisableRegisterButton() {
        btnRegister.setDisable(!(cmbStudentId.getSelectionModel().getSelectedItem() != null && !tblCart.getItems().isEmpty()));
    }
    public void btnRegisterOnAction(ActionEvent actionEvent) {
        try{
            registrationBO.Register(cmbStudentId.getValue(),lblReservation.getText());
            new Alert(Alert.AlertType.CONFIRMATION,"Registrashion Compelete").show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void generateNewId() {
        try {
            String lastRegistrationId = registrationBO.generateRegistrationId();
            System.out.println(lastRegistrationId);
            int newId = Integer.parseInt(lastRegistrationId.substring(2, 5))+1;
            System.out.println(newId);
            if (newId < 10) {
                lblReservation.setText("RS00"+newId);
            }else if (newId < 100) {
                lblReservation.setText("RS0"+newId);
            }else {
                lblReservation.setText("RS"+newId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
