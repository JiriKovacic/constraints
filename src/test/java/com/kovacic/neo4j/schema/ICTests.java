package com.kovacic.neo4j.schema;

import com.graphaware.test.unit.GraphUnit;
import org.junit.Test;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.event.TransactionData;
import org.neo4j.graphdb.event.TransactionEventHandler;
import org.neo4j.test.TestGraphDatabaseFactory;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class ICTests {

    @Test
    public void SchemaEnforcement() {
        SchemaConfiguration configuration = new SchemaConfiguration();

    }
}
