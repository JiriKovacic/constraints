package com.kovacic.neo4j.schema;

import com.graphaware.test.unit.GraphUnit;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.Iterator;
import java.util.LinkedList;

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
        NodeTemplate constraint = new NodeTemplate("u:User", "u:email", "icUnique", "unique", "validate", "deferred", "restrict", "restrict", false);
        nodeConf.addNodeTemplate(constraint);
        // Defining integrity constraints for relationships
        relationshipConf.addRelationshipTemplate(new RelationshipTemplate());

        // Register configuration to Schema and start enforcement
        schemaConfiguration.registerConfiguration(nodeConf, relationshipConf);
        // possible examples
        // configuration.registerConfiguration(null, relationshipConf);
        // configuration.registerConfiguration(nodeConf, null);

        schemaConfiguration.getAllConfiguration();
    }

    @Test
    public void uniqueTest()
    {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData transactionData) throws Exception {

                //Iterator<Node> iterator = transactionData.createdNodes().iterator();
                String temp = new SchemaConfiguration().enforce(transactionData);
                System.out.println(temp);
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

            Node jiri = database.createNode();
            jiri.addLabel(new Label() {
                @Override
                public String name() {
                    return "User";
                }
            });
            jiri.setProperty("name", "Jiri");

            michal.createRelationshipTo(jiri, DynamicRelationshipType.withName("FRIEND"));



            tx.success();
        } catch (Exception e) {
            System.out.println("Failed for some reason: " + e.getMessage());
            e.printStackTrace();
        }

    }
}
