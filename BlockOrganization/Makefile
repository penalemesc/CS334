JAVAC = javac

SRCS = Block.java RID.java SPTester.java SlottedBlock.java

project: $(SRCS)
	$(JAVAC)  $^

%.o : %.c $(HDRS)
	$(JAVAC)  $(CFLAGS) -c $<  -o $@

clean:
	rm *.class

