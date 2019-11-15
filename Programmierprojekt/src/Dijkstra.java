import java.io.*;



public class Dijkstra {
	public static void main(String[] args) {
		try {
			//Falls jemand von euch des sich bei sich selber testen möchte, soll er einfach den eigenen dateipfad angeben (Entpackt den Deutschland-Graphen)
			BufferedReader deutschlandgraph = new BufferedReader(new FileReader("C:\\Users\\kamig\\Desktop\\Neuer Ordner\\germany.fmi"));
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
	}
}