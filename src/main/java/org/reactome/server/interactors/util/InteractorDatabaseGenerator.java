package org.reactome.server.interactors.util;

import com.martiansoftware.jsap.*;
import org.reactome.server.interactors.database.InteractorsDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class InteractorDatabaseGenerator {

    static final Logger logger = LoggerFactory.getLogger(InteractorDatabaseGenerator.class);

    private static void generateNewDatabase(Connection connection) throws ClassNotFoundException {
        logger.info("Creating interactors database.");

        try {
            Statement statement = connection.createStatement();

            /** Create our tables **/
            logger.info("Creating table Interactor_Resource.");
            statement.executeUpdate(QueryStatement.CREATE_TABLE_INTERACTOR_RESOURCE);

            logger.info("Creating table Interaction Resource");
            statement.executeUpdate(QueryStatement.CREATE_TABLE_INTERACTION_RESOURCE);

            logger.info("Creating interactor");
            statement.executeUpdate(QueryStatement.CREATE_TABLE_INTERACTOR);

            logger.info("Creating interaction");
            statement.executeUpdate(QueryStatement.CREATE_TABLE_INTERACTION);

            logger.info("Creating participants");
            statement.executeUpdate(QueryStatement.CREATE_TABLE_INTERACTION_DETAILS);

            /** Create indexes **/
            logger.info("Creating indexes");
            statement.executeUpdate(QueryStatement.CREATE_INTERACTOR_ACC_INDEX);
            statement.executeUpdate(QueryStatement.CREATE_INTERACTOR_A_INDEX);
            statement.executeUpdate(QueryStatement.CREATE_INTERACTOR_B_INDEX);
            statement.executeUpdate(QueryStatement.CREATE_INTERACTION_DETAILS_ID_INDEX);

            /** Pre-populate tables **/
            logger.info("Populate table interaction resource");
            statement.executeUpdate(QueryStatement.INSERT_INTERACTION_RESOURCE_STATIC);

            logger.info("Populate table interactor resource");
            statement.executeUpdate(QueryStatement.INSERT_INTERACTOR_RESOURCE_UNDEFINED);
            statement.executeUpdate(QueryStatement.INSERT_INTERACTOR_RESOURCE_UNIPROT);
            statement.executeUpdate(QueryStatement.INSERT_INTERACTOR_RESOURCE_CHEBI);

            logger.info("Database has been created properly");

        } catch (SQLException e) {
            logger.error("Error creating interactor database", e);
        } catch (Exception e) {
            logger.error("Generic exception occurred. Please check stacktrace for further information", e);
        } finally {
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }

    public static void main(String[] args) throws JSAPException, ClassNotFoundException {
        // we can always start from a temp db and rename later on to a final...

        SimpleJSAP jsap = new SimpleJSAP(
                InteractorDatabaseGenerator.class.getName(),
                "A tool for creating Interactors Database, table generation and initial data.",
                new Parameter[]{
                        new FlaggedOption("interactors-database-path", JSAP.STRING_PARSER, null, JSAP.REQUIRED, 'g', "interactors-database-path",
                        "Interactor Database Path")
                }
        );

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        String database = config.getString("interactors-database-path");
        InteractorsDatabase interactors;
        try {

            File dbFile = new File(database);
            if(dbFile.exists()){
                logger.warn("Database already exists and it will be renamed.");
                if(!dbFile.renameTo(new File(dbFile.getPath() + "." + new Date().toString()))){
                    logger.warn("Database file has not been renamed properly");
                }
            }

            interactors = new InteractorsDatabase(database);
            generateNewDatabase(interactors.getConnection());

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
