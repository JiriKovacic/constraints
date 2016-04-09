package com.kovacic.neo4j.schema;

import javax.xml.soap.Node;
import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class NodeTemplate implements INodeTemplate{

    protected String nodeLabel; // label(s) of the node
    protected String nodeProperties; // properties of the node to be validated
    protected String icName; // integrity constraint name
    protected String action; // unique or exists
    protected String enable; // enable validate or novalidate
    protected String validation; // deferred or immediate validation
    protected String delete; // strict or cascade
    protected String update; // strict or cascade
    protected Boolean icFinal; // true or false for enabling or disabling changes to the node

    public NodeTemplate(){}

    public NodeTemplate(String nodeLabel, String nodeProperties,
                        String icName, String action, String enable, String validation,
                        String delete, String update, Boolean icFinal)
    {
        this.nodeLabel = nodeLabel;
        this.nodeProperties = nodeProperties;
        this.icName = icName;
        this.action = action;
        this.enable = enable;
        this.validation = validation;
        this.delete = delete;
        this.update = update;
        this.icFinal = icFinal;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getNodeProperties() {
        return nodeProperties;
    }

    public void setNodeProperties(String nodeProperties) {
        this.nodeProperties = nodeProperties;
    }

    public String getIcName() {
        return icName;
    }

    public void setIcName(String icName) {
        this.icName = icName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEnable() {
        return enable;
    }

    public void setEnable(String enable) {
        this.enable = enable;
    }

    public String getValidation() {
        return validation;
    }

    public void setValidation(String validation) {
        this.validation = validation;
    }

    public String getDelete() {
        return delete;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public String getUpdate() {
        return update;
    }

    public void setUpdate(String update) {
        this.update = update;
    }

    public Boolean getIcFinal() {
        return icFinal;
    }

    public void setIcFinal(Boolean icFinal) {
        this.icFinal = icFinal;
    }

    @Override
    public Boolean addNodeConstraint() {
        return null;
    }
}
