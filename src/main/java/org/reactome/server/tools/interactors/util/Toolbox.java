package org.reactome.server.tools.interactors.util;

import org.reactome.server.tools.interactors.model.Interaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class Toolbox {

    /**
     * Instead of calling the Double.valueOf(...) in a try-catch statement and many of the checks to fail due to
     * not being a number then performance of this mechanism will not be great, since you're relying upon
     * exceptions being thrown for each failure, which is a fairly expensive operation.
     * <p/>
     * An alternative approach may be to use a regular expression to check for validity of being a number:
     *
     * @return true if is Number
     */
    public static boolean isNumeric(String str) {
        return str != null && str.matches("-?\\d+(\\.\\d+)?");
    }

    /**
     * Retrieve the identifier url of a given accession
     *
     * @param acc is the Accession: Q13501 or CHEBI:16207
     * @return the identifier url
     */
    public static String getAccessionUrl(String acc) {
        String url = InteractorConstant.INTERACTOR_BASE_URL;
        if (acc.toUpperCase().contains("CHEBI")) {
            url = url.concat("chebi/").concat(acc);
        } else {
            /** Take into account the Uniprot Isoform **/
            if (isIsoform(acc)) {
                url = url.concat("uniprot.isoform/").concat(acc);
            } else {
                url = url.concat("uniprot/").concat(acc);
            }
        }

        return url;
    }

    /**
     * Check if accession is part of an isoform in Uniprot.
     */
    public static boolean isIsoform(String acc) {
        /** This regex is based on the identifiers.org **/
        String regex = "^([A-N,R-Z][0-9][A-Z][A-Z, 0-9][A-Z, 0-9][0-9])|([O,P,Q][0-9][A-Z, 0-9][A-Z, 0-9][A-Z, 0-9][0-9])(\\-\\d+)$";
        return acc.matches(regex);
    }

    /**
     * Retrieve the database name of a given accession.
     *
     * @param acc is the Accession: Q13501 or CHEBI:16207
     * @return Uniprot or ChEBI
     */
    public static String getDatabaseName(String acc) {
        String databaseName = "UniProt";
        if (acc.toUpperCase().contains("CHEBI")) {
            databaseName = "ChEBI";
        }
        return databaseName;
    }

    /**
     * Round score having three decimal places
     * The amount of zeros represents the decimal places.
     */
    public static Double roundScore(Double score) {
        return Math.round(score * 1000d) / 1000d;
    }

    /**
     * For the same Accession retrieve the list of interactors. If the interactors are the same we will
     * remove the duplicates and keep the one of highest score.
     * <p/>
     * Requirement: Keep only the one with highest score if the interactors are the same (with different identifiers)
     * e.g CHEBI:16027 (16027) for ChEMBL.
     */
    public static List<Interaction> removeDuplicatedInteractor(List<Interaction> interactions) {
        List<Interaction> ret = new ArrayList<>(interactions.size());

        MapSet<String, Interaction> interactionMapSet = new MapSet<>();

        /** Identify potential duplicates and put in a MapSet**/
        for (Interaction interaction : interactions) {
            /** When adding in the MapSet (TreeSet impl) it already sort the interaction by score **/
            interactionMapSet.add(interaction.getInteractorB().getAcc(), interaction);
        }

        /** Interactions in the MapSet have been sorted by score as defined in the Interaction.compareTo **/
        for (String accKey : interactionMapSet.keySet()) {
            Set<Interaction> interactionSet = interactionMapSet.getElements(accKey);
            if (interactionSet.size() >= 2) { // This interaction is not unique. Let's check the score
                Interaction highScoreInteraction = null;
                for (Interaction interaction : interactionSet) {
                    highScoreInteraction = interaction;
                }
                ret.add(highScoreInteraction);
            } else {
                /** Just have only one, just add it **/
                ret.add(interactionSet.iterator().next());
            }
        }

        return ret;

    }

    public static String getAccessionURL(String acc, String resource) {
        String retURL;
        ResourceURL resourceURL = ResourceURL.getByName(resource);

        boolean isChebi = acc.startsWith("CHEBI:");
        if (isChebi) {
            retURL = resourceURL.getChemical();
        } else {
            retURL = resourceURL.getProtein();
        }

        if (retURL != null) {
            retURL = retURL.replace("##ID##", acc);
        }

        return retURL;
    }

    public static String getEvidencesURL(List<String> evidences, String resource) {
        if (evidences == null || evidences.isEmpty()) {
            return null;
        }

        ResourceURL resourceURL = ResourceURL.getByName(resource);
        final String OR = "%20OR%20";

        String retURL = "";

        // Check if resource has interaction URL
        if (resourceURL.hasInteractionUrl()) {

            String term = "";
            String dbSource = resource.toUpperCase().replaceAll("-", "");

            // If resource is multivalue than the URL will concat the identifiers in the queryString
            if (resourceURL.isMultivalue()) {

                retURL = resourceURL.getInteraction().get(dbSource);

                for (int i = 0; i < evidences.size(); i++) {
                    String evidence = evidences.get(i);

                    term = term.concat(evidence);
                    if (i < evidences.size() - 1) {
                        term = term.concat(OR);
                    }
                }
            } else {
                term = evidences.get(0);

                // If evidences contains # then we have split and get the url accordingly to the dbsource
                if (evidences.get(0).contains("#")) {
                    // e.g IDBG-123123:innatedb
                    String[] eviArray = evidences.get(0).split("#");
                    term = eviArray[0];
                    dbSource = eviArray[1].toUpperCase();
                }

                retURL = resourceURL.getInteraction().get(dbSource);

            }

            if (term == null || term.isEmpty()) {
                retURL = null;
            } else {
                if (retURL == null) {
                    retURL = InteractorConstant.DEFAULT_INTERACTION_URL;
                }
                retURL = retURL.replace("##ID##", term);
            }
        }

        return retURL;
    }
}
