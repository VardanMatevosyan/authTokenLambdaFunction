package demo.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetMapperFunction<R> {

  R map(ResultSet rs) throws SQLException;

  static  <RU> ResultSetMapperFunction<RU> mapUnchecked(ResultSetMapperFunction<RU> mapper) {
    return rs -> {
      try {
        return mapper.map(rs);
      } catch (SQLException e) {
        throw new RuntimeException("Can't get the data from the ResultSet: " + e.getMessage(), e);
      }
    };
  }

}
