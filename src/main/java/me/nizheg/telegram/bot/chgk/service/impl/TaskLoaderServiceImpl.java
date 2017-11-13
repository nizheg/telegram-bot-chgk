package me.nizheg.telegram.bot.chgk.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.nizheg.telegram.bot.chgk.dto.Answer;
import me.nizheg.telegram.bot.chgk.dto.AttachedPicture;
import me.nizheg.telegram.bot.chgk.dto.Category;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.Picture;
import me.nizheg.telegram.bot.chgk.service.TaskService;
import me.nizheg.telegram.bot.chgk.service.AnswerService;
import me.nizheg.telegram.bot.chgk.service.PictureService;
import me.nizheg.telegram.bot.chgk.service.TaskLoaderService;
import me.nizheg.telegram.util.TelegramHtmlUtil;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import info.chgk.db.service.TasksImporter;
import info.chgk.db.xml.Question;
import info.chgk.db.xml.Search;
import info.chgk.db.xml.Tournament;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
@Service
public class TaskLoaderServiceImpl implements TaskLoaderService {

    private static final String CATEGORY_PREFIX = "db";
    @Autowired
    private TasksImporter tasksImporter;
    @Autowired
    private TaskService taskService;
    @Autowired
    private AnswerService answerService;
    @Autowired
    private PictureService pictureService;
    private Pattern pattern = Pattern.compile("(.*)\\[(.*)\\](.*)");

    @Override
    @Transactional
    public List<LightTask> loadTasks(int complexity) {
        Search search = tasksImporter.importTasks(complexity);
        List<LightTask> tasks = loadQuestions(search.getQuestion());
        return tasks;
    }

    @Override
    @Transactional
    public List<LightTask> loadTour(String id) {
        Tournament tournament = tasksImporter.importTour(id);
        List<Question> questions = tournament.getQuestion();
        List<LightTask> tasks = loadQuestions(questions);
        return tasks;
    }

    private List<LightTask> loadQuestions(List<Question> questions) {
        List<LightTask> tasks = new ArrayList<LightTask>();
        for (Question question : questions) {
            if (taskService.isExist(question.getQuestion())) {
                List<LightTask> foundTasks = taskService.getByText(question.getQuestion());
                for (LightTask foundTask : foundTasks) {
                    if (foundTask.getTourId() == null) {
                        foundTask.setTourId(question.getParentId());
                        foundTask.setNumberInTour(question.getNumber());
                        taskService.update(foundTask);
                    }
                }
                continue;
            }

            LightTask task = new LightTask();
            task.setText(question.getQuestion());
            task.setImportedText(question.getQuestion());
            task.setComment(question.getComments());
            task.setTourId(question.getParentId());
            task.setNumberInTour(question.getNumber());
            task = taskService.create(task);
            upgradeTaskText(task);
            upgradeTaskComment(task);
            taskService.update(task);
            tasks.add(task);

            if (StringUtils.isNotBlank(question.getComplexity())) {
                taskService.addCategory(task.getId(), CATEGORY_PREFIX + question.getComplexity());
            } else {
                taskService.addCategory(task.getId(), Category.UNKNOWN_COMPLEXITY);
            }

            String answerText = question.getAnswer();
            answerText = StringUtils.removeEnd(answerText, ".");
            String[] answerCollection;
            Matcher matcher = pattern.matcher(answerText);
            if (matcher.matches()) {
                String first = matcher.group(1).trim();
                String second = matcher.group(2).trim();
                String third = matcher.group(3).trim();
                answerCollection = new String[] { (first + " " + third).trim(), (first + " " + second + " " + third).trim() };
            } else {
                answerCollection = new String[] { answerText };
            }

            for (String answerPart : answerCollection) {
                answerPart = StringUtils.removeEnd(answerPart, ".");
                answerPart = answerPart.replace("\"", "");
                Answer answer = new Answer();
                answer.setText(answerPart);
                answer.setTaskId(task.getId());
                answerService.create(answer);
            }

            String passCriteria = question.getPassCriteria();
            if (!StringUtils.isEmpty(passCriteria)) {
                String[] additionalAnswers;
                matcher = pattern.matcher(passCriteria);
                if (matcher.matches()) {
                    String first = matcher.group(1).trim();
                    String second = matcher.group(2).trim();
                    String third = matcher.group(3).trim();
                    additionalAnswers = new String[] { first + third, first + second + third };
                } else if (passCriteria.contains(",") && !answerText.contains(",")) {
                    additionalAnswers = passCriteria.split("\\s*,\\s*");
                } else if (passCriteria.contains(";") && !answerText.contains(";")) {
                    additionalAnswers = passCriteria.split("\\s*;\\s*");
                } else {
                    additionalAnswers = new String[] { passCriteria };
                }
                for (String additionalAnswer : additionalAnswers) {
                    additionalAnswer = StringUtils.removeEnd(additionalAnswer, ".");
                    additionalAnswer = additionalAnswer.replace("\"", "");
                    Answer answer = new Answer();
                    answer.setText(additionalAnswer);
                    answer.setTaskId(task.getId());
                    answerService.create(answer);
                }
            }
        }
        return tasks;
    }

    private void upgradeTaskText(LightTask task) {
        String taskText = task.getText();
        List<AttachedPicture> attachedPictures = new ArrayList<AttachedPicture>();
        taskText = removeRedundantNewlines(taskText);
        taskText = resolveTags(taskText);
        taskText = parseImagesToList(taskText, attachedPictures);
        taskText = decorateBlitz(taskText);
        taskText = decorateDoublet(taskText);
        task.setText(taskText);
        for (AttachedPicture attachedPicture : attachedPictures) {
            Picture savedPicture = pictureService.create(attachedPicture);
            pictureService.savePictureToTaskTextAtPosition(savedPicture.getId(), task.getId(), attachedPicture.getPosition());
        }
    }

    private void upgradeTaskComment(LightTask task) {
        String taskComment = task.getComment();
        List<AttachedPicture> attachedPictures = new ArrayList<AttachedPicture>();
        taskComment = removeRedundantNewlines(taskComment);
        taskComment = resolveTags(taskComment);
        taskComment = parseImagesToList(taskComment, attachedPictures);
        task.setComment(taskComment);
        for (AttachedPicture attachedPicture : attachedPictures) {
            Picture savedPicture = pictureService.create(attachedPicture);
            pictureService.savePictureToTaskCommentAtPosition(savedPicture.getId(), task.getId(), attachedPicture.getPosition());
        }
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

    private String removeRedundantNewlines(String text) {
        final String newlineReplacement = "==newline==";
        final String newLineReplaceTarget = "\n   ";
        return text.replace(newLineReplaceTarget, newlineReplacement).replace("\n", " ").replace(newlineReplacement, newLineReplaceTarget);
    }

    private String resolveTags(String text) {
        return TelegramHtmlUtil.escape(text).replace(TelegramHtmlUtil.escape("   <раздатка>"), "Раздаточный материал<i>")
                .replace(TelegramHtmlUtil.escape("</раздатка>"), "</i>");
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
}
