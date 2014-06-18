package com.dci.intellij.dbn.language.common.element.lookup;

import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.DBNElementType;
import com.dci.intellij.dbn.language.common.element.LeafElementType;
import com.dci.intellij.dbn.language.common.element.UnknownElementType;
import com.dci.intellij.dbn.language.common.element.path.PathNode;

public class UnknownElementTypeLookupCache extends AbstractElementTypeLookupCache<UnknownElementType> {
    public UnknownElementTypeLookupCache(UnknownElementType elementType) {
        super(elementType);
    }

    @Override
    public boolean containsToken(TokenType tokenType) {
        return false;
    }

    public boolean isFirstPossibleLeaf(LeafElementType leaf, DBNElementType pathChild) {
        return false;
    }

    public boolean isFirstRequiredLeaf(LeafElementType leaf, DBNElementType pathChild) {
        return false;
    }

    public boolean containsLandmarkToken(TokenType tokenType, PathNode node) {return false;}
    
    public boolean startsWithIdentifier(PathNode node) {return false;}

}