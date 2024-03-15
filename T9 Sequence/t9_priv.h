#ifndef T9_PRIV_H_
#define T9_PRIV_H_

struct T9;

typedef struct T9 {
    // You can use this struct as a node in your trie
    char* word;
    struct T9 * children[11];
} T9;

#endif  // T9_PRIV_H_
