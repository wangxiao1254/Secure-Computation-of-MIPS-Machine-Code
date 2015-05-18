import oram.LinearScanOram;
import util.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;
import gc.GCSignal;

public class TestLinearScanORAM {
	static boolean use = false;
	public  static void main(String args[]) throws Exception {
		for(int i = 10; i <=20 ; i++) {
			GenRunnable gen = new GenRunnable(12345, i, 32);
			EvaRunnable eva = new EvaRunnable("localhost", 12345);
			Thread tGen = new Thread(gen);
			Thread tEva = new Thread(eva);
			tGen.start();
			Thread.sleep(10);
			tEva.start();
			tGen.join();
		}
	}

	final static int writeCount = 5;
	final static int readCount = 0;
	
	public TestLinearScanORAM() {
	}

	public static class GenRunnable extends network.Server implements Runnable {
		int port;
		int logN;
		int N;
		int dataSize;

		GenRunnable(int port, int logN, int dataSize) {
			this.port = port;
			this.logN = logN;
			this.N = 1 << logN;
			this.dataSize = dataSize;
		}

		public void run() {
			try {
				listen(port);

				os.write(logN);
				os.write(dataSize);
				os.flush();

				System.out.println(logN + " " + dataSize);
				@SuppressWarnings("unchecked")
				CompEnv<GCSignal> env = CompEnv.getEnv(Mode.OPT, Party.Alice, this);
				LinearScanOram<GCSignal> client = new LinearScanOram<GCSignal>(env, N, dataSize);
				

				GCSignal[] scData = env.inputOfAlice(Utils.fromInt(1, dataSize));
				for(int i = 0; i < client.content.length; ++i)
					client.content[i] = env.inputOfAlice(Utils.fromInt(i, dataSize));
				
				double t1 = System.nanoTime();
				for (int i = 0; i < writeCount; ++i) {					
					client.read(scData);

				}
				double total = System.nanoTime() - t1;

				System.out.println(total/writeCount/1000000000.0);

				os.flush();

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

	public static class EvaRunnable extends network.Client implements Runnable {
		String host;
		int port;

		EvaRunnable(String host, int port) {
			this.host = host;
			this.port = port;
		}

		public void run() {
			try {
				connect(host, port);

				int logN = is.read();
				int dataSize = is.read();

				int N = 1 << logN;
				@SuppressWarnings("unchecked")
				CompEnv<GCSignal> env = CompEnv.getEnv(Mode.OPT, Party.Bob, this);
				LinearScanOram<GCSignal> server = new LinearScanOram<GCSignal>(env, N, dataSize);
				GCSignal[] scData = env.inputOfAlice(Utils.fromInt(1, dataSize));
				for(int i = 0; i < server.content.length; ++i)
					server.content[i] = env.inputOfAlice(Utils.fromInt(i, dataSize));
				
				for (int i = 0; i < writeCount; ++i) {					
					server.read(scData);
				}

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}
