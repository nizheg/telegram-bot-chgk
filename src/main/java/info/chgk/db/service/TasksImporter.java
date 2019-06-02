package info.chgk.db.service;

import java.time.LocalDate;

import info.chgk.db.xml.Search;
import info.chgk.db.xml.Tournament;

/**

 *
 * @author Nikolay Zhegalin
 */
public interface TasksImporter {

    Search importTasks(int complexity, LocalDate toDate);

    Tournament importTour(String id);
}
