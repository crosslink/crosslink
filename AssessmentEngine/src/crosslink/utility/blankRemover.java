package crosslink.utility;

/**
 *
 * @author Darren
 */
public class blankRemover {

    public blankRemover() {
    }

    // replace all leading whitespaces with a single space
    public String removeLeadingSpaces(String text){
        String newText = "";
        newText = text.replaceAll("^\\s+", " ");
        return newText;
    }
    // replace all trailing whitespaces with a single space
    public String removeTrailingSpaces(String text){
        String newText = "";
        newText = text.replaceAll("\\s+$", " ");
        return newText;
    }
    // replace all white spaces between words with a single space
    public String removeInnerSpaces(String text){
        String newText = "";
        newText = text.replaceAll("\\b\\s{2,}\\b", " ");
        return newText;
    }
    // replace all whitespaces anywhere with a single space
    public String removeWhiteSpaces(String text){
        String newText = "";
        newText = text.replaceAll("\\s+", " ");
        return newText;
    }

}
