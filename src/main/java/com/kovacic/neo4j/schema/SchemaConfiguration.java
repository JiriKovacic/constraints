package com.kovacic.neo4j.schema;

import org.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
import org.neo4j.cypher.internal.compiler.v2_2.ast.In;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.kernel.api.Neo4jTypes;
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
        if (transactionData == null)
            throw new IllegalArgumentException("No transaction data");
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
            if (template.action.toLowerCase().equals("exists")) {
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

    // For unique clause data
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

    // For exists clause data
    private String validate(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {

        // Determine a template type
        switch (icType(template)) {
            case Mandatory:
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
                break;
            case Datatype:
                for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
                    Node node = item.next();
                    Iterator<Label> ll = node.getLabels().iterator();
                    while (ll.hasNext()) {
                        if (ll.next().name().equals(template.nodeLabel)) {
                            if (node.hasProperty(getPropertyName(template))) {
                                Object o = node.getProperty(getPropertyName(template));
                                // Boolean
                                if(getPropertyType(template).toLowerCase().equals("boolean"))
                                {
                                    if(!Boolean.valueOf(o.toString()))
                                        throw new IntegrityConstraintViolationException("The datatype property defined as " + template.nodeProperties + " required");
                                }
                                // Long
                                if(getPropertyType(template).toLowerCase().equals("long")) {
                                    try {
                                        Long.valueOf(o.toString());
                                    } catch (Exception e) {
                                        throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required");
                                    }
                                }
                                // Double
                                if(getPropertyType(template).toLowerCase().equals("double")) {
                                    try {
                                        Double.valueOf(o.toString());
                                    } catch (Exception e) {
                                        throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required");
                                    }
                                }
                                // Integer
                                if(getPropertyType(template).toLowerCase().equals("integer")) {
                                    try {
                                        Integer.valueOf(o.toString());
                                    } catch (Exception e) {
                                        throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required");
                                    }
                                }
                                // String
                                if(getPropertyType(template).toLowerCase().equals("string")) {
                                    try {
                                        String.valueOf(o);
                                    } catch (Exception e) {
                                        throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required");
                                    }
                                }
                                // Char
                                if(getPropertyType(template).toLowerCase().equals("char")) {
                                    try {
                                        String.valueOf((char)o);
                                    } catch (Exception e) {
                                        throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required");
                                    }
                                }

                            }
                        }
                    }
                }
                break;
            case Math:
                break;
            case Regex:
                break;
            case Error:
                throw new IntegrityConstraintViolationException("Not recognized the integrity constraint type...Try again...");
        }
        return "OK";
    }

    private TemplateType icType(NodeTemplate template) {
        String[] temp1 = template.nodeProperties.split(" ");
        String[] temp2 = template.nodeProperties.split("AS");
        System.out.println(temp1[0] + " " + temp1[1] + " " + temp1[2]);
        // Regular property
        if (temp1.length == temp2.length)
            return TemplateType.Mandatory;
        // Math
        if (temp1[1].contentEquals("<") || temp1[1].contentEquals(">") || temp1[1].contentEquals("<=") || temp1[1].contentEquals(">=") || temp1[1].contentEquals(">="))
            return TemplateType.Math;
        // Data type and regex
        if (temp1[1].toLowerCase().contentEquals("as")) {
            if (temp1[2].toLowerCase().contentEquals("boolean") || temp1[2].toLowerCase().contentEquals("long") || temp1[2].toLowerCase().contentEquals("double") || temp1[2].toLowerCase().contentEquals("string") || temp1[2].toLowerCase().contentEquals("char") || temp1[2].toLowerCase().contains("list"))
                return TemplateType.Datatype;
            else
                return TemplateType.Regex;
        }
        return TemplateType.Error;
    }

    private TemplateType icType(RelationshipTemplate template) {
        // Math
        // Data type
        // Regex
        return null;
    }

    private String getPropertyName(NodeTemplate template) {
        return template.nodeProperties.split(" ")[0];
    }

    private String getPropertyName(RelationshipTemplate template) {
        return template.relationshipProperties.split(" ")[0];
    }

    private String getPropertyType(NodeTemplate template) {
        return template.nodeProperties.split(" ")[2];
    }

    private String getPropertyType(RelationshipTemplate template) {
        return template.relationshipProperties.split(" ")[3];
    }
}
