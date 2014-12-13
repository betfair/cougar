/*
 * Copyright 2014, The Sporting Exchange Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.betfair.cougar.transport.impl;

import com.betfair.cougar.api.DehydratedExecutionContext;
import com.betfair.cougar.api.ExecutionContext;
import com.betfair.cougar.core.api.ev.ExecutionVenue;
import com.betfair.cougar.core.api.ev.OperationDefinition;
import com.betfair.cougar.core.api.ev.OperationKey;
import com.betfair.cougar.core.api.exception.CougarException;
import com.betfair.cougar.core.api.exception.CougarFrameworkException;
import com.betfair.cougar.core.api.tracing.Tracer;
import com.betfair.cougar.transport.api.CommandResolver;
import com.betfair.cougar.transport.api.CommandValidator;
import com.betfair.cougar.transport.api.ExecutionCommand;
import com.betfair.cougar.transport.api.TransportCommand;
import com.betfair.cougar.transport.api.TransportCommandProcessor;
import org.springframework.jmx.export.annotation.ManagedAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base implementation of TransportCommandProcessor.
 * @param <T> The type of TransportCommand that the TransportHandler implementation can process
 */
public abstract class AbstractCommandProcessor<T extends TransportCommand> implements TransportCommandProcessor<T> {

	private ExecutionVenue ev;

	private Executor executor;

    protected Tracer tracer;

    private AtomicLong executionsProcessed = new AtomicLong();
    private AtomicLong commandsProcessed = new AtomicLong();
    private AtomicLong errorsWritten = new AtomicLong();
    private AtomicLong ioErrorsEncountered = new AtomicLong();
	/**
	 * Set the ExecutionVenue that will execute the resolved command
	 * @param ev
	 */
	public final void setExecutionVenue(ExecutionVenue ev) {
		this.ev = ev;
	}

    /**
     * Returns the ExecutionVenue that will execute resolved commands
     * @return the ExecutionVenue
     */
    public ExecutionVenue getExecutionVenue() {
        return ev;
    }

    /**
	 *
	 * @param executor
	 */
	public void setExecutor(Executor executor) {
		this.executor = executor;
	}

    protected Executor getExecutor() {
        return executor;
    }

    /**
	 * Processes a TransportCommand.
	 * @param command
	 */
	public void process(final T command) {
        boolean traceStarted = false;
        incrementCommandsProcessed();
		DehydratedExecutionContext ctx = null;
		try {
            validateCommand(command);
			CommandResolver<T> resolver = createCommandResolver(command, tracer);
			ctx = resolver.resolveExecutionContext();
            List<ExecutionCommand> executionCommands = resolver.resolveExecutionCommands();
            if (executionCommands.size() > 1) {
                throw new CougarFrameworkException("Resolved >1 command in a non-batch call!");
            }
            ExecutionCommand exec = executionCommands.get(0);
            tracer.start(ctx.getRequestUUID(), exec.getOperationKey());
            traceStarted = true;
			executeCommand(exec, ctx);
		} catch(CougarException ce) {
            executeError(command, ctx, ce, traceStarted);
		} catch (Exception e) {
            executeError(command, ctx, new CougarFrameworkException("Unexpected exception while processing transport command", e), traceStarted);
		}
	}

    /**
     * Get the list of command validators to be used to validate commands.
     */
    protected abstract List<CommandValidator<T>> getCommandValidators();

    /**
     * Enables validation (and rejection) of processing of a command.
     */
    protected void validateCommand(final T command) throws CougarException {
        List<CommandValidator<T>> validators = getCommandValidators();
        for (CommandValidator<T> v : validators) {
            v.validate(command);
        }
    }

    /**
     * Execute the supplied command
     * @param finalExec
     * @param finalCtx
     */
    protected void executeCommand(final ExecutionCommand finalExec, final ExecutionContext finalCtx) {
        executionsProcessed.incrementAndGet();
        ev.execute(finalCtx,
                   finalExec.getOperationKey(),
                   finalExec.getArgs(),
                   finalExec,
                   executor,
                   finalExec.getTimeConstraints());
    }

    protected void executeError(final T finalExec, final DehydratedExecutionContext finalCtx, final CougarException finalError, final boolean traceStarted) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                writeErrorResponse(finalExec, finalCtx, finalError, traceStarted);
            }
        });
    }

	/**
	 * Create an implementation that will resolve the supplied command to an operation key,
	 * arguments and a listener for callback
	 *
     * @param command the command to resolve
     * @param tracer
     * @return the resolved operation, arguments and listener
	 */
	protected abstract CommandResolver<T> createCommandResolver(T command, Tracer tracer);

	/**
	 * Write an exception back to the client.
     * @param command the command that caused the error
     * @param e the exception that was thrown
     * @param traceStarted
     */
	protected abstract void writeErrorResponse(T command, DehydratedExecutionContext context, CougarException e, boolean traceStarted);

	protected final OperationDefinition getOperationDefinition(OperationKey key) {
		return ev.getOperationDefinition(key);
	}

    public void setTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    /**
	 * Convenience abstract implementation of CommandResolver where only a single
	 * ExecutionRequest is to be resolved.
	 * @param <C> The type of Command that the CommandResolver implementation can resolve
	 */
	protected abstract class SingleExecutionCommandResolver<C extends TransportCommand> implements CommandResolver<C> {


        private Tracer tracer;

        public SingleExecutionCommandResolver(Tracer tracer) {
            this.tracer = tracer;
        }

        public final List<ExecutionCommand> resolveExecutionCommands() {
			ArrayList<ExecutionCommand> list = new ArrayList<ExecutionCommand>();
			list.add(resolveExecutionCommand(tracer));
			return list;
		}
		public abstract ExecutionCommand resolveExecutionCommand(Tracer tracer);
	}

    protected final void incrementIoErrorsEncountered() {
        ioErrorsEncountered.incrementAndGet();
    }

    protected final void incrementCommandsProcessed() {
        commandsProcessed.incrementAndGet();
    }

    protected final void incrementErrorsWritten() {
        errorsWritten.incrementAndGet();
    }

    @ManagedAttribute
    public long getExecutionsProcessed() {
        return executionsProcessed.get();
    }

    @ManagedAttribute
    public long getCommandsProcessed() {
        return commandsProcessed.get();
    }

    @ManagedAttribute
    public long getErrorsWritten() {
        return errorsWritten.get();
    }

    @ManagedAttribute
    public long getIoErrorsEncountered() {
        return ioErrorsEncountered.get();
    }
}
