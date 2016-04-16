package com.kovacic.neo4j.schema;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.InvalidPropertiesFormatException;
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
    public List<Configuration> getConfiguration() {
        List<Configuration> configurations = new LinkedList<>();
        configurations.add(this);
        return configurations;
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
        try {
            nodeJsonParser(path);
            return true;
        }
        catch (IntegrityConstraintViolationException e)
        {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    // Not implemented yet
    public Boolean loadRelationshipConfiguration(String path) {
        return null;
    }

    private void nodeJsonParser(String path) throws IntegrityConstraintViolationException {

        if (!readFile(path))
            throw new IntegrityConstraintViolationException("Entered wrong file path " + path);
    }

    private void createNodeTemplate(String result) throws IntegrityConstraintViolationException {
        try {
            JSONObject obj = new JSONObject(result);
            JSONObject opt = new JSONObject(obj.get("options").toString());
            nodeRecords.add(new NodeTemplate(obj.get("pattern").toString(),
                    obj.get("properties").toString(),
                    obj.get("name").toString(),
                    obj.get("action").toString(),
                    opt.get("enable").toString(),
                    opt.get("validation").toString(),
                    opt.get("delete").toString(),
                    opt.get("update").toString(),
                    Boolean.valueOf(opt.get("final").toString())));
        } catch (Exception ex) {
            throw new IntegrityConstraintViolationException("Integrity constraint from .json file not loaded properly");
        }
    }

    // Not implemented yet
    private void relationshipJsonParser(String path) {

    }

    private Boolean readFile(String path) {
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
            return false;
        }
        return true;
    }
}
