package org.jcrom;

import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class CImpl extends AbstractJcrEntity implements C {

    @JcrProperty private String body;

    public CImpl() {
        this(null);
    }

    public CImpl( String name ) {
        super();
        this.name = name;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


}
