/**
* Copyright (c) 2009 SRA (Software Research Associates, Inc.)
*
* This file is part of CodeDepot.
* CodeDepot is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3.0
* as published by the Free Software Foundation and appearing in
* the file GPL.txt included in the packaging of this file.
*
* CodeDepot is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with CodeDepot. If not, see <http://www.gnu.org/licenses/>.
*
**/
package jp.co.sra.codedepot.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Merge tokens into one token until the specified endToken is met;
 * endToken itself is included as default. endToken is not if set so.
 *
 * For example, in program clone search, we merged tokens in one
 * statement line into one token.
 *
 * An alternative to this is to use the NGramTokenFilter
 * @author ye
 * @updater matubara
 * @$Id: MergeTokensFilter.java 2342 2017-11-09 05:36:32Z fang $
 */
public class MergeTokensFilter extends TokenFilter {

    private Set<String> _endTokens;
    private boolean _includeEndToken = true;

    private String[] _stopTypes;

    private int _nesting_parenthesis;
    private final String _left_parenthesis = "(";

    private boolean _check_forstatement;
    final private String forstatement_start = "for";
    final private String forstatement_end = ")";

    final private String[] _front_of_block_start_terms = new String[] { "if", "else", "switch", "for", "while", "do" };
    final private String[] _front_of_block_stop_terms = new String[]  { ")" , "else", ")"     , ")"  , ")"    , "do" };
    final private String _left_brace = "{";
    final private String _right_brace = "}";

    private Token _previousToken;
    private int _nesting_count;
    private ArrayList<Integer> _statement_counts;
    private Token _previousMergedToken;

    private ArrayList<Integer> _insert_right_brace_nests;

    public MergeTokensFilter(TokenStream input, Set<String> endTokens) {
        this(input, endTokens, new String[] {});
    }

    public MergeTokensFilter(TokenStream input, Set<String> endTokens, String[] stopTypes) {
        super(input);
        _endTokens = endTokens;
        _stopTypes = stopTypes;

        _check_forstatement = true;

        _nesting_count = 0;
        _statement_counts = new ArrayList<Integer>();
        _statement_counts.add(0);
        _insert_right_brace_nests = new ArrayList<Integer>();
    }

    public void setIncludeEndToken(boolean include) {
        _includeEndToken = include;
    }

    public void setCheckForStatement(boolean check) {
        _check_forstatement = check;
    }

    protected boolean isEndToken(Token token) {
        return _endTokens.contains(token.term());
    }

    protected boolean includeEndToken() {
        return _includeEndToken == true;
    }

    protected boolean isStopTypeToken(Token token) {
        for (String stopType : _stopTypes) {
            if (token.type().equals(stopType)) {
                return true;
            }
        }
        return false;
    }

    protected void updateNestingParenthesis(Token token) {
        if (_nesting_parenthesis >= 0 && token.term().equals(_left_parenthesis)) {
            _nesting_parenthesis++;
        } else if (token.term().equals(forstatement_end)) {
            if (_nesting_parenthesis > 0) {
                _nesting_parenthesis--;
            }
        }
    }

    protected boolean isForstatementStartToken(Token token) {
        return _check_forstatement && token.term().equals(forstatement_start);
    }

    protected boolean isForstatementEndToken(Token token) {
        if (_check_forstatement) {
            return token.term().equals(forstatement_end) && _nesting_parenthesis == 0;
        }
        return false;
    }

    protected boolean isFrontOfBlockStartToken(Token token) {
        for (String term : _front_of_block_start_terms) {
            if (token.term().equals(term)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isFrontOfBlockStopToken(Token token) {
        for (String term : _front_of_block_stop_terms) {
            if (token.term().equals(term)) {
                return _nesting_parenthesis == 0;
            }
        }
        return false;
    }

    protected boolean isLeftBrace(Token token) {
        return token.term().equals(_left_brace);
    }

    protected boolean isRightBrace(Token token) {
        return token.term().equals(_right_brace);
    }

    @Override
    public Token next(Token reusableToken) throws IOException {
        assert reusableToken != null;
        reusableToken.clear();
        int startOffset = 0, endOffset = 0;
        StringBuilder mergedText = new StringBuilder();
        boolean first = true;
        boolean isForStatement = false;
        boolean inFrontOfBlockToken = false;

        if (_previousMergedToken != null && _statement_counts.get(_nesting_count) > 0 && _insert_right_brace_nests.contains(_nesting_count - 1)) {
            _insert_right_brace_nests.remove(_insert_right_brace_nests.indexOf(_nesting_count - 1));
            _statement_counts.set(_nesting_count, 0);
            _nesting_count--;
            _statement_counts.set(_nesting_count, _statement_counts.get(_nesting_count) + 1);
            Token rightBraceToken = new Token(_right_brace, _previousMergedToken.endOffset(), _previousMergedToken.endOffset());
            _previousMergedToken = _insert_right_brace_nests.contains(_nesting_count - 1) ? rightBraceToken : null;
            return rightBraceToken;
        }

        Token token;
        for (token = _previousToken != null ? _previousToken : input.next(reusableToken); token != null; token = input.next(reusableToken)) {
            if (first) {
                startOffset = token.startOffset();
                first = false;
                isForStatement = this.isForstatementStartToken(token);
                inFrontOfBlockToken = this.isFrontOfBlockStartToken(token);
                _nesting_parenthesis = inFrontOfBlockToken ? 0 : -1;
                _previousToken = null;
            }

            this.updateNestingParenthesis(token);

            if ((!this.isEndToken(token) || this.includeEndToken()) && !this.isStopTypeToken(token)) {
                mergedText.append(token.term());
                endOffset = token.endOffset();
            }

            if (isForStatement && this.isForstatementEndToken(token)) {
                isForStatement = false;
            }

            if (inFrontOfBlockToken && this.isFrontOfBlockStopToken(token)) {
                Token currentToken = (Token) token.clone();

                //YE: 25Jan2010
                //in case there is no more next token
                //Token nextToken = (Token) input.next(reusableToken).clone();
                Token nextToken = new Token();
                nextToken = input.next(nextToken);
                if (nextToken == null) {
                	//no more next token
                	//drop the current one that could not end with ;{}
                	return null;
                }

                if (!this.isLeftBrace(nextToken)) {
                    _previousToken = nextToken;
                    token = new Token(_left_brace, currentToken.endOffset(), token.endOffset());
                    mergedText.append(token.term());
                    _insert_right_brace_nests.add(_nesting_count);
                } else {
                    token = nextToken;
                    mergedText.append(token.term());
                    endOffset = token.endOffset();
                }
                inFrontOfBlockToken = false;
                _nesting_parenthesis = -1;
            }

            if ((!isForStatement && this.isEndToken(token))) {
                break;
            }

            if (this.isStopTypeToken(token)) {
                if (token.startOffset() < token.endOffset()) {
                    mergedText.append(token.term());
                    endOffset = token.endOffset();
                }
                break;
            }
        }

        if (endOffset == 0) {
            return token;
        }
        if (token == null) {
            //System.out.println("last token is read");
            //drop the last token that does not end with endToken
            return null;
        }

        if (token.term().equals(_left_brace)) {
            _statement_counts.set(_nesting_count, _statement_counts.get(_nesting_count) + 1);
            _nesting_count++;
            if (_statement_counts.size() == _nesting_count) {
                _statement_counts.add(0);
            } else {
                _statement_counts.set(_nesting_count, 0);
            }
        } else if (token.term().equals(_right_brace)) {
            _statement_counts.set(_nesting_count, 0);
            _nesting_count = Math.max(0, _nesting_count - 1);
            if (_nesting_count >= 0) {
                _statement_counts.set(_nesting_count, _statement_counts.get(_nesting_count) + 1);
            }
        } else {
            _statement_counts.set(_nesting_count, _statement_counts.get(_nesting_count) + 1);
        }

        token.setTermBuffer(mergedText.toString());
        token.setStartOffset(startOffset);
        token.setEndOffset(endOffset);
        if (CharSeparatorTokenizer.debug)
            System.out.println("===" + token);

        if (_insert_right_brace_nests.contains(_nesting_count - 1)) {
            _previousMergedToken = (Token) token.clone();
        }

        return token;
    }
}
