package com.ibm.cfc.manufacturer.fabric;

import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class WalletConfig {

    private static Wallet wallet;

    public static Wallet get() {

        if (wallet == null) {
            try {
                Path walletPath = Paths.get("wallet");
                wallet = Wallets.newFileSystemWallet(walletPath);
                log.info("Created wallet at : {}", walletPath);
            } catch (IOException e) {
                log.error("Exception while creating FileSystemWallet", e);
                return null;
            }
        }
        return wallet;
    }
}
