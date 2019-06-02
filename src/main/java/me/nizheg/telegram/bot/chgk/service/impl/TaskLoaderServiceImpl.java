package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import info.chgk.db.service.TasksImporter;
import info.chgk.db.xml.Question;
import info.chgk.db.xml.Search;
import info.chgk.db.xml.Tournament;
import lombok.RequiredArgsConstructor;
import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.composite.Task;
import me.nizheg.telegram.bot.chgk.service.TaskBuilder;
import me.nizheg.telegram.bot.chgk.service.TaskLoaderService;
import me.nizheg.telegram.bot.chgk.service.TaskService;

/**
 * @author Nikolay Zhegalin
 */
@Service
@RequiredArgsConstructor
public class TaskLoaderServiceImpl implements TaskLoaderService {

    private final TasksImporter tasksImporter;
    private final TaskService taskService;

    @Override
    @Transactional
    public List<Task> loadTasks(int complexity) {
        Search search = tasksImporter.importTasks(complexity);
        return loadQuestions(search.getQuestion());
    }

    @Override
    @Transactional
    public List<Task> loadTour(String id) {
        Tournament tournament = tasksImporter.importTour(id);
        List<Question> questions = tournament.getQuestion();
        return loadQuestions(questions);
    }

    private List<Task> loadQuestions(List<Question> questions) {
        List<Task> tasks = new ArrayList<>();
        for (Question question : questions) {
            if (taskService.isExist(question.getQuestion())) {
                taskService.getByText(question.getQuestion()).forEach(foundTask ->
                        updateTourInformation(foundTask, question)
                );
                continue;
            }

            Task task = newTaskBuilder()
                    .questionText(question.getQuestion()).questionComment(question.getComments())
                    .tourIdAndNumber(question.getParentId(), question.getNumber())
                    .questionComplexity(question.getComplexity())
                    .questionAnswerAndPassCriteria(question.getAnswer(), question.getPassCriteria())
                    .build();
            tasks.add(taskService.create(task));
        }
        return tasks;
    }

    private TaskBuilder newTaskBuilder() {
        return new TaskBuilderImpl();
    }

    private void updateTourInformation(LightTask foundTask, Question question) {
        if (foundTask.getTourId() == null) {
            foundTask.setTourId(question.getParentId());
            foundTask.setNumberInTour(question.getNumber());
            taskService.update(foundTask);
        }
    }

}
