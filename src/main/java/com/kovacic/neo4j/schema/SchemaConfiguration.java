package com.kovacic.neo4j.schema;

import org.json.JSONObject;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;

import java.util.Iterator;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class SchemaConfiguration implements ISchemaConfiguration {
    protected ConfigurationFactory configurationFactory = new ConfigurationFactory();
    private Configuration nodeConfiguration = new NodeConfiguration();
    private Configuration relatinshipConfiguration = new RelationshipConfiguration();

    @Override
    public JSONObject getAllConfiguration() {
        return null;
    }

    @Override
    public void registerConfiguration(Configuration nodeConf, Configuration relConf) {

        if (nodeConf != null) {
            nodeConfiguration = nodeConf;
        }
        if (relConf != null) {
            relatinshipConfiguration = relConf;
        }
    }

    @Override
    public String enforce(TransactionData transactionData) {
        String temp = "";
        Iterator<Node> iterator = transactionData.createdNodes().iterator();

        while(iterator.hasNext()) {
            Node node = iterator.next();
            Object name = node.getProperty("name");
            System.out.println("Node id is " + node.getId() + " node label " + node.getLabels().iterator().next().name() + " name " + name);
            temp += "OK ";
        }

        return temp;
    }
}
