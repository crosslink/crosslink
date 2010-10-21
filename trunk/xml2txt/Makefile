CC := gcj
CFLAGS  := -g

SRCDIR = src
OBJDIR = bin
BINDIR = .

main = XML2TXT

all: $(main) header lib

$(main): $(OBJDIR)/$(main).o
	$(CC) --main=crosslink.$(main) $(LDFLAGS) -o $@ $^
    	
$(OBJDIR)/%.o: $(SRCDIR)/crosslink/%.java
	$(CC) $(CFLAGS) -c $< -o $@
	
header: ant
	#\rm $(main).h
	gcjh $(OBJDIR)/crosslink/$(main).class -o $(main).h -cni
	
ant:
	ant compile
	
lib:
	ar rcs $(BINDIR)/libxml2txt.a $(OBJDIR)/*.o
		
clean :
	\rm -rf $(OBJDIR)/* $(main) $(main).h
