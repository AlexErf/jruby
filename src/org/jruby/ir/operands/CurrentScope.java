package org.jruby.ir.operands;

import java.util.List;

import org.jruby.ir.IRScope;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.transformations.inlining.InlinerInfo;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;

public class CurrentScope extends Operand {
    private final IRScope scope;

    public CurrentScope(IRScope scope) {
        super(OperandType.CURRENT_SCOPE);
        this.scope = scope;
    }

    public IRScope getScope() {
        return scope;
    }
    
    @Override
    public void addUsedVariables(List<Variable> l) {
        /* Nothing to do */
    }    

    @Override
    public Operand cloneForInlining(InlinerInfo ii) {
        return this;
    }

    @Override
    public boolean canCopyPropagate() {
        return true;
    }

    @Override
    public Object retrieve(ThreadContext context, IRubyObject self, DynamicScope currDynScope, Object[] temp) {
        return scope.getStaticScope();
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.CurrentScope(this);
    }
}
