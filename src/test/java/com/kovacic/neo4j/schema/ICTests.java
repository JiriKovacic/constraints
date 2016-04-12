package com.kovacic.neo4j.schema;

import com.graphaware.test.unit.GraphUnit;
import org.json.JSONObject;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class ICTests {

    @Test
    public void schemaEnforcement() {
        SchemaConfiguration schemaConfiguration = new SchemaConfiguration();
        // Choosing configuration type
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        Configuration relationshipConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.RelationshipConfiguration);
        // Defining integrity constraints for nodes
        nodeConf.addNodeTemplate(new NodeTemplate());
        NodeTemplate constraintUser = new NodeTemplate("u:User", "u:email", "icUniqueUser", "unique", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintPerson = new NodeTemplate("p:Person", "p:username", "icUniquePerson", "unique", "validate", "deferred", "restrict", "restrict", false);
        nodeConf.addNodeTemplate(constraintUser);
        nodeConf.addNodeTemplate(constraintPerson);
        // Defining integrity constraints for relationships
        relationshipConf.addRelationshipTemplate(new RelationshipTemplate());

        // Register configuration to Schema and start enforcement
        schemaConfiguration.registerConfiguration(nodeConf, relationshipConf);
        // possible examples
        // configuration.registerConfiguration(null, relationshipConf);
        // configuration.registerConfiguration(nodeConf, null);

        /*List<JSONObject> ics = schemaConfiguration.getAllConfiguration();
        Iterator<JSONObject> iterator = ics.iterator();
        while(iterator.hasNext()) {
            System.out.println(iterator.next());
        }*/
    }

    @Test
    public void schemaTests()
    {
        SchemaConfiguration schemaConfiguration = new SchemaConfiguration();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        // Defining integrity constraints for nodes
        NodeTemplate constraintUserUnique = new NodeTemplate("u:User", "u:email", "icUniqueUser", "unique", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintUserEmailMandatory = new NodeTemplate("User", "email", "notNullMail", "exists", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintPropVal = new NodeTemplate("User", "email = abc", "'notNullMail", "exists", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintPerson = new NodeTemplate("p:Person", "p:username", "icUniquePerson", "unique", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintRegexProp = new NodeTemplate("User", "email AS \"[aA-zZ]+?@[a-z].+[a-z]\"", "boolActive", "exists", "validate", "deferred", "restrict", "restrict", false);

        nodeConf.addNodeTemplate(constraintRegexProp);
        nodeConf.addNodeTemplate(constraintPropVal);
        nodeConf.addNodeTemplate(constraintUserUnique);
        nodeConf.addNodeTemplate(constraintUserEmailMandatory);
        nodeConf.addNodeTemplate(constraintPerson);



        nodeConf.addNodeTemplate(constraintPerson);
        // Register configuration to Schema
        schemaConfiguration.registerConfiguration(nodeConf, null);




        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData transactionData) throws Exception {

                //Iterator<Node> iterator = transactionData.createdNodes().iterator();
                String temp = schemaConfiguration.enforce(transactionData);
                System.out.println(temp);
                if(temp.toLowerCase() != "ok")
                    throw new RuntimeException(temp);
                /*while(iterator.hasNext()) {
                    Node node = iterator.next();
                    Object name = node.getProperty("name");
                    System.out.println("Node id is " + node.getId() + " name " + name);
                }*/
                return null;
            }

            @Override
            public void afterCommit(TransactionData transactionData, Void aVoid) {
                System.out.println("Committed");
            }

            @Override
            public void afterRollback(TransactionData transactionData, Void aVoid) {
                System.out.println("Rollbacked");
            }
        });

        try (Transaction tx = database.beginTx()) {

            Node michal = database.createNode();
            michal.addLabel(new Label() {
                @Override
                public String name() {
                    return "User";
                }
            });
            michal.setProperty("name", "Michal");
            michal.setProperty("email", "Michal@graph.com");
            michal.addLabel(new Label() {
                @Override
                public String name() {
                    return "Author";
                }
            });


            Node jiri = database.createNode();
            jiri.addLabel(new Label() {
                @Override
                public String name() {
                    return "User";
                }
            });
            jiri.setProperty("name", "Jiri");
            jiri.setProperty("active", 0);

            michal.createRelationshipTo(jiri, DynamicRelationshipType.withName("FRIEND"));



            tx.success();
        } catch (Exception e) {
            System.out.println("Failed for some reason: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
