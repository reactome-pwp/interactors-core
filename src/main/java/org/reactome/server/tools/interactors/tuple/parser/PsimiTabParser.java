package org.reactome.server.tools.interactors.tuple.parser;

import org.reactome.server.tools.interactors.tuple.exception.ParserException;
import org.reactome.server.tools.interactors.tuple.model.CustomInteraction;
import org.reactome.server.tools.interactors.tuple.model.Summary;
import org.reactome.server.tools.interactors.tuple.model.UserDataContainer;
import org.reactome.server.tools.interactors.tuple.token.TokenUtil;
import psidev.psi.mi.tab.PsimiTabException;
import psidev.psi.mi.tab.PsimiTabReader;
import psidev.psi.mi.tab.model.BinaryInteraction;
import psidev.psi.mi.tab.model.ConfidenceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */
public class PsimiTabParser implements Parser {

    @Override
    public Summary parse(List<String> input) throws ParserException {
        List<BinaryInteraction> binaryInteractions = new ArrayList<>();

        PsimiTabReader mitabReader = new PsimiTabReader();

        UserDataContainer udc = new UserDataContainer();
        //binaryInteractions.addAll(mitabReader.read(url));
        try {

            for(String line : input){
                binaryInteractions.add(mitabReader.readLine(line));
            }
        } catch (PsimiTabException e) {
            throw new ParserException("Error parsing PSI-MITAB file", e);
        }

        for(BinaryInteraction binaryInteraction : binaryInteractions){
            CustomInteraction customInteraction = new CustomInteraction();
            customInteraction.setInteractorIdA(binaryInteraction.getInteractorA().getIdentifiers().get(0).getIdentifier());
            customInteraction.setInteractorIdB(binaryInteraction.getInteractorB().getIdentifiers().get(0).getIdentifier());

            customInteraction.setConfidenceValue(((ConfidenceImpl)binaryInteraction.getConfidenceValues().get(0)).getValue());

            udc.addCustomInteraction(customInteraction);
        }

        Summary rtn = new Summary(TokenUtil.generateToken(), udc);
        //set errors ...

        return rtn;
    }
}