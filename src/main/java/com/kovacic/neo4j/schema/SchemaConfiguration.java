package com.kovacic.neo4j.schema;

import org.json.JSONObject;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class SchemaConfiguration implements ISchemaConfiguration {
    @Override
    public JSONObject getAllConfiguration() {
        return null;
    }

    @Override
    public Boolean addNodeConfiguration(NodeTemplate template) {
        return null;
    }

    @Override
    public Boolean addRelationshipConfiguration(RelationshipTemplate template) {
        return null;
    }
}
