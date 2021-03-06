package org.aion.avm.core;

import org.aion.kernel.KernelInterface;
import org.aion.kernel.TransactionContext;
import org.aion.kernel.TransactionResult;
import org.aion.parallel.AddressResourceMonitor;
import org.aion.parallel.TransactionTask;


/**
 * The private/internal extension of the Avm public interface.  This is the internally-facing interface which supports things like internal
 * transactions, which aren't otherwise exposted/meaningful to calls originating outside an AVM implementation.
 */
public interface AvmInternal extends Avm {
    /**
     * Runs a transaction, originating from inside the AVM, itself.
     * 
     * @param parentKernel The KernelInterface of the origin of the transaction, in a parent call frame.
     * @param task The current transaction task.
     * @param context The transaction to run.
     * @return The result of the transaction.
     */
    TransactionResult runInternalTransaction(KernelInterface parentKernel, TransactionTask task, TransactionContext context);

    /**
     * Get the address resource monitor using by the current AVM.
     *
     * @return The address resource monitor.
     */
    AddressResourceMonitor getResourceMonitor();
}