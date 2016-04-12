package com.kovacic.neo4j.schema;

import org.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
import org.neo4j.cypher.internal.compiler.v2_2.ast.In;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
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
        while (iterator.hasNext()) {
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
        String message = "";
        Iterator<NodeTemplate> nodeIter = nodeConfiguration.getNodeRecords().iterator();
        // Validation for nodes
        while (nodeIter.hasNext()) {
            NodeTemplate template = nodeIter.next();
            /*if (template.action.toLowerCase() == "unique") {
                // do for unique
                try {
                    message = validate(transactionData);
                } catch (IntegrityConstraintViolationException e) {
                    //e.printStackTrace();
                    System.out.println(e.getMessage());
                }
            } else*/
            if (template.action.toLowerCase() == "exists") {
                // do for others
                try {
                    message = validate(transactionData, template);
                } catch (IntegrityConstraintViolationException e) {
                    //e.printStackTrace();
                    //System.out.println(message = e.getMessage());
                    message = e.getMessage();
                    break;
                }
            }
        }

        // Validation for relationships


        return message;
    }

    // For unique data
    private String validate(TransactionData transactionData) throws IntegrityConstraintViolationException {

        if (transactionData != null) {
            for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
                Label label = (Label) item.next().getLabels();
                for (Iterator<Node> item2 = transactionData.createdNodes().iterator(); item2.hasNext(); ) {
                    //item2.next().getLabels().iterator().next().name()
                }

                System.out.println(item.next());
            }
            throw new IntegrityConstraintViolationException("Error");

        }
        return "OK";
    }

    private String validate(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {

        // Determine a template type
        switch (icType(template)) {
            case Mandatory:
                if (transactionData != null) {
                    for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
                        Node node = item.next();
                        Iterator<Label> ll = node.getLabels().iterator();
                        while (ll.hasNext()) {
                            if (ll.next().name() == template.nodeLabel) {
                                if (!node.hasProperty(template.nodeProperties))
                                    throw new IntegrityConstraintViolationException("The mandatory property " + template.nodeProperties + " required");
                            }
                        }
                    }
                }break;
            case Datatype:
                break;
            case Math:
                break;
            case Regex:
                break;
        }
        return "OK";
    }

    private TemplateType icType(NodeTemplate template) {
        String[] temp1 = template.nodeProperties.split(" ");
        String[] temp2 = template.nodeProperties.split("AS");
        if (temp1.length == temp2.length)
            return TemplateType.Mandatory;
        // Math
        // Data type
        // Regex
        return null;
    }

    private TemplateType icType(RelationshipTemplate template) {
        // Math
        // Data type
        // Regex
        return null;
    }
}
