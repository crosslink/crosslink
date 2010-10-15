CC := gcj
CFLAGS  := -g

SRCDIR = src
OBJDIR = bin
BINDIR = .

main = xml2txt

all: $(main) header lib

$(main): $(OBJDIR)/$(main).o
	$(CC) --main=$(main) $(LDFLAGS) -o $@ $^
    	
$(OBJDIR)/%.o: $(SRCDIR)/%.java
	$(CC) $(CFLAGS) -c $< -o $@
	
header: ant
	gcjh $(OBJDIR)/$(main).class
	
ant:
	ant compile
	
lib:
	ar rcs $(BINDIR)/libxml2txt.a $(OBJDIR)/*.o
		
clean :
	\rm $(OBJDIR)/*.o $(main)
