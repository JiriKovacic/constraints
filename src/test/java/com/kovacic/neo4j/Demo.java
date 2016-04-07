package com.kovacic.neo4j;

import com.graphaware.test.unit.GraphUnit;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

public class Demo {

    /**
     * Test class
     */
    @Test
    public void firstTest() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();

        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData transactionData) throws Exception {
                return null;//System.out.println("Before commit");
                //throw new RuntimeException("Schema constrained violated");
            }

            @Override
            public void afterCommit(TransactionData transactionData, Void aVoid) {
                System.out.println("Committed");

            }

            @Override
            public void afterRollback(TransactionData transactionData, Void aVoid) {

            }
        });


        try (Transaction tx = database.beginTx()) {

            Node michal = database.createNode();
            michal.setProperty("name", "Michal");

            Node jiri = database.createNode();
            jiri.setProperty("name", "Jiri");

            michal.createRelationshipTo(jiri, DynamicRelationshipType.withName("FRIEND"));

            tx.success();
        } catch (Exception e) {
            System.out.println("Failed for some reason: " + e.getMessage());
            e.printStackTrace();
        }


        GraphUnit.printGraph(database);
    }
}
