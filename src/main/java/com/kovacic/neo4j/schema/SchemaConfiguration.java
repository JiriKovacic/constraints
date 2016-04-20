package com.kovacic.neo4j.schema;

import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONObject;
import org.neo4j.cypher.internal.compiler.v1_9.parser.ParserPattern;
import org.neo4j.cypher.internal.compiler.v2_2.ast.In;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.PropertyEntry;
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
    private GraphDatabaseService databaseService = null;
    private static SchemaConfiguration schemaConfiguration = null;

    private SchemaConfiguration() {
    }

    public static SchemaConfiguration getInstance() {
        if (schemaConfiguration == null)
            schemaConfiguration = new SchemaConfiguration();
        return schemaConfiguration;
    }

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
    public String enforce(TransactionData transactionData, GraphDatabaseService database) {
        if (transactionData == null)
            throw new IllegalArgumentException("No transaction data");
        this.databaseService = database;
        // Disable this print
        printAllConfigurations(getAllConfiguration());
        String message = "";
        Iterator<NodeTemplate> nodeIter = nodeConfiguration.getNodeRecords().iterator();
        // Validation for nodes
        while (nodeIter.hasNext()) {
            NodeTemplate template = nodeIter.next();
            try {
                message = validate(transactionData, template);
            } catch (IntegrityConstraintViolationException e) {
                //e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }

        // Validation for relationships


        return message;
    }

    // For exists clause data
    private String validate(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {
        if (template.action.toLowerCase().equals("unique")) {
            return unique(transactionData, template);
        } else if (template.action.toLowerCase().equals("exists")) {
            // Determine a template type
            switch (icType(template)) {
                case Mandatory:
                    return mandatory(transactionData, template);
                case Datatype:
                    return dataType(transactionData, template);
                case Math:
                    return math(transactionData, template);
                case Regex:
                    return regex(transactionData, template);
                case Error:
                    throw new IntegrityConstraintViolationException("Not recognized the integrity constraint type...Try again...");
            }
        }
        return "OK";
    }

    private String unique(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {
        Boolean isMultipleProperties = isMultipleProperties(template);
        Object obj1 = null;
        Object obj2 = null;
        String message = "ok";
        if (template.getIcFinal())
            message = checkFinal(transactionData);
        if (template.validation.toLowerCase().equals("immediate")) {
            if (template.enable.equals("novalidate")) {
                // Check created nodes in transactionData
                for (Iterator<Node> node1 = transactionData.createdNodes().iterator(); node1.hasNext(); ) {
                    Node node = node1.next();
                    //obj1 = node.getProperty(getPropertyName(template));
                    Iterator<Label> ll1 = node.getLabels().iterator();
                    while (ll1.hasNext()) {
                        if (ll1.next().name().equals(template.nodeLabel)) {
                            if (node.hasProperty(getPropertyName(template))) {
                                for (Iterator<Node> node2 = transactionData.createdNodes().iterator(); node2.hasNext(); ) {
                                    Node anotherNode = node2.next();
                                    Iterator<Label> ll2 = anotherNode.getLabels().iterator();
                                    while (ll2.hasNext()) {
                                        if (ll2.next().name().equals(template.nodeLabel)) {
                                            if (anotherNode.hasProperty(getPropertyName(template))) {
                                                if (node.getId() != anotherNode.getId()) {
                                                    obj1 = node.getProperty(getPropertyName(template));
                                                    obj2 = anotherNode.getProperty(getPropertyName(template));
                                                    if (obj1.equals(obj2))
                                                        throw new IntegrityConstraintViolationException("The UNIQUE constraint property violation at " + template.nodeProperties + ", duplicity value: " + obj1.toString() + " ");
                                                }
                                            }
                                        }
                                    }
                                }
                            } //else
                                //throw new IntegrityConstraintViolationException("The UNIQUE constraint must contain a property " + template.nodeProperties);
                        }
                    }
                }
                // Check updated nodeValues in transactionData
                Node tempNode1 = null, tempNode2 = null;
                String key1 = "", key2 = "";
                Object value1 = null, value2 = null;
                for (Iterator<PropertyEntry<Node>> node1 = transactionData.assignedNodeProperties().iterator(); node1.hasNext(); ) {
                    obj1 = node1.next();
                    key1 = ((PropertyEntry<Node>) obj1).key();
                    value1 = ((PropertyEntry<Node>) obj1).value();
                    tempNode1 = ((PropertyEntry<Node>) obj1).entity();
                    Iterator<Label> ll1 = tempNode1.getLabels().iterator();
                    while (ll1.hasNext()) {
                        if (ll1.next().name().equals(template.nodeLabel)) {
                            if (tempNode1.hasProperty(getPropertyName(template))) {
                                for (Iterator<PropertyEntry<Node>> node2 = transactionData.assignedNodeProperties().iterator(); node2.hasNext(); ) {
                                    obj2 = node2.next();
                                    key2 = ((PropertyEntry<Node>) obj2).key();
                                    value2 = ((PropertyEntry<Node>) obj2).value();
                                    tempNode2 = ((PropertyEntry<Node>) obj2).entity();
                                    Iterator<Label> ll2 = tempNode2.getLabels().iterator();
                                    while (ll2.hasNext()) {
                                        if (ll2.next().name().equals(template.nodeLabel)) {
                                            if (tempNode2.hasProperty(getPropertyName(template))) {
                                                if (tempNode1.getId() != tempNode2.getId()) {
                                                    if (value1.equals(value2))
                                                        throw new IntegrityConstraintViolationException("The UNIQUE constraint property violation at " + template.nodeProperties + ", duplicity value: " + obj1.toString() + " ");
                                                }
                                            }
                                        }
                                    }
                                }
                            } //else
                               // throw new IntegrityConstraintViolationException("The UNIQUE constraint must contain a property " + template.nodeProperties);
                        }
                    }
                }
                // Check with whole DB
                // Not implemented yet
                message = checkUniqueWholeDb(template);
            }
        } else if (template.enable.equals("validate")) {
            checkUniqueWholeDb(template);
        }

// Check updated nodeValues with database
        return message;
    }

    private Boolean isMultipleProperties(NodeTemplate template)
    {
        String[] temp1 = template.getNodeProperties().split(",");
        String[] temp2 = template.getNodeProperties().split("&&");
        System.out.println(temp1[0] + " " + temp1[1]);
        // Regular property
        /*if (temp1.length == temp2.length)
            return TemplateType.Mandatory;
        // Math
        if (temp1[1].contentEquals("<") || temp1[1].contentEquals(">") || temp1[1].contentEquals("<=") || temp1[1].contentEquals(">=") || temp1[1].contentEquals("=="))
            return TemplateType.Math;
        // Data type and regex
        if (temp1[1].toLowerCase().contentEquals("as")) {
            if (temp1[2].toLowerCase().contentEquals("boolean") || temp1[2].toLowerCase().contentEquals("long") || temp1[2].toLowerCase().contentEquals("double") || temp1[2].toLowerCase().contentEquals("string") || temp1[2].toLowerCase().contentEquals("char") || temp1[2].toLowerCase().contains("list"))
                return TemplateType.Datatype;
            else if (temp1[2].contains("\""))
                return TemplateType.Regex;
        }*/
        return null;
    }

    private String checkUniqueWholeDb(NodeTemplate template) throws IntegrityConstraintViolationException {
        String message = "ok";
        Object obj1, obj2;
        ResourceIterator<Node> rin2;
        commitUniqueChanges(template);
        try (Transaction tx = this.databaseService.beginTx()) {
            ResourceIterator<Node> rin1 = this.databaseService.findNodes(new Label() {
                @Override
                public String name() {
                    return template.nodeLabel;
                }
            });
            while (rin1.hasNext()) {
                Node node1 = rin1.next();

                Iterator<Label> ll1 = node1.getLabels().iterator();
                //System.out.println(node1.getProperty("name"));
                rin2 = refreshResourceIterator(template.getNodeLabel());
                while (ll1.hasNext()) {
                    if (ll1.next().name().equals(template.nodeLabel)) {
                        if (node1.hasProperty(getPropertyName(template))) {
                            while (rin2.hasNext()) {
                                Node node2 = rin2.next();
                                Iterator<Label> ll2 = node2.getLabels().iterator();
                                while (ll2.hasNext()) {
                                    if (ll2.next().name().equals(template.nodeLabel)) {
                                        if (node2.hasProperty(getPropertyName(template))) {
                                            if (node1.getId() != node2.getId()) {
                                                obj1 = node1.getProperty(getPropertyName(template));
                                                obj2 = node2.getProperty(getPropertyName(template));
                                                if (obj1.equals(obj2))
                                                    throw new IntegrityConstraintViolationException("The UNIQUE constraint property violation at " + template.nodeProperties + ", duplicity value: " + obj1.toString() + " ");
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            tx.success();
        } catch (Exception e) {
            throw new IntegrityConstraintViolationException("The UNIQUE constraint property violation at " + template.nodeProperties + "; " + e.getMessage());
            //System.out.println("Failed unique validation with DB -> (Check created nodes with database failed): " + e.getMessage());
            //e.printStackTrace();
        }
        return message;
    }

    private void commitUniqueChanges(NodeTemplate template) throws IntegrityConstraintViolationException {
        // Commit assigned tx's
        try (Transaction tx = this.databaseService.beginTx()) {
            tx.success();
        } catch (Exception ex) {
            throw new IntegrityConstraintViolationException("The UNIQUE constraint property violation at (" + template.nodeProperties + "); placebo transaction failed");
        }
    }

    private ResourceIterator<Node> refreshResourceIterator(String nodeLableName)
    {
        return this.databaseService.findNodes(new Label() {
            @Override
            public String name() {
                return nodeLableName;
            }
        });
    }

    private String mandatory(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {
        String message = "";
        if (template.getIcFinal())
            message = checkFinal(transactionData);
        if (template.validation.toLowerCase().equals("immediate")) {
            if (template.enable.equals("novalidate")) {
                // Check created node properties
                for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
                    Node node = item.next();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mandatoryCheck(ll, node, template);
                }

                // Check updated node properties
                for (Iterator<PropertyEntry<Node>> item = transactionData.assignedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mandatoryCheck(ll, node, template);
                }

                // Check removed node properties
                for (Iterator<PropertyEntry<Node>> item = transactionData.removedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mandatoryCheck(ll, node, template);
                }
            } else if (template.enable.equals("validate")) {
                //Iterator<Node> item1 = transactionData.createdNodes().iterator();
                Iterator<PropertyEntry<Node>> item2 = transactionData.assignedNodeProperties().iterator();
                // Commit assigned tx's
                try (Transaction tx = this.databaseService.beginTx()) {
                    //while (item2.hasNext()) {
                    // if (item2.next().key().equals(template.getNodeProperties()))
                    tx.success();
                    //}
                } catch (Exception ex) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at " + template.nodeProperties + "; mandatory property required");
                }
                //Iterator<PropertyEntry<Node>> item3 = transactionData.removedNodeProperties().iterator();
                try (Transaction tx = this.databaseService.beginTx()) {
                    //Node n = database.findNode(lab1, getPropertyName(template), node.getProperty(getPropertyName(template)));
                    ResourceIterator<Node> rin = this.databaseService.findNodes(new Label() {
                        @Override
                        public String name() {
                            return template.nodeLabel;
                        }
                    });
                    while (rin.hasNext()) {
                        //System.out.println(rin.next().getProperty(getPropertyName(template)));
                        rin.next().getProperty(getPropertyName(template));
                    }
                    tx.success();
                } catch (Exception e) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at " + template.nodeProperties + "; mandatory property required");
                    //System.out.println("Failed unique validation with DB -> (Check created nodes with database failed): " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
        return message;
    }

    private String mandatoryCheck(Iterator<Label> label, Node node, NodeTemplate template) throws IntegrityConstraintViolationException {
        while (label.hasNext()) {
            if (label.next().name().equals(template.nodeLabel)) {
                if (!node.hasProperty(template.nodeProperties))
                    throw new IntegrityConstraintViolationException("The mandatory property " + template.nodeProperties + " required");
            }
        }
        return "ok";
    }

    private String checkFinal(TransactionData transactionData) throws IntegrityConstraintViolationException {
        Iterator<NodeTemplate> nodeIter = nodeConfiguration.getNodeRecords().iterator();
        List<String> templateProperties = new LinkedList<>();
        NodeTemplate template = new NodeTemplate();
        String message = "";
        while (nodeIter.hasNext()) {
            template = nodeIter.next();
            if (template.getIcFinal())
                templateProperties.add(template.nodeProperties);
        }

        for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
            Node node = item.next();
            Iterable<String> keys = node.getPropertyKeys();
            message = finalCheck(templateProperties, keys);
        }
        return message;
    }

    private String finalCheck(List<String> templateProperties, Iterable<String> keys) throws IntegrityConstraintViolationException {
        Integer keysCnt = 0, tempCnt = 0;
        for (Iterator<String> key = keys.iterator(); key.hasNext(); ) {
            keysCnt++;
            //while(templateProperties.iterator().hasNext())
            for (Iterator<String> templ = templateProperties.iterator(); templ.hasNext(); ) {
                if (key.next().equals(templ.next()))
                    tempCnt++;
            }
        }
        if (keysCnt.equals(tempCnt))
            return "ok";
        else
            throw new IntegrityConstraintViolationException("Data violates integrity constraint(s) with FINAL turned on TRUE");
    }

    private String dataType(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {
        String message = "";
        if (template.getIcFinal())
            message = checkFinal(transactionData);
        if (template.validation.toLowerCase().equals("immediate")) {
            if (template.enable.equals("novalidate")) {
                // Check created node properties
                for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
                    Node node = item.next();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = dataTypeCheck(ll, node, template);
                }
                // Check updated node properties
                for (Iterator<PropertyEntry<Node>> item = transactionData.assignedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = dataTypeCheck(ll, node, template);
                }
                // Check removed node properties -> Validation is not needed
                /*for (Iterator<PropertyEntry<Node>> item = transactionData.removedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mandatoryCheck(ll, node, template);
                }*/

            } else if (template.enable.equals("validate")) {
                // Check whole DB
                Iterator<PropertyEntry<Node>> item2 = transactionData.assignedNodeProperties().iterator();
                // Commit assigned tx's
                try (Transaction tx = this.databaseService.beginTx()) {
                    tx.success();
                } catch (Exception ex) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at (" + template.nodeProperties + "); placebo transaction failed");
                }
                try (Transaction tx = this.databaseService.beginTx()) {
                    ResourceIterator<Node> rin = this.databaseService.findNodes(new Label() {
                        @Override
                        public String name() {
                            return template.nodeLabel;
                        }
                    });
                    while (rin.hasNext()) {
                        //System.out.println(rin.next().getProperty(getPropertyName(template)));
                        //rin.next().getProperty(getPropertyType(template));
                        Node node = rin.next();
                        System.out.println(node.getProperty("name"));
                        if (node.hasProperty("active"))
                            System.out.println(node.getProperty("name") + " : " + node.getProperty("active"));
                        message = dataTypeCheck(node.getLabels().iterator(), node, template);
                    }
                    tx.success();
                } catch (Exception e) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at " + template.nodeProperties + "; " + e.getMessage());
                    //System.out.println("Failed unique validation with DB -> (Check created nodes with database failed): " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
        return message;
    }

    private String dataTypeCheck(Iterator<Label> ll, Node node, NodeTemplate template) throws IntegrityConstraintViolationException {
        while (ll.hasNext()) {
            if (ll.next().name().equals(template.nodeLabel)) {
                if (node.hasProperty(getPropertyName(template))) {
                    Object o = node.getProperty(getPropertyName(template));
                    // Boolean
                    if (getPropertyType(template).toLowerCase().equals("boolean")) {
                        if (!Boolean.valueOf(o.toString()))
                            throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required; Inserted value is " + o.toString());
                    }
                    // Long
                    if (getPropertyType(template).toLowerCase().equals("long")) {
                        try {
                            Long.valueOf(o.toString());
                        } catch (Exception e) {
                            throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required; Inserted value is " + o.toString());
                        }
                    }
                    // Double
                    if (getPropertyType(template).toLowerCase().equals("double")) {
                        try {
                            Double.valueOf(o.toString());
                        } catch (Exception e) {
                            throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required; Inserted value is " + o.toString());
                        }
                    }
                    // Integer
                    if (getPropertyType(template).toLowerCase().equals("integer")) {
                        try {
                            Integer.valueOf(o.toString());
                        } catch (Exception e) {
                            throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required; Inserted value is " + o.toString());
                        }
                    }
                    // String
                    if (getPropertyType(template).toLowerCase().equals("string")) {
                        try {
                            String.valueOf(o);
                        } catch (Exception e) {
                            throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required; Inserted value is " + o.toString());
                        }
                    }
                    // Char
                    if (getPropertyType(template).toLowerCase().equals("char")) {
                        try {
                            String.valueOf((char) o);
                        } catch (Exception e) {
                            throw new IntegrityConstraintViolationException("The datatype property defined as (" + template.nodeProperties + ") required; Inserted value is " + o.toString());
                        }
                    }
                }
            }
        }
        return "OK";
    }

    private String math(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {
        String message = "";
        if (template.getIcFinal())
            message = checkFinal(transactionData);
        if (template.validation.toLowerCase().equals("immediate")) {
            if (template.enable.equals("novalidate")) {
                // Check created node properties
                for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
                    Node node = item.next();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mathCheck(ll, node, template);
                }
                // Check updated node properties
                for (Iterator<PropertyEntry<Node>> item = transactionData.assignedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mathCheck(ll, node, template);
                }
                // Check removed node properties -> Validation is not needed
                /*for (Iterator<PropertyEntry<Node>> item = transactionData.removedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mandatoryCheck(ll, node, template);
                }*/

            } else if (template.enable.equals("validate")) {
                // Check whole DB
                //Iterator<PropertyEntry<Node>> item2 = transactionData.assignedNodeProperties().iterator();
                // Commit assigned tx's
                try (Transaction tx = this.databaseService.beginTx()) {
                    tx.success();
                } catch (Exception ex) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at (" + template.nodeProperties + "); placebo transaction failed");
                }
                try (Transaction tx = this.databaseService.beginTx()) {
                    ResourceIterator<Node> rin = this.databaseService.findNodes(new Label() {
                        @Override
                        public String name() {
                            return template.nodeLabel;
                        }
                    });
                    while (rin.hasNext()) {
                        //System.out.println(rin.next().getProperty(getPropertyName(template)));
                        //rin.next().getProperty(getPropertyType(template));
                        Node node = rin.next();
                        System.out.println(node.getProperty("name"));
                        if (node.hasProperty("active"))
                            System.out.println(node.getProperty("name") + " : " + node.getProperty("active"));
                        message = mathCheck(node.getLabels().iterator(), node, template);
                    }
                    tx.success();
                } catch (Exception e) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at " + template.nodeProperties + "; " + e.getMessage());
                    //System.out.println("Failed unique validation with DB -> (Check created nodes with database failed): " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
        return message;
    }

    private String mathCheck(Iterator<Label> ll, Node node, NodeTemplate template) throws IntegrityConstraintViolationException {
        while (ll.hasNext()) {
            if (ll.next().name().equals(template.nodeLabel)) {
                if (node.hasProperty(getPropertyName(template))) {
                    Object o = node.getProperty(getPropertyName(template));
                    if (NumberUtils.isNumber(o.toString())) {
                        switch (getMathSymbol(template)) {
                            case "<":
                                if (!(NumberUtils.createDouble(o.toString()) < NumberUtils.createDouble(getPropertyValue(template))))
                                    throw new IntegrityConstraintViolationException("The property value " + o.toString() + " violates (" + template.nodeProperties + ") constraint");
                                break;
                            case ">":
                                if (!(NumberUtils.createDouble(o.toString()) > NumberUtils.createDouble(getPropertyValue(template))))
                                    throw new IntegrityConstraintViolationException("The property value " + o.toString() + " violates (" + template.nodeProperties + ") constraint");
                                break;
                            case "<=":
                                if (!(NumberUtils.createDouble(o.toString()) <= NumberUtils.createDouble(getPropertyValue(template))))
                                    throw new IntegrityConstraintViolationException("The property value " + o.toString() + " violates (" + template.nodeProperties + ") constraint");
                                break;
                            case ">=":
                                if (!(NumberUtils.createDouble(o.toString()) >= NumberUtils.createDouble(getPropertyValue(template))))
                                    throw new IntegrityConstraintViolationException("The property value " + o.toString() + " violates (" + template.nodeProperties + ") constraint");
                                break;
                            case "==":
                                if (!(NumberUtils.createDouble(o.toString()) == NumberUtils.createDouble(getPropertyValue(template))))
                                    throw new IntegrityConstraintViolationException("The property value " + o.toString() + " violates (" + template.nodeProperties + ") constraint");
                                break;
                        }
                    }
                }
            }
        }
        return "ok";
    }

    private String regex(TransactionData transactionData, NodeTemplate template) throws IntegrityConstraintViolationException {
        String message = "";
        if (template.getIcFinal())
            message = checkFinal(transactionData);
        if (template.validation.toLowerCase().equals("immediate")) {
            if (template.enable.equals("novalidate")) {
                // Check created node properties
                for (Iterator<Node> item = transactionData.createdNodes().iterator(); item.hasNext(); ) {
                    Node node = item.next();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = regexCheck(ll, node, template);
                }
                // Check updated node properties
                for (Iterator<PropertyEntry<Node>> item = transactionData.assignedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = regexCheck(ll, node, template);
                }
                // Check removed node properties -> Validation is not needed
                /*for (Iterator<PropertyEntry<Node>> item = transactionData.removedNodeProperties().iterator(); item.hasNext(); ) {
                    Node node = item.next().entity();
                    Iterator<Label> ll = node.getLabels().iterator();
                    message = mandatoryCheck(ll, node, template);
                }*/

            } else if (template.enable.equals("validate")) {
                // Check whole DB
                //Iterator<PropertyEntry<Node>> item2 = transactionData.assignedNodeProperties().iterator();
                // Commit assigned tx's
                try (Transaction tx = this.databaseService.beginTx()) {
                    tx.success();
                } catch (Exception ex) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at (" + template.nodeProperties + "); placebo transaction failed");
                }
                try (Transaction tx = this.databaseService.beginTx()) {
                    ResourceIterator<Node> rin = this.databaseService.findNodes(new Label() {
                        @Override
                        public String name() {
                            return template.nodeLabel;
                        }
                    });
                    while (rin.hasNext()) {
                        //System.out.println(rin.next().getProperty(getPropertyName(template)));
                        //rin.next().getProperty(getPropertyType(template));
                        Node node = rin.next();
                        System.out.println(node.getProperty("name"));
                        if (node.hasProperty("active"))
                            System.out.println(node.getProperty("name") + " : " + node.getProperty("active"));
                        message = regexCheck(node.getLabels().iterator(), node, template);
                    }
                    tx.success();
                } catch (Exception e) {
                    throw new IntegrityConstraintViolationException("The EXISTS constraint property violation at " + template.nodeProperties + "; " + e.getMessage());
                    //System.out.println("Failed unique validation with DB -> (Check created nodes with database failed): " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        }
        return message;
    }

    private String regexCheck(Iterator<Label> ll, Node node, NodeTemplate template) throws IntegrityConstraintViolationException {
        while (ll.hasNext()) {
            if (ll.next().name().equals(template.nodeLabel)) {
                if (node.hasProperty(getPropertyName(template))) {
                    Object o = node.getProperty(getPropertyName(template));
                    if (!(o.toString().matches(getPropertyValue(template).substring(1, getPropertyValue(template).length() - 1))))
                        throw new IntegrityConstraintViolationException("The property value " + o.toString() + " violates (" + template.nodeProperties + ") constraint");
                }
            }
        }
        return "ok";
    }

    private TemplateType icType(NodeTemplate template) {
        String[] temp1 = template.nodeProperties.split(" ");
        String[] temp2 = template.nodeProperties.split("AS");
        //System.out.println(temp1[0] + " " + temp1[1] + " " + temp1[2]);
        // Regular property
        if (temp1.length == temp2.length)
            return TemplateType.Mandatory;
        // Math
        if (temp1[1].contentEquals("<") || temp1[1].contentEquals(">") || temp1[1].contentEquals("<=") || temp1[1].contentEquals(">=") || temp1[1].contentEquals("=="))
            return TemplateType.Math;
        // Data type and regex
        if (temp1[1].toLowerCase().contentEquals("as")) {
            if (temp1[2].toLowerCase().contentEquals("boolean") || temp1[2].toLowerCase().contentEquals("long") || temp1[2].toLowerCase().contentEquals("double") || temp1[2].toLowerCase().contentEquals("string") || temp1[2].toLowerCase().contentEquals("char") || temp1[2].toLowerCase().contains("list"))
                return TemplateType.Datatype;
            else if (temp1[2].contains("\""))
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
        return template.relationshipProperties.split(" ")[2];
    }

    private String getMathSymbol(NodeTemplate template) {
        return template.nodeProperties.split(" ")[1];
    }

    private String getMathSymbol(RelationshipTemplate template) {
        return template.relationshipProperties.split(" ")[1];
    }

    private String getPropertyValue(NodeTemplate template) {
        return template.nodeProperties.split(" ")[2];
    }

    private String getPropertyValue(RelationshipTemplate template) {
        return template.relationshipProperties.split(" ")[2];
    }
}
