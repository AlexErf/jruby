package org.jruby.ir;

import org.jruby.ParseResult;
import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ast.RootNode;
import org.jruby.ir.persistence.IRPersistenceException;
import org.jruby.ir.persistence.persist.IRPersister;
import org.jruby.ir.persistence.util.ProfilingContext;

/**
 * Abstract class that contains general logic for both IR Compiler and IR
 * Interpreter
 * 
 * @param <R>
 *            type of returned object by translator
 * @param <S>
 *            type of specific for translator object
 */
public abstract class IRTranslator<R, S> {

    public R performTranslation(Ruby runtime, ParseResult parseResult, S specificObject) {
        R result = null;
        try {

            IRScope producedIRScope = null;
            if (parseResult instanceof RootNode) {                
                
                RootNode rootNode = (RootNode) parseResult;
                if (isIRPersistenceRequired()) {
                    ProfilingContext.INSTANCE.start("AST -> IR");
                    producedIRScope = produceIrScope(runtime, rootNode, false);
                    ProfilingContext.INSTANCE.stop();
                    IRPersister.persist(producedIRScope);
                    result = translationSpecificLogic(runtime, producedIRScope, specificObject);
                } else {
                    ProfilingContext.INSTANCE.start("AST -> IR");
                    producedIRScope = produceIrScope(runtime, rootNode, false);
                    ProfilingContext.INSTANCE.stop();
                    result = translationSpecificLogic(runtime, producedIRScope, specificObject);
                }                
                
            } else if (parseResult instanceof IRScope){
                producedIRScope = (IRScope) parseResult;
                result = translationSpecificLogic(runtime, producedIRScope, specificObject);
                
            } else {
                throw new IllegalArgumentException();
                
            }

        } catch (IRPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    protected abstract R translationSpecificLogic(Ruby runtime, IRScope producedIrScope,
            S specificObject);

    private static boolean isIRPersistenceRequired() {
        return RubyInstanceConfig.IR_PERSISTENCE;
    }

    private IRScope produceIrScope(Ruby runtime, RootNode rootNode, boolean isDryRun) {
        IRManager irManager = runtime.getIRManager();
        boolean is1_9 = runtime.is1_9();
        irManager.setDryRun(isDryRun);
        IRBuilder irBuilder = IRBuilder.createIRBuilder(irManager, is1_9);

        final IRScope irScope = irBuilder.buildRoot(rootNode);
        return irScope;
    }

}
