package org.jruby.ir.instructions;

import org.jruby.ir.IRVisitor;
import org.jruby.ir.Operation;
import org.jruby.ir.operands.Operand;
import org.jruby.ir.transformations.inlining.InlinerInfo;

public class PushFrameInstr extends Instr {
    public PushFrameInstr() {
        super(Operation.PUSH_FRAME);
    }

    public Operand[] getOperands() { 
        return EMPTY_OPERANDS;
    }

    @Override
    public Instr cloneForInlining(InlinerInfo ii) {
        return this;
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.PushFrameInstr(this);
    }
}
