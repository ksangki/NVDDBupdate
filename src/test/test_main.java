package test;

public class test_main {
	public static void gogo() throws Exception{
		throw new Exception();
	}
	
	public static void gogo2() throws Exception {
		try {
			gogo();
			System.out.println("gogo ex");
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			System.out.println("gogo2 end");
		}
		System.out.println("gogo2 Error");
	}
	public static void main(String[] args) {
		try {
			gogo2();
		} catch (Exception e) {
			System.out.println("main catch");
		} finally {
			System.out.println("main end");
		}
		System.out.println("Main EEEEnd");
	}
	
	
}
