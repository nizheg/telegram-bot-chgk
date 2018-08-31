package me.nizheg.telegram.bot.chgk.work;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.service.Properties;


public class JdbcBroadcastMessageDao implements BroadcastMessageDao {

    private final NamedParameterJdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;


    public JdbcBroadcastMessageDao(DataSource dataSource) {
        this.template = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("broadcast_message")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public void createBroadcastToActiveChats(String data, String type, String status) {
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

    }

    @Override
    public void createBroadcastToChats(String data, String type, String status, List<Long> chatIds) {
        Number id = createBroadcastMessage(data, type);
        MapSqlParameterSource[] batchParameters = chatIds.stream()
                .map(chatId -> new MapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("chat_id", chatId)
                        .addValue("status", status))
                .toArray(MapSqlParameterSource[]::new);
        template.batchUpdate("insert into broadcast_message_receiver(broadcast_message_id, chat_id, status)\n"
                + "values (:id, :chat_id, :status)", batchParameters);
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
                    BroadcastMessage.BroadcastMessageBuilder builder = BroadcastMessage.builder()
                            .id(rs.getLong("id"))
                            .chatId(rs.getLong("chat_id"))
                            .data(rs.getString("data"))
                            .status(rs.getString("status"))
                            .type(rs.getString("type"));
                    return builder.build();
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
    public void updateStatusByIdStatus(long id, List<String> fromStatuses, String status) {
        if (fromStatuses == null || fromStatuses.isEmpty()) {
            return;
        }
        Map<String, Object> parameters = new HashMap<>(5);
        parameters.put("status", status);
        parameters.put("change_time", OffsetDateTime.now());
        parameters.put("id", id);
        parameters.put("statuses", fromStatuses);
        template.update(
                "update broadcast_message_receiver set status = :status, change_time = :change_time "
                        + "where broadcast_message_id = :id and status in (:statuses)",
                parameters);
    }
}
