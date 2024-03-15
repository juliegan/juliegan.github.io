CC = gcc
CFLAGS = -Wall -std=c11 -g
LIB_SOURCES = t9_lib.c
LIB_OBJECTS = $(LIB_SOURCES:.c=.o)
DEMO_SOURCES = t9_demo.c
TEST_SOURCES = t9_tests.c
TEST_OBJECTS = $(TEST_SOURCES:.c=.o)
DEMO_OBJECTS = $(DEMO_SOURCES:.c=.o)
DICTIONARY = small_dictionary.txt
EXECUTABLES = t9_demo t9_tests

all: $(EXECUTABLES)

# Compile the library
t9_lib.o: $(LIB_SOURCES) t9_lib.h t9_priv.h
	$(CC) $(CFLAGS) -c $(LIB_SOURCES)

# Compile the demo program
t9_demo: $(DEMO_OBJECTS) t9_lib.o
	$(CC) $(CFLAGS) -o $@ $^

# Compile the test program
t9_tests: $(TEST_OBJECTS) t9_lib.o
	$(CC) $(CFLAGS) -o $@ $^

# Compile test suite
t9_tests.o: $(TEST_SOURCES) t9_lib.h t9_priv.h
	$(CC) $(CFLAGS) -c $(TEST_SOURCES)

# Compile demo program
t9_demo.o: $(DEMO_SOURCES) t9_lib.h
	$(CC) $(CFLAGS) -c $(DEMO_SOURCES)

# Test the code with small dictionary
test_small_dictionary: all
	valgrind --leak-check=full ./t9_demo $(DICTIONARY)

# Clean object files and executables
clean:
	rm -f $(LIB_OBJECTS) $(TEST_OBJECTS) $(DEMO_OBJECTS) $(EXECUTABLES)

# Check code style with clint.py
lint:
	clint.py $(LIB_SOURCES) $(TEST_SOURCES) $(DEMO_SOURCES)

.PHONY: all clean lint test_small_dictionary
