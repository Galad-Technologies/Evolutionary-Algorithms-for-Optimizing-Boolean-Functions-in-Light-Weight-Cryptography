package ec.app.cryptography;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;
import ec.util.Parameter;

public class XNOR extends GPNode {

    public void checkConstraints(final EvolutionState state, final int tree, final GPIndividual typicalIndividual, final Parameter individualBase) {

        super.checkConstraints(state,tree,typicalIndividual,individualBase);
        if (children.length!=2)
            state.output.error("Incorrect number of children for node " +
                    toStringForError() + " at " +
                    individualBase);

    }

    @Override
    public int expectedChildren() {

        return 2;

    }

    @Override
    public String toString() {
        return "XNOR";
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {

        int result;
        BinaryData rd = ((BinaryData)(input));

        children[0].eval(state,thread,input,stack,individual,problem);
        result = rd.value;

        children[1].eval(state,thread,input,stack,individual,problem);

        rd.value = (result == rd.value) ? 1 : 0;

    }

}
