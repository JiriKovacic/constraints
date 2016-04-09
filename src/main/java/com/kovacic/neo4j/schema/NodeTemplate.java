package com.kovacic.neo4j.schema;

import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class NodeTemplate implements INodeTemplate{

    protected String nodeLabel;
    protected List<String> nodeProperties;
    protected String icName;
    protected String enable;
    protected String validation;
    protected String delete;
    protected String update;
    protected Boolean icFinal;

    @Override
    public Boolean addNodeConstraint() {
        return null;
    }
}
