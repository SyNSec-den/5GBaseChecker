package org.example.uelearner.db;

import org.example.uelearner.LearningConfig;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Cache {

    public LearningConfig config = null;
    String cache_log = "";


    public Cache(String cache_log) {
        try {
            config = new LearningConfig("fgue.properties");
        } catch (Exception e) {
            System.out.println("Howcome!");
        }
        this.cache_log = cache_log;
        //this.load_cache_log();
    }

    public static void main(String[] args) {
        Cache myCache = new Cache("cache.log");
        System.out.println(myCache.query_cache("enable_s1 identity_request"));
    }

    public Connection getCacheConnection() {
        return DBHelper.getConnection();
    }

    public String query_cache(String command) {
        //System.out.println("IK in query cache!");
        //System.out.println(command);
        Connection myConn = this.getCacheConnection();
        if (myConn == null) {
            System.out.println("*** IN Cache.query_cache(): Cache DB Connection not established ***");
        }

        if (command == null) {
            return null;
        }
        String Myquery = "select * from queryNew_" + config.db_table_name + " where command = ?";

        try {
            assert myConn != null;
            PreparedStatement preparedstatement = myConn.prepareStatement(Myquery);
            preparedstatement.setString(1, command);
            System.out.println("***** Search Command = " + command + " *****");
            ResultSet rs = preparedstatement.executeQuery();
            if (rs.next()) {
                //System.out.println("##################################################################### in Cache!");
                String saved_query = rs.getString("result");
                preparedstatement.close();
                return saved_query;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }

    }

}

