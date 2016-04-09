package com.kovacic.neo4j.schema;

import org.json.JSONObject;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public interface IConfiguration {
    JSONObject getConfiguration();
    Boolean addNodeTemplate(NodeTemplate template);
    Boolean addRelationshipTemplate(RelationshipTemplate template);
}
