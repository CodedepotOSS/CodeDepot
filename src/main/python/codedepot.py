# -*- coding: utf-8 -*-

from pygments.formatter import Formatter
from pygments.lexer import Lexer
from pygments.token import Token, STANDARD_TYPES
from pygments.lexers import get_lexer_for_filename
from pygments.lexers import get_lexer_by_name

import simplejson as json

__all__ = [
    'JsonFormatter',
    'get_lang_for_filename',
    'parse_token',
]

class JsonFormatter(Formatter):
    r"""
    Format tokens as a json representation for storing token streams.
    """

    name = 'Json'
    aliases = ['json']
    filenames = ['*.json']

    def __init__(self, **options):
        Formatter.__init__(self, **options)

    def format(self, tokensource, outfile):
        tokens = [ (str(ttype), value) for ttype, value in tokensource ]
        outfile.write(json.dumps(tokens))
        outfile.flush()

def get_lang_for_filename(filename):
    lexer = get_lexer_for_filename(name)
    if lexer:
        return lexer.alias
    else:
        return None

def parse_token(lang, code):
    lexer = get_lexer_by_name(lang, encoding="utf-8")
    if not lexer:
	return None

    def get_token_name(ttype):
       return str(ttype)[6:]

    if isinstance(code, str):
        code = unicode(code, "utf-8")

    tokens = lexer.get_tokens(code)
    tokens = [ (get_token_name(ttype), value) for ttype, value in tokens if len(value) > 0 ]
    return json.dumps(tokens)

def main():
    import sys
    if len(sys.argv) < 2:
        sys.exit(2)

    arg0 = sys.argv[0]
    lang = sys.argv[1]

    if len(sys.argv) > 2:
        try:
            f = open(sys.argv[2])
            print parse_token(lang, f.read())
            f.close()
            sys.exit(0)
        except:
            sys.exit(1)
    else:
        print parse_token(lang, sys.stdin.read())
        sys.exit(0)

if __name__ == "__main__":
    main()

