package org.aion.avm.core;

import java.math.BigInteger;
import org.aion.avm.api.ABIEncoder;
import org.aion.avm.api.Address;
import org.aion.avm.core.dappreading.JarBuilder;
import org.aion.avm.core.util.CodeAndArguments;
import org.aion.avm.core.util.Helpers;
import org.aion.avm.core.util.TestingHelper;
import org.aion.avm.userlib.AionList;
import org.aion.avm.userlib.AionMap;
import org.aion.avm.userlib.AionSet;
import org.aion.kernel.*;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * As part of issue-215, we want to see if Synthetic Methods would break any of our assumptions in method
 * invocation. The Java compiler will generate two methods in the bytecode: one takes the Obejct as argument,
 * the other takes the specific type as argument. This test operates on SyntheticMethodsTestTarget to observe
 * any possible issues when we have a concrete method that overrides a generic method.
 */
public class SyntheticMethodsTest {
    private byte[] from = KernelInterfaceImpl.PREMINED_ADDRESS;
    private byte[] dappAddr;

    private Block block = new Block(new byte[32], 1, Helpers.randomBytes(Address.LENGTH), System.currentTimeMillis(), new byte[0]);
    private long energyLimit = 6_000_0000;
    private long energyPrice = 1;

    private KernelInterfaceImpl kernel;
    private Avm avm;

    @Before
    public void setup() {
        byte[] basicAppTestJar = JarBuilder.buildJarForMainAndClasses(SyntheticMethodsTestTarget.class
                , AionMap.class
                , AionSet.class
                , AionList.class);

        byte[] txData = new CodeAndArguments(basicAppTestJar, null).encodeToBytes();

        this.kernel = new KernelInterfaceImpl();
        this.avm = CommonAvmFactory.buildAvmInstance(this.kernel);
        Transaction tx = Transaction.create(from, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        dappAddr = avm.run(new TransactionContext[] {context})[0].get().getReturnData();
    }

    @After
    public void tearDown() {
        this.avm.shutdown();
    }

    @Test
    public void testDappWorking() {
        TransactionResult result = createAndRunTransaction("getCompareResult");

        Assert.assertEquals(TransactionResult.Code.SUCCESS, result.getStatusCode());
        Assert.assertEquals(SyntheticMethodsTestTarget.DEFAULT_VALUE, TestingHelper.decodeResult(result));
    }

    @Test
    public void testCompareTo() {
        // BigInteger
        TransactionResult result1 = createAndRunTransaction("compareSomething", 1);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());
        TransactionResult result1Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(1, TestingHelper.decodeResult(result1Value));

        TransactionResult result2 = createAndRunTransaction("compareSomething", 2);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result2.getStatusCode());
        TransactionResult result2Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(0, TestingHelper.decodeResult(result2Value));

        TransactionResult result3 = createAndRunTransaction("compareSomething", 3);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result3.getStatusCode());
        TransactionResult result3Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(-1, TestingHelper.decodeResult(result3Value));

        TransactionResult result4 = createAndRunTransaction("compareSomething", 4);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result4.getStatusCode());
        TransactionResult result4Value = createAndRunTransaction("getCompareResult");
        Assert.assertEquals(100, TestingHelper.decodeResult(result4Value));
    }

    @Test
    public void testTarget(){
        // pick target1Impl
        TransactionResult result1 = createAndRunTransaction("pickTarget", 1);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());

        // check for correctness in synthetic, should get impl1 name
        TransactionResult result2 = createAndRunTransaction("getName");
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result2.getStatusCode());
        Assert.assertEquals("TargetClassImplOne", TestingHelper.decodeResult(result2));

        // pick target2Impl
        TransactionResult result3 = createAndRunTransaction("pickTarget", 2);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result3.getStatusCode());

        // check for correctness in synthetic, should get abstract name
        TransactionResult result4 = createAndRunTransaction("getName");
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result4.getStatusCode());
        Assert.assertEquals("TargetAbstractClass", TestingHelper.decodeResult(result4));
    }

    @Test
    public void testGenericMethodOverride(){
        int inputGeneric = 10;
        int inputOverrideGeneric = 20;

        // calling setup generics
        TransactionResult result1 = createAndRunTransaction("setGenerics",
                inputGeneric, inputOverrideGeneric);
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result1.getStatusCode());

        // retrieve each object
        TransactionResult result2 = createAndRunTransaction("getIntGen");
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result2.getStatusCode());
        Assert.assertEquals(inputGeneric, TestingHelper.decodeResult(result2));

        TransactionResult result3 = createAndRunTransaction("getIntGenSub");
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result3.getStatusCode());
        Assert.assertEquals(inputOverrideGeneric, TestingHelper.decodeResult(result3));

        TransactionResult result4 = createAndRunTransaction("getSubCopy");
        Assert.assertEquals(TransactionResult.Code.SUCCESS, result4.getStatusCode());
        Assert.assertEquals(inputOverrideGeneric, TestingHelper.decodeResult(result4));
    }

    private TransactionResult createAndRunTransaction(String methodName, Object ... args){
        byte[] txData = ABIEncoder.encodeMethodArguments(methodName, args);
        Transaction tx = Transaction.call(from, dappAddr, kernel.getNonce(from), BigInteger.ZERO, txData, energyLimit, energyPrice);
        TransactionContextImpl context = new TransactionContextImpl(tx, block);
        return avm.run(new TransactionContext[]{context})[0].get();
    }
}
