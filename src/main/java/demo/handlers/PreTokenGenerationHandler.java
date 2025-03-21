package demo.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPreTokenGenerationEvent;
import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPreTokenGenerationEvent.ClaimsOverrideDetails;
import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPreTokenGenerationEvent.Response;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class PreTokenGenerationHandler implements RequestHandler<CognitoUserPoolPreTokenGenerationEvent, CognitoUserPoolPreTokenGenerationEvent> {

    private static final String DB_HOST_URL = System.getenv("DB_HOST_URL");
    private static final String DB_USER = System.getenv("RDS_USERNAME");
    private static final String DB_PASSWORD = System.getenv("RDS_PASSWORD");
    private static final String DB_PORT = System.getenv("RDS_PORT");
    private static final String DB_URL = "jdbc:postgresql://%s:%s/postgres".formatted(DB_HOST_URL, DB_PORT);

    public CognitoUserPoolPreTokenGenerationEvent handleRequest(final CognitoUserPoolPreTokenGenerationEvent event, final Context context) {
        String idpSub = event.getRequest().getUserAttributes().get("sub");
        try {
            List<String> permissions = getPermissions(idpSub, context);
            Map<String, String> overrideClaims = getCustomPermissionsClaim(permissions);
            ClaimsOverrideDetails claimsOverrideDetails = new ClaimsOverrideDetails();
            claimsOverrideDetails.setClaimsToAddOrOverride(overrideClaims);
            Response eventResponse = new Response(claimsOverrideDetails);
            event.setResponse(eventResponse);
            logClaims(context, event.getResponse().getClaimsOverrideDetails());
        } catch (RuntimeException e) {
            context.getLogger().log("Trying to add permissions to the id token: " + e.getMessage());
        }
        return event;
    }

    private static Map<String, String> getCustomPermissionsClaim(List<String> permissions) {
      return Map.of("custom:permissions", String.join(",", permissions));
    }

    private void logClaims(Context context, ClaimsOverrideDetails claimsOverrideDetails) {
        claimsOverrideDetails.getClaimsToAddOrOverride()
            .forEach((key, value) ->
                context.getLogger().log("Claim: key: " + key + ", value: " + value));
    }

    private List<String> getPermissions(String idpSub, Context context) {
        List<String> permissions = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            PreparedStatement preparedStatement = connection
                .prepareStatement("SELECT up.permission_name AS permissions "
                    + " FROM \"aws-demo\".user AS u "
                    + " INNER JOIN \"aws-demo\".user_permission AS up ON u.id = up.user_id "
                    + " WHERE u.idp_sub=? ");
            preparedStatement.setString(1, idpSub);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                permissions.add(resultSet.getString("permissions"));
            }
        } catch (SQLException e) {
            context.getLogger().log("Can't select the user permissions from the db: " + e.getMessage());
          throw new RuntimeException(e);
        }
      context.getLogger().log("Getting permissions for " + idpSub);
        return permissions;
    }

}
