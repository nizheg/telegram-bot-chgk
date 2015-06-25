package info.chgk.db.service;

import info.chgk.db.xml.Search;
import info.chgk.db.xml.Tournament;

/**
 * //todo add comments
 *
 * @author Nikolay Zhegalin
 */
public interface TasksImporter {

    Search importTasks(int complexity);

    Tournament importTour(String id);
}
