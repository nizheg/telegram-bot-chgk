package info.chgk.db.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import info.chgk.db.service.TasksImporter;
import info.chgk.db.xml.Search;
import info.chgk.db.xml.Tournament;

/**
 * @author Nikolay Zhegalin
 */
@Service
public class TasksImporterImpl implements TasksImporter {

    @Value("${chgk.db.url}")
    private String sourceUrl;

    @Override
    public Search importTasks(int complexity) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(sourceUrl + "/xml/random/answers/types1/complexity" + complexity,
                Search.class);
    }

    @Override
    public Tournament importTour(String id) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(sourceUrl + "/tour/" + id + "/xml", Tournament.class);
    }
}
