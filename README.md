# Project - "authTokenLambdaFunction"
### **Authentication Lambda Function is used as a Cognito User Pool Trigger.**
- used to modify the JWT ID token after successful authentication
- creates users with default roles and permissions to the RDS PostgreSQL database  
- adds Cognito user group that is used as a user role

_The Security Group and User Poll was already exists
If not then ideal to add config for these two resource to the serverless template_
