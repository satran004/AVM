package org.aion.kernel;

import org.aion.avm.core.types.InternalTransaction;
import org.aion.avm.core.util.Helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionResult {

    private enum CodeType {
        SUCCESS, REJECTED, FAILED
    }

    public enum  Code {
        /**
         * The transaction was executed successfully.
         */
        SUCCESS(CodeType.SUCCESS),

        // rejected transactions should not be included on chain.

        /**
         * This transaction was rejected.
         */
        REJECTED(CodeType.REJECTED),

        /**
         * Insufficient balance to conduct the transaction.
         */
        REJECTED_INSUFFICIENT_BALANCE(CodeType.REJECTED),

        /**
         * The transaction nonce does not match the account nonce.
         */
        REJECTED_INVALID_NONCE(CodeType.REJECTED),

        // failed transaction can be included on chain, but energy charge will apply.

        /**
         * A failure occurred during the execution of the transaction.
         */
        FAILED(CodeType.FAILED),

        /**
         * The transaction data is malformed, for internal tx only.
         */
        FAILED_INVALID_DATA(CodeType.FAILED),

        /**
         * Transaction failed due to out of energy.
         */
        FAILED_OUT_OF_ENERGY(CodeType.FAILED),

        /**
         * Transaction failed due to stack overflow.
         */
        FAILED_OUT_OF_STACK(CodeType.FAILED),

        /**
         * Transaction failed due to exceeding the internal call depth limit.
         */
        FAILED_CALL_DEPTH_LIMIT_EXCEEDED(CodeType.FAILED),

        /**
         * Transaction failed due to a REVERT operation.
         */
        FAILED_REVERT(CodeType.FAILED),

        /**
         * Transaction failed due to an INVALID operation.
         */
        FAILED_INVALID(CodeType.FAILED),

        /**
         * Transaction failed due to an uncaught exception.
         */
        FAILED_EXCEPTION(CodeType.FAILED),

        /**
         * CREATE transaction failed due to a rejected of the user-provided classes.
         */
        FAILED_REJECTED(CodeType.FAILED),

        /**
         * Transaction failed due to an early abort.
         */
        FAILED_ABORT(CodeType.FAILED);

        private CodeType type;

        Code(CodeType type) {
            this.type = type;
        }

        public boolean isSuccess() {
            return type == CodeType.SUCCESS;
        }

        public boolean isRejected() {
            return type == CodeType.REJECTED;
        }

        public boolean isFailed() {
            return type == CodeType.FAILED;
        }
    }

    /**
     * Any uncaught exception that flows to the AVM.
     */
    private Throwable uncaughtException;

    /**
     * The status code.
     */
    private Code statusCode;

    /**
     * The return data.
     */
    private byte[] returnData;

    /**
     * The cumulative energy used.
     */
    private long energyUsed;

    /**
     * The storage root hash of the target account, after the transaction.
     * Note that this is only set on SUCCESS.
     * This is just a proof-of-concept for issue-246 and will change in the future.
     */
    private int storageRootHash;

    /**
     * The logs emitted during execution.
     */
    private List<Log> logs = new ArrayList<>();

    /**
     * The internal transactions created during execution.
     */
    private List<InternalTransaction> internalTransactions = new ArrayList<>();

    public void merge(TransactionResult other) {
        internalTransactions.addAll(other.getInternalTransactions());

        if (other.statusCode == Code.SUCCESS) {
            logs.addAll(other.getLogs());
        }
    }

    public void addLog(Log log) {
        this.logs.add(log);
    }

    public void addInternalTransaction(InternalTransaction tx) {
        this.internalTransactions.add(tx);
    }

    /**
     * Creates an empty result, where statusCode = SUCCESS and energyUsed = 0.
     */
    public TransactionResult() {
        this.statusCode = Code.SUCCESS;
        this.energyUsed = 0;
    }

    public Code getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Code statusCode) {
        this.statusCode = statusCode;
    }

    public byte[] getReturnData() {
        return returnData;
    }

    public void setReturnData(byte[] returnData) {
        this.returnData = returnData;
    }

    public long getEnergyUsed() {
        return energyUsed;
    }

    public void setEnergyUsed(long energyUsed) {
        this.energyUsed = energyUsed;
    }

    public int getStorageRootHash() {
        return this.storageRootHash;
    }

    public void setStorageRootHash(int storageRootHash) {
        this.storageRootHash = storageRootHash;
    }

    public List<Log> getLogs() {
        return logs;
    }

    public List<InternalTransaction> getInternalTransactions() {
        return internalTransactions;
    }

    public void clearLogs() {
        this.logs.clear();
    }

    public void rejectInternalTransactions() {
        this.internalTransactions.forEach(InternalTransaction::markAsRejected);
    }

    public Throwable getUncaughtException() {
        return uncaughtException;
    }

    public void setUncaughtException(Throwable uncaughtException) {
        this.uncaughtException = uncaughtException;
    }

    @Override
    public String toString() {
        return "TransactionResult{" +
                "statusCode=" + statusCode +
                ", returnData=" + (returnData == null ? "NULL" : Helpers.bytesToHexString(returnData)) +
                ", energyUsed=" + energyUsed +
                ", logs=[" + logs.stream().map(Log::toString).collect(Collectors.joining(",")) + "]" +
                '}';
    }
}
