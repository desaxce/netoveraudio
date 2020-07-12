import java.util.BitSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import java.util.Scanner;
import jnt.FFT.RealDoubleFFT_Radix2;

public class sendFile {

	public static void main(String[] args) throws LineUnavailableException, InterruptedException {

		Scanner sc = new Scanner(System.in);
		String myFavoriteMessage = sc.nextLine();


		AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, (float) 48000, 8, 1, 1, (float) 48000, false);
		SourceDataLine out = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, fmt));

		out.open(fmt);
		out.start();
		
		int numberOfFrequencies = 16; // 32 et 26
		int taille = 131072;
		int i0 = 1000;
		int stride = 26;

		byte[] mess = new byte[taille];
		double[] buffer = new double[taille];
		BitSet set = new BitSet(numberOfFrequencies);
		RealDoubleFFT_Radix2 calculus = new RealDoubleFFT_Radix2(taille);
		
		//for (int i = 0; i < numberOfFrequencies / 2; i++) {
		//	set.set(2 * i); // setting all the even frequencies --> is it a cosine ?
		//}

		int j = 0;
		for (int i = 0; i < myFavoriteMessage.length(); i++) {
			char c = myFavoriteMessage.charAt(i);
			for (int k = 0; k < 8; k++) {
				int mod = c%2;
				if (mod == 1) {
					set.set(j);
				}
				c /= 2;
				j++;
			}
		}

		// set.set(0);
		// set.set(6);
		// set.set(9);
		// set.set(14);
		for (int i = numberOfFrequencies-1; i > -1; i--) {
			if (set.get(i)) {
				System.out.print("1");
			}
			else {
				System.out.print("0");
			}
			if (i%8==0)
				System.out.print(" ");
		}
		

		for (int i = 0; i < numberOfFrequencies; i++) {
			// There was a problem here because if i==0, then you forgot to count the first frequency! (thats why I put i+1 instead of i)
			if (set.get(i)) {
				buffer[(i0 + stride*(i+1))* taille / 48000] = (i+1); // If frequency #i is set, then add it.
			} else {
				buffer[i] = 0.0;
			}

		}

		calculus.backtransform(buffer); // Inverse transform the buffer array
	
		// TODO: Replace this loop to find the minimum with library functions MIN/MAX
		double maximum = buffer[0], minimum = buffer[0];
		for (int i = 0; i < taille; i++) {
			if (maximum < buffer[i]) {
				maximum = buffer[i];
			}
			if (minimum > buffer[i]) {
				minimum = buffer[i];
			}
		}

		// TODO: Replace these hard values 127 and 254 by const int.
		// I think this loop is to scale/regularize the buffer array to get something centered on 127 and varying between 0 and 255.
		for (int i = 0; i < taille; i++) {
			mess[i] = (byte) (127 + 254 * (buffer[i] - maximum) / (maximum - minimum));
			//System.out.print(mess[i] + " ");
		}
		

		// TODO: Remove this below to avoid having to clone this array --> not pretty
		// I think the part below is just here to check that internally what is send is really what is send (meaning the receiver should receive the same thing)
		double[] messClone = new double[taille];
		for (int i = 0; i < taille; i++) {
			messClone[i] = (double) mess[i];
		}
	
		// Compute FFT of messClone
		calculus.transform(messClone);

		double[] coeff = new double[numberOfFrequencies];
		for (int i = 0; i < numberOfFrequencies; i++) {
			coeff[i] = module(messClone, (i0 + stride * (i+1)) * taille / 48000);
		}

		double maximum2 = coeff[0];
		for (int i = 0; i < numberOfFrequencies; i++) {
			if (coeff[i] > maximum2) {
				maximum2 = coeff[i];
			}
		}

		BitSet nset = new BitSet(numberOfFrequencies);
		// The hard value below should be removed.
		if (maximum2 > 2000000) {
			System.out.println(maximum2);System.out.println();
			for (int i = 0; i < numberOfFrequencies; i++) {
				if (coeff[i] > maximum2 / 100) {
					nset.set(i);
				}
			}
			System.out.println();
		}

		if (!set.isEmpty()) {
			for (int i = numberOfFrequencies-1; i > -1; i--) {
				if (nset.get(i)) {
					System.out.print("1");
				}
				else {
					System.out.print("0");
				}
				if (i%8==0)
					System.out.print(" ");
			}
		}

		
		while (true) {
			// Creates the real sound
			out.write(mess, 0, mess.length); 
			//Thread.sleep(1000);
		}
	}

	// Computes square norm of (x, y)
	private static double module(double[] buffer, int i) {
		double x = buffer[i];
		double y = buffer[buffer.length - i];
		return x * x + y * y;
	}

}
