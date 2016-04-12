package com.kovacic.neo4j.schema;

import java.util.List;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class RelationshipTemplate implements IRelationshipTemplate {

    protected String relationshipType;
    protected String relationshipProperties;
    protected String icName;
    protected String enable;
    protected String validation;
    protected String delete;
    protected String update;
    protected Boolean icFinal;

    public RelationshipTemplate() {}

    public RelationshipTemplate(String relationshipType, String relationshipProperties,
                        String icName, String enable, String validation,
                        String delete, String update, Boolean icFinal)
    {
        this.relationshipType = relationshipType;
        this.relationshipProperties = relationshipProperties;
        this.icName = icName;
        this.enable = enable;
        this.validation = validation;
        this.delete = delete;
        this.update = update;
        this.icFinal = icFinal;
    }

    @Override
    public Boolean addRelationshipConstraint() {
        return null;
    }
}
