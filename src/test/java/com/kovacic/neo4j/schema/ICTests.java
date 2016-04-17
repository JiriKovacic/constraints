package com.kovacic.neo4j.schema;

import com.graphaware.test.unit.GraphUnit;
import org.json.JSONObject;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.graphaware.common.util.DatabaseUtils.registerShutdownHook;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class ICTests {

    private static String path = "C:\\Users\\Jirka\\Documents\\Neo4j\\default.graphdb";

    @Test
    public void clearDB() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        try (Transaction tx = database.beginTx()) {
            database.execute("MATCH (n) OPTIONAL MATCH (n)-[r]-() DELETE n,r;");
            tx.success();
        } catch (Exception e) {
            System.out.println("Failed for some reason: " + e.getMessage());
            e.printStackTrace();
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void loadInitialData() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.execute("create (u:User {name:'Pepa', email:'pepa@test.com'})");
            database.execute("create (u:User {name:'Honza', email:'honza@test.com'})");
            database.execute("create (u:User {name:'Jirka', email:'jirka@test.com'})");
            database.execute("create (u:User {name:'Pavel', email:'pavel@test.com'})");
            tx.success();
        } catch (Exception e) {
            System.out.println("Failed for some reason: " + e.getMessage());
            e.printStackTrace();
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void schemaDefinition() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
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

        List<JSONObject> ics = schemaConfiguration.getAllConfiguration();
        Iterator<JSONObject> iterator = ics.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }

    @Test
    public void schemaDefinitonLoadJSON()
    {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        nodeConf.loadNodeConfiguration("./schemaConfigurations/nodeConfigs.json");
        schemaConfiguration.registerConfiguration(nodeConf, null);
        List<JSONObject> ics = schemaConfiguration.getAllConfiguration();
        Iterator<JSONObject> iterator = ics.iterator();
        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
        List<Configuration> conf = nodeConf.getConfiguration();
        nodeConf.deleteNodeTemplate("icUniqueUser");
        List<Configuration> conf2 = nodeConf.getConfiguration();
    }

    @Test
    public void mandatoryPropertyTest()
    {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        NodeTemplate constraintUser = new NodeTemplate("User", "name", "icMandatoryName", "exists", "validate", "immediate", "restrict", "restrict", false);
        nodeConf.addNodeTemplate(constraintUser);
        schemaConfiguration.registerConfiguration(nodeConf, null);

        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData transactionData) throws RuntimeException {
                String temp = schemaConfiguration.enforce(transactionData, database);
                System.out.println(temp);
                if (!temp.toLowerCase().equals("ok"))
                    throw new RuntimeException(temp);
                return null;
            }

            @Override
            public void afterCommit(TransactionData transactionData, Void aVoid) {

            }

            @Override
            public void afterRollback(TransactionData transactionData, Void aVoid) {

            }
        });

        try (Transaction tx = database.beginTx()) {

            // enable novalidate
            // should fail
            //database.execute("create (u:User {email:'karel@test.com'})");
            // should pass
            //database.execute("create (u:User {name: 'alois', email:'alois@test.com'})");
            // should fail - remove property
            //database.execute("MATCH (u:User { name: 'alois' }) SET u.name = NULL");

            // enable validate - should fail
            database.execute("create (u:User {email:'karel@test.com'})");
            database.execute("create (u:User {name: 'Sofia', email:'sofia@test.com'})");
            //database.execute("MATCH (u:User { name: 'alois' }) SET u.name = NULL");

            tx.success();
        }
        catch (Exception ex)
        {
            System.out.println(ex.getMessage());
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void schemaTests() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        // Defining integrity constraints for nodes
        /*
        NodeTemplate constraintUserEmailMandatory = new NodeTemplate("User", "email", "notNullMail", "exists", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintPropVal = new NodeTemplate("User", "email = abc", "'notNullMail", "exists", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintPerson = new NodeTemplate("p:Person", "p:username", "icUniquePerson", "unique", "validate", "deferred", "restrict", "restrict", false);
        NodeTemplate constraintRegexProp = new NodeTemplate("User", "email AS \"[aA-zZ]+?@[a-z].+[a-z]\"", "boolActive", "exists", "validate", "deferred", "restrict", "restrict", false);

        nodeConf.addNodeTemplate(constraintRegexProp);
        nodeConf.addNodeTemplate(constraintPropVal);
        nodeConf.addNodeTemplate(constraintUserEmailMandatory);
        nodeConf.addNodeTemplate(constraintPerson);
        // Register configuration to Schema
        schemaConfiguration.registerConfiguration(nodeConf, null);*/

        // Unique test
        NodeTemplate constraintUserUnique = new NodeTemplate("User", "name", "icUniqueUser", "unique", "validate", "deferred", "restrict", "restrict", false);
        nodeConf.addNodeTemplate(constraintUserUnique);
        schemaConfiguration.registerConfiguration(nodeConf, null);

        //GraphDatabaseService database = new TestGraphDatabaseFactory().newImpermanentDatabase();
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);

        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData transactionData) throws Exception {

                //Iterator<Node> iterator = transactionData.createdNodes().iterator();
                String temp = schemaConfiguration.enforce(transactionData, database);
                System.out.println(temp);
                if (!temp.toLowerCase().equals("ok"))
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

            /*Node michal = database.createNode();
            michal.addLabel(new Label() {
                @Override
                public String name() {
                    return "User";
                }
            });
            michal.setProperty("name", "Michal");
            michal.setProperty("email", "test@test.com");
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
            jiri.setProperty("email", "example@test.com");

            michal.createRelationshipTo(jiri, DynamicRelationshipType.withName("FRIEND"));*/

            //database.execute("create (u:User {name:'Pepa', email:'pepa@test.com'})");
            //database.execute("create (u:User {name:'Honza', email:'honza@test.com'})");
            database.execute("create (u:User {name:'Amalka', email:'amalka@test.com'})");
            //database.execute("create (u:User {name:'Pavel', email:'pavel@test.com'})");
            //database.execute("MATCH (u:User { name: 'Jirka' }) SET u.name = 'Taylor'");
            //database.execute("MATCH (u:User) SET u.name = 'Taylor'");

            tx.success();
        } catch (Exception e) {
            System.out.println("Failed for some reason: " + e.getMessage());
            e.printStackTrace();
        }
        GraphUnit.printGraph(database);

        /*try (Transaction tx = database.beginTx()) {
            database.execute("create (u:User {name:'Jirka', email:'jirka@test.com'})");
        } catch (Exception e) {
            System.out.println("Failed for some reason: " + e.getMessage());
            e.printStackTrace();
        }*/

        database.shutdown();
    }

    @Test
    public void databaseTest() {
        String path = "C:\\Users\\Jirka\\Documents\\Neo4j\\default.graphdb";
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        try (Transaction tx = database.beginTx()) {
            database.execute("match (n) return n;");
            // Database operations go here
            tx.success();
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }
}
