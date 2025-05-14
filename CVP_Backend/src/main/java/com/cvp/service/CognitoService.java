package com.cvp.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.ConfirmSignUpRequest;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.SignUpRequest;
import com.amazonaws.services.cognitoidp.model.UserNotConfirmedException;

@Service
public class CognitoService {

        private final AWSCognitoIdentityProvider cognitoClient;

        @Value("${aws.cognito.userPoolId}")
        private String userPoolId;

        @Value("${aws.cognito.clientId}")
        private String clientId;

        public CognitoService(
                        @Value("${aws.accessKey}") String awsAccessKey,
                        @Value("${aws.secretKey}") String awsSecretKey,
                        @Value("${aws.dynamodb.sessionKey}") String sessionToken, // Add session token
                        @Value("${aws.region}") String awsRegion) {

                this.cognitoClient = AWSCognitoIdentityProviderClientBuilder.standard()
                                .withCredentials(new AWSStaticCredentialsProvider(
                                                new BasicSessionCredentials(awsAccessKey, awsSecretKey, sessionToken))) // Use
                                                                                                                        // session
                                                                                                                        // token
                                .withRegion(Regions.fromName(awsRegion))
                                .build();
        }

        public void signUpUser(String email, String password) {
                SignUpRequest signUpRequest = new SignUpRequest()
                                .withUsername(email)
                                .withPassword(password)
                                .withUserAttributes(
                                                new AttributeType()
                                                                .withName("email")
                                                                .withValue(email))
                                .withClientId(clientId);

                cognitoClient.signUp(signUpRequest);
        }

        public void signUpOrganization(String email, String password) {
                SignUpRequest signUpRequest = new SignUpRequest()
                                .withUsername(email)
                                .withPassword(password)
                                .withUserAttributes(
                                                new AttributeType()
                                                                .withName("email")
                                                                .withValue(email))
                                .withClientId(clientId);

                cognitoClient.signUp(signUpRequest);
        }

        public String login(String email, String password) {
                try {
                        Map<String, String> authParams = new HashMap<>();
                        authParams.put("USERNAME", email);
                        authParams.put("PASSWORD", password);

                        AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                                        .withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                                        .withUserPoolId(userPoolId)
                                        .withClientId(clientId)
                                        .withAuthParameters(authParams);

                        AdminInitiateAuthResult authResult = cognitoClient.adminInitiateAuth(authRequest);

                        // Verify user is confirmed
                        if (authResult.getChallengeName() != null &&
                                        authResult.getChallengeName().equals("NEW_PASSWORD_REQUIRED")) {
                                throw new RuntimeException("User must change password");
                        }

                        return authResult.getAuthenticationResult().getIdToken();
                } catch (NotAuthorizedException e) {
                        throw new RuntimeException("Invalid username/password");
                } catch (UserNotConfirmedException e) {
                        throw new RuntimeException("User not confirmed - check email for verification code");
                }
        }

        public void confirmUser(String email, String otp) {
                ConfirmSignUpRequest confirmRequest = new ConfirmSignUpRequest()
                                .withUsername(email)
                                .withConfirmationCode(otp)
                                .withClientId(clientId);

                cognitoClient.confirmSignUp(confirmRequest);
        }
}