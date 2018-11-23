
DEFAULT
  <NEW_LINE>               '\r\n|\r|\n'                 -> DEFAULT
  <WHITE_SPACE>            '( |\t)+'                    -> DEFAULT
  <LEXER_CLASS>            '([a-zA-Z_])[a-zA-Z0-9_]*'   -> DEFAULT
  <TOKEN_NAME>             '<([a-zA-Z_])[a-zA-Z0-9_]*>' -> DEFAULT
  <REGEX_START>            '\''                         -> REGEX
  <NEXT_STATE>             '->'                         -> DEFAULT

REGEX
  <REGEX_END>              '\''                         -> DEFAULT
  <REGEX_PART>             '[^\'\\]*'                   -> REGEX
  <REGEX_SINGLE_QUOTE>     '\\\''                       -> REGEX
  <REGEX_DOUBLE_BACKSLASH> '\\\\'                       -> REGEX
  <REGEX_BACKSLASH>        '\\'                         -> REGEX
