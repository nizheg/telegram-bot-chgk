package me.nizheg.telegram.bot.chgk.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

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

    private static LocalDate V2_START_DATE = LocalDate.of(2019, Month.MARCH, 1);

    private final TasksImporter tasksImporter;
    private final TaskService taskService;

    @Override
    @Transactional
    public List<Task> loadTasks(int complexity) {
        LocalDate toDate = V2_START_DATE;
        Search search = tasksImporter.importTasks(complexity, toDate);
        return loadQuestions(search.getQuestion(), () -> newTaskBuilder(toDate));
    }

    @Override
    @Transactional
    public List<Task> loadTour(String id) {
        Tournament tournament = tasksImporter.importTour(id);
        List<Question> questions = tournament.getQuestion();
        ZonedDateTime zonedDateTime = tournament.getCreatedAt().toGregorianCalendar().toZonedDateTime();
        return loadQuestions(questions, () -> newTaskBuilder(zonedDateTime.toLocalDate()));
    }

    private List<Task> loadQuestions(List<Question> questions, Supplier<TaskBuilder> taskBuilderFactory) {
        List<Task> tasks = new ArrayList<>();
        for (Question question : questions) {
            if (taskService.isExist(question.getQuestion())) {
                taskService.getByText(question.getQuestion()).forEach(foundTask ->
                        updateTourInformation(foundTask, question)
                );
                continue;
            }

            Task task = taskBuilderFactory.get()
                    .questionText(question.getQuestion()).questionComment(question.getComments())
                    .tourIdAndNumber(question.getParentId(), question.getNumber())
                    .questionComplexity(question.getComplexity())
                    .questionAnswerAndPassCriteria(question.getAnswer(), question.getPassCriteria())
                    .build();
            tasks.add(taskService.create(task));
        }
        return tasks;
    }

    private TaskBuilder newTaskBuilder(LocalDate toDate) {
        if (toDate.isAfter(V2_START_DATE)) {
            return new TaskBuilderV2();
        }
        return new TaskBuilderV1();
    }

    private void updateTourInformation(LightTask foundTask, Question question) {
        if (foundTask.getTourId() == null) {
            taskService.changeStatus(foundTask.getId(), LightTask.Status.NEW);
            foundTask.setTourId(question.getParentId());
            foundTask.setNumberInTour(question.getNumber());
            taskService.update(foundTask);
            taskService.changeStatus(foundTask.getId(), foundTask.getStatus());
        }
    }

}
