package demo.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class QueryExecutorImpl {

  private static final String DB_HOST_URL = System.getenv("DB_HOST_URL");
  private static final String DB_USER = System.getenv("DB_USER");
  private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");
  private static final String DB_PORT = System.getenv("DB_PORT");
  private static final String DB_URL = "jdbc:postgresql://%s:%s/postgres".formatted(DB_HOST_URL, DB_PORT);

  public QueryExecutorImpl() {
  }

  public ResultSet executeQuery(String query, Object ... params) throws SQLException {
    try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        PreparedStatement preparedStatement = connection.prepareStatement(query)) {
      for (int i = 0; i < params.length; i++) {
        preparedStatement.setObject(i + 1, params[i]);
      }
      return preparedStatement.executeQuery();
    }
  }
}
