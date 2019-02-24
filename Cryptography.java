package ec.app.cryptography;

import ec.EvolutionState;
import ec.Individual;
import ec.gp.GPIndividual;
import ec.gp.GPProblem;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;

public class Cryptography extends GPProblem implements SimpleProblemForm {

    public static final String P_DATA = "data";

    public static final int INPUT_SIZE = 7;
    public int currentC1;
    public int currentC2;
    public int[] inputs = new int[INPUT_SIZE];
    private int[] function = new int[(int) Math.pow(2, INPUT_SIZE)];

    @Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum) {

        if (!ind.evaluated) { // don't bother reevaluating (original comment)
            BinaryData input = (BinaryData)(this.input);
            double NL = 0;
            double maxWalsh;
            int walsh;
            int hammingWeight;
            int balancedness = 0;
            int penalty;

            // This array a is the constant vector that is going to be multiplied with the input vector x in order to
            // get the Walsh Transform.
            int[] a = new int[INPUT_SIZE];

            for (int y=0;y<100;y++) {

                // Generate non-input terminals.
                currentC1 = state.random[threadnum].nextInt(2);
                currentC2 = state.random[threadnum].nextInt(2);

                // Generate all the binary numbers of length INPUT_SIZE.
                // This loop is the summation when calculating NL, namely, all the possible value of vector a.
                maxWalsh = Double.NEGATIVE_INFINITY;
                balancedness = 0;
                hammingWeight = 0;
                for (int n = 0; n < Math.pow(2, INPUT_SIZE); n++) {
                    String an = Integer.toBinaryString(n);
                    while (an.length() < INPUT_SIZE) {
                        an = "0" + an;
                    }
                    for (int i = 0; i < INPUT_SIZE; i++) {
                        a[i] = Character.getNumericValue(an.charAt(i));
                    }

                    // Generate, again, all the binary numbers of length INPUT_SIZE, but this time it's for the input
                    // over F(n,2), so that each value of a would be tested by all possible inputs', based on the already
                    // randomly set currentX and currentY.
                    walsh = 0; // Zero the walsh, don't forget.
                    for (int i = 0; i < Math.pow(2, INPUT_SIZE); i++) {
                        String b = Integer.toBinaryString(i);
                        while (b.length() < INPUT_SIZE) {
                            b = "0" + b;
                        }
                        for (int j = INPUT_SIZE - 1; j >= 0; j--) {
                            inputs[j] = Character.getNumericValue(b.charAt(j));
                        }

                        ((GPIndividual) ind).trees[0].child.eval(
                                state, threadnum, input, stack, ((GPIndividual) ind), this);

                        /*
                         * <The Walsh-Hadamard Transform --------------------------------------------------------------
                         */
                        // Get the inner product of a and x.
                        int[] aTx = new int[INPUT_SIZE]; // aTx is the inner product of a and x before xoring them.
                        for (int ii = 0; ii < INPUT_SIZE; ii++) {
                            aTx[ii] = a[ii] * inputs[ii];
                        }
                        int ax = (aTx[0] == aTx[1]) ? 0 : 1;
                        for (int jj = 2; jj < INPUT_SIZE; jj++) {
                            ax = (ax == aTx[jj]) ? 0 : 1;
                        }

                        int fx = input.value; // The final result to function f(x) after all the work done.
                        if (n == 0) {
                            function[i] = fx;
                        }
                        walsh += Math.pow(-1, ((fx == ax) ? 0 : 1));
                        /*
                         * The Walsh-Hadamard Transform>--------------------------------------------------------------------------------------------
                         */

                        hammingWeight += (fx == 1) ? 1 : 0;

                    }

                    maxWalsh = Math.max(maxWalsh, Math.abs(walsh));

                    // <Only need to do this part for one iteration of number n since nothing involves the vector a.
                    penalty = -5;
                    if (n == 0) {
                        int ones = hammingWeight;
                        int zeros = (int) Math.pow(2, INPUT_SIZE) - hammingWeight;
                        if (ones == zeros) { // This function is balanced.
                            balancedness = 1;
                        } else {
                            balancedness = Math.abs(ones * 2 - (int) Math.pow(2, INPUT_SIZE)) * penalty;
                        }
                    }
                    // >

                }
                NL = Math.pow(2, INPUT_SIZE - 1) - 0.5 * maxWalsh;

            }

            double[] nonlinearityFitness = new double[1];
            double[] nonlinearityAndBalancednessFitness = new double[2];
            double[] nonlinearityAndBalancednessAndCorrelationImmunityFitness = new double[3];
            double[] nonlinearityDividedByBalancedness = new double[4];
            int t = correlationImmunity();

            nonlinearityFitness[0] = NL;

            nonlinearityAndBalancednessFitness[0] = NL;
            nonlinearityAndBalancednessFitness[1] = Math.abs(balancedness - 1);

            nonlinearityAndBalancednessAndCorrelationImmunityFitness[0] = NL;
            nonlinearityAndBalancednessAndCorrelationImmunityFitness[1] = Math.abs(balancedness - 1);
            nonlinearityAndBalancednessAndCorrelationImmunityFitness[2] = t;

            nonlinearityDividedByBalancedness[0] = NL;
            nonlinearityDividedByBalancedness[1] = Math.abs(balancedness - 1);
            nonlinearityDividedByBalancedness[3] = NL / Math.abs(balancedness);
            nonlinearityDividedByBalancedness[2] = t;

            MultiObjectiveFitness fitness = ((MultiObjectiveFitness) ind.fitness);
            //fitness.setObjectives(state, nonlinearityFitness);
            //fitness.setObjectives(state, nonlinearityAndBalancednessFitness);
            fitness.setObjectives(state, nonlinearityAndBalancednessAndCorrelationImmunityFitness);
            //fitness.setObjectives(state, nonlinearityDividedByBalancedness);

        }

    }

    private int correlationImmunity() {

        long[] spectrum = new long[(int) Math.pow(2, INPUT_SIZE)];
        for (int i = 0; i < Math.pow(2, INPUT_SIZE); i++) {
            spectrum[i] = function[i];
        }
        long temp;
        for (int j = 0; j < INPUT_SIZE; j++) {
            for (int k = 0; k < Math.pow(2, INPUT_SIZE); k += Math.pow(2, j + 1)) {
                for (int i = k; i < k + Math.pow(2, j); i++) {
                    temp = spectrum[i];
                    spectrum[i] = temp + spectrum[i + (int) Math.pow(2, j)];
                    spectrum[i + (int) Math.pow(2, j)] = temp - spectrum[i + (int) Math.pow(2, j)];
                }
            }
        }
        int CI = INPUT_SIZE;
        for (int i = 1; i < Math.pow(2, INPUT_SIZE); i++) {
            if (spectrum[i] != 0) {
                if (weight(i) < CI) {
                    CI = weight(i);
                }
            }
        }
        if (CI == INPUT_SIZE) {
            return CI;
        } else {
            return CI - 1;
        }

    }

    private int weight(int value) {

        int mask = 1;
        int wt = 0;
        for (int i = 1; i < 32; i++) {
            wt += (value & mask);
            value >>= 1;
        }
        return wt;

    }

    public void setup(final EvolutionState state, final Parameter base) {

        // very important, remember this (original comment)
        super.setup(state,base);

        // verify our input is the right class (or subclasses from it) (original comment)
        if (!(input instanceof BinaryData)) {
            state.output.fatal("GPData class must subclass from " + BinaryData.class, base.push(P_DATA), null);
        }

    }

}
