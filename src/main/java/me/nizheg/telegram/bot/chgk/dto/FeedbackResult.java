package me.nizheg.telegram.bot.chgk.dto;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public class FeedbackResult {
    private String link;
    private String errorDescription;

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }
}
