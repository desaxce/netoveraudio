import java.nio.ByteBuffer;
import java.util.BitSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

import jnt.FFT.RealDoubleFFT_Radix2;

public class receiveFile {
	public static void main(String[] args) throws LineUnavailableException,
			InterruptedException {
		AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				(float) 48000, 8, 1, 1, (float) 48000, false);
		TargetDataLine in = (TargetDataLine) AudioSystem
				.getLine(new DataLine.Info(TargetDataLine.class, fmt));
		in.open(fmt);
		in.start();
		int taille = 131072 ;

		byte[] array = new byte[taille];
		while (true) {
			in.read(array, 0, array.length);
			int nbOctets = 26;
			int nbfr = 8 * nbOctets;
			int i0 = 400;
			int stride = 26;
			BitSet set = new BitSet(nbfr);

			double[] arrayClone = new double[array.length];
			RealDoubleFFT_Radix2 calculus = new RealDoubleFFT_Radix2(taille);
			double maximum = arrayClone[0];
			double minimum = arrayClone[0];

			for (int i = 0; i < array.length; i++) {
				arrayClone[i] = (double) array[i];
				if (arrayClone[i] < minimum) {
					minimum = arrayClone[i];
				}
				if (arrayClone[i] > maximum) {
					maximum = arrayClone[i];
				}
			}

			/*
			 * for(int i=0; i<taille; i++){ //arrayClone[i] =
			 * 127+254*(arrayClone[i]-maximum)/(maximum-minimum); for(int k=0;
			 * k<50+arrayClone[i]/3;k++){ System.out.print(" "); }
			 * System.out.println("#"); }
			 */

			/*
			 * for (int i = 0; i<taille; i++) { System.out.print(array[i]+ " ");
			 * } System.out.println();
			 */

			calculus.transform(arrayClone);

			double[] coeff = new double[nbfr];
			for (int i = 0; i < nbfr; i++) {
				coeff[i] = module(arrayClone, (i0 + stride * i) * taille
						/ 48000);
			}

			double maximum2 = coeff[0];
			for (int i = 0; i < nbfr; i++) {
				if (coeff[i] > maximum2) {
					maximum2 = coeff[i];
				}
			}
			/*
			 * System.out.println(maximum2); System.out.println();
			 */

			if (maximum2 > 2E6) {
				for (int i = 0; i < nbfr; i++) {
					if (coeff[i] > maximum2 / 4) {
						set.set(i);
					}
				}
			

			
				/*
				 * for (int i = 0; i < nbfr; i++) { System.out.print(set.get(i)
				 * + " "); } System.out.println();
				 */

				
				for (int j = 0; j < nbOctets; j++) {
					byte b = 0;
					for (int i =(int) (8 * j); i < (int) (8 * (j + 1)); i++) {
						if (set.get(i)) {
							b += (byte) Math.pow(2, (double) i - 8 * (i / 8));
						}
					}
					System.out.print((char) b);
				}

			}

		}

	}

	private static double module(double[] buffer, int i) {
		double x = buffer[i];
		double y = buffer[buffer.length - i];
		return x * x + y * y;
	}

	/*
	 * public static float coefficient(byte[] array, int frequence) { float
	 * result = 0; for (int t = 0; t < 3 * Math .floor((48000 / (2 * Math.PI *
	 * (1000 + 1000 * frequence)))); t++) { result += array[t] Math.exp(2*
	 * Math.PI * (1000 + 1000 * frequence) * t / 48000); } return result; }
	 */

}