package com.kovacic.neo4j.schema;


import org.jcp.xml.dsig.internal.SignerOutputStream;
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

    public Configuration() {
        nodeRecords = new LinkedList<>();
        relationshipRecords = new LinkedList<>();
    }

    @Override
    public List<RelationshipTemplate> getRelationshipRecords() {
        return relationshipRecords;
    }

    @Override
    public List<NodeTemplate> getNodeRecords() {
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
        nodeJsonParser(path);
        return true;
    }

    @Override
    public Boolean loadRelationshipConfiguration(String path) {
        return null;
    }

    private void nodeJsonParser(String path) {
        readFile(path);
        /*JSONObject obj = new JSONObject(jsonData);
        System.out.println(obj.length());
        System.out.println("options: " + obj.get("name"));*/
        //System.out.println(jsonData);
    }

    private void createNodeTemplate(String result) {
        JSONObject obj = new JSONObject(result);
        //System.out.println("clause: " + obj.get("clause"));
        JSONObject opt = new JSONObject(obj.get("options").toString());
        System.out.println(obj.get("options").toString());//.subSequence(0, obj.get("options").toString().length()));
        System.out.println(opt.get("final"));
        /*nodeRecords.add(new NodeTemplate(obj.get("pattern"),
                obj.get("properties"),
                obj.get("name"),
                obj.get("acton"),
                obj.get("options"),
                obj.get("options")[0],
                obj.get("options")[0],
                obj.get("options")[0],
                obj.get("options")[0],
                ));*/
        //System.out.println(result);
    }

    private void relationshipJsonParser(String path) {

    }

    private void readFile(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                if (!line.equals(""))
                    sb.append(line);
                else {
                    createNodeTemplate(sb.toString());
                    sb.delete(0, sb.length());
                }
                line = br.readLine();
            }
            createNodeTemplate(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
