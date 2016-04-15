package com.kovacic.neo4j.schema;



import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
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

    @Override
    public Boolean loadNodeConfiguration(String path) {
        File file = new File(path);
        System.out.println(file.exists());
        nodeJsonParser(path);
        return true;
    }

    @Override
    public Boolean loadRelationshipConfiguration(String path) {
        return null;
    }

    private void nodeJsonParser(String path)
    {
        String jsonData = readFile(path);
        JSONObject obj = new JSONObject(jsonData);
        System.out.println(obj.length());
        System.out.println("options: " + obj.get("clause"));
    }

    private void relationshipJsonParser(String path)
    {

    }

    private String readFile (String path)
    {
        String result = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                line = br.readLine();
            }
            result = sb.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
