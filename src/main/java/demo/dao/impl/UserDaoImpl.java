package demo.dao.impl;

import demo.dao.QueryExecutorImpl;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDaoImpl {

  private static final Logger LOGGER = LoggerFactory.getLogger(UserDaoImpl.class);

  private final QueryExecutorImpl queryExecutor;

  public UserDaoImpl() {
    queryExecutor = new QueryExecutorImpl();
  }

  public List<String> getPermissions(String idpSub) {
    final String query = "SELECT up.permission_name AS permissions "
        + " FROM \"aws-demo\".user AS u "
        + " INNER JOIN \"aws-demo\".user_permission AS up ON u.id = up.user_id "
        + " WHERE u.idp_sub=? ";

    List<String> result = new ArrayList<>();
    try (ResultSet rs = queryExecutor.executeQuery(query, idpSub)) {
      while (rs.next()) {
        result.add(rs.getString("permissions"));
      }
      LOGGER.info("Getting permissions for {}", idpSub);
    } catch (SQLException | RuntimeException e) {
      LOGGER.error("Can't select the user permissions from the db: {}", e.getMessage(), e);
    }
    return result;
  }


}
