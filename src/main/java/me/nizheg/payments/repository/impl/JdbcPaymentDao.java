package me.nizheg.payments.repository.impl;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.dto.PaymentTransaction;
import me.nizheg.payments.repository.PaymentDao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

/**
 * @author Nikolay Zhegalin
 */
public class JdbcPaymentDao implements PaymentDao {

    private JdbcTemplate template;
    private SimpleJdbcInsert paymentTransactionInsert;
    private PaymentTransactionMapper paymentTransactionMapper = new PaymentTransactionMapper();

    public void setDataSource(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
        this.paymentTransactionInsert = new SimpleJdbcInsert(dataSource).withTableName("payment").usingGeneratedKeyColumns("id");
    }

    @Override
    public long create(PaymentTransaction paymentTransaction) {
        Map<String, Object> parameters = new HashMap<String, Object>(3);
        parameters.put("telegram_user_id", paymentTransaction.getTelegramUserId());
        parameters.put("creation_time", paymentTransaction.getCreationTime());
        parameters.put("sum", paymentTransaction.getSum());
        if (paymentTransaction.getStatus() != null) {
            parameters.put("status", paymentTransaction.getStatus().name());
        }
        parameters.put("result", paymentTransaction.getResult());
        long id = paymentTransactionInsert.executeAndReturnKey(parameters).longValue();
        return id;
    }

    @Override
    public void updateStatus(long transactionId, PaymentStatus status, String details) {
        template.update("update payment set status = ?, result = ? where id = ?", status.name(), details, transactionId);
    }

    @Override
    public PaymentTransaction getById(long transactionId) {
        return template.queryForObject("select * from payment where id = ?", paymentTransactionMapper, transactionId);
    }

    private static class PaymentTransactionMapper implements RowMapper<PaymentTransaction> {

        @Override
        public PaymentTransaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            long telegramUserId = rs.getLong("telegram_user_id");
            Date creationTime = rs.getTimestamp("creation_time");
            BigDecimal sum = rs.getBigDecimal("sum");
            String status = rs.getString("status");
            String result = rs.getString("result");
            PaymentTransaction paymentTransaction = new PaymentTransaction(id, telegramUserId, sum, creationTime);
            if (status != null) {
                paymentTransaction.setStatus(PaymentStatus.valueOf(status));
            }
            paymentTransaction.setResult(result);
            return paymentTransaction;
        }
    }
}
