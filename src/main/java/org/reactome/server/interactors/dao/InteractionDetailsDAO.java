package org.reactome.server.interactors.dao;

import org.reactome.server.interactors.model.InteractionDetails;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public interface InteractionDetailsDAO {
    // add here something specific for Interactor

    boolean create(List<InteractionDetails> interactionDetails) throws SQLException;

    List<InteractionDetails> getByInteraction(Long interactionId) throws SQLException;

}
