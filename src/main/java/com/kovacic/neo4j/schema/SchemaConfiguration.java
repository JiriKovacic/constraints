package com.kovacic.neo4j.schema;

import org.json.JSONObject;
import org.neo4j.graphdb.event.TransactionData;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class SchemaConfiguration implements ISchemaConfiguration {
    protected ConfigurationFactory configurationFactory = new ConfigurationFactory();
    private Configuration nodeConfiguration = new NodeConfiguration();
    private Configuration relatinshipConfiguration = new RelationshipConfiguration();

    @Override
    public JSONObject getAllConfiguration() {
        return null;
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
        return null;
    }
}
