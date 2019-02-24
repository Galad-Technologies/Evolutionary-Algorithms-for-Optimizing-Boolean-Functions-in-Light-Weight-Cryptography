package ec.app.cryptography;

import ec.gp.GPData;

public class BinaryData extends GPData {

    public int value; // return value

    public void copyTo(final GPData gpd) {

        ((BinaryData) gpd).value = value;

    }

}
