package demo.dao.impl;

import demo.dao.QueryExecutorImpl;
import demo.dao.ResultSetMapperFunction;
import java.sql.SQLException;
import java.util.Collections;
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

    try {
      LOGGER.info("Getting permissions for {}", idpSub);
      return queryExecutor.executeQuery(query, getColumnMapperFunction(), idpSub);
    } catch (SQLException | RuntimeException e) {
      LOGGER.error("Can't select the user permissions from the db: {}", e.getMessage(), e);
    }
    return Collections.emptyList();
  }

  private ResultSetMapperFunction<String> getColumnMapperFunction() {
    return ResultSetMapperFunction.mapUnchecked(
        rs -> rs.getString("permissions"));

  }


}
