package edu.arizona.biosemantics.author.parse;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SentenceSplitter {
	
	public static void main(String[] args) throws IOException {
		SentenceSplitter splitter = new SentenceSplitter();
		String text = "Herbs, perennial, cespitose or not, rhizomatous, rarely stoloniferous. "
				+ "Culms usually trigonous, sometimes round. Leaves basal and cauline, sometimes "
				+ "all basal; ligules present; blades flat, V-shaped, or M-shaped in cross section, "
				+ "rarely filiform, involute, or rounded, commonly less than 20 mm wide, if flat "
				+ "then with distinct midvein. Inflorescences terminal, consisting of spikelets "
				+ "borne in spikes arranged in spikes, racemes, or panicles; bracts subtending "
				+ "spikes leaflike or scalelike; bracts subtending spikelets scalelike, very "
				+ "rarely leaflike. Spikelets 1-flowered; scales 0–1. Flowers unisexual; staminate "
				+ "flowers without scales; pistillate flowers with 1 scale with fused margins "
				+ "(perigynium) enclosing flower, open only at apex; perianth absent; stamens 1–3; "
				+ "styles deciduous or variously persistent, linear, 2–3(–4)-fid. Achenes biconvex, "
				+ "plano-convex, or trigonous, rarely 4-angled. x = 10.";
		splitter.split(text);
	}

	public List<String> split(String text) throws IOException {
		String[] cmdArray = { "perl", "-I", "perl", "perl/charaparserWebSentenceSplitter.pl", text };
		return runCommand(cmdArray);
	}

	private class TerminatePerlHook extends Thread {
		final Process process;
		public TerminatePerlHook(Process process) {
			this.process = process;
		}
		@Override
		public void run() {
			try {
				process.getInputStream().close();
				process.getOutputStream().close();
				process.getErrorStream().close();

				//if (process instanceof UNIXProcess) {
				Field field = process.getClass().getDeclaredField("pid");
				field.setAccessible(true);
				int pid =field.getInt(process);
				Runtime.getRuntime().exec("kill -9 " + pid);
				//}

			} catch(Throwable t) {
				t.printStackTrace();
			}
			process.destroy();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private List<String> runCommand(String[] cmdArray) throws IOException {
		List<String> result = new ArrayList<String>();
		System.out.println("running command: " + cmdArray);
		long time = System.currentTimeMillis();

		Process process = Runtime.getRuntime().exec(cmdArray);

		// add shutdown hook to clean up in case of failure
		TerminatePerlHook terminatePerlHook = new TerminatePerlHook(process);
		Runtime.getRuntime().addShutdownHook(terminatePerlHook);

		BufferedReader stdInput = new BufferedReader(new InputStreamReader(process
				.getInputStream()));

		BufferedReader errInput = new BufferedReader(new InputStreamReader(process
				.getErrorStream()));

		// read the output from the command
		String s = "";
		int i = 0;
		while ((s = stdInput.readLine()) != null) {
			result.add(s);
			System.out.println(s + " at " + (System.currentTimeMillis() - time)
					/ 1000 + " seconds");
		}

		// read the errors from the command
		String e = "";
		while ((e = errInput.readLine()) != null) {
			System.out.println(e + " at " + (System.currentTimeMillis() - time)
					/ 1000 + " seconds");
		}

		// remove shutdown hook
		Runtime.getRuntime().removeShutdownHook(terminatePerlHook);
		return result;
	}
}
