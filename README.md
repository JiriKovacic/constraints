# Integrity constraints for Neo4j
## Intro
The integrity constraints for Neo4j is a prototype implementation of defining a database schema. There are implemented three types of constraints. 

* **Node property uniqueness**,
    * Single property value *(like email, name or whatever)*, or
    * two independent property values *(like firstName and lastName and so on)*.
* **Mandatory properties** for nodes,
    * A property that must be assigned *(i.e. email must have filled certain value)* 
* **Property value limitations** for nodes
    * Defined math rules *(user credits > 0)*,
    * basic datatypes *(like integer, string, bool etc.)* and
    * *regular expression* matching.

The implementation consists of Schema configuration interface **SchemaConfigurationAPI** for defining, registering and starting the database enforcement. The implementation structure can be found at diagrams folder where a class diagram is kept.

## Usage
Works as an *embedded mode* only. If you want to use this API in your own project there is required to use to put a produced .jar file into the project classpath or clone this repository, run `mvn clean package` and put the .jar file into your project classpath. This implementation uses some libraries from [https://github.com/graphaware](GraphAware). They are listed in *pom.xml* file.

### Example of defining a new integrity constraint
First paragraph is a schema definition. The second paragraph is a use of **SchemaConfigurationAPI** to *enforce* the database.

    SchemaConfiguration schemaConfiguration = SchemaConfiguration.getInstance();
    Configuration nodeConf = schemaConfiguration.configurationFactory.getConfiguration(ConfigurationType.NodeConfiguration);
    NodeTemplate constraintPersonMultUnique = new NodeTemplate("Person", "firstName, lastName", "icUniqueMultiplePerson", "unique", "novalidate", "immediate", "restrict", "restrict", false);

    schemaConfiguration.registerConfiguration(constraintPersonMultUnique, null);

    ...
    GraphDatabaseService database;
    database.registerTransactionEventHandler(new TransactionEventHandler<Void>()
    {
    @Override
    public Void beforeCommit(TransactionData transactionData) throws Exception
    {
         schemaConfiguration.enforce(transactionData, database);
         // Other operations
         return;
    }
    @Override
    public void afterCommit(...) {...}
    @Override
    public void afterRollback(...) {...}
    });
    
    ...
    
    try (Transaction tx = database.beginTx()) {
            
        // Should pass
        database.execute("create (p:Person {firstName:'Jitka', lastName:'Hodna'})");
        database.execute("create (p:Person {firstName:'Karla', lastName:'Hodna'})");
            
        // Should fail
        database.execute("create (p:Person {firstName:'Jitka', lastName:'Hodna'})");

        tx.success();
    } catch (Exception ex) {
        System.out.println(ex.getMessage());
    }
