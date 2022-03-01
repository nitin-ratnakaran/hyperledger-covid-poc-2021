package com.ibm.cfc.repository;

import com.ibm.cfc.manufacturer.fabric.GatewayConfig;
import com.ibm.cfc.model.Asset;
import lombok.extern.slf4j.Slf4j;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;

import javax.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
@Slf4j
public class AssetFabricRepository implements AssetRepository {

    private Contract contract;

    @Override
    public void createAsset(Asset asset) {
        log.info("Creating asset {}", asset);

        try {
            byte[] result = getContract().createTransaction("createMyAsset")
                    .submit(asset.getId(), asset.getManufacturer()
                            , asset.getManufactureDateTime(), asset.getExpiryDate(),
                            asset.getStatus(), asset.getOwner());
            log.info("Created transaction : {}", result);
        } catch (ContractException | InterruptedException | TimeoutException e) {
            log.error("Exception while submitting transaction", e);
        }
    }

    @Override
    public Asset findAsset(String assetId) {
        try {
            byte[] bytes = getContract().evaluateTransaction("readMyAsset", assetId);
            return Asset.fromJSONString(new String(bytes, StandardCharsets.UTF_8));
        } catch (ContractException e) {
            log.error("Exception while evaluating transaction", e);
            return null;
        }
    }

    @Override
    public void updateAsset(Asset asset) {
        try {
            byte[] result = getContract().createTransaction("updateMyAsset")
                    .submit(asset.getId(), asset.getManufacturer(), asset.getManufactureDateTime(), asset.getExpiryDate(),
                            asset.getStatus(), asset.getOwner());
            log.info("Created transaction : {}", result);
        } catch (ContractException | InterruptedException | TimeoutException e) {
            log.error("Exception while submitting transaction", e);
        }
    }

    @Override
    public String getAssetHistory(String assetId) {
        try {
            byte[] bytes = getContract().evaluateTransaction("readMyAssetHistory", assetId);
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (ContractException e) {
            log.error("Exception while submitting transaction", e);
            return null;
        }
    }

    private Contract getContract() {
        if (contract != null) return contract;

        contract = GatewayConfig.get()
                .getNetwork("mychannel")
                .getContract("smart-contract");
        return contract;
    }
}
