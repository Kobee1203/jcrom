package org.jcrom.entities;

import java.math.BigDecimal;

import org.jcrom.AbstractJcrEntity;
import org.jcrom.annotations.JcrSerializedProperty;

public class EntityWithBigDecimalSerialization extends AbstractJcrEntity {

    private static final long serialVersionUID = 1L;

    @JcrSerializedProperty
    private BigDecimal bigDecimal;

    public BigDecimal getBigDecimal() {
        return bigDecimal;
    }

    public void setBigDecimal(BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }
}