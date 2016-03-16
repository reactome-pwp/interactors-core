package org.reactome.server.tools.interactors.tuple.parser;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.reactome.server.tools.interactors.tuple.exception.ParserException;
import org.reactome.server.tools.interactors.tuple.exception.TupleParserException;
import org.reactome.server.tools.interactors.tuple.model.ColumnDefinition;
import org.reactome.server.tools.interactors.tuple.model.CustomInteraction;
import org.reactome.server.tools.interactors.tuple.model.Summary;
import org.reactome.server.tools.interactors.tuple.model.UserDataContainer;
import org.reactome.server.tools.interactors.tuple.parser.response.Response;
import org.reactome.server.tools.interactors.tuple.token.TokenUtil;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.reactome.server.tools.interactors.tuple.parser.response.Response.*;

/**
 * @author Guilherme S Viteri <gviteri@ebi.ac.uk>
 */

public class ExtendedParser extends CommonParser {

    @Override
    public Summary parse(List<String> input) throws ParserException {

        /** File clean up **/
        File file = cleanUp(input);

        /** Store file content **/
        UserDataContainer userDataContainer = new UserDataContainer();

        /** Instantiate CsvBeanReader based on Standard Preferences **/
        ICsvBeanReader beanReader = null;
        try {
            beanReader = new CsvBeanReader(
                    new InputStreamReader(new FileInputStream(file)),
                    new CsvPreference.Builder(CsvPreference.STANDARD_PREFERENCE)
                            .ignoreEmptyLines(true)
                            .surroundingSpacesNeedQuotes(false)
                            .build());

            /** Get our column definition **/
            Map<String, ColumnDefinition> headerColumnMapping = getHeaderMapping();

            /** read the CSV header (and set any unwanted columns to null) **/
            String[] header = beanReader.getHeader(true);
            for (int i = 0; i < header.length; i++) {
                if (headerColumnMapping.containsKey(header[i].toUpperCase())) {
                    header[i] = headerColumnMapping.get(header[i]).attribute;
                } else {
                    header[i] = null;
                }
            }

            setCustomInterationFromCsvBeanReader(beanReader, header, userDataContainer);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (beanReader != null) {
                    beanReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        file.deleteOnExit();

        if (hasError()) {
            throw new TupleParserException("Error parsing your interactors overlay", errorResponses);
        }


        Summary summary = new Summary(TokenUtil.generateToken(), userDataContainer);

        summary.setErrorMessages(errorResponses);
        summary.setWarningMessages(warningResponses);
        //summary.setHeaderColumns(headerColumnNames);
        summary.setNumberOfInteractors(userDataContainer.getCustomInteractions().size());

        return summary;

    }

    private void setCustomInterationFromCsvBeanReader(ICsvBeanReader beanReader, String[] header, UserDataContainer userDataContainer) throws IOException {
        CustomInteraction customInteraction;

        try {
            /** Read method uses reflection in order to set all CustomInteraction attributes based on header **/
            while ((customInteraction = beanReader.read(CustomInteraction.class, header)) != null) {

                /** Check mandatory fields based on column definition enum **/
                List<String> mandatoryMessages = checkMandatoriesAttributes(customInteraction);
                if (mandatoryMessages.size() == 0) {

                    /** Check if an interaction exists based on AccessionA and AccessionB **/
                    if (userDataContainer.getCustomInteractions() != null && userDataContainer.getCustomInteractions().contains(customInteraction)) {
                        warningResponses.add(getMessage(DUPLICATE_AB, beanReader.getLineNumber(), customInteraction.getInteractorIdA(), customInteraction.getInteractorIdB()));
                    }

                    /** Flip a and b and check again if the interactions exists **/
                    customInteraction.flip(customInteraction.getInteractorIdA(), customInteraction.getInteractorIdB());

                    if (userDataContainer.getCustomInteractions() != null && userDataContainer.getCustomInteractions().contains(customInteraction)) {
                        warningResponses.add(getMessage(DUPLICATE_BA, beanReader.getLineNumber(), customInteraction.getInteractorIdB(), customInteraction.getInteractorIdA(), customInteraction.getInteractorIdA(), customInteraction.getInteractorIdB()));
                    } else {
                        /** Flip back to original form **/
                        customInteraction.flip(customInteraction.getInteractorIdA(), customInteraction.getInteractorIdB());

                        /** Add to the list **/
                        userDataContainer.addCustomInteraction(customInteraction);
                    }
                } else {
                    errorResponses.add(getMessage(MISSING_MANDATORY_FIELDS, beanReader.getLineNumber(), mandatoryMessages));
                }
            }
        } catch (IllegalArgumentException e) {
            /**
             * SuperCSV throws IllegalArgumentException (RuntimeException) when columns do not match.
             * In order to keep parsing, save the errorResponse and invoke the reader again.
             **/
            errorResponses.add(Response.getMessage(Response.COLUMN_MISMATCH, beanReader.getLineNumber(), header.length, beanReader.length()));
            setCustomInterationFromCsvBeanReader(beanReader, header, userDataContainer);
        }
    }


    private List<String> checkMandatoriesAttributes(CustomInteraction customInteraction) {
        List<String> mandatoriesList = new ArrayList<>();

        List<ColumnDefinition> mand = ColumnDefinition.getMandatoryColumns();
        for (ColumnDefinition columnDefinition : mand) {
            try {
                String getter = "get" .concat(StringUtils.capitalize(columnDefinition.attribute));
                Method method = customInteraction.getClass().getMethod(getter);
                Object returnValue = method.invoke(customInteraction);

                if (method.getReturnType().equals(String.class)) {
                    String returnValueStr = (String) returnValue;
                    if (StringUtils.isBlank(returnValueStr)) {
                        mandatoriesList.add(columnDefinition.name());
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }

        return mandatoriesList;

    }

    private Map<String, ColumnDefinition> getHeaderMapping() {
        /** key, column expected in the file value setter  **/
        Map<String, ColumnDefinition> columnMapping = new HashMap<>();

        columnMapping.put("ID A", ColumnDefinition.ID_A);
        columnMapping.put("ID B", ColumnDefinition.ID_B);
        columnMapping.put("ALIAS A", ColumnDefinition.ALIAS_A);
        columnMapping.put("ALIAS B", ColumnDefinition.ALIAS_B);
        columnMapping.put("TAX_ID A", ColumnDefinition.TAX_ID_A);
        columnMapping.put("TAX_ID B", ColumnDefinition.TAX_ID_B);
        columnMapping.put("EVIDENCE", ColumnDefinition.EVIDENCE);
        columnMapping.put("SCORE", ColumnDefinition.SCORE);

        return columnMapping;
    }

    private File cleanUp(List<String> input) throws ParserException {
        String header = "";
        int firstLineIndex = 0;
        for (String line : input) {
            firstLineIndex++;
            if (StringUtils.isNotEmpty(line)) {
                header = line.replaceAll("\\t+", ",");
                break;
            }
        }

        // is valid header
        if (!hasHeaderLine(header)) {
            errorResponses.add(Response.getMessage(Response.NO_HEADER_ERROR));
            throw new TupleParserException("Error parsing. Header is not present", errorResponses);
        }

        List<String> newInput = new ArrayList<>(input.size());
        newInput.add(header.replaceAll("^(#|//)", ""));

        String cleanLine;
        for (int i = firstLineIndex; i < input.size(); i++) {
            String line = input.get(i);
            if (StringUtils.isNotEmpty(line)) {
                cleanLine = line.trim().replaceAll("\\t+", ","); // convert to CSV file
                newInput.add(cleanLine);
            }
        }

        try {
            File tempFile = File.createTempFile(UUID.randomUUID().toString(), ".txt");
            FileUtils.writeLines(tempFile, newInput);

            return tempFile;

        } catch (IOException e) {
            throw new ParserException("Error parsing your file. ", e);
        }
    }

}