CC := gcj
CFLAGS  := -g

SRCDIR = src
OBJDIR = bin
BINDIR = .

main = xml2txt

all: $(main) header lib

$(main): $(OBJDIR)/$(main).o
	$(CC) --main=crosslink.$(main) $(LDFLAGS) -o $@ $^
    	
$(OBJDIR)/%.o: $(SRCDIR)/crosslink/%.java
	$(CC) $(CFLAGS) -c $< -o $@
	
header: ant
	gcjh $(OBJDIR)/crosslink/$(main).class
	
ant:
	ant compile
	
lib:
	ar rcs $(BINDIR)/libxml2txt.a $(OBJDIR)/*.o
		
clean :
	\rm $(OBJDIR)/*.o $(main)
