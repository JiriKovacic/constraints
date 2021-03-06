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

    //private static String path = "C:\\Users\\Jirka\\Documents\\Neo4j\\default.graphdb";
    private static String path = "C:\\Users\\Jirka\\Documents\\Neo4j\\cineasts_12k_movies_50k_actors.db";

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
    public void loadInitialDataUser() {
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
    public void loadInitialDataPerson() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);

        try (Transaction tx = database.beginTx()) {
            database.execute("create (p:Person {firstName:'Pepa', lastName:'Midas'})");
            database.execute("create (p:Person {firstName:'Honza', lastName:'Koral'})");
            database.execute("create (p:Person {firstName:'Jitka', lastName:'Hodna'})");
            database.execute("create (p:Person {firstName:'Pavel', lastName:'Novotny'})");
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
    public void schemaDefinitonLoadJSON() {
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
    public void mandatoryPropertyTest() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        NodeTemplate constraintUser = new NodeTemplate("User", "name", "icMandatoryName", "exists", "validate", "immediate", "restrict", "restrict", true);
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
            //database.execute("create (u:User {email:'karel@test.com'})");
            //database.execute("create (u:User {name: 'Sofia', email:'sofia@test.com'})");
            //database.execute("MATCH (u:User { name: 'alois' }) SET u.name = NULL");

            // test final = true
            database.execute("create (u:User {name:'AAA'})");
            database.execute("create (u:User {name: 'BBB', email:'bbb@test.com'})");

            tx.success();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void dataTypePropertyTest() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        //CREATE CONSTRAINT (name:'UABool') ON
        //(u:User) ASSERT EXISTS(u.active AS BOOLEAN);

        NodeTemplate constraintUser = new NodeTemplate("User", "active AS Boolean", "UActiveBool", "exists", "validate", "immediate", "restrict", "restrict", true);
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
            //database.execute("create (u:User {name:'Vaclav', email:'Vaclav@test.com', active:'truee'})");
            // should pass
            //database.execute("create (u:User {name:'Vaclav', email:'Vaclav@test.com', active:'true'})");
            //database.execute("MATCH (u:User { name: 'karel2' }) SET u.name = 'Karel'");
            // should fail -> final = true
            //database.execute("create (u:User {name:'Vaclav22', email:'Vaclav22@test.com', active:'false'})");
            tx.success();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void mathPropertyTest() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        //CREATE CONSTRAINT (name:'UABool') ON
        //(u:User) ASSERT EXISTS(u.active AS BOOLEAN);

        NodeTemplate constraintUser = new NodeTemplate("User", "credits < 0", "UCredit", "exists", "validate", "immediate", "restrict", "restrict", false);
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
            database.execute("create (u:User {name:'Ptacek100', email:'ptacek@test.com', credits:'-100'})");
            // should pass
            //database.execute("create (u:User {name:'Vaclav', email:'Vaclav@test.com', active:'true'})");
            //database.execute("MATCH (u:User { name: 'karel2' }) SET u.name = 'Karel'");
            // should fail -> final = true
            //database.execute("create (u:User {name:'Vaclav22', email:'Vaclav22@test.com', active:'false'})");
            tx.success();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void regexPropertyTest() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        //CREATE CONSTRAINT (name:'UABool') ON
        //(u:User) ASSERT EXISTS(u.active AS BOOLEAN);

        NodeTemplate constraintRegexProp = new NodeTemplate("User", "email AS \"[aA-zZ]+?@[a-z].+[a-z]\"", "RegexMail", "exists", "validate", "immediate", "restrict", "restrict", false);
        nodeConf.addNodeTemplate(constraintRegexProp);
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
            // should pass
            //database.execute("create (u:User {name:'Amalka', email:'amalka@test.com'})");
            // should fail
            //database.execute("create (u:User {name:'Amalka11', email:'amalka11@test.com'})");

            // enable novalidate
            //should pass
            database.execute("create (u:User {name:'Emanuel', email:'emanuel@test.com'})");

            tx.success();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void uniqueTest() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        NodeTemplate constraintUserUnique = new NodeTemplate("User", "name", "icUniqueUser", "unique", "novalidate", "immediate", "restrict", "restrict", false);

        nodeConf.addNodeTemplate(constraintUserUnique);
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
            database.execute("create (u:User {name:'Jana', email:'jana@test.com'})");
            //database.execute("create (u:User {name:'Amalka', email:'amalka@test.com'})");
            // should fail
            //database.execute("match (u:User) set u.name = 'Jaja'");

            // enable novalidate
            //should pass
            //database.execute("create (u:User {name:'Emanuel', email:'emanuel@test.com'})");

            tx.success();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void uniqueMultipleTest() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        NodeTemplate constraintPersonMultUnique = new NodeTemplate("Person", "firstName, lastName", "icUniqueMultiplePerson", "unique", "novalidate", "immediate", "restrict", "restrict", false);
        //NodeTemplate constraintPersonMultUnique = new NodeTemplate("Person", "firstName && lastname", "icUniqueMultiplePerson", "unique", "novalidate", "immediate", "restrict", "restrict", false);

        nodeConf.addNodeTemplate(constraintPersonMultUnique);
        schemaConfiguration.registerConfiguration(nodeConf, null);

        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData transactionData) throws RuntimeException {
                long startTime = System.nanoTime();
                String temp = schemaConfiguration.enforce(transactionData, database);
                long estimatedTime = System.nanoTime() - startTime;
                System.out.println("Result " + temp + " estimatedTime " + estimatedTime + " [ns]");
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

            //database.execute("create (:User)");

            // enable novalidate
            // should pass
            //database.execute("create (p:Person {firstName:'Verca', lastName:'Hodna'})");
            //database.execute("create (p:Person {firstName:'Karla', lastName:'Hodna'})");
            //database.execute("create (p:Person {firstName:'Jitka', lastName:'Zla'})");

            // should fail
            database.execute("create (p:Person {firstName:'Jitka', lastName:'Hodna'})");

            // enable novalidate
            //should pass
            //database.execute("create (u:User {name:'Emanuel', email:'emanuel@test.com'})");

            tx.success();
        } catch (Exception ex) {
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

    @Test
    public void cineastsDBTest() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        Result res;
        try (Transaction tx = database.beginTx()) {
            ResourceIterator<Node> rin = database.findNodes(() -> "Director");
            while (rin.hasNext()) {
                Node node = rin.next();
                //if(node.hasProperty("name"))
                System.out.println(node.getProperty("name"));
            }
            tx.success();
        } catch (Exception e) {
            System.out.println("Cineasts failed");
        }
        database.shutdown();
    }

    @Test
    public void deleteSomeNode() {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        try (Transaction tx = database.beginTx()) {
            database.execute("match (n:Director {name:'Raj Nidimoru'}) WITH n SKIP 1 DETACH DELETE n");
            //database.execute("match (n:User {name:'Heidi Gower'}) WITH n SKIP 1 SET n.name = 'Heidi Gower II'");
            //database.execute("create (u:User {name:'test'})");
            tx.success();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        database.shutdown();
    }

    @Test
    public void cineastsUniqueUserTest() {
        SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
        Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
        NodeTemplate constraintPersonMultUnique = new NodeTemplate("Actor", "name AS string", "uniqueUserT", "exists", "validate", "immediate", "restrict", "restrict", false);
        //NodeTemplate constraintPersonMultUnique = new NodeTemplate("Person", "firstName && lastname", "icUniqueMultiplePerson", "unique", "novalidate", "immediate", "restrict", "restrict", false);

        nodeConf.addNodeTemplate(constraintPersonMultUnique);
        schemaConfiguration.registerConfiguration(nodeConf, null);

        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        database.registerTransactionEventHandler(new TransactionEventHandler<Void>() {
            @Override
            public Void beforeCommit(TransactionData transactionData) throws RuntimeException {
                long startTime = System.nanoTime();
                String temp = schemaConfiguration.enforce(transactionData, database);
                long estimatedTime = System.nanoTime() - startTime;
                System.out.println("Result " + temp + " estimatedTime " + estimatedTime + " [ns]");
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
            database.execute("create (:Pokus {active:'false'})");
            tx.success();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        //GraphUnit.printGraph(database);
        database.shutdown();
    }

    @Test
    public void neo4jUniqueTest()
    {
        GraphDatabaseService database = new TestGraphDatabaseFactory().newEmbeddedDatabase(new File(path));
        registerShutdownHook(database);
        try (Transaction tx = database.beginTx()) {
            long startTime = System.nanoTime();
            //database.execute("DROP INDEX ON :Director(name)");
            //database.execute("CREATE CONSTRAINT ON (u:Director) ASSERT u.name IS UNIQUE");
            database.execute("create (d:Director {name:'Cheri Pugh'})");
            tx.success();
            long estimatedTime = System.nanoTime() - startTime;
            System.out.println("estimatedTime " + estimatedTime + " [ns]");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        //GraphUnit.printGraph(database);
        database.shutdown();
    }
}
