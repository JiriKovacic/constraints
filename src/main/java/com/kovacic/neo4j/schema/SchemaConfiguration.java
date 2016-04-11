package com.kovacic.neo4j.schema;

import org.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import sun.awt.ConstrainableGraphics;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class SchemaConfiguration implements ISchemaConfiguration {
    protected ConfigurationFactory configurationFactory = new ConfigurationFactory();
    private Configuration nodeConfiguration = new NodeConfiguration();
    private Configuration relatinshipConfiguration = new RelationshipConfiguration();

    @Override
    public List<JSONObject> getAllConfiguration() {
        List<JSONObject> configuration = new LinkedList<>();

        Iterator<NodeTemplate> nodeIter = nodeConfiguration.getNodeRecords().iterator();

        while (nodeIter.hasNext()) {
            NodeTemplate template = nodeIter.next();
/*
            obj.put("icName", template.getIcName());
            obj.put("nodeLabel", template.getNodeLabel());
            obj.put("nodeProperties", template.getNodeProperties());
            obj.put("action", template.getAction());
            obj.put("enable", template.getEnable());
            obj.put("validation", template.getValidation());
            obj.put("delete", template.getDelete());
            obj.put("update", template.getUpdate());
            obj.put("icFinal", template.getIcFinal());*/
            configuration.add(new JSONObject().put("icName", template.getIcName())
                    .put("nodeLabel", template.getNodeLabel())
                    .put("nodeProperties", template.getNodeProperties())
                    .put("action", template.getAction())
                    .put("enable", template.getEnable())
                    .put("validation", template.getValidation())
                    .put("delete", template.getDelete())
                    .put("update", template.getUpdate())
                    .put("icFinal", template.getIcFinal())
            );
            //System.out.println(obj);
        }
        return configuration;
    }

    @Override
    public void printAllConfigurations(List<JSONObject> allConstraints) {
        Iterator<JSONObject> iterator = allConstraints.iterator();
        while(iterator.hasNext()) {
            System.out.println(iterator.next());
        }
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
        // Disable this print
        printAllConfigurations(getAllConfiguration());

        String temp = "";
        Iterator<Node> iterator = transactionData.createdNodes().iterator();

        while (iterator.hasNext()) {
            Node node = iterator.next();
            Object name = node.getProperty("name");
            System.out.println("Node id is " + node.getId() + " node label " + node.getLabels().iterator().next().name() + " name " + name);
            temp += "OK ";
        }

        return temp;
    }
}
