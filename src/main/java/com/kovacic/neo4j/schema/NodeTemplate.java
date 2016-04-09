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

    @Override
    public Boolean addNodeConstraint() {
        return null;
    }
}
