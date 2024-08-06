package org.example.uelearner;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;


public class LearningResumer {
    static final String url = "jdbc:sqlite:my_database.sqlite";
    public LearningConfig config = null;
    String learning_log = "";
    String plain_replay_log = "";
    Map<String, String> learning_map = null;


    public LearningResumer(String learning_log, String plain_replay_log) {

        this.learning_log = learning_log;
        this.plain_replay_log = plain_replay_log;
        load_learning_log();
    }

    private void load_learning_log() {
        Connection myConn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            myConn = DriverManager.getConnection(url);
            config = new LearningConfig("fgue.properties");
        } catch (Exception e) {
            System.out.println("DB Connection Error!");
            e.printStackTrace();
        }

        try {
            String sql = "SELECT * FROM queryNew_" + config.device;
            Statement stmt = myConn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("$$$$$$$$$$$$$$$$$");
            try {
                String create = "CREATE TABLE \"queryNew_" + config.device + "\"" + "(\"command\"   TEXT, \"result\"    TEXT, PRIMARY KEY(\"command\"))";
                Statement stmt = myConn.createStatement();
                stmt.executeUpdate(create);
            } catch (Exception ex) {
                System.out.println("Failed to create table");
            }
        }
        try {

            String sql = "SELECT * FROM query_" + config.device;
            Statement stmt = myConn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
        } catch (Exception e) {
            System.out.println("$$$$$$$$$$$$$$$$$");
            try {
                String create = "CREATE TABLE \"query_" + config.device + "\"" + "(\"command\"  TEXT, \"result\"    TEXT, PRIMARY KEY(\"command\"))";
                Statement stmt = myConn.createStatement();
                stmt.executeUpdate(create);
            } catch (Exception ex) {
                System.out.println("Failed to create table");
            }
        }
        try {
            //check query* is empty or not
            String sql = "SELECT * FROM queryNew_" + config.device;
            Statement stmt = myConn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                //There are some entry in query*
                //Copy everything from query* in query
                Statement st = myConn.createStatement();

                Statement st1 = myConn.createStatement();
                rs = st1.executeQuery("select * from queryNew_" + config.device);
                PreparedStatement ps = null;

                while (rs.next()) {
                    try {
                        ps = myConn.prepareStatement("insert into query_" + config.device + " (command, result) values(?,?)");
                        ps.setString(1, rs.getString("command"));
                        ps.setString(2, rs.getString("result"));
                        ps.executeUpdate();
                        ps.close();
                    } catch (SQLException e) {
                        //System.out.println("Duplicate Entry!");
                        //e.printStackTrace();
                        //Got a duplicate basically
                    }
                }

                //Delete everything from query*
                sql = "delete from queryNew_" + config.device + " where 1=1";
                st.executeUpdate(sql);
                System.out.println("Deleted all entries in queryNew");
                st1.close();
                stmt.close();
            }
            File f = new File(this.learning_log);
            File f1 = new File(this.plain_replay_log);
            if (f.createNewFile()) {

                System.out.println(this.learning_log + " file has been created.");
                System.out.println(this.plain_replay_log + " file has been created.");
            } else {

                System.out.println(this.learning_log + " file already exists.");
                System.out.println("Reading learning log: " + this.learning_log);
                System.out.println(this.plain_replay_log + " file already exists.");
                System.out.println("Reading learning log: " + this.plain_replay_log);
                PrintWriter writer = new PrintWriter(f);
                PrintWriter writer1 = new PrintWriter(f1);
                writer.print("");
                writer.close();
                writer1.print("");
                writer1.close();
            }

            myConn.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Duplicate Entry!");
            e.printStackTrace();
        }

    }

    public String query_resumer(String command, int prefLen) {
        Connection myConn = null;
        try {
            if (myConn == null) {
                Class.forName("org.sqlite.JDBC");
                myConn = DriverManager.getConnection(url);
            }
        } catch (Exception e) {
            System.out.println("DB Connection Error!");
            e.printStackTrace();
        }
        System.out.println("In query resumer, looking for: " + command);
        String query = "select * from query_" + config.device + " where command = ?";
        command = command.replaceAll("\\|", " ");
        try {
            PreparedStatement preparedstatement = myConn.prepareStatement(query);
            preparedstatement.setString(1, command);
            ResultSet rs = preparedstatement.executeQuery();
            if (rs.next()) {
                //System.out.println("##################################################################### in Resumer!");
                String fromDB = rs.getString("result");
                String[] splited = fromDB.split(" ");
                System.out.println("IK: " + fromDB + splited.length);
                String prefix;
                String suffix;
                prefix = splited[0];
                for (int i = 1; i < prefLen; i++) {
                    prefix += " " + splited[i];
                }
                suffix = splited[prefLen];
                if (prefLen + 1 < fromDB.length()) {
                    for (int i = prefLen + 1; i < splited.length; i++) {
                        suffix += " " + splited[i];
                    }
                }


                System.out.println("found in log " + prefix + "|" + suffix);
                //Add this to queryNew

                try {
                    String query2 = " insert into queryNew_" + config.device + " (command, result)"
                            + " values (?, ?)";
                    PreparedStatement preparedstatement2 = myConn.prepareStatement(query2);
                    preparedstatement2.setString(1, rs.getString("command"));
                    preparedstatement2.setString(2, rs.getString("result"));
                    preparedstatement2.execute();
                    preparedstatement.close();
                    preparedstatement2.close();
                } catch (Exception e) {
                    myConn.close();
                    System.out.println("Already exists in queryNew!");
                }
                myConn.close();
                return prefix + "|" + suffix;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void add_Entry(String entry, int prefLen) {
        Connection myConn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            myConn = DriverManager.getConnection(url);
        } catch (Exception e) {
            System.out.println("DB Connection Error!");
            e.printStackTrace();
        }
        System.out.println("In add!");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.learning_log, true))) {
            bw.append(entry + '\n');

        } catch (Exception e) {
            System.err.println("ERROR: Could not update learning log");

        }
        try (BufferedWriter bw1 = new BufferedWriter(new FileWriter(this.plain_replay_log, true))) {
            String command = entry.split("/")[0].split("\\[")[1];
            String result = entry.split("/")[1].split("]")[0];
            command = String.join(" ", command.split("\\s+"));
            result = String.join(" ", result.split("\\s+"));
            command = command.replaceAll("\\|", " ");
            result = result.replaceAll("\\|", " ");
            String[] splited_command = command.split(" ");
            String[] splited_result = result.split(" ");
            for (int i = 0; i < splited_command.length; i++) {
                if (splited_command[i].startsWith("dl_nas_transport_plain") || /*splited_command[i].startsWith("auth_request_replay") ||*/
                        splited_command[i].startsWith("security_mode_command_replay") || splited_command[i].startsWith("GUTI_reallocation_replay") ||
                        splited_command[i].startsWith("dl_nas_transport_replay") || splited_command[i].startsWith("rrc_reconf_replay") ||
                        splited_command[i].startsWith("rrc_security_mode_command_replay") || splited_command[i].startsWith("GUTI_reallocation_plain") ||
                        splited_command[i].startsWith("rrc_security_mode_command_downgraded") || splited_command[i].startsWith("rrc_reconf_downgraded") ||
                        splited_command[i].startsWith("attach_accept_no_integrity") || splited_command[i].contains("attach_accept_null_header") ||
                        splited_command[i].startsWith("security_mode_command_no_integrity") || splited_command[i].startsWith("security_mode_command_plain") ||
                        splited_command[i].startsWith("tau_accept_plain") || splited_command[i].startsWith("security_mode_command_ns_replay")
                ) {
                    if (!splited_result[i].startsWith("null_action") && !splited_result[i].startsWith("security_mode_reject") && !splited_result[i].startsWith("auth_failure_seq") && !splited_result[i].startsWith("rrc_connection_reest_req")) {
                        bw1.append(entry + '\n');
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("ERROR: Could not update learning log");

        }
        try {
            String command = entry.split("/")[0].split("\\[")[1];
            String result = entry.split("/")[1].split("]")[0];
            command = String.join(" ", command.split("\\s+"));
            result = String.join(" ", result.split("\\s+"));
            String query = " insert into queryNew_" + config.device + " (command, result)"
                    + " values (?, ?)";
            command = command.replaceAll("\\|", " ");
            result = result.replaceAll("\\|", " ");
            System.out.println("OUTPUT: " + command + " " + result);

            PreparedStatement preparedStmt = myConn.prepareStatement(query);
            preparedStmt.setString(1, command);
            preparedStmt.setString(2, result);
            preparedStmt.execute();
            myConn.close();
            System.out.println("Added to DB! in Resumer");
        } catch (SQLException e) {
            System.out.println("history already exist in Add_Entry in QueryNew (Learning Resumer)!!");
            //e.printStackTrace();
        } catch (Exception e) {
            System.out.println("DB add_Entry Error!");
            e.printStackTrace();
        }
    }

}

