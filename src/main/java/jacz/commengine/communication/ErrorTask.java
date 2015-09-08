package jacz.commengine.communication;

import jacz.util.concurrency.task_executor.ParallelTask;

/**
 * This task invokes the error method in the communication module so that synchronization issues are avoided
 */
public class ErrorTask implements ParallelTask {
    
    private CommModuleOld communicationModule;

    private CommError commError;

    public ErrorTask(CommModuleOld communicationModule, CommError commError) {
        this.communicationModule = communicationModule;
        this.commError = commError;
    }

    @Override
    public void performTask() {
        communicationModule.error(commError);
    }
}
