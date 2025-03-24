package demo.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPreTokenGenerationEvent;
import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPreTokenGenerationEvent.ClaimsOverrideDetails;
import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPreTokenGenerationEvent.Response;
import demo.dao.impl.UserDaoImpl;
import java.util.List;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for requests to Lambda function.
 */
public class PreTokenGenerationHandler
    implements RequestHandler<CognitoUserPoolPreTokenGenerationEvent, CognitoUserPoolPreTokenGenerationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreTokenGenerationHandler.class);

    private final UserDaoImpl userDao;

    public PreTokenGenerationHandler() {
        userDao = new UserDaoImpl();
    }

    public CognitoUserPoolPreTokenGenerationEvent handleRequest(
        final CognitoUserPoolPreTokenGenerationEvent event,
        final Context context) {
        try {
            var overrideClaims = getCustomPermissionsClaim(event);
            var eventResponse = buildResponse(overrideClaims);
            event.setResponse(eventResponse);
        } catch (RuntimeException e) {
            context.getLogger().log("Trying to add permissions to the id token: " + e.getMessage());
        }
        return event;
    }

    private Response buildResponse(Map<String, String> overrideClaims) {
        ClaimsOverrideDetails claimsOverrideDetails = new ClaimsOverrideDetails();
        claimsOverrideDetails.setClaimsToAddOrOverride(overrideClaims);
        return new Response(claimsOverrideDetails);
    }

    private Map<String, String> getCustomPermissionsClaim(CognitoUserPoolPreTokenGenerationEvent event) {
        String idpSub = event.getRequest().getUserAttributes().get("sub");
        List<String> permissions = userDao.getPermissions(idpSub);
        var permissionsMap = Map.of("custom:permissions", String.join(",", permissions));
        LOGGER.info("Custom permissions map: {}", StructuredArguments.entries(permissionsMap));
        return permissionsMap;
    }

}
