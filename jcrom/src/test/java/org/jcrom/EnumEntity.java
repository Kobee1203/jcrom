package org.jcrom;

import java.util.ArrayList;
import java.util.List;
import org.jcrom.annotations.JcrProperty;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class EnumEntity extends AbstractJcrEntity {

    public enum Suit { CLUBS, DIAMONDS, HEARTS, SPADES }
    
    @JcrProperty private Suit suit;
    @JcrProperty private Suit[] suitAsArray;
    @JcrProperty private List<Suit> suitAsList;
    
    public EnumEntity() {
        this.suitAsList = new ArrayList<Suit>();
    }

    public Suit getSuit() {
        return suit;
    }

    public void setSuit(Suit suit) {
        this.suit = suit;
    }

    public Suit[] getSuitAsArray() {
        return suitAsArray;
    }

    public void setSuitAsArray(Suit[] suitAsArray) {
        this.suitAsArray = suitAsArray;
    }

    public List<Suit> getSuitAsList() {
        return suitAsList;
    }

    public void setSuitAsList(List<Suit> suitAsList) {
        this.suitAsList = suitAsList;
    }
    
    public void addSuitToList( Suit suit ) {
        suitAsList.add(suit);
    }
    
}
