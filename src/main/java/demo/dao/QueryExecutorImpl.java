package demo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryExecutorImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(QueryExecutorImpl.class);

  private static final String DB_HOST_URL = System.getenv("DB_HOST_URL");
  private static final String DB_USER = System.getenv("DB_USER");
  private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
  private static final String DB_PORT = System.getenv("DB_PORT");
  private static final String DB_URL = "jdbc:postgresql://%s:%s/postgres".formatted(DB_HOST_URL, DB_PORT);

  public QueryExecutorImpl() {
  }

  public <T> List<T> executeQuery(String query, ResultSetMapperFunction<T> mapper, Object ... params) throws SQLException {
    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        PreparedStatement preparedStatement = connection.prepareStatement(query))  {
      List<T> result = new ArrayList<>();
      for (int i = 0; i < params.length; i++) {
        preparedStatement.setObject(i + 1, params[i]);
      }
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        result.add(mapper.map(rs));
      }
      return result;
    } catch (SQLException e) {
      LOGGER.error("SQL Exception: Can't get connection {}", e.getMessage());
      throw e;
    }
  }
}
