package io.jenkins.plugins.analysis.core.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.hm.hafner.analysis.Issue;
import edu.hm.hafner.analysis.Report;
import edu.hm.hafner.analysis.Severity;

import j2html.tags.DomContent;
import j2html.tags.UnescapedText;

import io.jenkins.plugins.analysis.core.model.StaticAnalysisLabelProvider.AgeBuilder;
import io.jenkins.plugins.analysis.core.util.LocalizedSeverity;
import io.jenkins.plugins.analysis.core.util.Sanitizer;

import static edu.hm.hafner.util.IntegerParser.*;
import static j2html.TagCreator.*;

/**
 * Provides the model for the issues details table. The model consists of the following parts:
 *
 * <ul>
 * <li>header name for each column</li>
 * <li>width for each column</li>
 * <li>content for each row</li>
 * <li>content for whole table</li>
 * </ul>
 *
 * @author Ullrich Hafner
 */
public abstract class DetailsTableModel {
    private static final Sanitizer SANITIZER = new Sanitizer();

    private final AgeBuilder ageBuilder;
    private final FileNameRenderer fileNameRenderer;
    private final DescriptionProvider descriptionProvider;

    /**
     * Creates a new instance of {@link DetailsTableModel}.
     *
     * @param ageBuilder
     *         renders the age column
     * @param fileNameRenderer
     *         renders the file name column
     * @param descriptionProvider
     *         renders the description text
     */
    public DetailsTableModel(final AgeBuilder ageBuilder, final FileNameRenderer fileNameRenderer,
            final DescriptionProvider descriptionProvider) {
        this.ageBuilder = ageBuilder;
        this.fileNameRenderer = fileNameRenderer;
        this.descriptionProvider = descriptionProvider;
    }

    /**
     * Returns the file name renderer.
     *
     * @return the file name renderer
     */
    protected FileNameRenderer getFileNameRenderer() {
        return fileNameRenderer;
    }

    /**
     * Returns the table headers of the report table.
     *
     * @param report
     *         the report to show
     *
     * @return the table headers
     */
    @SuppressWarnings("unused") // called by Jelly view
    public abstract List<String> getHeaders(Report report);

    @SuppressWarnings("unused") // called by Jelly view
    public abstract String getColumnsDefinition(Report report);

    /**
     * Returns the widths of the table headers of the report table.
     *
     * @param report
     *         the report to show
     *
     * @return the width of the table headers
     */
    @SuppressWarnings("unused") // called by Jelly view
    public abstract List<Integer> getWidths(Report report);

    /**
     * Converts the specified set of issues into a table.
     *
     * @param report
     *         the report to show in the table
     *
     * @return the table as String
     */
    public List<Object> getContent(final Report report) {
        List<Object> rows = new ArrayList<>();
        for (Issue issue : report) {
            rows.add(getRow(report, issue, descriptionProvider.getDescription(issue)));
        }
        return rows;
    }

    public abstract Object getRow(Report report, Issue issue, String description);

    /**
     * Formats the text of the details column. The details column is not directly shown, it rather is a hidden element
     * that is expanded if the corresponding button is selected. The actual text value is stored in the {@code
     * data-description} attribute.
     *
     * @param issue
     *         the issue in a table row
     * @param description
     *         description of the issue
     *
     * @return the formatted column
     */
    protected String formatDetails(final Issue issue, final String description) {
        UnescapedText details;
        if (StringUtils.isBlank(issue.getMessage())) {
            details = new UnescapedText(description);
        }
        else {
            details = join(p(strong().with(new UnescapedText(issue.getMessage()))), description);
        }
        return div().withClass("details-control").attr("data-description", render(details)).render();
    }

    /**
     * Formats the text of the age column. The age shows the number of builds a warning is reported.
     *
     * @param issue
     *         the issue in a table row
     *
     * @return the formatted column
     */
    protected String formatAge(final Issue issue) {
        return ageBuilder.apply(parseInt(issue.getReference()));
    }

    /**
     * Formats the text of the severity column.
     *
     * @param severity
     *         the severity of the issue
     *
     * @return the formatted column
     */
    protected String formatSeverity(final Severity severity) {
        return String.format("<a href=\"%s\">%s</a>",
                severity.getName(), LocalizedSeverity.getLocalizedString(severity));
    }

    /**
     * Formats the text of the specified property column. T he text actually is a link to the UI representation of the
     * property.
     *
     * @param property
     *         the property to format
     * @param value
     *         the value of the property
     *
     * @return the formatted column
     */
    protected String formatProperty(final String property, final String value) {
        return String.format("<a href=\"%s.%d/\">%s</a>", property, value.hashCode(), render(value));
    }

    /**
     * Formats the text of the file name column. The text actually is a link to the UI representation of the file.
     *
     * @param issue
     *         the issue to show the file name for
     *
     * @return the formatted file name
     */
    protected String formatFileName(final Issue issue) {
        return fileNameRenderer.renderAffectedFileLink(issue);
    }

    /**
     * Formats the text of the file name column. The text actually is a link to the UI representation of the file.
     *
     * @param issue
     *         the issue to show the file name for
     *
     * @return the formatted file name
     */
    protected DomContent getFileNameLink(final Issue issue) {
        return fileNameRenderer.createAffectedFileLink(issue);
    }

    /**
     * Renders the specified HTML code. Removes unsafe HTML constructs.
     *
     * @param text
     *         the HTML to render
     *
     * @return safe HTML
     */
    protected String render(final UnescapedText text) {
        return SANITIZER.render(text);
    }

    /**
     * Renders the specified HTML code. Removes unsafe HTML constructs.
     *
     * @param html
     *         the HTML to render
     *
     * @return safe HTML
     */
    protected String render(final String html) {
        return SANITIZER.render(html);
    }
}
