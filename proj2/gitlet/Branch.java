package gitlet;

import java.io.Serializable;

public class Branch implements Serializable {
    /* the sha1 of name is branch uid*/
    private String name;

    public Branch(String name) {
        this.name = name;
    }

}
