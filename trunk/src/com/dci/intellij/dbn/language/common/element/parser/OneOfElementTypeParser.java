package com.dci.intellij.dbn.language.common.element.parser;

import com.dci.intellij.dbn.language.common.ParseException;
import com.dci.intellij.dbn.language.common.TokenType;
import com.dci.intellij.dbn.language.common.element.DBNElementType;
import com.dci.intellij.dbn.language.common.element.OneOfElementType;
import com.dci.intellij.dbn.language.common.element.path.ParsePathNode;
import com.intellij.lang.PsiBuilder;

public class OneOfElementTypeParser extends AbstractElementTypeParser<OneOfElementType> {
    public OneOfElementTypeParser(OneOfElementType elementType) {
        super(elementType);
    }

    public ParseResult parse(ParsePathNode parentNode, PsiBuilder builder, boolean optional, int depth, long timestamp) throws ParseException {
        logBegin(builder, optional, depth);
        ParsePathNode node = createParseNode(parentNode, builder.getCurrentOffset());
        PsiBuilder.Marker marker = builder.mark();

        getElementType().sort();
        TokenType tokenType = (TokenType) builder.getTokenType();

        if (tokenType!= null && !tokenType.isChameleon()) {
            String tokenText = builder.getTokenText();
            // TODO !!!! if elementType is an identifier: then BUILD VARIANTS!!!
            for (DBNElementType elementType : getElementType().getPossibleElementTypes()) {
                if (isDummyToken(tokenText) || elementType.getLookupCache().canStartWithToken(tokenType) || isSuppressibleReservedWord(tokenType, node)) {
                    ParseResult result = elementType.getParser().parse(node, builder, true, depth + 1, timestamp);
                    if (result.isMatch()) {
                        return stepOut(builder, marker, depth, result.getType(), result.getMatchedTokens(), node);
                    }
                }
            }
            if (!optional) {
                //updateBuilderError(builder, this);
            }

        }
        return stepOut(builder, marker, depth, ParseResultType.NO_MATCH, 0, node);
    }
}