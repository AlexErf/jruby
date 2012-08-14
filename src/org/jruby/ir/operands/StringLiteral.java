package org.jruby.ir.operands;

import org.jruby.RubyString;
import org.jruby.ir.IRVisitor;
import org.jruby.ir.transformations.inlining.InlinerInfo;
import org.jruby.runtime.DynamicScope;
import org.jruby.runtime.ThreadContext;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.util.ByteList;

import java.util.List;

/**
 * Represents a literal string value.
 * 
 * This is not an immutable literal because I can gsub!, 
 * for example, and modify the contents of the string.
 * This is not like a Java string.
 */
public class StringLiteral extends Operand {
    // SSS FIXME: Pick one of bytelist or string, or add internal conversion methods to convert to the default representation

    final public ByteList bytelist;
    final public String   string;

    public StringLiteral(ByteList val) {
        this(val.toString(), val);
    }
    
    public StringLiteral(String s) {
        this(s, ByteList.create(s));
    }
    
    private StringLiteral(String string, ByteList byteList ) {
        super(OperandType.STRING_LITERAL);
        bytelist = byteList;
        this.string = string;
    }

    @Override
    public boolean hasKnownValue() {
        return true;
    }

    @Override
    public void addUsedVariables(List<Variable> l) {
        /* Do nothing */
    }

    @Override
    public Operand cloneForInlining(InlinerInfo ii) {
        return this;
    }

    @Override
    public Object retrieve(ThreadContext context, IRubyObject self, DynamicScope currDynScope, Object[] temp) {
        // SSS FIXME: AST interpreter passes in a coderange argument.
        return RubyString.newStringShared(context.getRuntime(), bytelist);
    }

    @Override
    public void visit(IRVisitor visitor) {
        visitor.StringLiteral(this);
    }

    public ByteList getByteList() {
        return bytelist;
    }

    public String getString() {
        return string;
    }
}
