package me.nizheg.telegram.bot.chgk.config;

import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

/**
 * @author Nikolay Zhegalin
 */
@EnableJdbcHttpSession(tableName = "http_session")
public class HttpSessionConfig {
}
