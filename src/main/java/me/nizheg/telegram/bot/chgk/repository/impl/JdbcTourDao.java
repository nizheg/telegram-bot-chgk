package me.nizheg.telegram.bot.chgk.repository.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.dto.LightTask;
import me.nizheg.telegram.bot.chgk.dto.LightTour;
import me.nizheg.telegram.bot.chgk.dto.PageResult;
import me.nizheg.telegram.bot.chgk.dto.composite.LightTourWithStat;
import me.nizheg.telegram.bot.chgk.repository.TourDao;

/**
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcTourDao implements TourDao {

    private final Log logger = LogFactory.getLog(getClass());
    private final JdbcTemplate template;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert tourInsert;
    private final TourMapper tourMapper = new TourMapper();

    public JdbcTourDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.tourInsert = new SimpleJdbcInsert(dataSource).withTableName("tour");
        namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public boolean isTourExists(long tourId) {
        return template.queryForObject("select exists (select 1 from tour where id = ?)", Boolean.class, tourId);
    }

    @Override
    public LightTour create(LightTour lightTour) {
        Map<String, Object> parameters = new HashMap<>(7);
        parameters.put("id", lightTour.getId());
        parameters.put("parent_id", lightTour.getParentTourId());
        parameters.put("title", lightTour.getTitle());
        if (lightTour.getStatus() != null) {
            parameters.put("status", lightTour.getStatus().name());
        }
        if (lightTour.getType() != null) {
            parameters.put("type", lightTour.getType().name());
        }
        if (lightTour.getNumber() != null) {
            parameters.put("number", lightTour.getNumber());
        }
        if (lightTour.getPlayedAt() != null) {
            parameters.put("played_at", lightTour.getPlayedAt());
        }
        this.tourInsert.execute(parameters);
        return lightTour;
    }

    @Override
    public LightTour update(LightTour tour) {
        template.update("update tour set parent_id = ?, title = ?, status = ?, type = ?, number = ? where id = ?",
                tour.getParentTourId(), tour.getTitle(),
                (tour.getStatus() == null ? null : tour.getStatus().name()),
                (tour.getType() == null ? null : tour.getType().name()), tour.getNumber(),
                tour.getId());
        return tour;
    }

    @Override
    @CheckForNull
    public LightTour getById(long id) {
        try {
            return template.queryForObject("select * from tour where id = ?", tourMapper, id);
        } catch (IncorrectResultSizeDataAccessException ex) {
            logger.error("Error during retrieve tour " + id, ex);
            return null;
        }
    }

    @Override
    public List<LightTour> getByParentTour(long tourId) {
        return template.query("select * from tour where parent_id = ? order by number nulls first, id", tourMapper,
                tourId);
    }

    @Override
    public List<LightTour> getByTypeAndStatus(LightTour.Type type, LightTour.Status status) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("select * from tour where (1=1)");
        if (type != null) {
            sql.append(" and type = :type");
            parameters.addValue("type", type.name());
        }
        if (status != null) {
            sql.append(" and status = :status");
            parameters.addValue("status", status.name());
        }
        sql.append(" order by id");
        return namedParameterJdbcTemplate.query(sql.toString(), parameters, tourMapper);
    }

    @Override
    public PageResult<LightTour> getPublishedTournamentsByQuery(String query, int limit, int offset) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("type", LightTour.Type.TOURNAMENT.name());
        parameters.addValue("status", LightTour.Status.PUBLISHED.name());
        parameters.addValue("query", query);
        String queryFormat = "select * from tour where type=:type and status=:status and (%s) order by %s";
        String sql = String.format(queryFormat, "title~*:query", "title");
        List<LightTour> result = queryPaged(sql, limit, offset, parameters);
        if (query.length() > 4 && result.isEmpty()) {
            String similarity = "word_similarity(:query, title)";
            sql = String.format(queryFormat, similarity + ">=0.4", similarity);
            result = queryPaged(sql, limit, offset, parameters);
        }
        long count = namedParameterJdbcTemplate.queryForObject("select count(*) from (" + sql + ") as t",
                parameters, Long.class);
        return new PageResult<>(result, count);
    }

    private List<LightTour> queryPaged(String sql, int limit, int offset, MapSqlParameterSource parameters) {
        return namedParameterJdbcTemplate.query(sql + " limit " + limit + " offset "
                + limit * offset, parameters, tourMapper);
    }

    @Override
    public List<LightTourWithStat> getPublishedTournamentsWithStatForChat(long chatId, int limit, int offset) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("type", LightTour.Type.TOURNAMENT.name());
        parameters.addValue("status", LightTour.Status.PUBLISHED.name());
        parameters.addValue("taskStatus", LightTask.Status.PUBLISHED.name());
        parameters.addValue("chatId", chatId);
        String sql =
                "select tournament.id, tournament.parent_id, tournament.title, tournament.number, tournament.status, tournament.type, tournament.played_at,\n"
                        + "round(((-1)^(count(al.id) / count(t.id)) * count(al.id)::float / count(t.id)) * 100) as done, count(t.*) as tasks_count "
                        + "from task t\n" //
                        + "inner join tour on tour.id = t.tour_id\n" //
                        + "inner join tour tournament on tournament.id = tour.parent_id\n" //
                        + "left join answer_log al on al.chat_id = :chatId and al.task_id = t.id\n" //
                        + "where tournament.type = :type and tournament.status = :status and t.status = :taskStatus\n"
                        + "group by tournament.id, tournament.parent_id, tournament.title, tournament.number, tournament.status, tournament.type, tournament.played_at\n"
                        + "order by done desc, tournament.played_at desc nulls last limit " + limit + " offset "
                        + limit * offset;
        return namedParameterJdbcTemplate.query(sql, parameters, (rs, rowNum) -> {
            LightTour lightTour = tourMapper.mapRow(rs, rowNum);
            LightTourWithStat lightTourWithStat = new LightTourWithStat(lightTour);
            lightTourWithStat.setDonePercent(rs.getInt("done"));
            lightTourWithStat.setTasksCount(rs.getInt("tasks_count"));
            return lightTourWithStat;
        });
    }

    @Override
    public int getPublishedTournamentsCount() {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("type", LightTour.Type.TOURNAMENT.name());
        parameters.addValue("status", LightTour.Status.PUBLISHED.name());
        parameters.addValue("taskStatus", LightTask.Status.PUBLISHED.name());
        return namedParameterJdbcTemplate.queryForObject(
                "select count(distinct (tournament.*))\n" //
                        + "from task t\n" //
                        + "inner join tour on tour.id = t.tour_id\n" //
                        + "inner join tour tournament on tournament.id = tour.parent_id\n" //
                        + "where tournament.type = :type and tournament.status = :status and t.status = :taskStatus",
                parameters, Integer.class);
    }

    private static class TourMapper implements RowMapper<LightTour> {

        @Override
        public LightTour mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            Long parentId = (Long) rs.getObject("parent_id");
            String title = rs.getString("title");
            Integer number = (Integer) rs.getObject("number");
            String status = rs.getString("status");
            String type = rs.getString("type");
            Date playedAt = rs.getDate("played_at");
            LightTour lightTour = new LightTour();
            lightTour.setId(id);
            lightTour.setParentTourId(parentId);
            lightTour.setTitle(title);
            lightTour.setStatus(status == null ? null : LightTour.Status.valueOf(status));
            lightTour.setType(type == null ? null : LightTour.Type.valueOf(type));
            lightTour.setNumber(number);
            lightTour.setPlayedAt(playedAt);
            return lightTour;
        }
    }
}
