package org.example.uelearner.ue;

import org.example.uelearner.LearningConfig;
import java.io.IOException;

public class SocketConfig extends LearningConfig{
    String alphabet;
    String hostname;
    int port;

    boolean combine_query;
    String delimiter_input;
    String delimiter_output;

    public SocketConfig(String filename) throws IOException {
        super(filename);
    }

    public SocketConfig(LearningConfig config) {
        super(config);
    }

    @Override
    public void loadProperties() {
        super.loadProperties();

        if(properties.getProperty("alphabet") != null)
            alphabet = properties.getProperty("alphabet");

        if(properties.getProperty("hostname") != null)
            hostname = properties.getProperty("hostname");

        if(properties.getProperty("port") != null)
            port = Integer.parseInt(properties.getProperty("port"));

        if(properties.getProperty("combine_query") != null)
            combine_query = Boolean.parseBoolean(properties.getProperty("combine_query"));
        else
            combine_query = false;

        if(properties.getProperty("delimiter_input") != null)
            delimiter_input = properties.getProperty("delimiter_input");
        else
            delimiter_input = ";";

        if(properties.getProperty("delimiter_output") != null)
            delimiter_output = properties.getProperty("delimiter_output");
        else
            delimiter_output = ";";
    }

    public boolean getCombineQuery() {
        return combine_query;
    }
}
