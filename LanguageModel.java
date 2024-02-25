import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;

public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        char c;
                
        In in = new In(fileName);
        // Reads just enough characters to form the first window
        for (int i = 0; i < windowLength; i++) {
            c = in.readChar();
            window = window + c;
        }

        // Processes the entire text, one character at a time
        while (!in.isEmpty()) {
            // Gets the next character
            c = in.readChar();

            // Checks if the window is already in the map

            // If the window was not found in the map
            // Creates a new empty list, and adds (window,list) to the map
            if (CharDataMap.get(window) == null) {
                List probs = new List();
                CharDataMap.put(window, probs);
            }

            // Calculates the counts of the current character.
            List probs = new List();
            probs = CharDataMap.get(window);
            probs.update(c);
            CharDataMap.put(window, probs);

            // Advances the window: adds c to the window’s end, and deletes the
            // window's first character.
            window = window.substring(1,windowLength) + c;
        }
        // The entire file has been processed, and all the characters have been counted.
        // Proceeds to compute and set the p and cp fields of all the CharData objects
        // in each linked list in the map.
        for (List probs : CharDataMap.values()){
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	public void calculateProbabilities(List probs) {				
        int size = probs.getSize();
        int numChar = 0;
        CharData chrData;
        // a probability (number between 0 and 1)
	    double p, tmpP;    
        // a commulative probability (number between 0 and 1)
        double prvCp;

        for (int i = 0; i < size ; i++) {
            chrData = probs.get(i);
            numChar = numChar + chrData.count;
        }
        p = 1.0/numChar;
        
        prvCp = 0;
        for (int i = 0; i < size ; i++) {
            ListIterator iterator = probs.listIterator(i);
            tmpP = p*iterator.current.cp.count;
            iterator.current.cp.p = tmpP;
            iterator.current.cp.cp = tmpP + prvCp;
            prvCp = prvCp + tmpP;
        }
	}

    // Returns a random character from the given probabilities list.
	public char getRandomChar(List probs) {
        char chr = ' ';
        int size = probs.getSize();

        double  d = randomGenerator.nextDouble();
        //System.out.println("getRandomChar: random = " +d);

        for (int i = 0; i < size ; i++) {
            ListIterator iterator = probs.listIterator(i);
            if (iterator.current.cp.cp > d ){
                chr = iterator.current.cp.chr;
                return chr;
            }
        }
        return chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		String generatedText = initialText;
        List probs = new List();
        String currWindow = initialText;
        char chr = ' ';

        if (initialText.length() < windowLength)
            return initialText;

        while (generatedText.length() < textLength + initialText.length()) {

            probs = CharDataMap.get(currWindow);

            if (probs == null)
                return generatedText;

            chr = getRandomChar(probs);

		    generatedText = generatedText + chr;

            // Advances the window: 
            currWindow = currWindow.substring(1,windowLength) + chr;

        }
        return generatedText;

	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();

	}

    public static void main(String[] args) {
        int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
        lm = new LanguageModel(windowLength);
        else
        lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));    }
}
