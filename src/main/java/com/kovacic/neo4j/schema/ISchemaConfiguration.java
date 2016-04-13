package com.kovacic.neo4j.schema;

import org.json.JSONObject;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;

import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public interface ISchemaConfiguration {
    List<JSONObject> getAllConfiguration();
    void printAllConfigurations(List<JSONObject> allConstraints);
    void registerConfiguration(Configuration nodeConf, Configuration relConf);
    String enforce(TransactionData transactionData, GraphDatabaseService databse);
}
