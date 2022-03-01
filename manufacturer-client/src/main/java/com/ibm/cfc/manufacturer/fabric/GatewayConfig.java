package com.ibm.cfc.manufacturer.fabric;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class GatewayConfig {

    private static Gateway gateway;

    public static Gateway get() {
        if (gateway != null) return gateway;
        return getCAGateway();
    }

    private static Gateway getGateway() {
        try {

//            Path networkConfigFile = Paths.get("ManufacturerGatewayConnection.json");
            Path networkConfigFile = Paths.get("connection-org1.json");
            gateway = Gateway.createBuilder()
                    .identity(WalletConfig.get(), "appUser")
//                    .identity(WalletConfig.get(), "User1@org1.example.com")
                    .networkConfig(networkConfigFile)
                    .discovery(true)
                    .connect();

            return gateway;
        } catch (IOException e) {
            log.error("Exception while creating Gateway", e);
            return null;
        }
    }

    private static Gateway getCAGateway() {
        System.setProperty("org.hyperledger.fabric.sdk.service_discovery.as_localhost", "true");
        enrollAdmin();
        registerUser();

        try {
//            Path networkConfigFile = Paths.get("connection-org1.json");
//            Path networkConfigPath = Paths.get("..", "..", "test-network", "organizations", "peerOrganizations", "org1.example.com", "connection-org1.yaml");
//            Path networkConfigFile = Paths.get("/home/nitin/workspace/github/hyperledger/fabric-samples", "test-network", "organizations", "peerOrganizations", "org1.example.com", "connection-org1.json");
            Path networkConfigFile = Paths.get(".", "connection-org1.json");
            gateway = Gateway.createBuilder()
                    .identity(WalletConfig.get(), "appUser")
                    //                    .identity(WalletConfig.get(), "User1@org1.example.com")
                    .networkConfig(networkConfigFile)
                    .discovery(true)
                    .connect();
            return gateway;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void registerUser() {
        try {
            // Create a CA client for interacting with the CA.
            HFCAClient caClient = getHfcaClient();

            Wallet wallet = WalletConfig.get();
            // Check to see if we've already enrolled the user.
            if (wallet.get("appUser") != null) {
                System.out.println("An identity for the user \"appUser\" already exists in the wallet");
                return;
            }

            X509Identity adminIdentity = (X509Identity) wallet.get("admin");
            if (adminIdentity == null) {
                System.out.println("\"admin\" needs to be enrolled and added to the wallet first");
                return;
            }
            User admin = new User() {

                @Override
                public String getName() {
                    return "admin";
                }

                @Override
                public Set<String> getRoles() {
                    return null;
                }

                @Override
                public String getAccount() {
                    return null;
                }

                @Override
                public String getAffiliation() {
                    return "org1.department1";
                }

                @Override
                public Enrollment getEnrollment() {
                    return new Enrollment() {

                        @Override
                        public PrivateKey getKey() {
                            return adminIdentity.getPrivateKey();
                        }

                        @Override
                        public String getCert() {
                            return Identities.toPemString(adminIdentity.getCertificate());
                        }
                    };
                }

                @Override
                public String getMspId() {
                    return "Org1MSP";
                }

            };

            // Register the user, enroll the user, and import the new identity into the wallet.
            RegistrationRequest registrationRequest = new RegistrationRequest("appUser");
            registrationRequest.setAffiliation("org1.department1");
            registrationRequest.setEnrollmentID("appUser");
            String enrollmentSecret = caClient.register(registrationRequest, admin);
            Enrollment enrollment = caClient.enroll("appUser", enrollmentSecret);
            Identity user = Identities.newX509Identity("Org1MSP", enrollment);
            wallet.put("appUser", user);
            System.out.println("Successfully enrolled user \"appUser\" and imported it into the wallet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void enrollAdmin() {
        try {
            // Create a CA client for interacting with the CA.
            HFCAClient caClient = getHfcaClient();

            Wallet wallet = WalletConfig.get();

            // Check to see if we've already enrolled the admin user.
            if (wallet.get("admin") != null) {
                System.out.println("An identity for the admin user \"admin\" already exists in the wallet");
                return;
            }

            // Enroll the admin user, and import the new identity into the wallet.
            final EnrollmentRequest enrollmentRequestTLS = new EnrollmentRequest();
            enrollmentRequestTLS.addHost("localhost");
            enrollmentRequestTLS.setProfile("tls");
            Enrollment enrollment = caClient.enroll("admin", "adminpw", enrollmentRequestTLS);
            Identity user = Identities.newX509Identity("Org1MSP", enrollment);
            wallet.put("admin", user);
            System.out.println("Successfully enrolled user \"admin\" and imported it into the wallet");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HFCAClient getHfcaClient() throws MalformedURLException, CryptoException, InvalidArgumentException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, URISyntaxException {
        Properties props = new Properties();
//        String certPath = "/home/nitin/workspace/github/hyperledger/fabric-samples/test-network/organizations/peerOrganizations/org1.example.com/ca/ca.org1.example.com-cert.pem";
        String certPath = Paths.get(".", "ca.org1.example.com-cert.pem").toString();

        log.info("certPath : {}", certPath);
        props.put("pemFile", certPath);
        props.put("allowAllHostNames", "true");
        HFCAClient caClient = null;
        caClient = HFCAClient.createNewInstance("https://localhost:7054", props);
        CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite();
        caClient.setCryptoSuite(cryptoSuite);
        return caClient;
    }
}
