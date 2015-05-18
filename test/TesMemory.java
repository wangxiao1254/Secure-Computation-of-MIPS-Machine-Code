import oram.Memory;
import util.Utils;
import flexsc.CompEnv;
import flexsc.Mode;
import flexsc.Party;
import gc.GCSignal;

public class TesMemory {
	static boolean use = false;
	public  static void main(String args[]) throws Exception {
		for(int i = 15; i <=23 ; i++) {
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

	final static int writeCount = 1 << 3;
	final static int readCount = 0;
	
	public TesMemory() {
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
				Memory<GCSignal> client = new Memory<GCSignal>(env, N, dataSize);
				double t1 = System.nanoTime();

				for (int i = 0; i < writeCount; ++i) {
					int element = i % N;
					GCSignal[] scData = env.inputOfAlice(Utils.fromInt(element*2, dataSize));
					os.flush();
					client.write(client.lib.toSignals(element, client.lengthOfIden), scData);

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
				System.out.println(logN + " " + dataSize);
				@SuppressWarnings("unchecked")
				CompEnv<GCSignal> env = CompEnv.getEnv(Mode.OPT, Party.Bob, this);
				Memory<GCSignal> server = new Memory<GCSignal>(env, N, dataSize);
				for (int i = 0; i < writeCount; ++i) {
					int element = i % N;
					GCSignal[] scData = env.inputOfAlice(new boolean[dataSize]);
					server.write(server.lib.toSignals(element, server.lengthOfIden), scData);
				}

				disconnect();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
	}
}