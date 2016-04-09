package com.kovacic.neo4j.schema;

import org.json.JSONObject;
import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public interface ISchemaConfiguration {
    JSONObject getAllConfiguration();
    Boolean addNodeConfiguration(NodeTemplate template);
    Boolean addRelationshipConfiguration(RelationshipTemplate template);
}
