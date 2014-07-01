
public class TestObject {	
	public long randomSleep() throws InterruptedException {        
        // randomly sleeps between 500ms and 1000s
        long randomSleepDuration = (long) (500 + Math.random() * 500);
        System.out.printf("Sleeping for %d ms ..\n", randomSleepDuration);
        Thread.sleep(randomSleepDuration);
        
        return randomSleepDuration;
    }
	
	public void hello(int n) {
		if (n == 0) {
			System.out.println("Hello world!");
		} else if (n == 1) {
			System.out.println("Hello~");
		} else {
			System.out.println("What?");
		}
	}
	
	public static void main(String[] args) {
		System.out.println("foo");
		if (args.length == 0) {
			System.out.println("bar");
		} else {
			System.out.println("baz");
		}
	}
}
