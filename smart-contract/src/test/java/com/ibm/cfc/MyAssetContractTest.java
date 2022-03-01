/*
 * SPDX-License-Identifier: Apache License 2.0
 */

package com.ibm.cfc;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class MyAssetContractTest {

    @Test
    public void assetRead() {
        MyAssetContract contract = new MyAssetContract();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);

        MyAsset asset = new MyAsset();
        asset.setManufacturer("Valuable");

        String json = asset.toJSONString();
        when(stub.getState("10001")).thenReturn(json.getBytes(StandardCharsets.UTF_8));

        MyAsset returnedAsset = contract.readMyAsset(ctx, "10001");
        assertEquals(returnedAsset.getManufacturer(), asset.getManufacturer());
    }

    @Test
    public void assetDelete() {
        MyAssetContract contract = new MyAssetContract();
        Context ctx = mock(Context.class);
        ChaincodeStub stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        when(stub.getState("10001")).thenReturn(null);

        Exception thrown = assertThrows(RuntimeException.class, () -> {
            contract.deleteMyAsset(ctx, "10001");
        });

        assertEquals(thrown.getMessage(), "The asset 10001 does not exist");
    }

    @Nested
    class AssetExists {
        @Test
        public void noProperAsset() {

            MyAssetContract contract = new MyAssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(new byte[]{});
            boolean result = contract.myAssetExists(ctx, "10001");

            assertFalse(result);
        }

        @Test
        public void assetExists() {

            MyAssetContract contract = new MyAssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(new byte[]{42});
            boolean result = contract.myAssetExists(ctx, "10001");

            assertTrue(result);

        }

        @Test
        public void noKey() {
            MyAssetContract contract = new MyAssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10002")).thenReturn(null);
            boolean result = contract.myAssetExists(ctx, "10002");

            assertFalse(result);

        }

    }

    @Nested
    class AssetCreates {

        @Test
        public void newAssetCreate() {
            MyAssetContract contract = new MyAssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            contract.createMyAsset(ctx, "10001", "name", "TheMyAsset", "manufactureDateTime", "status", "retailer");

            verify(stub).putState(eq("10001"), any(byte[].class));
        }

        @Test
        public void alreadyExists() {
            MyAssetContract contract = new MyAssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10002")).thenReturn(new byte[]{42});

            Exception thrown = assertThrows(RuntimeException.class, () -> {
                contract.createMyAsset(ctx, "10002", "", "TheMyAsset", "", "", "");
            });

            assertEquals(thrown.getMessage(), "The asset 10002 already exists");

        }

    }

    @Nested
    class AssetUpdates {
        @Test
        public void updateExisting() {
            MyAssetContract contract = new MyAssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getState("10001")).thenReturn(new byte[]{42});

            contract.updateMyAsset(ctx, "10001", "", "TheMyAsset", "", "", "");

            verify(stub).putState(eq("10001"), any(byte[].class));
        }

        @Test
        public void updateMissing() {
            MyAssetContract contract = new MyAssetContract();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);

            when(stub.getState("10001")).thenReturn(null);

            Exception thrown = assertThrows(RuntimeException.class, () -> {
                contract.updateMyAsset(ctx, "10001", "", "TheMyAsset", "", "", "");
            });

            assertEquals(thrown.getMessage(), "The asset 10001 does not exist");
        }

    }

}
