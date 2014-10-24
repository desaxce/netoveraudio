import java.util.BitSet;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import jnt.FFT.RealDoubleFFT_Radix2;

public class sendFile {
	public static void main(String[] args) throws LineUnavailableException,
			InterruptedException {
		AudioFormat fmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				(float) 48000, 8, 1, 1, (float) 48000, false);
		SourceDataLine out = (SourceDataLine) AudioSystem
				.getLine(new DataLine.Info(SourceDataLine.class, fmt));
		out.open(fmt);
		out.start();

		int taille = 2048;
		byte[] mess = new byte[taille];
		int nbFr = 8;// 32 et 26
		int i0 = 80;
		int stride = 26;
		BitSet set = new BitSet(nbFr);
		
		//for (int i = 0; i < nbFr / 2; i++) { set.set(2 * i); }
		set.set(2);
		
		double[] buffer = new double[taille];
		double maximum = buffer[0];
		double minimum = buffer[0];

		for (int i = 0; i < nbFr; i++) {
			if (set.get(i)) {
				buffer[(i0 + stride*i)* taille / 48000] = i;
			} else {
				buffer[i] = 0.0;
			}

		}

		RealDoubleFFT_Radix2 calculus = new RealDoubleFFT_Radix2(taille);
		calculus.backtransform(buffer);
		
		for (int i = 0; i < taille; i++) {
			if (maximum < buffer[i]) {
				maximum = buffer[i];
			}
			if (minimum > buffer[i]) {
				minimum = buffer[i];
			}
		}
		for (int i = 0; i < taille; i++) {
			mess[i] = (byte) (127 + 254 * (buffer[i] - maximum)
					/ (maximum - minimum));
			//System.out.print(mess[i] + " ");
		}
		
		

		double[] messClone = new double[taille];
		for (int i = 0; i<taille; i++) {
			messClone[i]=(double) mess[i];
		}
		
		
		calculus.transform(messClone);

		double[] coeff = new double[nbFr];
		for (int i = 0; i < nbFr; i++) {
			coeff[i] = module(messClone, (i0 + stride * i) * taille / 48000);
		}

		
		double maximum2 = coeff[0];
		for (int i = 0; i < nbFr; i++) {
			if (coeff[i] > maximum2) {
				maximum2 = coeff[i];
			}
		}
		System.out.println(maximum2);

		if (maximum2 > 2000000) {
			for (int i = 0; i < nbFr; i++) {
				if (coeff[i] > maximum2 / 4) {
					set.set(i);
				}
			}
			System.out.println();
		}

		if (!set.isEmpty()) {
			for (int i = 0; i < nbFr; i++) {
				System.out.print(set.get(i) + " ");
			}
			System.out.println();
		}

		
		while (true) { out.write(mess, 0, mess.length); 
		
		
		//Thread.sleep(1000);
		}
		

	}

	private static double module(double[] buffer, int i) {
		double x = buffer[i];
		double y = buffer[buffer.length - i];
		return x * x + y * y;
	}
}
