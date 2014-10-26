all: receiveFile.class sendFile.class

%.class: %.java
	javac $<

.PHONY: clean

clean:
	find ./ -name "*.class" -type f -delete
