package hospital;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;


import javax.swing.*;
import java.io.IOException;
import java.sql.ResultSet;

public class RegisterController {

    @FXML
    private ComboBox<String> inputDepartment;
    @FXML
    private ComboBox<String> inputDoctor;
    @FXML
    private ComboBox<String> inputIfSp;
    @FXML
    private ComboBox<String> inputNameRegister;
    @FXML
    private TextField feeLabel;  //费用
    @FXML
    private Label statusLabel;  //状态信息
    @FXML
    private TextField refundLabel;  //找零
    @FXML
    private Label welcomeLabel;  //欢迎label
    @FXML
    private TextField inputMoney;  //输入的交款金额
    @FXML
    private TextField registerNum;  //挂号号码
    @FXML
    private Button registerButton;  //挂号
    @FXML
    private Button exitButton;  //退出按钮
    @FXML
    private AnchorPane pane;

    private static String patientName;  //病人姓名
    private static String patientId;  //病人编号
    private static double patientYCYE;  //病人预存金额

    private static double registerFee;  //挂号费用
    private static double patientPay;  //病人付费费用

    private int lastInputDepartment = -1;
    private int lastInputDoctor = -1;
    private int lastInputIfSP = -1;
    private int lastInputRegister = -1;

    @FXML
    void initialize() {
        welcomeLabel.setText("欢迎进入医院挂号系统，" + patientName + "!\t当前账户余额:" + patientYCYE + "元");
        //setInputDepartment();
        inputDepartment.addEventHandler(ComboBox.ON_SHOWING, event -> {
            if (!inputDepartment.getEditor().getText().isEmpty()) {
                return;
            }
            setInputDepartment();
            event.consume();
        });
        inputDepartment.getEditor().setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN)
                return;
            setInputDepartment();
            if (!inputDepartment.isShowing())
                inputDepartment.show();
            else {
                inputDepartment.hide();
                inputDepartment.show();
            }
        });
        inputDepartment.addEventHandler(ComboBox.ON_HIDDEN, event -> {
            int index;
            if ((index = inputDepartment.getSelectionModel().getSelectedIndex()) != lastInputDepartment) {
                lastInputDepartment = index;
                setInputDoctor();
                inputIfSp.getItems().clear();
                inputNameRegister.getItems().clear();
            }
            event.consume();
        });
        inputDoctor.getEditor().setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN)
                return;
            setInputDoctor();
            if (!inputDoctor.isShowing())
                inputDoctor.show();
            else {
                inputDoctor.hide();
                inputDoctor.show();
            }
        });
        inputDoctor.addEventHandler(ComboBox.ON_SHOWING, event -> {
            if (!inputDoctor.getEditor().getText().isEmpty())
                return;
            setInputDoctor();
            event.consume();
        });
        inputDoctor.addEventHandler(ComboBox.ON_HIDDEN, event -> {
            int index;
            if ((index = inputDoctor.getSelectionModel().getSelectedIndex()) != lastInputDoctor) {
                lastInputDoctor = index;
                setInputIfSp();
                inputNameRegister.getItems().clear();
            }
            event.consume();
        });
        inputIfSp.addEventHandler(ComboBox.ON_SHOWING, event -> {
            if (!inputIfSp.getEditor().getText().isEmpty())
                return;
            setInputIfSp();
            event.consume();
        });
        inputIfSp.getEditor().setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN)
                return;
            setInputIfSp();
            if (!inputIfSp.isShowing())
                inputIfSp.show();
            else {
                inputIfSp.hide();
                inputIfSp.show();
            }
        });
        inputIfSp.addEventHandler(ComboBox.ON_HIDDEN, event -> {
            int index;
            if ((index = inputIfSp.getSelectionModel().getSelectedIndex()) != lastInputIfSP) {
                lastInputIfSP = index;
                setInputNameRegister();
            }
            event.consume();
        });
        inputNameRegister.getEditor().setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.UP || keyEvent.getCode() == KeyCode.DOWN)
                return;
            setInputNameRegister();
            if (!inputNameRegister.isShowing())
                inputNameRegister.show();
            else {
                inputNameRegister.hide();
                inputNameRegister.show();
            }
        });
        inputNameRegister.addEventHandler(ComboBox.ON_SHOWING, event -> {
            if (!inputNameRegister.getEditor().getText().isEmpty())
                return;
            setInputNameRegister();
            event.consume();
        });
        inputNameRegister.addEventHandler(ComboBox.ON_HIDDEN, event -> {
            int index;
            if ((index = inputNameRegister.getSelectionModel().getSelectedIndex()) != lastInputRegister) {
                lastInputRegister = index;
            }
            if (index != -1) {
                String registerInformation = inputNameRegister.getEditor().getText();
                String[] strings = registerInformation.split(" ");
                feeLabel.setText(strings[3] + " 元");
                registerFee = Double.parseDouble(strings[3]);
                checkYCJE();
            }
            event.consume();
        });

        inputMoney.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER || keyEvent.getCode() == KeyCode.BACK_SPACE)
                return;
            String s = inputMoney.getText().trim();
            if (!s.matches("([1-9]+[0-9]*|0)(\\.[\\d]+)?")) {
                setStatusLabel("输入非法，请重新输入");
                return;
            }
            setStatusLabel("");
            patientPay = Double.parseDouble(s);
        });

        exitButton.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                try {
                    onExitButtonClick();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        registerButton.setOnKeyReleased(keyEvent -> {

        });
    }

    /**
     * 退出按钮点击事件
     *
     * @throws IOException 异常
     */
    @FXML
    void onExitButtonClick() throws IOException {
        // DBConnector.getInstance().closeConnect();
        Stage nextStage = (Stage) exitButton.getScene().getWindow();
        nextStage.close();
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 598, 343);
        scene.setRoot(root);
        Stage stage = new Stage();
        stage.setTitle("简易医院挂号系统");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 设置复选框科室信息,可以根据拼音选择科室
     */
    private void setInputDepartment() {
        String pingyin = inputDepartment.getEditor().getText();//获取输入的拼音
        if (pingyin == null) {
            inputDepartment.getItems().clear();
            inputDepartment.getItems().addAll(DBConnector.getInstance().getDepInfor());
        } else {
            inputDepartment.getItems().clear();
            inputDepartment.getItems().addAll(DBConnector.getInstance().getInfor("department", pingyin));
        }
    }

    /**
     * 设置复选框医生信息，可以根据拼音选择医生
     */
    private void setInputDoctor() {
        String depart = inputDepartment.getEditor().getText();
        if (depart.isEmpty()) {
            setStatusLabel("请先选择科室");
            return;
        }
        String[] strings = depart.split(" ");
        depart = strings[0];
        String pinyin = inputDoctor.getEditor().getText();
        if (pinyin == null) {
            inputDoctor.getItems().clear();
            //inputDoctor.getItems().addAll(DBConnector.getInstance().getInfor("doctor",null));
            inputDoctor.getItems().addAll(DBConnector.getInstance().getDepartDoctor(depart, null));
        } else {
            inputDoctor.getItems().clear();
            //inputDoctor.getItems().addAll(DBConnector.getInstance().getInfor("doctor",pinyin));
            inputDoctor.getItems().addAll(DBConnector.getInstance().getDepartDoctor(depart, pinyin));
        }
    }

    /**
     * 设置复选框专家号普通号
     */
    private void setInputIfSp() {
        String depart = inputDepartment.getEditor().getText();
        if (depart.isEmpty()) {
            setStatusLabel("请先选择科室");
            return;
        }
        String doctor = inputDoctor.getEditor().getText();
        if (doctor.isEmpty()) {
            setStatusLabel("请先选择医生");
            return;
        }
        String[] strings = doctor.split(" ");
        doctor = strings[0];
        boolean flag = false;
        try {
            ResultSet resultSet = DBConnector.getInstance().getDoctorInfo(doctor);
            while (resultSet.next()) {
                if (resultSet.getInt("SFZJ") == 1)
                    flag = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        inputIfSp.getItems().clear();
        String pinyin = inputIfSp.getEditor().getText().trim();
        if (!pinyin.isEmpty()) {
            pinyin = pinyin.toLowerCase();
            if (flag && "zjh".contains(pinyin))
                inputIfSp.getItems().add("专家号");
            if ("pth".contains(pinyin))
                inputIfSp.getItems().add("普通号");
        } else {
            if (flag)
                inputIfSp.getItems().add("专家号");
            inputIfSp.getItems().add("普通号");
        }
    }

    /**
     * 设置复选框挂号信息：号种编号+号种名称+医生+号种类别+挂号费用
     */
    private void setInputNameRegister() {
        String pinyin = inputNameRegister.getEditor().getText();
        String depart = inputDepartment.getEditor().getText();
        if (depart.isEmpty()) {
            setStatusLabel("请先选择科室");
            return;
        }
        String doctor = inputDoctor.getEditor().getText();
        if (doctor.isEmpty()) {
            setStatusLabel("请先选择医生");
            return;
        }
        String specialist = inputIfSp.getEditor().getText();
        if (specialist.isEmpty()) {
            setStatusLabel("请先选择挂号类别");
            return;
        }
        String[] departs = depart.split(" ");
        if (pinyin.isEmpty()) {
            inputNameRegister.getItems().clear();
            inputNameRegister.getItems().addAll(DBConnector.getInstance().getRegisterInfor(departs[0], inputIfSp.getEditor().getText(), null));
        } else {
            pinyin = pinyin.toLowerCase();
            inputNameRegister.getItems().clear();
            inputNameRegister.getItems().addAll(DBConnector.getInstance().getRegisterInfor(departs[0], inputIfSp.getEditor().getText(), pinyin));
        }
    }

    /**
     * 检查预存金额是否够付款
     */
    private void checkYCJE() {
        if (patientYCYE < registerFee) {
            setStatusLabel("余额不足,请使用现金交款");
            inputMoney.setText("");
            inputMoney.setEditable(true);
            inputMoney.setDisable(false);
        } else {
            inputMoney.setText("使用余额付款");
            inputMoney.setText("");
            inputMoney.setEditable(false);
            inputMoney.setDisable(true);
        }
    }

    /**
     * 挂号按钮点击事件
     */
    @FXML
    void onRegisterClick() {
        String depart = inputDepartment.getEditor().getText();
        if (depart.isEmpty()) {
            setStatusLabel("请先选择科室");
            return;
        }
        String doctor = inputDoctor.getEditor().getText();
        if (doctor.isEmpty()) {
            setStatusLabel("请先选择医生");
            return;
        }
        String specialist = inputIfSp.getEditor().getText();
        if (specialist.isEmpty()) {
            setStatusLabel("请先选择挂号类别");
            return;
        }
        String registerName = inputNameRegister.getEditor().getText();
        if (registerName.isEmpty()) {
            setStatusLabel("请选择挂号信息");
            return;
        }
        if (!inputMoney.isDisable() && !inputMoney.getText().isEmpty()) {
            if (!inputMoney.getText().matches("([1-9]+[0-9]*|0)(\\.[\\d]+)?")) {
                setStatusLabel("输入非法，请重新输入");
                return;
            }
            patientPay = Double.parseDouble(inputMoney.getText());
        } else
            patientPay = 0;
        if (patientYCYE < registerFee && patientYCYE + patientPay < registerFee) {
            setStatusLabel("缴费金额不足或余额不足");
            inputMoney.setText("");
            inputMoney.setEditable(true);
            inputMoney.setDisable(false);
            return;
        }
        String[] doctors = doctor.split(" ");
        String[] registers = registerName.split(" ");
        pane.setDisable(true);
        double tmp = patientPay;
        patientPay += patientYCYE;
        String status = DBConnector.getInstance().tryRegister(registers[0], doctors[0], patientId, registerFee, patientPay);
        if (status.contains("success")) {
            welcomeLabel.setText("欢迎进入医院挂号系统，" + patientName + "!\t当前账户余额:" + patientYCYE + "元");
            if (tmp == 0)
                refundLabel.setText(patientYCYE + " 元");
            else {
                refundLabel.setText(String.format("%.2f", patientPay - registerFee) + " 元");
                statusLabel.setText("找零已自动存入余额");
                statusLabel.setStyle("-fx-text-fill: #000000;");
            }
            String[] temps = status.split(" ");
            registerNum.setText(temps[1]);
            JOptionPane.showMessageDialog(null, "挂号成功!", "医院门诊挂号系统", JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, status, "医院门诊挂号系统", JOptionPane.ERROR_MESSAGE);
        }
        pane.setDisable(false);
    }

    /**
     * 设置出错信息
     *
     * @param warningMsg 出错提示
     */
    private void setStatusLabel(String warningMsg) {
        statusLabel.setText(warningMsg);
        statusLabel.setStyle("-fx-text-fill: #ff0000;");
    }

    public static String getPatientName() {
        return patientName;
    }

    public static void setPatientName(String patientName) {
        RegisterController.patientName = patientName;
    }

    public static String getPatientId() {
        return patientId;
    }

    public static void setPatientId(String patientId) {
        RegisterController.patientId = patientId;
    }

    public static void setPatientYCYE(double patientYCYE) {
        RegisterController.patientYCYE = patientYCYE;
        //welcomeLabel.setText("欢迎进入医院挂号系统，" + patientName + "!\t当前账户余额:" + RegisterController.patientYCYE + "元");
    }

    public static double getPatientYCYE() {
        return patientYCYE;
    }
}
