package com.kovacic.neo4j.schema;

import javax.xml.soap.Node;
import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class NodeTemplate implements INodeTemplate{

    protected String nodeLabel;
    protected String nodeProperties;
    protected String icName;
    protected String enable;
    protected String validation;
    protected String delete;
    protected String update;
    protected Boolean icFinal;

    public NodeTemplate(){}

    public NodeTemplate(String nodeLabel, String nodeProperties,
                        String icName, String enable, String validation,
                        String delete, String update, Boolean icFinal)
    {
        this.nodeLabel = nodeLabel;
        this.nodeProperties = nodeProperties;
        this.icName = icName;
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
