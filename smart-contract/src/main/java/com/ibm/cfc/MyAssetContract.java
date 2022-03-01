/*
 * SPDX-License-Identifier: Apache-2.0
 */
package com.ibm.cfc;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.nio.charset.StandardCharsets.UTF_8;

@Contract(name = "MyAssetContract",
        info = @Info(title = "MyAsset contract",
                description = "My Smart Contract",
                version = "0.0.1",
                license =
                @License(name = "Apache-2.0",
                        url = ""),
                contact = @Contact(email = "blockchain@example.com",
                        name = "blockchain",
                        url = "http://blockchain.me")))
@Default
public class MyAssetContract implements ContractInterface {
    public MyAssetContract() {

    }

    @Transaction()
    public boolean myAssetExists(Context ctx, String myAssetId) {
        byte[] buffer = ctx.getStub().getState(myAssetId);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void createMyAsset(Context ctx, String myAssetId, String manufacturer, String manufactureDateTime, String expiryDate, String status, String owner) {
        boolean exists = myAssetExists(ctx, myAssetId);
        if (exists) {
            throw new RuntimeException("The asset " + myAssetId + " already exists");
        }

        MyAsset myAsset = MyAsset.builder()
                .id(myAssetId)
                .manufacturer(manufacturer)
                .manufactureDateTime(manufactureDateTime)
                .expiryDate(expiryDate)
                .status(status)
                .owner(owner)
                .build();

        ctx.getStub().putState(myAssetId, myAsset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public MyAsset readMyAsset(Context ctx, String myAssetId) {
        boolean exists = myAssetExists(ctx, myAssetId);
        if (!exists) {
            throw new RuntimeException("The asset " + myAssetId + " does not exist");
        }

        MyAsset newAsset = MyAsset.fromJSONString(new String(ctx.getStub().getState(myAssetId), UTF_8));
        return newAsset;
    }

    @Transaction()
    public void updateMyAsset(Context ctx, String myAssetId, String manufacturer, String manufactureDateTime, String expiryDate, String status, String owner) {
        boolean exists = myAssetExists(ctx, myAssetId);
        if (!exists) {
            throw new RuntimeException("The asset " + myAssetId + " does not exist");
        }

        MyAsset myAsset = MyAsset.builder()
                .id(myAssetId)
                .manufacturer(manufacturer)
                .manufactureDateTime(manufactureDateTime)
                .expiryDate(expiryDate)
                .status(status)
                .owner(owner)
                .build();

        ctx.getStub().putState(myAssetId, myAsset.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public void deleteMyAsset(Context ctx, String myAssetId) {
        boolean exists = myAssetExists(ctx, myAssetId);
        if (!exists) {
            throw new RuntimeException("The asset " + myAssetId + " does not exist");
        }
        ctx.getStub().delState(myAssetId);
    }

    @Transaction()
    public String readMyAssetHistory(Context ctx, String myAssetId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(myAssetId);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Asset %s does not exist", myAssetId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        QueryResultsIterator<KeyModification> historyForKey = stub.getHistoryForKey(myAssetId);

        StringBuilder history = new StringBuilder();
        history.append("{\"currentState\": ").append(assetJSON).append(",\"history\": [");

        history.append(StreamSupport.stream(historyForKey.spliterator(), false)
                .map(a -> String.format("{\"txId\": \"%s\", \"state\": %s, \"timestamp\": \"%s\"}",
                        a.getTxId(), a.getStringValue(), a.getTimestamp().toString()))
                .collect(Collectors.joining(","))
        );
        history.append("]}");

        return history.toString();
    }

    private enum AssetTransferErrors {
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }
}
