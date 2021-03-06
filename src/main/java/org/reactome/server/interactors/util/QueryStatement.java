package org.reactome.server.interactors.util;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class QueryStatement {

    /** CREATE TABLE STATEMENTS **/
    public static String CREATE_TABLE_INTERACTOR_RESOURCE = "CREATE TABLE INTERACTOR_RESOURCE (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR NOT NULL, URL VARCHAR NOT NULL)";
    public static String CREATE_TABLE_INTERACTION_RESOURCE = "CREATE TABLE INTERACTION_RESOURCE (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME VARCHAR NOT NULL, URL VARCHAR)";
    
    public static String CREATE_TABLE_INTERACTOR =
            "CREATE TABLE INTERACTOR ( " +
                 "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "ACC VARCHAR NOT NULL, " +
                "INTACT_ID VARCHAR, " +
                "INTERACTOR_RESOURCE_ID INTEGER NOT NULL, " +
                "CREATE_DATE DEFAULT CURRENT_TIMESTAMP, " +
                "ALIAS VARCHAR, " +
                "SYNONYMS VARCHAR, " +
                "TAXID INTEGER, " +
                "FOREIGN KEY(INTERACTOR_RESOURCE_ID) REFERENCES  INTERACTOR_RESOURCE(ID) " +
            ")";

    public static String CREATE_TABLE_INTERACTION =
            "CREATE TABLE INTERACTION ( " +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "INTERACTOR_A INTEGER NOT NULL, " +
                    "INTERACTOR_B INTEGER NOT NULL, " +
                    "AUTHOR_SCORE NUMERIC, " +
                    "MISCORE NUMERIC, " +
                    "INTERACTION_RESOURCE_ID NUMERIC, " +
                    "PUBMEDIDS VARCHAR(2048), " +
                    "FOREIGN KEY(INTERACTOR_A) REFERENCES INTERACTOR(ID), " +
                    "FOREIGN KEY(INTERACTOR_B) REFERENCES INTERACTOR(ID), " +
                    "FOREIGN KEY(INTERACTION_RESOURCE_ID) REFERENCES  INTERACTION_RESOURCE(ID) " +
             ")";

    public static String CREATE_TABLE_INTERACTION_DETAILS =
            "CREATE TABLE INTERACTION_DETAILS ( " +
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "INTERACTION_ID INTEGER," +
                    "INTERACTION_AC VARCHAR NOT NULL, " +
                    "FOREIGN KEY(INTERACTION_ID) REFERENCES INTERACTION(ID) " +
             ")";

    /** INSERTS **/
    /** Example:EBI-7121639 **/
    public static String INSERT_INTERACTOR_RESOURCE_UNDEFINED = "INSERT OR REPLACE INTO INTERACTOR_RESOURCE (NAME, URL) VALUES ('undefined','do-not-have-url')";
    public static String INSERT_INTERACTOR_RESOURCE_INTACT = "INSERT OR REPLACE INTO INTERACTOR_RESOURCE (NAME, URL) VALUES ('IntAct','https://www.ebi.ac.uk/intact/query/##ID##')";
    public static String INSERT_INTERACTOR_RESOURCE_UNIPROT = "INSERT OR REPLACE INTO INTERACTOR_RESOURCE (NAME, URL) VALUES ('UniProt','http://www.uniprot.org/uniprot/##ID##')";
    public static String INSERT_INTERACTOR_RESOURCE_CHEBI = "INSERT OR REPLACE INTO INTERACTOR_RESOURCE (NAME, URL) VALUES ('ChEBI','https://www.ebi.ac.uk/chebi/searchId.do?chebiId=##ID##')";
    public static String INSERT_INTERACTOR_RESOURCE_ENSEMBL = "INSERT OR REPLACE INTO INTERACTOR_RESOURCE (NAME, URL) VALUES ('ENSEMBL','http://ensemblgenomes.org/id/##ID##')";
    public static String INSERT_INTERACTOR_RESOURCE_EMBL = "INSERT OR REPLACE INTO INTERACTOR_RESOURCE (NAME, URL) VALUES ('EMBL','http://www.ebi.ac.uk/ena/data/view/##ID##')";

    public static String INSERT_INTERACTION_RESOURCE_STATIC = "INSERT OR REPLACE INTO INTERACTION_RESOURCE (NAME, URL) VALUES ('static','http://www.ebi.ac.uk/intact/interaction/##ID##')";

    /** CREATE INDEX STATEMENTS **/
    public static String CREATE_INTERACTOR_ACC_INDEX = "CREATE INDEX INTERACTOR_ACC_IDX ON INTERACTOR (ACC)";
    public static String CREATE_INTERACTOR_A_INDEX = "CREATE INDEX INTERACTION_INTERACTOR_A_IDX ON INTERACTION (INTERACTOR_A)";
    public static String CREATE_INTERACTOR_B_INDEX = "CREATE INDEX INTERACTION_INTERACTOR_B_IDX ON INTERACTION (INTERACTOR_B)";
    public static String CREATE_INTERACTION_DETAILS_ID_INDEX = "CREATE INDEX INTERACTION_DETAILS_INTERACTION_ID_IDX ON INTERACTION_DETAILS (INTERACTION_ID)";

    /** CREATE FOREIGN KEYS RELATIONSHIP **/


    /** CREATE FURTHER VALIDATION CONSTRAINTS **/


}
