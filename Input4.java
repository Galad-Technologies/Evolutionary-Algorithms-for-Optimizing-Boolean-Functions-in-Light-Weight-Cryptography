package ec.app.cryptography;

import ec.EvolutionState;
import ec.Problem;
import ec.gp.ADFStack;
import ec.gp.GPData;
import ec.gp.GPIndividual;
import ec.gp.GPNode;

public class Input4 extends GPNode {

    @Override
    public String toString() {
        return "Input4";
    }

    @Override
    public void eval(EvolutionState state, int thread, GPData input, ADFStack stack, GPIndividual individual, Problem problem) {

        BinaryData rd = ((BinaryData)(input));
        rd.value= ((Cryptography) problem).inputs[3];

    }

    public int expectedChildren() { return 0; }

}
