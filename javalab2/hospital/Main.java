package hospital;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        DBConnector.getInstance().connectDB("localhost",3306,"JAVA_lab2","root","root");
        stage.setTitle("简易医院挂号系统");
        Parent root = FXMLLoader.load(getClass().getResource("login.fxml"));
        Scene scene = new Scene(root, 598, 343);
        stage.setScene(scene);
        stage.show();
    }
}
