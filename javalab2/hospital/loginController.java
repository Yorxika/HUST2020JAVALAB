package hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.sql.ResultSet;


public class loginController{
    @FXML
    private Button loginButton;
    @FXML
    private TextField inputUsername;
    @FXML
    private PasswordField inputPassword;
    @FXML
    public ChoiceBox<String> loginType;
    @FXML
    public Label labelStatus;
    String loginIdenty = "患者";

    @FXML
    void initialize(){
        loginButton.setOnKeyReleased(keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER)
                    onLoginClick();
        });
    }

    public void onLoginClick(){
        if(!checkInputValid())
            return;
        if(loginType.getValue().equals("医生"))
            loginIdenty = "医生";
        try{
            if(tryLogin()) {
                String nextScene = "Register.fxml";
                String title = "医院挂号系统";
                if (loginIdenty.equals("医生")) {
                 doctorController.setDoctorId(inputUsername.getText().trim());
                 doctorController.setDoctorName(DBConnector.getInstance().getDoctorName(inputUsername.getText().trim()));
                    nextScene = "Doctor.fxml";
                    title = "医生系统";
                } else {
                    RegisterController.setPatientId(inputUsername.getText().trim());
                    ResultSet resultSet = DBConnector.getInstance().getPatientInfo(inputUsername.getText().trim());
                    while (resultSet.next()){
                        RegisterController.setPatientName(resultSet.getString("BRMC"));
                        RegisterController.setPatientYCYE(resultSet.getDouble("YCJE"));
                    }
                }
                Stage nextStage = (Stage) loginButton.getScene().getWindow();
                nextStage.close();
                Parent root = FXMLLoader.load(getClass().getResource(nextScene));
                Scene scene = new Scene(root, 619, 396);
                scene.setRoot(root);
                Stage stage = new Stage();
                stage.setTitle(title);
                stage.setScene(scene);
                stage.show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 检查输入是否有效
     * @return 检查登录是否有效
     */
    private boolean checkInputValid(){
        if(!inputUsername.getText().isEmpty())
            inputUsername.setStyle("-fx-background-color: white;");
        if(!inputPassword.getText().isEmpty())
            inputPassword.setStyle("-fx-background-color: white;");
        if (inputUsername.getText().isEmpty()) {
            inputUsername.setStyle("-fx-background-color: pink;");
            labelStatus.setText("请输入用户名");
            labelStatus.setStyle("-fx-text-fill: red;");
            return false;
        }
        if (inputPassword.getText().isEmpty()) {
            inputPassword.setStyle("-fx-background-color: pink;");
            labelStatus.setText("请输入密码");
            labelStatus.setStyle("-fx-text-fill: red;");
            return false;
        }

        labelStatus.setText("登录中...");
        labelStatus.setStyle("");
        return true;
    }

    /**
     * 尝试登录
     * @return
     */
    public boolean tryLogin(){
        if(!checkInputValid())
            return false;
        ResultSet result = loginIdenty.equals("患者") ?
                DBConnector.getInstance().getPatientInfo(inputUsername.getText().trim()):
                DBConnector.getInstance().getDoctorInfo(inputUsername.getText().trim());
        if(result == null){
            labelStatus.setText("数据库读取错误");
            labelStatus.setStyle("-fx-text-fill: red;");
        }
        try {
            if(!result.next()){
                labelStatus.setText("用户不存在");
                labelStatus.setStyle("-fx-text-fill: red;");
                return false;
            }else if(!((ResultSet) result).getString("dlkl").equals(inputPassword.getText())) {
                labelStatus.setText("密码错误");
                labelStatus.setStyle("-fx-text-fill: red;");
                return false;
            }
            //登录成功需要设置最后一次登录事件
            DBConnector.getInstance().updateDLRQ(loginIdenty,inputUsername.getText().trim());
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

}
