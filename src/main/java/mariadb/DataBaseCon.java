package mariadb;

import entity.Rules;
import entity.RulesMapping;
import org.apache.commons.codec.language.bm.Rule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataBaseCon {

    private final Logger log = LogManager.getLogger(DataBaseCon.class);
    private final static String DRIVER = "org.mariadb.jdbc.Driver";
    private final static String URL = "jdbc:mariadb://192.168.60.82:3306/mariadb";
    private final static String userID = "root";
    private final static String PASSWORD = "naim4321";

    static Connection con = null;
    static PreparedStatement pst = null;
    static ResultSet rs = null;

    // RDB connection
    public void connection() {
        try {
            Class.forName(DRIVER);
            con = DriverManager.getConnection(URL, userID, PASSWORD);
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage());
        }
    }

    public Map<String, Float> mappingRiskLevel(int rule_id) {
        Map<String, Float> mappingRiskLevel = new HashMap<>();
        try {
            String query = "SELECT b.value, b.risk_level_comment " +
                             "FROM rule_risk a " +
                             "JOIN (SELECT * " +
                                     "FROM rule_risk a " +
                                     "JOIN risk_levels b " +
                                       "ON a.risk_level = b.id) b " +
                               "ON a.rule_id = b.rule_id " +
                            "WHERE a.rule_id=? ";

            pst = con.prepareStatement(query);
            pst.setInt(1, rule_id);
            rs = pst.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                // Map<column, row> 형태로 저장
                for (int i = 1; i <= columnCount; i++) {
                    mappingRiskLevel.put(rs.getString("risk_level_comment"), rs.getFloat("value"));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return mappingRiskLevel;
    }

    public List<String> mappingAgent(int rule_id) {
        List<String> mappingAgentList = new ArrayList<>();
        try {
            String query = "SELECT b.agent_id " +
                             "FROM rules a " +
                             "JOIN (SELECT * " +
                                     "FROM rule_agent a " +
                                     "JOIN agent_info_list b " +
                                       "ON a.agent_id = b.id) b " +
                               "ON a.rule_id = b.rule_id " +
                            "WHERE a.rule_id=? ";

            pst = con.prepareStatement(query);
            pst.setInt(1, rule_id);
            rs = pst.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                // Map<column, row> 형태로 저장
                for (int i = 1; i <= columnCount; i++) {
                    mappingAgentList.add(rs.getString("agent_id"));
                }
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return mappingAgentList;
    }

    public List<Rules> getRules() {
        List<Rules> ruleList = new ArrayList<>();
        try {
            String query = "SELECT DISTINCT a.rule_id, a.resource_type, a.duration_time, a.count " +
                    "FROM rules a " +
                        "JOIN (SELECT * " +
                                "FROM rule_risk a " +
                                "JOIN risk_levels b " +
                                  "ON a.risk_level = b.id) b " +
                          "ON a.rule_id = b.rule_id " +
                    "ORDER BY value desc";

            pst = con.prepareStatement(query);
            rs = pst.executeQuery();

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            List<Map<String, Object>> result = new ArrayList<>();
            Map<String, Object> valueAndLevelComment = new HashMap<>();
            while (rs.next()) {
                Map<String, Object> ruleMap = new ConcurrentHashMap<>();
                // Map<column, row> 형태로 저장
                for (int i = 1; i <= columnCount; i++) {
                    ruleMap.put(metaData.getColumnName(i), rs.getObject(i));
                }

                if (valueAndLevelComment.isEmpty()) {
                    valueAndLevelComment.put("value", ruleMap.get("value"));
                    valueAndLevelComment.put("risk_level_comment", ruleMap.get("risk_level_comment"));

                    result.add(valueAndLevelComment);
                }

                Rules rules = new Rules(
                        (Integer) ruleMap.get("rule_id"),
                        (Integer) ruleMap.get("resource_type"),
                        (Integer) ruleMap.get("duration_time"),
                        (Integer) ruleMap.get("count")
                );
                ruleList.add(rules);
            }
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return ruleList;
    }


    public List<Integer> getRulesIds() {

        List<Integer> rules = new ArrayList<>();
        try {
            String query = "SELECT a.rule_id " +
                             "FROM rules a " +
//                             "JOIN (SELECT * " +
//                                     "FROM rule_agent a " +
//                                     "JOIN agent_info_list b " +
//                                       "ON a.agent_id = b.id) b " +
//                                       "ON a.rule_id = b.rule_id " +
                             "JOIN (SELECT * " +
                                     "FROM rule_risk a " +
                                     "JOIN risk_levels b " +
                                       "ON a.risk_level = b.id) b " +
                               "ON a.rule_id = b.rule_id " +
//                            "WHERE a.rule_id=? " +
                            "ORDER BY value desc ";


            pst = con.prepareStatement(query);
//            pst.setInt(1, rule_id);
            rs = pst.executeQuery();

            // select 해 온 metaData 꺼냄
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                int id = rs.getInt("rule_id");
                rules.add(id);
            }
            return rules;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return rules;
    }

    // agent_info RDB insert
    public void insert(String ip, String key, String mac_address, String gateway, String os_type, String name) {
        try {

            String query = "INSERT INTO agent_info_list (ip, id, mac_address, gateway, os_type, name) values (?,?,?,?,?,?)";

            pst = con.prepareStatement(query);
            pst.setString(1, ip);
            pst.setString(2, key);
            pst.setString(3, mac_address);
            pst.setString(4, gateway);
            pst.setString(5, os_type);
            pst.setString(6, name);
            pst.executeQuery();

            log.info("database insert ");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public void update(String ip, String key, String mac_address, String gateway, String os_type, String name) {
        try {
            String query = "UPDATE agent_info_list " +
                              "SET ip=?, id=?, mac_address=?, gateway=?, os_type=?, NAME=? " +
                            "WHERE id=?";

            pst = con.prepareStatement(query);
            pst.setString(1, ip);
            pst.setString(2, key);
            pst.setString(3, mac_address);
            pst.setString(4, gateway);
            pst.setString(5, os_type);
            pst.setString(6, name);
            pst.setString(7, key);
            pst.executeQuery();

            log.info("database update ");
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }

    public boolean updateRules(float value, int id) {
        try {
            String query = "UPDATE rules " +
                    "SET value=?" +
                    "WHERE id=?";

            pst = con.prepareStatement(query);
            pst.setFloat(1, value);
            pst.setFloat(2, id);
            int successCnt = pst.executeUpdate();

            if (successCnt == 1) {
                log.info("rules Table update success !");
                return true;
            }

        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return false;
    }

    public List<String> select() {
        List<String> keys = new ArrayList<>();
        try {
            String query = "SELECT * FROM agent_info_list";

            pst = con.prepareStatement(query);
            rs = pst.executeQuery();

            while (rs.next()) {
                String id = rs.getString("id");
                keys.add(id);
            }
            return keys;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return keys;
    }

    public void close() {
        try {
            rs.close();
            pst.close();
            con.close();
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
    }
}
