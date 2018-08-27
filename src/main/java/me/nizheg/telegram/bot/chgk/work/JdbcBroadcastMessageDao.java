package me.nizheg.telegram.bot.chgk.work;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import me.nizheg.telegram.bot.chgk.service.Properties;


public class JdbcBroadcastMessageDao implements BroadcastMessageDao {

    private final JdbcTemplate template;
    private final SimpleJdbcInsert jdbcInsert;


    public JdbcBroadcastMessageDao(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource).withTableName("broadcast_message")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    public void createBroadcastToActiveChats(String data, String type, String status) {
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("data", data);
        parameters.put("type", type);
        Number id = jdbcInsert.executeAndReturnKey(parameters);
        template.update("insert into broadcast_message_receiver(broadcast_message_id, chat_id, status)\n"
                        + "select ?, t.chat_id, ?\n"
                        + "from (\n"
                        + "\tselect distinct(property.chat_id), max(using_time) as utm\n"
                        + "\tfrom property \n"
                        + "\tinner join used_task ut on ut.chat_id = property.chat_id\n"
                        + "\twhere key = ? and value = ?\n"
                        + "\tgroup by property.chat_id\n"
                        + "\torder by utm desc\n"
                        + ") as t",
                id, status, Properties.CHAT_ACTIVE_KEY, Boolean.TRUE.toString());

    }

    @Override
    public void createBroadcastToChats(String data, String type, String status, List<Long> chatIds) {
        Map<String, Object> parameters = new HashMap<>(2);
        parameters.put("data", data);
        parameters.put("type", type);
        Number id = jdbcInsert.executeAndReturnKey(parameters);
        List<Object[]> batchParameters = chatIds.stream()
                .map(chatId -> new Object[] {id, chatId, status})
                .collect(Collectors.toList());
        template.batchUpdate("insert into broadcast_message_receiver(broadcast_message_id, chat_id, status)\n"
                + "values (?, ?, ?)", batchParameters);
    }

    @Override
    public List<BroadcastMessage> findByStatus(String status, int limit) {
        return this.template.query("select m.id, m.data, m.type, r.status, r.chat_id "
                        + "from broadcast_message_receiver r "
                        + "inner join broadcast_message m on m.id = r.broadcast_message_id "
                        + "where r.status = ? order by r.number limit ?",
                (rs, rowNum) -> {
                    BroadcastMessage.BroadcastMessageBuilder builder = BroadcastMessage.builder()
                            .id(rs.getLong("id"))
                            .chatId(rs.getLong("chat_id"))
                            .data(rs.getString("data"))
                            .status(rs.getString("status"))
                            .type(rs.getString("type"));
                    return builder.build();
                }, status, limit);
    }

    @Override
    public void updateStatus(long id, long chatId, String status) {
        template.update("update broadcast_message_receiver set status = ? "
                + "where chat_id = ? and broadcast_message_id = ?", status, chatId, id);
    }
}
