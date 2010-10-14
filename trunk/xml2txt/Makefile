CC := gcj
CFLAGS  := -g

SRCDIR = src
OBJDIR = bin
BINDIR = .

main = xml2txt

all: $(main)

$(main): $(OBJDIR)/$(main).o
	$(CC) --main=$(main) $(LDFLAGS) -o $@ $^
    	
$(OBJDIR)/%.o: $(SRCDIR)/%.java
	$(CC) $(CFLAGS) -c $< -o $@
	
clean :
	\rm $(OBJDIR)/*.o $(main)
