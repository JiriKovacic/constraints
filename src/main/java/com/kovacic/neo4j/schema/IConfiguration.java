package com.kovacic.neo4j.schema;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public interface IConfiguration {
    List<NodeTemplate> getNodeRecords();
    List<RelationshipTemplate> getRelationshipRecords();
    List<Configuration> getConfiguration();
    Boolean addNodeTemplate(NodeTemplate template);
    Boolean addRelationshipTemplate(RelationshipTemplate template);
    Boolean loadNodeConfiguration(String path);
    Boolean loadRelationshipConfiguration(String path);
}
