package com.kovacic.neo4j.schema;

import org.json.JSONObject;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class SchemaConfiguration implements ISchemaConfiguration {
    protected ConfigurationFactory configurationFactory = new ConfigurationFactory();

    @Override
    public JSONObject getAllConfiguration() {
        return null;
    }

}
