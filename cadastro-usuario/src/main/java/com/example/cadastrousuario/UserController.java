package com.example.cadastrousuario;

import com.example.cadastrousuario.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

    @FXML
    private Button editButton;

    private ObservableList<User> userList = FXCollections.observableArrayList();
    private HttpClient httpClient = HttpClient.newHttpClient();

    private void modalEditUser(User user) {
        display(user, true);
        userTable.refresh();
    }

    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        telefoneColumn.setCellValueFactory(new PropertyValueFactory<>("telefone"));

        userTable.setItems(userList);

        addButton.setOnAction(event -> addUser());

        editButton.setOnAction(event -> {
            User selectedUser = userTable.getSelectionModel().getSelectedItem();
            if (selectedUser != null) {
                modalEditUser(selectedUser);
            } else {
                showAlert("Nenhum usuário selecionado");
            }
        });

        deleteButton.setOnAction(event -> deleteUser());
    }

    private void addUser() {
        String name = nameField.getText();
        String email = emailField.getText();
        String telefone = telefoneField.getText();

        if (!isValidEmail(email)) {
            showAlert("E-mail inválido");
            return;
        }

        if (!isValidPhone(telefone)) {
            showAlert("Telefone inválido, o padrão para salvar telefone é 11 0000-0000");
            return;
        }

        // Criar um novo usuário
        User newUser = new User(name, email, telefone);
        userList.add(newUser);

        // Enviar requisição POST para a API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"name\":\"" + name + "\", \"email\":\"" + email + "\", \"telefone\":\"" + telefone + "\"}"))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> System.out.println("User added: " + response.body()));

        // Atualizar a tabela
        userTable.refresh();
    }

    private void editUser(User user) {
        String name = nameField.getText();
        String email = emailField.getText();
        String telefone = telefoneField.getText();

        if (!isValidEmail(email)) {
            showAlert("E-mail inválido");
            return;
        }

        if (!isValidPhone(telefone)) {
            showAlert("Telefone inválido, o padrão para salvar telefone é 11 0000-0000");
            return;
        }

        // Atualizar o usuário existente
        user.setName(name);
        user.setEmail(email);
        user.setTelefone(telefone);

        // Atualizar a fonte de dados
        updateUserData(user);

        // Enviar requisição PUT para a API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/api/users/" + user.getEmail()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"name\":\"" + name + "\", \"email\":\"" + email + "\", \"telefone\":\"" + telefone + "\"}"))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> System.out.println("User updated: " + response.body()));

        // Atualizar a tabela
        userTable.refresh();
    }

    private void updateUserData(User user) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getEmail().equals(user.getEmail())) {
                userList.set(i, user);
                break;
            }
        }
    }

    private void deleteUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser != null) {
            userList.remove(selectedUser);

            // Enviar requisição DELETE para a API
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/api/users/" + selectedUser.getEmail()))
                    .DELETE()
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> System.out.println("User deleted: " + response.body()));
        }
    }

    public void display(User user, boolean isEdit) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle(isEdit ? "Editar Usuário" : "Adicionar Usuário");

        // Campos de texto
        nameField = new TextField(user != null ? user.getName() : "");
        emailField = new TextField(user != null ? user.getEmail() : "");
        telefoneField = new TextField(user != null ? user.getTelefone() : "");

        // Botão de salvar
        Button saveButton = new Button("Salvar");
        saveButton.setOnAction(e -> {
            editUser(user);
            window.close();
        });

        // Layout
        GridPane layout = new GridPane();
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setVgap(8);
        layout.setHgap(10);

        // Adicionando componentes ao layout
        layout.add(new Label("Nome:"), 0, 0);
        layout.add(nameField, 1, 0);
        layout.add(new Label("E-mail:"), 0, 1);
        layout.add(emailField, 1, 1);
        layout.add(new Label("Telefone:"), 0, 2);
        layout.add(telefoneField, 1, 2);
        layout.add(saveButton, 1, 3);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isValidPhone(String phone) {
        String phoneRegex = "^\\d{2} \\d{4}-\\d{4}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro de Validação");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}