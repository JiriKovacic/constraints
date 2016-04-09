package com.kovacic.neo4j.schema;

import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class NodeConfiguration extends Configuration {

    private List<NodeTemplate> nodeRecords = new LinkedList<>();

    public List<NodeTemplate> getNodeRecords()
    {
        return nodeRecords;
    }

    @Override
    public Boolean addNodeTemplate(NodeTemplate template) {
        System.out.println("NodeConfiguration fires " + template.icName);
        return null;
    }


}
