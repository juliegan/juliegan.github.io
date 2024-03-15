#include <string.h>

#include "safe_assert.h"
#include "t9_lib.h"
void AssertReturnedStringEquals(char* expected, char* actual);

suite("T9") {
    test("Empty initialization") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        DestroyT9(dict);
    }

    test("InitializeEmptyT9_uniquePointers") {
      T9* dict1 = InitializeEmptyT9();
      T9* dict2 = InitializeEmptyT9();
    
      // Verify that the pointers are unique
      safe_assert(dict1 != dict2);

      DestroyT9(dict1);
      DestroyT9(dict2);
    }


    test("InitializeFromFileT9 with valid file") {
        // Test InitializeFromFileT9 with a valid file
        const char* valid_filename = "small_dictionary.txt"; // Use the provided small dictionary file
        T9* dict = InitializeFromFileT9(valid_filename);
        safe_assert(dict != NULL); // Check if the returned pointer is not NULL
        valid_filename = "dictionary.txt"; // Use the prov    ided small dictionary file
        T9* dict2 = InitializeFromFileT9(valid_filename);
        safe_assert(dict2 != NULL);
        DestroyT9(dict);
    }

    test("InitializeFromFileT9 with invalid file") {
        // Test InitializeFromFileT9 with an invalid file
        const char* invalid_filename = "nonexistent_file.txt"; // Use a non-existent file
        T9* dict = InitializeFromFileT9(invalid_filename);
        safe_assert(dict == NULL); // Check if the returned pointer is NULL
        
    }
    test("InitializeFromFileT9 with own words") {
        const char* ownWords = "hello";
        T9* dict = InitializeFromFileT9(ownWords);
        safe_assert(dict == NULL);
    }
    test("initializeFromFileT9 with weird names") {
        const char* weirdName = "hello terrible name.txt";
        T9* dict = InitializeFromFileT9(weirdName);
        const char* weirdName2 = "12345689.txt";
        T9* dict2 = InitializeFromFileT9(weirdName2);
        const char* weirdName3 = " .txt";
        T9* dict3 = InitializeFromFileT9(weirdName3);
        safe_assert(dict == NULL);
        safe_assert(dict2 == NULL);
        safe_assert(dict3 == NULL);
    }
    test("initializeFromFileT9 start file") {
        const char* valid_filename = "small_dictionary.txt";
        T9* dict = InitializeFromFileT9(valid_filename);
        char* word = PredictT9(dict, "22738275");
        AssertReturnedStringEquals("aardvark", word); 
        DestroyT9(dict); 
    }
    test("initializeFromFileT9 middle file") {
        const char* valid_filename = "small_dictionary.txt";
        T9* dict = InitializeFromFileT9(valid_filename);
        char* word = PredictT9(dict, "639");
        AssertReturnedStringEquals("new", word);
        DestroyT9(dict);
    }
    test("initializeFromFileT9 final file") {
        const char* valid_filename = "small_dictionary.txt";
        T9* dict = InitializeFromFileT9(valid_filename);
        char* word = PredictT9(dict, "93272");
        AssertReturnedStringEquals("zebra", word);
        DestroyT9(dict);
    }
    test("AddWordToT9_checksNull") {
        // Test AddWordToT9
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
    }
    test("AddWordToT9_empty string") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "");
        char* word = PredictT9(dict, "2");
        safe_assert(word == NULL);
        DestroyT9(dict);
    }

    test("AddWordTo9_addsWordToDict") {
        // Test AddWordToT9
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "book");

        AddWordToT9(dict, "cool");

        AddWordToT9(dict, "apple");
        char* word = PredictT9(dict, "27753");
        safe_assert(word != NULL);
        char* word2 = PredictT9(dict, "2665#");
        safe_assert(word2 != NULL);
        DestroyT9(dict);
    }

    test("AddWordToT9_simple") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        AddWordToT9(dict, "book");

        char* word = PredictT9(dict, "2665"); // Attempt to identify a word
        safe_assert(word != NULL);
        AssertReturnedStringEquals("book", word);
    }
    test("AddWordToT9_withSimilarBeginnings") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        // Add words with similar beginnings
        AddWordToT9(dict, "wood");
        AddWordToT9(dict, "wool");
        AddWordToT9(dict, "woo");

        // Attempt to predict words with similar beginnings
        char* prediction1 = PredictT9(dict, "9663"); // T9 sequence for "boo"
        char* prediction2 = PredictT9(dict, "9665"); // T9 sequence for "books"
        char* prediction3 = PredictT9(dict, "966"); // T9 sequence for "book"

        // Ensure the predictions return the expected words
        AssertReturnedStringEquals("wood", prediction1);
        AssertReturnedStringEquals("wool", prediction2);
        AssertReturnedStringEquals("woo", prediction3);

        DestroyT9(dict);
    }
    test("AddWordToT9_withSimilarEndings") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        // Add words with similar beginnings
        AddWordToT9(dict, "w");
        AddWordToT9(dict, "ww");
        AddWordToT9(dict, "www");
        AddWordToT9(dict, "wwz");
        char* word =  PredictT9(dict, "9");
        AssertReturnedStringEquals("w", word);
        word =  PredictT9(dict, "99");
        AssertReturnedStringEquals("ww", word);
        word =  PredictT9(dict, "999");
        AssertReturnedStringEquals("www", word);
        word =  PredictT9(dict, "999#");
        AssertReturnedStringEquals("wwz", word);
        DestroyT9(dict); 
    }
    test("AddWordToT9 with nodes") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "book");
        AddWordToT9(dict, "bool");
        AddWordToT9(dict, "cook");
        AddWordToT9(dict, "cool");
        char* word =  PredictT9(dict, "2665##");
        AssertReturnedStringEquals("cook", word);
        DestroyT9(dict);
    }
    test("AddWordToT9 with similar word but dif length") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "week");
        AddWordToT9(dict, "wee");
        AddWordToT9(dict, "weeks");
        char* word =  PredictT9(dict, "93357");
        AssertReturnedStringEquals("weeks", word);
        char* word1 = PredictT9(dict, "9335");
        AssertReturnedStringEquals("week", word1);
        char* word2 = PredictT9(dict, "933");
        AssertReturnedStringEquals("wee", word2);
        DestroyT9(dict);
    }

    test("AddWordToT9_withSpecialCharacters") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "can*t");
        AddWordToT9(dict, "mother*in*law");
        char* word =  PredictT9(dict, "226*8");
        safe_assert(word == NULL);
        char* word2 =  PredictT9(dict, "6684374*65*29");
        safe_assert(word2 == NULL);
        DestroyT9(dict);
    }
    test("AddWordToT9_withLargeDictionary") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        // Add a large number of words to the dictionary
        int word_count = 0;
        for (char c1 = 'a'; c1 <= 'z'; ++c1) {
                for (char c2 = 'a'; c2 <= 'z'; ++c2) {
                    for (char c3 = 'a'; c3 <= 'z'; ++c3) {
                        char word[4];
                        snprintf(word, sizeof(word), "%c%c%c", c1, c2, c3);
                        AddWordToT9(dict, word);
                        word_count++;
                        if (word_count >= 10000) {
                            break;  // Limit reached
                        }       
                    }  
                    if (word_count >= 10000) {
                        break;  // Limit reached
                    }
                }   
                if (word_count >= 10000) {
                   break;  // Limit reached
                }
        }

        // Add some additional words for testing
        AddWordToT9(dict, "wade");
        AddWordToT9(dict, "zack");

        // Check predictions for added words
        char* word1 = PredictT9(dict, "9233");
        AssertReturnedStringEquals("wade", word1);

        char* word2 = PredictT9(dict, "9225");
        AssertReturnedStringEquals("zack", word2);

        // Cleanup
        DestroyT9(dict);
    }

    test("AddWordToT9_withWeirdInput") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        // Add words with weird characters to the dictionary
        AddWordToT9(dict, "apple&");
        AddWordToT9(dict, "orange!");
        AddWordToT9(dict, "banana%");

        // Ensure weird characters are handled properly
        // Perform some predictions to verify the behavior
        char* prediction1 = PredictT9(dict, "27753&"); // T9 sequence for "apple&"
        safe_assert(prediction1 == NULL);

        char* prediction2 = PredictT9(dict, "672643!"); // T9 sequence for "orange!"
        safe_assert(prediction2 == NULL);

        char* prediction3 = PredictT9(dict, "226262%"); // T9 sequence for "banana%"
        safe_assert(prediction3 == NULL);

        DestroyT9(dict);
    }
    test("AddWordToT9_withDuplicateWord") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        AddWordToT9(dict, "book");
        AddWordToT9(dict, "book"); // Attempt to add the same word again

        // Ensure only one instance of "book" exists in the dictionary
        char* prediction = PredictT9(dict, "2665");
        AssertReturnedStringEquals("book", prediction);
        char* prediction1 = PredictT9(dict, "2665");
        AssertReturnedStringEquals("book", prediction1);
        DestroyT9(dict);
    }  
   test("AddWordToT9_withMixedCase") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        AddWordToT9(dict, "Book");
        AddWordToT9(dict, "bOOk");
        AddWordToT9(dict, "BOOK");

        // Ensure all variations of "book" are added as one entry in the dictionary
        char* prediction = PredictT9(dict, "2665");
        safe_assert(prediction == NULL);

        DestroyT9(dict);
   }
   test("AddWordToT9_withEmptyWord") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        AddWordToT9(dict, "");

        // Ensure the dictionary remains empty
        char* prediction = PredictT9(dict, "2665");
        safe_assert(prediction == NULL);

        DestroyT9(dict);
}
       
    test("AddWordToT9_withInvalidInputs") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        // Test adding NULL pointer
        AddWordToT9(dict, NULL);
        // Test adding empty string
        char* prediction = PredictT9(dict, "2");
        safe_assert(prediction == NULL);

        DestroyT9(dict);
    }
    test("AddWordToT9_withSingleChar") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict,"w");  
        char* prediction = PredictT9(dict, "9");
        AssertReturnedStringEquals("w", prediction);
        DestroyT9(dict);
    }


test("AddWordToT9_withLongWord") {
    T9* dict = InitializeEmptyT9();
    safe_assert(dict != NULL);

    // Attempt to add a word longer than the maximum allowed length
    AddWordToT9(dict, "thisIsALongWordThatExceedsTheMaximumAllowedLength");

    // Ensure the word is not added to the dictionary
    char* prediction = PredictT9(dict, "8447475"); // T9 sequence for "this"
    safe_assert(prediction == NULL);

    DestroyT9(dict);
}

test("AddWordToT9_withNonAlphabeticCharacters") {
    T9* dict = InitializeEmptyT9();
    safe_assert(dict != NULL);

    // Attempt to add words with non-alphabetic characters
    AddWordToT9(dict, "word123");
    AddWordToT9(dict, "special!@#");

    // Ensure the words with non-alphabetic characters are not added
    char* prediction1 = PredictT9(dict, "9673123"); // T9 sequence for "word"
    char* prediction2 = PredictT9(dict, "7732425!@#"); // T9 sequence for "special"
    safe_assert(prediction1 == NULL && prediction2 == NULL);

    DestroyT9(dict);
}

test("AddWordToT9_withWordContainingOnlySpecialCharacters") {
    T9* dict = InitializeEmptyT9();
    safe_assert(dict != NULL);

    // Attempt to add words containing only special characters
    AddWordToT9(dict, "*");
    AddWordToT9(dict, "$$$");

    // Ensure the words with only special characters are not added
    char* prediction1 = PredictT9(dict, "*"); // T9 sequence for "*"
    char* prediction2 = PredictT9(dict, "$$$"); // T9 sequence for "$$$"
    safe_assert(prediction1 == NULL && prediction2 == NULL);

    DestroyT9(dict);
}
    test("PredictT9_invalid sequence followed by #") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "book");
        char* word = PredictT9(dict, "123#");
        safe_assert(word == NULL);
        //AssertReturnedStringEquals("book", word);
        DestroyT9(dict);
    }

    test("PredictT9_withEmptySequence") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        AddWordToT9(dict, "book");

        char* word = PredictT9(dict, ""); // Attempt to identify a word with empty sequence
        safe_assert(word == NULL);
    }

    test("PredictT9_withNonexistentSequence") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        AddWordToT9(dict, "book");
        AddWordToT9(dict, "cool");

        char* word = PredictT9(dict, "123");
        safe_assert(word == NULL);
    }

    test("PredictT9_withMultipleWords") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        AddWordToT9(dict, "book");
        AddWordToT9(dict, "cool");

        char* word = PredictT9(dict, "2665#"); // Attempt to identify the first word
        safe_assert(word != NULL);
        AssertReturnedStringEquals("cool", word);

        char* nextWord = PredictT9(dict, "2665"); // Attempt to identify the next word
        safe_assert(nextWord != NULL);
        AssertReturnedStringEquals("book", nextWord);
        DestroyT9(dict);
    }
    
    test("PredictT9_withRepeatedCharacters") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        // Add word to the dictionary with repeated characters
        AddWordToT9(dict, "book");

        // Predict word using T9 sequence with repeated characters
        char* prediction = PredictT9(dict, "2##665");
        safe_assert(prediction == NULL);

        DestroyT9(dict);
    }

    test("PredictT9_withInvalidCharacters") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);

        // Add word to the dictionary
        AddWordToT9(dict, "book");

        // Predict word using T9 sequence with invalid characters
        char* prediction = PredictT9(dict, "2665*");
        safe_assert(prediction == NULL);

        DestroyT9(dict);
    }

    test("PredictT9_withZeroAndOne") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        // Add word to the dictionary
        AddWordToT9(dict, "book");

        // Predict word using T9 sequence with invalid characters
        char* prediction = PredictT9(dict, "2665001");
        safe_assert(prediction == NULL);
        DestroyT9(dict);
    }

    test("PredictT9_withSimilarWordsDifferentLength") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
    
        // Add two similar words with different lengths
        AddWordToT9(dict, "book");
        AddWordToT9(dict, "books");
    
        // Predict word using T9 sequence that matches the prefix of both words
        char* prediction = PredictT9(dict, "2665");
        AssertReturnedStringEquals("book", prediction);
    
        DestroyT9(dict);
    }

    test("PredictT9_wordsNearTheEnd") {
         T9* dict = InitializeEmptyT9();
         safe_assert(dict != NULL);
         AddWordToT9(dict, "zoom");
         char* prediction = PredictT9(dict, "9666");
         AssertReturnedStringEquals("zoom", prediction);
         DestroyT9(dict);
    }
    test("PredictT9_node with multiple words") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "book");
        AddWordToT9(dict, "bool");
        AddWordToT9(dict, "cook");
        AddWordToT9(dict, "cool");
        char* prediction1 = PredictT9(dict, "2665");
        AssertReturnedStringEquals("book", prediction1);
        char* prediction2 = PredictT9(dict, "2665#");
        AssertReturnedStringEquals("bool", prediction2);
        char* prediction3 = PredictT9(dict, "2665##");
        AssertReturnedStringEquals("cook", prediction3);
        char* prediction4 = PredictT9(dict, "2665###");
        AssertReturnedStringEquals("cool", prediction4);
        DestroyT9(dict);
        
     }
     test("PredictT9_node with single word") {
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "book");
        char* prediction1 = PredictT9(dict, "2665");
        AssertReturnedStringEquals("book", prediction1);
        char* prediction2 = PredictT9(dict, "2665#");
        safe_assert(prediction2 == NULL);
        char* prediction3 = PredictT9(dict, "2665##");
        safe_assert(prediction3 == NULL);
        DestroyT9(dict);
    }
    test("PredictT9_sequence with single digit"){
        T9* dict = InitializeEmptyT9();
        safe_assert(dict != NULL);
        AddWordToT9(dict, "a");
        char* prediction1 = PredictT9(dict, "2");
        AssertReturnedStringEquals("a", prediction1);
    }
    
        
}
void AssertReturnedStringEquals(char* expected, char* actual) {
    safe_assert(actual > 0);
    safe_assert(actual != NULL);
    safe_assert(strlen(actual) == strlen(expected));
    safe_assert(strcmp(actual, expected) == 0);
}
