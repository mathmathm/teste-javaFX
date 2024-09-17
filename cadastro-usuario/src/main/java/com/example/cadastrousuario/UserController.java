package com.example.cadastrousuario;

import com.example.cadastrousuario.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

public class UserController {

    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;

    @FXML
    public TextField telefoneField;
    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> telefoneColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button deleteButton;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private HttpClient httpClient = HttpClient.newHttpClient();

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        userTable.setItems(userList);

        addButton.setOnAction(event -> addUser());
        deleteButton.setOnAction(event -> deleteUser());
    }

    private void addUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        User user = new User(name, email);
        userList.add(user);

        // Enviar requisição POST para a API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://sua-api.com/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"" + name + "\", \"email\":\"" + email + "\"}"))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> System.out.println("User added: " + response.body()));
    }

    private void deleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            userList.remove(selectedUser);

            // Enviar requisição DELETE para a API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://sua-api.com/users/" + selectedUser.getEmail()))
                    .DELETE()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> System.out.println("User deleted: " + response.body()));
        }
    }
}