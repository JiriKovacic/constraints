package com.kovacic.neo4j.schema;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public interface IConfiguration {
    List<NodeTemplate> getNodeRecords();
    List<RelationshipTemplate> getRelationshipRecords();
    JSONObject getConfiguration();
    Boolean addNodeTemplate(NodeTemplate template);
    Boolean addRelationshipTemplate(RelationshipTemplate template);
}
