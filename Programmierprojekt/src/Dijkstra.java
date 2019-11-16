import java.io.*;



public class Dijkstra {
	private static int anzahlKnoten = 25115477;
	private static int anzahlKanten = 50790030;
	//womöglich muss das parallelisiert werden damit die Zeitschranke eingehalten wird
	volatile static double[] breitengrad = new double[anzahlKnoten];
	volatile static double[] längengrad = new double[anzahlKnoten];
	public static void main(String[] args) throws IOException {
		try {
			//Falls jemand von euch des sich bei sich selber testen möchte, soll er einfach den eigenen dateipfad angeben (Entpackt den Deutschland-Graphen)
			BufferedReader deutschlandgraph = new BufferedReader(new FileReader("C:\\Users\\kamig\\Desktop\\Neuer Ordner\\germany.fmi"));
			String line = deutschlandgraph.readLine();
			for(int unnötigeZeilen = 1; unnötigeZeilen <= 6; unnötigeZeilen++) {
				deutschlandgraph.readLine();
			}
			long startTime = System.currentTimeMillis();
			for(int knoten = 0; knoten< anzahlKnoten; knoten++) {
				String[] knotenInformationS = deutschlandgraph.readLine().split(" ");
				double breitengradKnoten = Double.parseDouble(knotenInformationS[2]);
				breitengrad[knoten] = breitengradKnoten;
				double längengradKnoten = Double.parseDouble(knotenInformationS[3]);
			    längengrad[knoten] = längengradKnoten;
			}
			long stopTime = System.currentTimeMillis();
			long elapsedTime = stopTime - startTime;
			System.out.println(elapsedTime);
			System.out.println(breitengrad[4]);
			//Speicherung der Kanten kommt noch
		} catch (FileNotFoundException e) {
			System.out.println("Datei ist nicht im system oder im angegebenen Verzeichnis");
			e.printStackTrace();
		}
	}
}