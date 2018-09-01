package me.nizheg.telegram.bot.chgk.work;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.sql.DataSource;

import lombok.NonNull;
import me.nizheg.telegram.bot.chgk.dto.PagingParameters;
import me.nizheg.telegram.bot.chgk.service.Properties;


public class JdbcBroadcastMessageDao implements BroadcastMessageDao {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;
    private BroadcastMessageDescriptionMapper broadcastMessageDescriptionMapper = new
            BroadcastMessageDescriptionMapper();


    public JdbcBroadcastMessageDao(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("broadcast_message")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public BroadcastMessagePackage createBroadcastToActiveChats(String data, String type, String status) {
        Number id = createBroadcastMessage(data, type);
        Map<String, Object> parameters = new HashMap<>(4);
        parameters.put("id", id);
        parameters.put("status", status);
        parameters.put("key", Properties.CHAT_ACTIVE_KEY);
        parameters.put("value", Boolean.TRUE.toString());
        template.update("insert into broadcast_message_receiver(broadcast_message_id, chat_id, status)\n"
                        + "select :id, t.chat_id, :status\n"
                        + "from (\n"
                        + "\tselect distinct(property.chat_id), max(using_time) as utm\n"
                        + "\tfrom property \n"
                        + "\tinner join used_task ut on ut.chat_id = property.chat_id\n"
                        + "\twhere key = :key and value = :value\n"
                        + "\tgroup by property.chat_id\n"
                        + "\torder by utm desc\n"
                        + ") as t",
                parameters
        );
        return getPackage(id.longValue());
    }

    @Override
    public BroadcastMessagePackage createBroadcastToChats(String data, String type, String status, List<Long> chatIds) {
        Number id = createBroadcastMessage(data, type);
        MapSqlParameterSource[] batchParameters = chatIds.stream()
                .map(chatId -> new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("chat_id", chatId)
                        .addValue("status", status))
                .toArray(MapSqlParameterSource[]::new);
        template.batchUpdate("insert into broadcast_message_receiver(broadcast_message_id, chat_id, status)\n"
                + "values (:id, :chat_id, :status)", batchParameters);
        return getPackage(id.longValue());
    }

    @Nonnull
    private Number createBroadcastMessage(String data, String type) {
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("data", data);
        parameters.put("type", type);
        return jdbcInsert.executeAndReturnKey(parameters);
    }

    @Override
    public List<BroadcastMessage> findByStatus(String status, int limit) {
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("status", status);
        parameters.put("limit", limit);
        return this.template.query("select m.id, m.data, m.type, r.status, r.chat_id "
                        + "from broadcast_message_receiver r "
                        + "inner join broadcast_message m on m.id = r.broadcast_message_id "
                        + "where r.status = :status order by r.number limit :limit", parameters,
                (rs, rowNum) -> {
                    BroadcastMessageDescription description = BroadcastMessageDescription.builder()
                            .id(rs.getLong("id"))
                            .data(rs.getString("data"))
                            .type(rs.getString("type"))
                            .build();
                    return BroadcastMessage.builder()
                            .description(description)
                            .chatId(rs.getLong("chat_id"))
                            .status(rs.getString("status"))
                            .build();
                });
    }

    @Override
    public void updateStatusByIdChatIdStatus(long id, long chatId, List<String> fromStatuses, String status) {
        if (fromStatuses == null || fromStatuses.isEmpty()) {
            return;
        }
        Map<String, Object> parameters = new HashMap<>(5);
        parameters.put("status", status);
        parameters.put("change_time", OffsetDateTime.now());
        parameters.put("id", id);
        parameters.put("chat_id", chatId);
        parameters.put("statuses", fromStatuses);
        template.update("update broadcast_message_receiver set status = :status, change_time = :change_time "
                        + "where broadcast_message_id = :id and chat_id = :chat_id and status in (:statuses)",
                parameters);
    }

    @Override
    public void updateStatusByIdStatus(long id, List<String> fromStatuses, String status, @Nullable Integer limit) {
        if (fromStatuses == null || fromStatuses.isEmpty()) {
            return;
        }
        Map<String, Object> parameters = new HashMap<>(5);
        parameters.put("status", status);
        parameters.put("change_time", OffsetDateTime.now());
        parameters.put("id", id);
        parameters.put("statuses", fromStatuses);

        StringBuilder conditionBuilder = new StringBuilder("select number from broadcast_message_receiver where "
                + "broadcast_message_id = :id and status in (:statuses) order by number");
        if (limit != null) {
            conditionBuilder.append(" limit :limit");
            parameters.put("limit", limit);
        }
        String updateQuery = String.format("update broadcast_message_receiver "
                + "set status = :status, change_time =:change_time "
                + "where number in (%s)", conditionBuilder.toString());
        template.update(updateQuery, parameters);
    }

    @Override
    public BroadcastMessagePackage getPackage(long broadcastId) {
        Map<String, Integer> statusesCount = new HashMap<>();
        BroadcastMessageDescription descriptions = this.template.queryForObject(
                "select id, data, type from broadcast_message where id = :id",
                Collections.singletonMap("id", broadcastId), broadcastMessageDescriptionMapper);
        this.template.query(
                "select r.status, count(r.*) \n"
                        + "from broadcast_message_receiver r \n"
                        + "inner join broadcast_message m on m.id = r.broadcast_message_id \n"
                        + "where m.id = :id\n"
                        + "group by r.status", Collections.singletonMap("id", broadcastId),
                (rs) -> {
                    statusesCount.put(rs.getString(1), rs.getInt(2));
                });

        return new BroadcastMessagePackage(descriptions, statusesCount);
    }

    @Override
    public List<BroadcastMessagePackage> getPackages(@NonNull PagingParameters pagingParameters) {
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("limit", pagingParameters.getLimit());
        parameters.put("offset", pagingParameters.getOffset());
        List<BroadcastMessageDescription> descriptions = this.template.query(
                "select id, data, type from broadcast_message order by id desc limit :limit offset :offset",
                parameters, broadcastMessageDescriptionMapper);
        if (descriptions.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> messageIds = descriptions.stream()
                .map(BroadcastMessageDescription::getId)
                .collect(Collectors.toList());
        Map<Long, Map<String, Integer>> statusesMapById = new HashMap<>();

        this.template.query(
                "select m.id, r.status, count(r.*) \n"
                        + "from broadcast_message_receiver r \n"
                        + "inner join broadcast_message m on m.id = r.broadcast_message_id \n"
                        + "where m.id in (:ids)\n"
                        + "group by m.id, r.status", Collections.singletonMap("ids", messageIds),
                (rs) -> {
                    Map<String, Integer> statuses = statusesMapById.computeIfAbsent(rs.getLong(1),
                            id -> new HashMap<>());
                    statuses.put(rs.getString(2), rs.getInt(3));
                });
        return descriptions.stream().map(broadcastMessageDescription -> {
            Map<String, Integer> statuses = statusesMapById.get(broadcastMessageDescription.getId());
            return new BroadcastMessagePackage(broadcastMessageDescription, statuses);
        }).collect(Collectors.toList());

    }

    private static class BroadcastMessageDescriptionMapper implements RowMapper<BroadcastMessageDescription> {

        @Override
        public BroadcastMessageDescription mapRow(@Nonnull ResultSet rs, int rowNum) throws SQLException {
            return BroadcastMessageDescription.builder()
                    .id(rs.getLong("id"))
                    .data(rs.getString("data"))
                    .type(rs.getString("type"))
                    .build();
        }
    }

}
