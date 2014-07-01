
public class TestPerf {
	static int count;

	public static void main(String[] args) {
		long start = System.nanoTime();
		for(int i=0; i<100000000; i++) {
			count += (int)Math.floor(i * Math.sin(i));
		}
		long end = System.nanoTime();
		System.out.println(count+ " "+(end - start));
	}
}
