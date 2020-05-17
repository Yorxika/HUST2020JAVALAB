package hospital;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.LinkedList;

public class DBConnector {
    private static volatile DBConnector instance = null;
    private Connection connection;
    private Statement statement;

    /**
     * 加载mysql驱动
     */
    private DBConnector(){
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("not found mysql driver");
            e.printStackTrace();
        }
    }

    /*private static class instanceHolder{
        private static final DBConnector instance = new DBConnector();
    }*/

    /**
     * 线程安全的单例模式
     * @return 数据库连接实例
     */
    public static DBConnector getInstance(){
        if(instance == null){
            synchronized (DBConnector.class){
                if (instance == null)
                    instance = new DBConnector();
            }
        }
        return instance;
        //return instanceHolder.instance;
    }

    /**
     * 建立数据库连接
     * @param hostName 主机名
     * @param port 端口号
     * @param dbName 数据库名
     * @param user 用户名
     * @param passwd 密码
     */
    public void connectDB(String hostName,int port,String dbName,String user,String passwd){
        String url = "jdbc:mysql://"+hostName+":"+port+"/"+dbName+"?autoReconnect=true&characterEncoding=UTF-8&characterSetResults=UTF-8&serverTimezone=UTC";
        try {
            connection = DriverManager.getConnection(url,user,passwd);
            statement = connection.createStatement();
        } catch (SQLException throwables) {
            System.out.println("cannot connect to database");
            throwables.printStackTrace();
        }
    }

    /**
     * 关闭数据库连接
     */
    public void closeConnect(){
        try{
            connection.close();
            statement.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取病人信息
     * @param brbh 病人编号
     * @return 病人信息
     */
    public ResultSet getPatientInfo(String brbh) {
        try {
            return statement.executeQuery("select * from t_brxx where brbh =" + brbh);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 获取医生信息
     * @param ysbh 医生编号
     * @return 医生信息
     */
    public ResultSet getDoctorInfo(String ysbh) {
        try {
            return statement.executeQuery("select * from  t_ksys where ysbh =" + ysbh);
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * 更新登录日期
     * @param loginIdenty 登录身份
     * @param user 用户
     */
    public void updateDLRQ(String loginIdenty,String user){
        try{
            String table = loginIdenty.equals("患者")?"t_brxx" : "t_ksys";
            String bh = loginIdenty.equals("患者")?"brbh" : "ysbh";
            statement.executeUpdate("update " + table + " set dlrq = DATE_FORMAT(NOW(),'%Y-%m-%d %H:%m:%s') where " + bh + "=" + user);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取科室信息表
     * @return 科室编号+科室名称
     */
    public LinkedList<String> getDepInfor(){
        try{
            ResultSet resultSet = statement.executeQuery("SELECT * FROM T_KSXX");
            LinkedList<String> depInfor = new LinkedList<>();
            while (resultSet.next()){
                String KSBH = resultSet.getString("KSBH").trim();
                String KSMC = resultSet.getString("KSMC").trim();
                //String PYZS = resultSet.getString("PYZS").trim();
                depInfor.add(KSBH+" "+KSMC);
            }
            return depInfor;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取挂号信息
     * @param department 科室
     * @param isSP 是否专家
     * @param pinyin 号种名称的拼音
     * @return 号种编号+号种+挂号费用
     */
    public LinkedList<String> getRegisterInfor(String department, String isSP, String pinyin){
        try{
            int ifSp = isSP.equals("专家号") ? 1 : 0;
            ResultSet result = statement.executeQuery("SELECT HZBH,HZMC,PYZS,GHRS,GHFY FROM T_HZXX WHERE KSBH = "+department+" AND SFZJ = "+ ifSp +" AND (GHRS > (SELECT IFNULL(MAX(GHRC),0) FROM T_GHXX WHERE HZBH=T_HZXX.HZBH AND TO_DAYS(RQSJ)=TO_DAYS(NOW())))");
            LinkedList<String> registerInfor = new LinkedList<>();
            String registerPY;
            while (result.next()){
                String registerId = result.getString("HZBH").trim();
                String registerName = result.getString("HZMC").trim();
                registerPY = result.getString("PYZS").trim();
                Double registerPay = result.getDouble("GHFY");
                if(pinyin == null)
                    registerInfor.add(registerId+" "+registerName+" "+isSP+" "+registerPay+" 元");
                else{
                    if(registerPY.contains(pinyin))
                        registerInfor.add(registerId+" "+registerName+" "+isSP+" "+registerPay+" 元");
                }
            }
            return registerInfor;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取医生名称
     * @param doctor 医生编号
     * @return 医生名称
     */
    public String getDoctorName(String doctor){
        try {
            ResultSet resultSet = statement.executeQuery("SELECT YSMC FROM T_KSYS WHERE YSBH = "+doctor);
            while (resultSet.next())
                return resultSet.getString("YSMC");
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取病人信息
     * @param doctor 医生编号
     * @return 病人信息
     */
    public ObservableList<doctorController.Register> getPatients(String doctor){
        try {
            ObservableList<doctorController.Register> patients = FXCollections.observableArrayList();
            ResultSet resultSet = statement.executeQuery("SELECT GHBH,BRMC,RQSJ,SFZJ FROM T_GHXX,T_BRXX,T_HZXX WHERE T_GHXX.YSBH= "+doctor+" AND T_GHXX.BRBH=T_BRXX.BRBH AND T_GHXX.HZBH=T_HZXX.HZBH ORDER BY GHBH ASC");
            while (resultSet.next()){
                String registerId = resultSet.getString("GHBH").trim();
                String patientName = resultSet.getString("BRMC").trim();
                String regTime = resultSet.getString("RQSJ").trim();
                //String isSP = resultSet.getInt("SFZJ")==1? "专家号":"普通号";
                int isSP = resultSet.getInt("SFZJ");
                doctorController.Register patient = new doctorController.Register(registerId,patientName,regTime,isSP);
                patients.add(patient);
            }
            return patients;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据输入表名或者拼音得到具体科室信息、号种信息或挂号信息
     * @param table 要访问的表
     * @param pingyin 拼音
     * @return 具体信息
     */
    public LinkedList<String> getInfor(String table,String pingyin){
        LinkedList<String> infor = new LinkedList<>();
        String queryTable = "";
        String number = "";
        String name = "";
        String fee = "";
        if(!table.equals("")){
            switch (table){
                case "department":
                    queryTable = "T_KSXX";
                    number = "KSBH";
                    name = "KSMC";
                    break;
                case "haozhong":
                    queryTable = "T_HZXX";
                    number = "HZBH";
                    name = "HZMC";
                    fee = "GHFY";
                    break;
                case "doctor":
                    queryTable = "T_KSYS";
                    number = "YSBH";
                    name = "YSMC";
                    break;
                default:
                    queryTable = null;
                    break;
            }
        }
        try{
            ResultSet resultSet = statement.executeQuery("SELECT * FROM "+queryTable);
            while (resultSet.next()){
                if(pingyin.isEmpty()){
                    infor.add(resultSet.getString(number)+" "+resultSet.getString(name));
                    if(!fee.equals(""))
                        infor.add(infor.getLast()+" "+resultSet.getDouble(fee)+"元");
                }else{
                    pingyin = pingyin.toLowerCase();  //需要设置为小写
                    if(resultSet.getString("PYZS").contains(pingyin)){
                        infor.add(resultSet.getString(number)+" "+resultSet.getString(name));
                        if(!fee.equals(""))
                            infor.add(infor.getLast()+" "+resultSet.getDouble(fee)+"元");
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return infor;
    }

    /**
     * 根据输入的科室号选择医生
     * @param departId 科室编号
     * @param pingyin 医生拼音
     * @return 医生信息
     */
    public LinkedList<String> getDepartDoctor(String departId, String pingyin){
        LinkedList<String> infor = new LinkedList<>();
        try{
            ResultSet resultSet = statement.executeQuery("SELECT YSBH,YSMC,PYZS FROM T_KSYS WHERE KSBH = " + departId);
            while (resultSet.next()){
                if(pingyin == null){
                    infor.add(resultSet.getString("YSBH")+" " + resultSet.getString("YSMC"));
                }else
                {
                    pingyin = pingyin.toLowerCase();
                    if(resultSet.getString("PYZS").contains(pingyin))
                        infor.add(resultSet.getString("YSBH")+" " + resultSet.getString("YSMC"));
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return infor;
    }

    /**
     * 挂号
     * @param HZBH 号种编号
     * @param YSBH 医生编号
     * @param BRBH 病人编号
     * @param GHFY 挂号费用
     * @param patientPay 病人付款
     * @return 状态信息
     */
    public String tryRegister(String HZBH,String YSBH,String BRBH,double GHFY,double patientPay){
        try{
            ResultSet resultSet = statement.executeQuery("select * from t_ghxx order by ghbh desc limit 1");
            int registerId,count,maxRegister = 0;  //分别为挂号id和当前挂号人数，最大挂号人数
            if(!resultSet.next())
                registerId = 1;
            else
                registerId = Integer.parseInt(resultSet.getString("GHBH"))+1;
            resultSet = statement.executeQuery("select * from t_ghxx where HZBH = " + HZBH + " and RQSJ >= DATE_FORMAT(NOW(),'%Y-%m-%d 00:00:00') order by GHRC desc limit 1");
            if(!resultSet.next())
                count = 0;
            else
                count = resultSet.getInt("GHRC");
            resultSet = statement.executeQuery("select * from t_hzxx where hzbh = " + HZBH);
            while (resultSet.next()){
                maxRegister = resultSet.getInt("GHRS");
            }
            if(count >= maxRegister){
                return "当前号种挂号人数已满,请选择其他号种";
            }
          //  statement.execute("insert into t_ghxx values ("+registerId+", "+HZBH+", "+YSBH+", "+BRBH+", "+(count+1)+", "+0+", "+GHFY+", DATE_FORMAT(NOW(),'%Y-%m-%d %H:%m:%s'))");
            statement.executeUpdate(
                    String.format("insert into t_ghxx values (\"%06d\",\"%s\",\"%s\",\"%s\",%d,0,%.2f, current_timestamp)",
                            registerId, HZBH, YSBH, BRBH, count+1, GHFY));
            //病人付款后更新余额
            //statement.executeUpdate("update t_brxx set ycje = " + (patientPay-GHFY)+" where brbh = "+BRBH);
            statement.executeUpdate(String.format("update t_brxx set ycje = %.2f where brbh = %s",patientPay-GHFY,BRBH));
            //RegisterController.setPatientYCYE(patientPay-GHFY);
            //RegisterController.setrRefundLabel(patientPay-GHFY);
            double refund = Double.parseDouble(String.format("%.2f",patientPay-GHFY));
            RegisterController.setPatientYCYE(refund);
            return "success "+registerId;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 医生查询时间病人信息
     * @param doctorID 医生编号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 病人列表
     */
    public LinkedList<doctorController.Register> getPatientInfor(String doctorID,String startTime,String endTime){
        LinkedList<doctorController.Register> patients = new LinkedList<>();
        try{
            String query = "select t_ghxx.GHBH,t_brxx.BRMC,t_ghxx.RQSJ,t_hzxx.SFZJ from t_ghxx,t_brxx,t_hzxx where t_ghxx.YSBH = "+ doctorID + " and t_hzxx.HZBH = t_ghxx.HZBH and t_ghxx.brbh = t_brxx.brbh and rqsj >= '" + startTime + "' and rqsj <= '"+ endTime + "'";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                patients.add(new doctorController.Register(resultSet.getString("GHBH"),resultSet.getString("BRMC"),resultSet.getString("RQSJ"),resultSet.getInt("SFZJ")));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return patients;
    }

    /**
     * 获取收入信息
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 收入信息列表
     */
    public ObservableList<doctorController.income> getIncomeInfor(String startTime,String endTime){
        ObservableList<doctorController.income> incomes = FXCollections.observableArrayList();
        try {
            String query = "SELECT KSMC,T_GHXX.YSBH,YSMC,T_HZXX.SFZJ,COUNT(*) AS CNT ,SUM(T_GHXX.GHFY) AS INCOME FROM T_GHXX,T_KSXX,T_KSYS,T_HZXX WHERE " +
                    "T_GHXX.YSBH=T_KSYS.YSBH AND T_KSYS.KSBH=T_KSXX.KSBH AND T_GHXX.HZBH=T_HZXX.HZBH AND T_GHXX.RQSJ >= '" +
                    startTime + "' AND T_GHXX.RQSJ <= '" + endTime + "' AND T_GHXX.THBZ = 0 GROUP BY T_GHXX.YSBH";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                incomes.add(new doctorController.income(resultSet.getString("KSMC"),resultSet.getString("YSBH"),resultSet.getString("YSMC"),
                        resultSet.getInt("SFZJ"),resultSet.getInt("cnt"),resultSet.getDouble("income")));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return incomes;
    }

}
