package me.nizheg.payments.repository.impl;

import org.apache.commons.lang3.Validate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import me.nizheg.payments.dto.PaymentStatus;
import me.nizheg.payments.dto.PaymentTransaction;
import me.nizheg.payments.repository.PaymentDao;

/**
 * @author Nikolay Zhegalin
 */
@Repository
public class JdbcPaymentDao implements PaymentDao {

    private final JdbcTemplate template;
    private final SimpleJdbcInsert paymentTransactionInsert;
    private final PaymentTransactionMapper paymentTransactionMapper = new PaymentTransactionMapper();

    public JdbcPaymentDao(@Nonnull DataSource dataSource) {
        Validate.notNull(dataSource, "dataSource should be defined");
        this.template = new JdbcTemplate(dataSource);
        this.paymentTransactionInsert = new SimpleJdbcInsert(dataSource).withTableName("payment").usingGeneratedKeyColumns("id");
    }

    @Override
    public long create(PaymentTransaction paymentTransaction) {
        Map<String, Object> parameters = new HashMap<>(3);
        parameters.put("telegram_user_id", paymentTransaction.getTelegramUserId());
        parameters.put("creation_time", paymentTransaction.getCreationTime());
        parameters.put("sum", paymentTransaction.getSum());
        if (paymentTransaction.getStatus() != null) {
            parameters.put("status", paymentTransaction.getStatus().name());
        }
        parameters.put("result", paymentTransaction.getResult());
        return paymentTransactionInsert.executeAndReturnKey(parameters).longValue();
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
        public PaymentTransaction mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
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
