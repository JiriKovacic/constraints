package com.kovacic.neo4j.schema;

import org.json.JSONObject;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public abstract class Configuration implements IConfiguration {

    @Override
    public JSONObject getConfiguration() {
        return null;
    }

    @Override
    public Boolean addNodeTemplate(NodeTemplate template) {
        System.out.println("Configuration fires " + template.icName);
        return null;
    }

    @Override
    public Boolean addRelationshipTemplate(RelationshipTemplate template) {
        return null;
    }
}
