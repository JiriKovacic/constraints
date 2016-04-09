package com.kovacic.neo4j.schema;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public abstract class Configuration implements IConfiguration {

    private List<NodeTemplate> nodeRecords;
    private List<RelationshipTemplate> relationshipRecords;

    public Configuration()
    {
        nodeRecords = new LinkedList<>();
        relationshipRecords = new LinkedList<>();
    }

    @Override
    public List<RelationshipTemplate> getRelationshipRecords() {
        return relationshipRecords;
    }

    @Override
    public List<NodeTemplate> getNodeRecords()
    {
        return nodeRecords;
    }

    @Override
    public Configuration getConfiguration() {
        return null;
    }

    @Override
    public Boolean addNodeTemplate(NodeTemplate template) {
        System.out.println("Configuration fires " + template.icName);

        return this.nodeRecords.add(template);
    }

    @Override
    public Boolean addRelationshipTemplate(RelationshipTemplate template) {
        return this.relationshipRecords.add(template);
    }
}
