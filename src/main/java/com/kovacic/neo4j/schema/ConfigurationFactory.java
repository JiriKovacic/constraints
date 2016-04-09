package com.kovacic.neo4j.schema;

/**
 * Created by Jirka on 9. 4. 2016.
 */
public class ConfigurationFactory {

    public Configuration getConfiguration(ConfigurationType type) {

        if (type == ConfigurationType.NodeConfiguration)
        {
            return new NodeConfiguration();
        }
        else if(type == ConfigurationType.RelationshipConfiguration)
        {
            return new RelationshipConfiguration();
        }
        return null;
    }
}
