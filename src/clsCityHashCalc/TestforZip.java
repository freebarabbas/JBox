package clsCityHashCalc;

public class TestforZip {

	public static void main(String[] args) {
		
		String  a="wekwjekljijwlekrjklwlekrj23k4jklsdjflwlel90234klsdjf-02348902482908l;sdfm,. 2349023409j234u02348klsldfj23894290342,msou29034nmfsoi2934";
		System.out.println(a.length());
		byte[] b=ZipProcess.zip(a.getBytes());
		System.out.println(b.length);
		System.out.println(new String(b));
		byte[] c=ZipProcess.unzip(b);
		System.out.println(c.length);
		System.out.println(new String(c));

	}

}
