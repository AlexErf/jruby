package org.jruby.ir.instructions;

import org.jruby.ir.Operation;
import org.jruby.ir.operands.Variable;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

/*
 * Argument receive in IRExecution scopes.
 */
public abstract class ReceiveArgBase extends ResultBaseInstr {
    protected int argIndex;

    public ReceiveArgBase(Operation op, Variable result, int argIndex) {
        super(op, result, EMPTY_OPERANDS);

        assert result != null: "ReceiveArgBase result is null";

        this.argIndex = argIndex;
    }

    public int getArgIndex() {
        return argIndex;
    }

    public IRubyObject receiveArg(ThreadContext context, IRubyObject[] args, boolean keywordArgumentSupplied) {
        throw new RuntimeException("ReceiveArgBase.interpret called! " + this.getClass().getName() + " does not define receiveArg");
    }
}
