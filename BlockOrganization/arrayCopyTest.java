import java.nio.*;

public class arrayCopyTest {
	
	public static void main(String[] args) {
		byte[] array1  = {2, 3, 4};
		byte[] array2 = {1, 5, 6, 7, 8};
		System.out.println("The the first array has values: ");

		for (int i = 0; i < array1.length; i++) {
            System.out.print(array1[i] + " ");
		}
		System.out.println(" ");
		System.out.println("The second array has values: ");
		for (int i = 0; i < array2.length; i++){
            System.out.println(array2[i] + " ");
			}

		System.arraycopy(array1, 0, array2, 1, 3);

		System.out.println("The second array with the values of the first array looks like: ");
		for (int i = 0; i < array2.length; i++){
            System.out.println(array2[i] + " ");
		}
	}

}
