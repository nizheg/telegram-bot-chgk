package me.nizheg.telegram.bot.chgk.service.impl;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.nizheg.telegram.bot.api.util.TelegramHtmlUtil;
import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.TaskBuilder;

public class TaskBuilderImpl implements TaskBuilder {

    private static final String CATEGORY_PREFIX = "db";
    private static final Pattern PATTERN = Pattern.compile("(.*)\\[(.*)\\](.*)");
    private final Task task;

    public TaskBuilderImpl() {
        task = new Task();
        task.setAnswers(new ArrayList<>());
    }

    @Override
    public TaskBuilder tourIdAndNumber(long tourId, int number) {
        task.setTourId(tourId);
        task.setNumberInTour(number);
        return this;
    }

    @Override
    public TaskBuilder questionText(String taskText) {
        task.setImportedText(taskText);
        taskText = removeRedundantNewlines(taskText);
        taskText = resolveTags(taskText);
        List<AttachedPicture> attachedPictures = new ArrayList<>();
        taskText = parseImagesToList(taskText, attachedPictures);
        taskText = decorateBlitz(taskText);
        taskText = decorateDoublet(taskText);
        task.setText(taskText);
        task.setTextPictures(attachedPictures);
        return this;
    }

    @Override
    public TaskBuilder questionComment(String taskComment) {
        taskComment = removeRedundantNewlines(taskComment);
        taskComment = resolveTags(taskComment);
        List<AttachedPicture> attachedPictures = new ArrayList<>();
        taskComment = parseImagesToList(taskComment, attachedPictures);
        task.setComment(taskComment);
        task.setCommentPictures(attachedPictures);
        return this;
    }

    private String removeRedundantNewlines(String text) {
        final String newlineReplacement = "==newline==";
        final String newLineReplaceTarget = "\n   ";
        return text.replace(newLineReplaceTarget, newlineReplacement)
                .replace("\n", " ")
                .replace(newlineReplacement, newLineReplaceTarget);
    }

    private String resolveTags(String text) {
        return TelegramHtmlUtil.escape(text)
                .replace(TelegramHtmlUtil.escape("   <раздатка>"), "Раздаточный материал<i>")
                .replace(TelegramHtmlUtil.escape("</раздатка>"), "</i>");
    }

    private String parseImagesToList(String text, List<AttachedPicture> attachedPictures) {
        Pattern picPattern = Pattern.compile("\\(pic: (.*)\\)\n?");
        boolean isFound;
        int i = 0;
        do {
            Matcher matcher = picPattern.matcher(text);
            isFound = matcher.find();
            if (isFound) {
                int position = matcher.start();
                text = text.substring(0, position) + text.substring(matcher.end());
                AttachedPicture attachedPicture = new AttachedPicture();
                attachedPicture.setPosition((position + i++));
                attachedPicture.setSourceUrl("http://db.chgk.info/images/db/" + matcher.group(1));
                attachedPictures.add(attachedPicture);
            }
        } while (isFound);
        return text;
    }

    private String decorateBlitz(String text) {
        if (text.startsWith("Блиц")) {
            return text + "\n\n<i>Ответ вводить в формате \"ответ1 ответ2 ответ3\"</i>";
        }
        return text;
    }

    private String decorateDoublet(String text) {
        if (text.startsWith("Дуплет")) {
            return text + "\n\n<i>Ответ вводить в формате \"ответ1 ответ2\"</i>";
        }
        return text;
    }

    @Override
    public TaskBuilder questionAnswerAndPassCriteria(String answerText, String passCriteria) {
        answerText = StringUtils.removeEnd(answerText, ".");
        String[] answerCollection;
        Matcher matcher = PATTERN.matcher(answerText);
        if (matcher.matches()) {
            String first = matcher.group(1).trim();
            String second = matcher.group(2).trim();
            String third = matcher.group(3).trim();
            answerCollection = new String[] {(first + " " + third).trim(), (first + " " + second + " " + third).trim()};
        } else {
            answerCollection = new String[] {answerText};
        }

        for (String answerPart : answerCollection) {
            answerPart = StringUtils.removeEnd(answerPart, ".");
            answerPart = answerPart.replace("\"", "");
            Answer answer = new Answer();
            answer.setText(answerPart);
            task.getAnswers().add(answer);
        }
        setQuestionPassCriteriaByAnswer(answerText, passCriteria);
        return this;
    }

    private void setQuestionPassCriteriaByAnswer(String answerText, String passCriteria) {
        Matcher matcher;
        if (!StringUtils.isBlank(passCriteria)) {
            String[] additionalAnswers;
            matcher = PATTERN.matcher(passCriteria);
            if (matcher.matches()) {
                String first = matcher.group(1).trim();
                String second = matcher.group(2).trim();
                String third = matcher.group(3).trim();
                additionalAnswers = new String[] {first + third, first + second + third};
            } else if (passCriteria.contains(",") && !answerText.contains(",")) {
                additionalAnswers = passCriteria.split("\\s*,\\s*");
            } else if (passCriteria.contains(";") && !answerText.contains(";")) {
                additionalAnswers = passCriteria.split("\\s*;\\s*");
            } else {
                additionalAnswers = new String[] {passCriteria};
            }
            for (String additionalAnswer : additionalAnswers) {
                additionalAnswer = StringUtils.removeEnd(additionalAnswer, ".");
                additionalAnswer = additionalAnswer.replace("\"", "");
                Answer answer = new Answer();
                answer.setText(additionalAnswer);
                task.getAnswers().add(answer);
            }
        }
    }

    @Override
    public TaskBuilder questionComplexity(String complexity) {
        if (StringUtils.isNotBlank(complexity)) {
            task.setCategories(Collections.singletonList(CATEGORY_PREFIX + complexity));
        } else {
            task.setCategories(Collections.singletonList(Category.UNKNOWN_COMPLEXITY));
        }
        return this;
    }


    @Override
    public Task build() {
        return task;
    }
}
